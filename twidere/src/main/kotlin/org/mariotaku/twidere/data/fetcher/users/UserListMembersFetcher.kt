/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.data.fetcher.users

import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.microblog.PageableResponseList
import org.mariotaku.microblog.library.model.microblog.User
import org.mariotaku.twidere.model.UserKey

class UserListMembersFetcher(
        listId: String?,
        userKey: UserKey?,
        screenName: String?,
        listName: String?
) : UserListRelatedUsersFetcher(listId, userKey, screenName, listName) {

    override fun getByListId(microBlog: Twitter, listId: String, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembers(listId, paging)
    }

    override fun getByUserKey(microBlog: Twitter, listName: String, userKey: UserKey, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembers(listName, userKey.id, paging)
    }

    override fun getByScreenName(microBlog: Twitter, listName: String, screenName: String, paging: Paging): PageableResponseList<User> {
        return microBlog.getUserListMembersByScreenName(listName, screenName, paging)
    }

}
