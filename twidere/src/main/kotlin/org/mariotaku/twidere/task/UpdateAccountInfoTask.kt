package org.mariotaku.twidere.task

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import androidx.collection.LongSparseArray
import android.text.TextUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE
import org.mariotaku.twidere.extension.model.setAccountKey
import org.mariotaku.twidere.extension.model.setAccountUser
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import java.io.IOException

/**
 * Created by mariotaku on 16/3/8.
 */
class UpdateAccountInfoTask(
        private val context: Context
) : AbstractTask<Pair<AccountDetails, ParcelableUser>, Unit, Unit?>() {

    override fun doLongOperation(params: Pair<AccountDetails, ParcelableUser>) {
        val resolver = context.contentResolver
        val (details, user) = params
        if (user.is_cache) {
            return
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return
        }

        val am = AccountManager.get(context)
        val account = Account(details.account.name, ACCOUNT_TYPE)
        account.setAccountUser(am, user)
        account.setAccountKey(am, user.key)

        val accountKeyValues = ContentValues().apply {
            put(AccountSupportColumns.ACCOUNT_KEY, user.key.toString())
        }
        val accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val accountKeyWhereArgs = arrayOf(details.key.toString())

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Messages.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Messages.Conversations.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)

        updateTabs(resolver, user.key)
    }

    private fun updateTabs(resolver: ContentResolver, accountKey: UserKey) {
        resolver.queryReference(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null,
                null)?.use { (tabsCursor) ->
            val indices = ObjectCursor.indicesFrom(tabsCursor, Tab::class.java)
            val creator = ObjectCursor.valuesCreatorFrom(Tab::class.java)
            tabsCursor.moveToFirst()
            val values = LongSparseArray<ContentValues>()
            try {
                while (!tabsCursor.isAfterLast) {
                    val tab = indices.newObject(tabsCursor)
                    val arguments = tab.arguments
                    if (arguments != null) {
                        val accountId = arguments.accountId
                        val keys = arguments.accountKeys
                        if (TextUtils.equals(accountKey.id, accountId) && keys == null) {
                            arguments.accountKeys = arrayOf(accountKey)
                            values.put(tab.id, creator.create(tab))
                        }
                    }
                    tabsCursor.moveToNext()
                }
            } catch (e: IOException) {
                // Ignore
            }
            for (i in 0 until values.size()) {
                val where = Expression.equals(Tabs._ID, values.keyAt(i)).sql
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, null)
            }
        }
    }
}
