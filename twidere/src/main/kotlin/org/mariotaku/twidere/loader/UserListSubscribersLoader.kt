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
import org.mariotaku.microblog.library.twitter.model.PageableResponseList
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

class UserListSubscribersLoader(
        context: Context,
        accountKey: UserKey,
        private val listId: String?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val listName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : CursorSupportUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getCursoredUsers(twitter: MicroBlog, details: AccountDetails, paging: Paging): PageableResponseList<User> {
        if (listId != null)
            return twitter.getUserListSubscribers(listId, paging)
        else if (userKey != null)
            return twitter.getUserListSubscribers(listName!!.replace(' ', '-'), userKey.id, paging)
        else if (screenName != null)
            return twitter.getUserListSubscribersByScreenName(listName!!.replace(' ', '-'), screenName, paging)
        throw MicroBlogException("list_id or list_name and user_id (or screen_name) required")
    }

}
