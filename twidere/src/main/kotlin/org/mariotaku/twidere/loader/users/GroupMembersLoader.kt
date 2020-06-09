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
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.extension.model.api.microblog.mapToPaginated
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList

class GroupMembersLoader(
        context: Context,
        accountKey: UserKey?,
        private val groupId: String?,
        private val groupName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : AbsRequestUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        return getMicroBlogUsers(details, paging).mapToPaginated(pagination) {
            it.toParcelable(details, profileImageSize = profileImageSize)
        }
    }

    private fun getMicroBlogUsers(details: AccountDetails, paging: Paging): ResponseList<User> {
        val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
        return when {
            groupId != null -> microBlog.getGroupMembers(groupId, paging)
            groupName != null -> microBlog.getGroupMembersByName(groupName, paging)
            else -> throw MicroBlogException("groupId or groupName required")
        }
    }

}
