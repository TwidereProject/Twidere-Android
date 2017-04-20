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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.CursorSupport
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.loader.iface.ICursorSupportLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

abstract class CursorSupportUsersLoader(
        context: Context,
        accountKey: UserKey?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : AbsRequestUsersLoader(context, accountKey, data, fromUser), ICursorSupportLoader {

    var page = -1
    override var cursor: Long = 0
    val count: Int

    override var nextCursor: Long = 0
        protected set
    override var prevCursor: Long = 0
        protected set
    var nextPage: Int = 0
        protected set

    init {
        val preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT)
        count = Math.min(100, loadItemLimit)
    }

    protected fun setCursors(cursor: CursorSupport?) {
        if (cursor == null) return
        nextCursor = cursor.nextCursor
        prevCursor = cursor.previousCursor
    }

    protected fun incrementPage(users: List<ParcelableUser>) {
        if (users.isEmpty()) return
        if (page == -1) {
            page = 1
        }
        nextPage = page + 1
    }


    @Throws(MicroBlogException::class)
    protected abstract fun getUsers(details: AccountDetails, paging: Paging): List<ParcelableUser>

    @Throws(MicroBlogException::class)
    override fun getUsers(details: AccountDetails): List<ParcelableUser> {
        val paging = Paging()
        paging.count(count)
        if (cursor > 0) {
            paging.setCursor(cursor)
        } else if (page > 1) {
            paging.setPage(page)
        }
        val users = getUsers(details, paging)
        incrementPage(users)
        return users
    }

}
