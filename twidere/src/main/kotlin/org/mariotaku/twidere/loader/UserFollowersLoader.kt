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

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.Account
import org.mariotaku.microblog.library.twitter.model.CursorSupport
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

class UserFollowersLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : CursorSupportUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getUsers(details: AccountDetails, paging: Paging): List<ParcelableUser> {
        when (details.type) {
            AccountType.MASTODON -> return getMastodonUsers(details, paging).map {
                it.toParcelable(details.key)
            }
            else -> return getMicroBlogUsers(details, paging).map {
                it.toParcelable(details.key, details.type, profileImageSize = profileImageSize)
            }
        }
    }

    private fun getMastodonUsers(details: AccountDetails, paging: Paging): List<Account> {
        val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
        if (userKey == null) throw MicroBlogException("Only ID supported")
        return mastodon.getFollowers(userKey.id, paging)
    }

    private fun getMicroBlogUsers(details: AccountDetails, paging: Paging): List<User> {
        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
        when (details.type) {
            AccountType.STATUSNET -> if (userKey != null) {
                return microBlog.getStatusesFollowersList(userKey.id, paging).also {
                    setCursors(it as? CursorSupport)
                }
            } else if (screenName != null) {
                return microBlog.getStatusesFollowersListByScreenName(screenName, paging).also {
                    setCursors(it as? CursorSupport)
                }
            }
            AccountType.FANFOU -> if (userKey != null) {
                return microBlog.getUsersFollowers(userKey.id, paging)
            } else if (screenName != null) {
                return microBlog.getUsersFollowers(screenName, paging)
            }
            else -> if (userKey != null) {
                return microBlog.getFollowersList(userKey.id, paging).also {
                    setCursors(it as? CursorSupport)
                }
            } else if (screenName != null) {
                return microBlog.getFollowersListByScreenName(screenName, paging).also {
                    setCursors(it as? CursorSupport)
                }
            }
        }
        throw MicroBlogException("user_id or screen_name required")
    }

}
