package org.mariotaku.twidere.task

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.support.v4.util.LongSparseArray
import android.text.TextUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.extension.setAccountKey
import org.mariotaku.twidere.extension.setAccountUser
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.provider.TwidereDataStore.*
import java.io.IOException

/**
 * Created by mariotaku on 16/3/8.
 */
class UpdateAccountInfoTask(private val context: Context) : AbstractTask<Pair<AccountDetails, ParcelableUser>, Any, Unit>() {

    override fun doLongOperation(params: Pair<AccountDetails, ParcelableUser>) {
        val resolver = context.contentResolver
        val details = params.first
        val user = params.second
        if (user.is_cache) {
            return
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return
        }

        val am = AccountManager.get(context)
        val account = Account(details.account.name, TwidereConstants.ACCOUNT_TYPE)
        account.setAccountUser(am, user)
        account.setAccountKey(am, user.key)

        val accountKeyValues = ContentValues().apply {
            put(AccountSupportColumns.ACCOUNT_KEY, user.key.toString())
        }
        val accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val accountKeyWhereArgs = arrayOf(details.key.toString())

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)

        updateTabs(resolver, user.key)
    }

    private fun updateTabs(resolver: ContentResolver, accountKey: UserKey) {
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
            for (i in 0 until values.size()) {
                val whereArgs = arrayOf(values.keyAt(i).toString())
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, whereArgs)
            }
        } catch (e: IOException) {
            // Ignore
        } finally {
            tabsCursor.close()
        }
    }
}
