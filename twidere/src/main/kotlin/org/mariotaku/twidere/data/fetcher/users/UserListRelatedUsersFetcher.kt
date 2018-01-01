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

package org.mariotaku.twidere.data.fetcher.users

import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.microblog.PageableResponseList
import org.mariotaku.microblog.library.model.microblog.User
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey

abstract class UserListRelatedUsersFetcher(
        private val listId: String?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val listName: String?
) : UsersFetcher {

    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging): List<User> {
        when {
            listId != null -> {
                return getByListId(twitter, listId, paging)
            }
            listName != null && userKey != null -> {
                return getByUserKey(twitter, listName.replace(' ', '-'), userKey, paging)
            }
            listName != null && screenName != null -> {
                return getByScreenName(twitter, listName.replace(' ', '-'), screenName, paging)
            }
        }
        throw RequiredFieldNotFoundException("list_id || list_name && (user_id || screen_name)")
    }

    protected abstract fun getByListId(microBlog: Twitter, listId: String, paging: Paging): PageableResponseList<User>

    protected abstract fun getByUserKey(microBlog: Twitter, listName: String, userKey: UserKey, paging: Paging): PageableResponseList<User>

    protected abstract fun getByScreenName(microBlog: Twitter, listName: String, screenName: String, paging: Paging): PageableResponseList<User>

}
