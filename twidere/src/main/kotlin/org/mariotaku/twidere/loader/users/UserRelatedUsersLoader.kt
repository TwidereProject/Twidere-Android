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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList

abstract class UserRelatedUsersLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : AbsRequestUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    final override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        return when {
            userKey != null -> getUsersByKey(details, paging, userKey)
            screenName != null -> getUsersByScreenName(details, paging, screenName)
            else -> throw MicroBlogException("user_id or screen_name required")
        }
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getUsersByKey(details: AccountDetails, paging: Paging, userKey: UserKey): PaginatedList<ParcelableUser>

    @Throws(MicroBlogException::class)
    protected abstract fun getUsersByScreenName(details: AccountDetails, paging: Paging, screenName: String): PaginatedList<ParcelableUser>
}
