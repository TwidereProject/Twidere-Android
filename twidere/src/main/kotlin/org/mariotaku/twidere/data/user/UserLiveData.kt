/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.data.user

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.*
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.ComputableExceptionLiveData
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.findMatchingDetailsOrThrow
import org.mariotaku.twidere.extension.insert
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.host
import org.mariotaku.twidere.extension.model.isAcctPlaceholder
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ModelCreationConfig
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.UserColorNameManager

class UserLiveData(
        val context: Context,
        val accountKey: UserKey,
        val userKey: UserKey?,
        val screenName: String?,
        val profileUrl: String? = null,
        val isAccountProfile: Boolean = false
) : ComputableExceptionLiveData<Pair<AccountDetails, ParcelableUser>>(false) {

    var loadFromCache: Boolean = false
    var extraUser: ParcelableUser? = null

    val account: AccountDetails?
        get() = value?.data?.first
    val user: ParcelableUser?
        get() = value?.data?.second

    private val profileImageSize = ModelCreationConfig.obtain(context)

    override fun compute(): Pair<AccountDetails, ParcelableUser> {
        val context = context
        val resolver = context.contentResolver
        val accountKey = accountKey
        val am = AccountManager.get(context)
        val details = am.findMatchingDetailsOrThrow(accountKey)
        val extraUser = this.extraUser
        val manager = UserColorNameManager.get(context)
        if (extraUser != null) {
            ParcelableUserUtils.updateExtraInformation(extraUser, details, manager)
            resolver.insert(CachedUsers.CONTENT_URI, extraUser, ParcelableUser::class.java)
            return Pair(details, extraUser)
        }
        if (loadFromCache) {
            val where: Expression
            val whereArgs: Array<String>
            when {
                userKey != null -> {
                    where = Expression.equalsArgs(CachedUsers.USER_KEY)
                    whereArgs = arrayOf(userKey.toString())
                }
                screenName != null -> {
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
                }
                else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
            }
            val cached = resolver.queryOne(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where.sql,
                    whereArgs, null, ParcelableUser::class.java)
            if (cached != null && cached.host == cached.key.host) {
                cached.account_key = accountKey
                cached.account_color = details.color
                return Pair(details, cached)
            }
        }
        val user = when (details.type) {
            AccountType.MASTODON -> showMastodonUser(details)
            else -> showMicroBlogUser(details)
        }
        resolver.insert(CachedUsers.CONTENT_URI, user, ParcelableUser::class.java)
        ParcelableUserUtils.updateExtraInformation(user, details, manager)
        return Pair(details, user)
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
        val response = if (isAccountProfile) {
            details.newMicroBlogInstance(context, MicroBlog::class.java).verifyCredentials()
        } else when (details.type) {
            AccountType.TWITTER -> details.newMicroBlogInstance(context, Twitter::class.java).tryShowUser(userKey?.id, screenName)
            AccountType.FANFOU -> details.newMicroBlogInstance(context, Fanfou::class.java).showFanfouUser(userKey?.id
                    ?: screenName)
            AccountType.STATUSNET -> {
                val statusNet = details.newMicroBlogInstance(context, StatusNet::class.java)
                when {
                    isRemoteUser(details) -> statusNet.showExternalProfile(profileUrl)
                    userKey != null -> {
                        statusNet.showUser(userKey.id)
                    }
                    screenName != null -> {
                        statusNet.showUserByScreenName(screenName)
                    }
                    else -> throw RequiredFieldNotFoundException("id", "screen_name")
                }
            }
            else -> throw APINotSupportedException(platform = details.type)
        }
        return response.toParcelable(details, creationConfig = profileImageSize)
    }

    private fun isRemoteUser(details: AccountDetails) =
            (details.type == AccountType.STATUSNET && userKey != null && profileUrl != null
                    && details.key.host != userKey.host)
}
