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

package org.mariotaku.twidere.extension.api

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.model.microblog.IDs
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.microblog.library.model.microblog.User
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.pagination.CursorPagination
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList

@Throws(MicroBlogException::class)
fun MicroBlog.tryShowUser(id: String?, screenName: String?, accountType: String?): User {
    try {
        return showUser(id, screenName, accountType)
    } catch (e: MicroBlogException) {
        // Twitter specific error for private API calling through proxy
        if (e.statusCode == 200) {
            return showUserAlternative(id, screenName)
        }
        throw e
    }

}

@Throws(MicroBlogException::class)
inline fun <R> MicroBlog.lookupUsersMapPaginated(ids: IDs, transform: (User) -> R): PaginatedList<R> {
    val response = lookupUsers(ids.iDs)
    val result = response.mapTo(PaginatedArrayList(response.size), transform)
    result.previousPage = CursorPagination.valueOf(ids.previousCursor)
    result.nextPage = CursorPagination.valueOf(ids.nextCursor)
    return result
}

@Throws(MicroBlogException::class)
private fun MicroBlog.showUser(id: String?, screenName: String?, accountType: String?): User {
    return when {
        accountType == AccountType.FANFOU -> showFanfouUser(id ?: screenName ?:
                throw MicroBlogException("Invalid user id or screen name"))
        id != null -> showUser(id)
        screenName != null -> showUserByScreenName(screenName)
        else -> throw MicroBlogException("Invalid user id or screen name")
    }
}

@Throws(MicroBlogException::class)
private fun MicroBlog.showUserAlternative(id: String?, screenName: String?): User {
    val searchScreenName: String = screenName ?: run {
        if (id == null) throw IllegalArgumentException()
        return@run showFriendship(id).targetUserScreenName
    }
    val paging = Paging().count(1)
    val users = searchUsers(searchScreenName, paging)
    val match = users.firstOrNull { it.id == id || searchScreenName.equals(it.screenName, ignoreCase = true) }
    if (match != null) return match
    if (id != null) {
        val timeline = getUserTimeline(id, paging, null)
        val status = timeline.firstOrNull { it.user?.id == id }
        if (status != null) return status.user
    } else {
        val timeline = getUserTimelineByScreenName(searchScreenName, paging, null)
        val status = timeline.firstOrNull { searchScreenName.equals(it.user?.screenName, ignoreCase = true) }
        if (status != null) return status.user
    }
    throw MicroBlogException("Can't find user")
}

