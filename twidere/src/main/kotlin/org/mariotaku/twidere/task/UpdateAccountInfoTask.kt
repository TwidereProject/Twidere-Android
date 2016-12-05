package org.mariotaku.twidere.task

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.support.v4.util.LongSparseArray
import android.text.TextUtils
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_KEY
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_USER
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.provider.TwidereDataStore.*
import java.io.IOException

/**
 * Created by mariotaku on 16/3/8.
 */
class UpdateAccountInfoTask(private val context: Context) : AbstractTask<Pair<AccountDetails, ParcelableUser>, Any, Any>() {

    override fun doLongOperation(params: Pair<AccountDetails, ParcelableUser>): Any? {
        val resolver = context.contentResolver
        val account = params.first
        val user = params.second
        if (user.is_cache) {
            return null
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return null
        }

        val am = AccountManager.get(context)
        val account1 = Account(account.user.name, TwidereConstants.ACCOUNT_TYPE)
        am.setUserData(account1, ACCOUNT_USER_DATA_USER, LoganSquare.serialize(user))
        am.setUserData(account1, ACCOUNT_USER_DATA_KEY, user.key.toString())

        val accountKeyValues = ContentValues()
        accountKeyValues.put(AccountSupportColumns.ACCOUNT_KEY, user.key.toString())
        val accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val accountKeyWhereArgs = arrayOf(account.key.toString())

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)

        updateTabs(context, resolver, user.key)


        return null
    }

    private fun updateTabs(context: Context, resolver: ContentResolver, accountKey: UserKey) {
        val tabsCursor = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null) ?: return
        try {
            val indices = TabCursorIndices(tabsCursor)
            tabsCursor.moveToFirst()
            val values = LongSparseArray<ContentValues>()
            while (!tabsCursor.isAfterLast) {
                val tab = indices.newObject(tabsCursor)
                val arguments = tab.arguments
                if (arguments != null) {
                    val accountId = arguments.accountId
                    val keys = arguments.accountKeys
                    if (TextUtils.equals(accountKey.id, accountId) && keys == null) {
                        arguments.accountKeys = arrayOf(accountKey)
                        values.put(tab.id, TabValuesCreator.create(tab))
                    }
                }
                tabsCursor.moveToNext()
            }
            val where = Expression.equalsArgs(Tabs._ID).sql
            var i = 0
            val j = values.size()
            while (i < j) {
                val whereArgs = arrayOf(values.keyAt(i).toString())
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, whereArgs)
                i++
            }
        } catch (e: IOException) {
            // Ignore
        } finally {
            tabsCursor.close()
        }
    }
}
