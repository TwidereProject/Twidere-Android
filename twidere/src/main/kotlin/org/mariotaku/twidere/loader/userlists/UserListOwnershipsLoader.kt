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

package org.mariotaku.twidere.loader.userlists

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey

class UserListOwnershipsLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableUserList>?
) : BaseUserListsLoader(context, accountKey, data) {

    @Throws(MicroBlogException::class)
    override fun getUserLists(twitter: MicroBlog, paging: Paging): List<UserList> {
        if (userKey != null) {
            return twitter.getUserListOwnerships(userKey.id, paging)
        } else if (screenName != null) {
            return twitter.getUserListOwnershipsByScreenName(screenName, paging)
        }
        throw MicroBlogException("Invalid user")
    }

    override fun isFollowing(list: UserList): Boolean {
        return true
    }
}
