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

package org.mariotaku.twidere.extension.model.api.mastodon

import org.mariotaku.microblog.library.mastodon.model.Results
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList

inline fun <T, R> Results.mapToPaginated(listSelector: (Results) -> List<T>?, transform: (T) -> R): PaginatedList<R> {
    val list = listSelector(this) ?: return PaginatedArrayList()
    val result = list.mapTo(PaginatedArrayList(list.size), transform)
    result.previousPage = getLinkPagination("prev")
    result.nextPage = getLinkPagination("next")
    return result
}
