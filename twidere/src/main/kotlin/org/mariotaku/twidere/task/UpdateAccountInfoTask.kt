package org.mariotaku.twidere.task

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE
import org.mariotaku.twidere.extension.model.setAccountKey
import org.mariotaku.twidere.extension.model.setAccountUser
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.updateItems

class UpdateAccountInfoTask(
        private val context: Context
) : PromiseTask<Pair<AccountDetails, ParcelableUser>, Unit> {

    override fun toPromise(param: Pair<AccountDetails, ParcelableUser>): Promise<Unit, Exception> {
        val resolver = context.contentResolver
        val (details, user) = param
        if (user.is_cache) {
            return Promise.ofSuccess(Unit)
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return Promise.ofSuccess(Unit)
        }
        return task {
            val am = AccountManager.get(context)
            val account = Account(details.account.name, ACCOUNT_TYPE)
            account.setAccountUser(am, user)
            account.setAccountKey(am, user.key)
        }.then {
            updateTimeline(user, details, resolver)
        }.then {
            updateTabs(resolver, user.key)
        }
    }

    private fun updateTimeline(user: ParcelableUser, details: AccountDetails, resolver: ContentResolver) {
        val accountKeyValues = ContentValues().apply {
            put(AccountSupportColumns.ACCOUNT_KEY, user.key.toString())
        }
        val accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val accountKeyWhereArgs = arrayOf(details.key.toString())

        resolver.update(Statuses.HomeTimeline.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Messages.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(Messages.Conversations.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs)
    }

    private fun updateTabs(resolver: ContentResolver, accountKey: UserKey) {
        resolver.updateItems(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tab::class.java) { tab ->
            val arguments = tab.arguments
            if (arguments != null) {
                val accountId = arguments.accountId
                val keys = arguments.accountKeys
                if (accountKey.id == accountId && keys == null) {
                    arguments.accountKeys = arrayOf(accountKey)
                }
            }
            return@updateItems tab
        }
    }
}
