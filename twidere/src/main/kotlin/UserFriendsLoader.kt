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
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils

class UserFriendsLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : CursorSupportUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getCursoredUsers(twitter: MicroBlog,
                                  credentials: ParcelableCredentials, paging: Paging): ResponseList<User> {
        when (ParcelableAccountUtils.getAccountType(credentials)) {
            ParcelableAccount.Type.STATUSNET -> {
                run {
                    if (userKey != null) {
                        return twitter.getStatusesFriendsList(userKey.id, paging)
                    } else if (screenName != null) {
                        return twitter.getStatusesFriendsListByScreenName(screenName, paging)
                    }
                }
                run {
                    if (userKey != null) {
                        return twitter.getUsersFriends(userKey.id, paging)
                    } else if (screenName != null) {
                        return twitter.getUsersFriends(screenName, paging)
                    }
                }
                run {
                    if (userKey != null) {
                        return twitter.getFriendsList(userKey.id, paging)
                    } else if (screenName != null) {
                        return twitter.getFriendsListByScreenName(screenName, paging)
                    }
                }
            }
            ParcelableAccount.Type.FANFOU -> {
                run {
                    if (userKey != null) {
                        return twitter.getUsersFriends(userKey.id, paging)
                    } else if (screenName != null) {
                        return twitter.getUsersFriends(screenName, paging)
                    }
                }
                run {
                    if (userKey != null) {
                        return twitter.getFriendsList(userKey.id, paging)
                    } else if (screenName != null) {
                        return twitter.getFriendsListByScreenName(screenName, paging)
                    }
                }
            }
            else -> {
                if (userKey != null) {
                    return twitter.getFriendsList(userKey.id, paging)
                } else if (screenName != null) {
                    return twitter.getFriendsListByScreenName(screenName, paging)
                }
            }
        }
        throw MicroBlogException("user_id or screen_name required")
    }
}
