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

package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.ContentRefreshParam

fun ContentRefreshParam.getMaxId(index: Int): String? {
    return (pagination?.get(index) as? SinceMaxPagination)?.maxId
}

fun ContentRefreshParam.getSinceId(index: Int): String? {
    return (pagination?.get(index) as? SinceMaxPagination)?.sinceId
}

fun ContentRefreshParam.getMaxSortId(index: Int): Long {
    return (pagination?.get(index) as? SinceMaxPagination)?.maxSortId ?: -1
}

fun ContentRefreshParam.getSinceSortId(index: Int): Long {
    return (pagination?.get(index) as? SinceMaxPagination)?.sinceSortId ?: -1
}