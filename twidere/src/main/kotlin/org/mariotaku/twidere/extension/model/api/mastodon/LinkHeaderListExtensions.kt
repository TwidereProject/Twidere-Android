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

import android.net.Uri
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderResponse
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination

inline fun <T, R> LinkHeaderList<T>.mapToPaginated(transform: (T) -> R): PaginatedList<R> {
    val result = mapTo(PaginatedArrayList(size), transform)
    result.previousPage = getLinkPagination("prev")
    result.nextPage = getLinkPagination("next")
    return result
}

fun LinkHeaderResponse.getLinkPagination(key: String): Pagination? {
    val uri = getLinkPart(key)?.let(Uri::parse) ?: return null
    val maxId = uri.getQueryParameter("max_id")
    val sinceId = uri.getQueryParameter("since_id")
    if (maxId != null || sinceId != null) {
        return SinceMaxPagination().apply {
            this.maxId = maxId
            this.sinceId = sinceId
        }
    }
    return null
}