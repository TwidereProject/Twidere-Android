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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.PageableResponseList
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.model.api.microblog.mapToPaginated
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList

abstract class UserListRelatedUsersLoader(
        context: Context,
        accountKey: UserKey?,
        private val listId: String?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val listName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : AbsRequestUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    final override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        when (details.type) {
            AccountType.TWITTER -> return getTwitterUsers(details, paging).mapToPaginated {
                it.toParcelable(details, profileImageSize = profileImageSize)
            }
            else -> {
                throw APINotSupportedException(details.type)
            }
        }
    }

    protected abstract fun getByListId(microBlog: MicroBlog, listId: String, paging: Paging): PageableResponseList<User>

    protected abstract fun getByUserKey(microBlog: MicroBlog, listName: String, userKey: UserKey, paging: Paging): PageableResponseList<User>

    protected abstract fun getByScreenName(microBlog: MicroBlog, listName: String, screenName: String, paging: Paging): PageableResponseList<User>

    @Throws(MicroBlogException::class)
    private fun getTwitterUsers(details: AccountDetails, paging: Paging): PageableResponseList<User> {
        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
        when {
            listId != null -> {
                return getByListId(microBlog, listId, paging)
            }
            listName != null && userKey != null -> {
                return getByUserKey(microBlog, listName.replace(' ', '-'), userKey, paging)
            }
            listName != null && screenName != null -> {
                return getByScreenName(microBlog, listName.replace(' ', '-'), screenName, paging)
            }
        }
        throw MicroBlogException("list_id or list_name and user_id (or screen_name) required")
    }

}
