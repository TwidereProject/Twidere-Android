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

package org.mariotaku.twidere.loader.users

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.twitter.model.PageableResponseList
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

class UserListMembersLoader(
        context: Context,
        accountKey: UserKey?,
        listId: String?,
        userKey: UserKey?,
        screenName: String?,
        listName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : UserListRelatedUsersLoader(context, accountKey, listId, userKey, screenName, listName, data, fromUser) {

    override fun getByListId(microBlog: MicroBlog, listId: String, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembers(listId, paging)
    }

    override fun getByUserKey(microBlog: MicroBlog, listName: String, userKey: UserKey, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembers(listName, userKey.id, paging)
    }

    override fun getByScreenName(microBlog: MicroBlog, listName: String, screenName: String, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembersByScreenName(listName, screenName, paging)
    }

}
