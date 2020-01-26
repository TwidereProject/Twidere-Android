/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.loader

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import androidx.loader.content.FixedAsyncTaskLoader
import android.text.TextUtils
import android.util.Log
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.set
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.host
import org.mariotaku.twidere.extension.model.isAcctPlaceholder
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.task.UpdateAccountInfoTask
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

class ParcelableUserLoader(
        context: Context,
        private val accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val extras: Bundle?,
        private val omitIntentExtra: Boolean,
        private val loadFromCache: Boolean
) : FixedAsyncTaskLoader<SingleResponse<ParcelableUser>>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun loadInBackground(): SingleResponse<ParcelableUser> {
        val context = context
        val resolver = context.contentResolver
        val accountKey = accountKey ?: return SingleResponse(MicroBlogException("No account"))
        val am = AccountManager.get(context)
        val details = AccountUtils.getAllAccountDetails(am, AccountUtils.getAccounts(am), true).firstOrNull {
            if (it.key == accountKey) {
                return@firstOrNull true
            } else if (it.user.account_key == accountKey) {
                return@firstOrNull true
            }
            return@firstOrNull false
        } ?: return SingleResponse()
        if (!omitIntentExtra && extras != null) {
            val user = extras.getParcelable<ParcelableUser?>(EXTRA_USER)
            if (user != null) {
                val values = ObjectCursor.valuesCreatorFrom(ParcelableUser::class.java).create(user)
                resolver.insert(CachedUsers.CONTENT_URI, values)
                ParcelableUserUtils.updateExtraInformation(user, details, userColorNameManager)
                return SingleResponse(user).apply {
                    extras[EXTRA_ACCOUNT] = details
                }
            }
        }
        if (loadFromCache) {
            val where: Expression
            val whereArgs: Array<String>
            if (userKey != null) {
                where = Expression.equalsArgs(CachedUsers.USER_KEY)
                whereArgs = arrayOf(userKey.toString())
            } else if (screenName != null) {
                val host = accountKey.host
                if (host != null) {
                    where = Expression.and(
                            Expression.likeRaw(Columns.Column(CachedUsers.USER_KEY), "'%@'||?"),
                            Expression.equalsArgs(CachedUsers.SCREEN_NAME))
                    whereArgs = arrayOf(host, screenName)
                } else {
                    where = Expression.equalsArgs(CachedUsers.SCREEN_NAME)
                    whereArgs = arrayOf(screenName)
                }
            } else {
                return SingleResponse()
            }
            resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where.sql,
                    whereArgs, null)?.let { cur ->
                @Suppress("ConvertTryFinallyToUseCall")
                try {
                    cur.moveToFirst()
                    val indices = ObjectCursor.indicesFrom(cur, ParcelableUser::class.java)
                    while (!cur.isAfterLast) {
                        val user = indices.newObject(cur)
                        if (TextUtils.equals(user.host, user.key.host)) {
                            user.account_key = accountKey
                            user.account_color = details.color
                            return SingleResponse(user).apply {
                                extras[EXTRA_ACCOUNT] = details
                            }
                        }
                        cur.moveToNext()
                    }
                } finally {
                    cur.close()
                }
            }
        }
        try {
            val user = when (details.type) {
                AccountType.MASTODON -> showMastodonUser(details)
                else -> showMicroBlogUser(details)
            }
            val creator = ObjectCursor.valuesCreatorFrom(ParcelableUser::class.java)
            val cachedUserValues = creator.create(user)
            resolver.insert(CachedUsers.CONTENT_URI, cachedUserValues)
            ParcelableUserUtils.updateExtraInformation(user, details, userColorNameManager)
            return SingleResponse(user).apply {
                extras[EXTRA_ACCOUNT] = details
            }
        } catch (e: MicroBlogException) {
            Log.w(LOGTAG, e)
            return SingleResponse(exception = e)
        }

    }

    private fun showMastodonUser(details: AccountDetails): ParcelableUser {
        val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
        if (userKey == null) throw MicroBlogException("Invalid user id")
        if (!userKey.isAcctPlaceholder) {
            return mastodon.getAccount(userKey.id).toParcelable(details)
        }
        if (screenName == null) throw MicroBlogException("Screen name required")
        val resultItem = mastodon.searchAccounts("$screenName@${userKey.host}", Paging().count(1))
                .firstOrNull() ?: throw MicroBlogException("User not found")
        return resultItem.toParcelable(details)
    }

    private fun showMicroBlogUser(details: AccountDetails): ParcelableUser {
        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
        val profileUrl = extras?.getString(EXTRA_PROFILE_URL)
        val response = if (extras != null && extras.getBoolean(EXTRA_IS_ACCOUNT_PROFILE)) {
            microBlog.verifyCredentials()
        } else if (details.type == AccountType.STATUSNET && userKey != null && profileUrl != null
                && details.key.host != userKey.host) {
            microBlog.showExternalProfile(profileUrl)
        } else {
            microBlog.tryShowUser(userKey?.id, screenName, details.type)
        }
        return response.toParcelable(details, profileImageSize = profileImageSize)
    }

    override fun onStartLoading() {
        if (!omitIntentExtra && extras != null) {
            val user = extras.getParcelable<ParcelableUser>(EXTRA_USER)
            if (user != null) {
//                deliverResult(SingleResponse(user))
            }
        }
        forceLoad()
    }

    override fun deliverResult(data: SingleResponse<ParcelableUser>?) {
        super.deliverResult(data)
        val user = data?.data ?: return
        if (user.is_cache) return
        val account = data.extras.getParcelable<AccountDetails>(EXTRA_ACCOUNT)
        if (account != null) {
            val task = UpdateAccountInfoTask(context)
            task.params = Pair(account, user)
            TaskStarter.execute(task)
        }
    }
}
