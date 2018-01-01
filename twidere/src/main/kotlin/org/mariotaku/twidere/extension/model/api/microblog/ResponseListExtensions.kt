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

package org.mariotaku.twidere.extension.model.api.microblog

import org.mariotaku.microblog.library.model.microblog.PageableResponseList
import org.mariotaku.microblog.library.model.microblog.ResponseList
import org.mariotaku.twidere.model.pagination.*

inline fun <T, R> ResponseList<T>.mapToPaginated(current: Pagination?, transform: (T) -> R): PaginatedList<R> {
    val result = mapTo(PaginatedArrayList(size), transform)
    if (current == null) {
        // Assume we are on page 1
        result.previousPage = null
        result.nextPage = PagePagination.valueOf(2)
    } else if (current is PagePagination) {
        result.previousPage = PagePagination.valueOf(current.page - 1)
        result.nextPage = PagePagination.valueOf(current.page + 1)
    }
    return result
}

inline fun <T, R> PageableResponseList<T>.mapToPaginated(transform: (T) -> R): PaginatedList<R> {
    val result = mapTo(PaginatedArrayList(size), transform)
    result.previousPage = CursorPagination.valueOf(previousCursor)
    result.nextPage = CursorPagination.valueOf(nextCursor)
    return result
}
