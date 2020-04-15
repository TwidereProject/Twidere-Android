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

package org.mariotaku.twidere.extension

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Created by mariotaku on 2017/3/30.
 */

val StaggeredGridLayoutManager.reachingStart: Boolean
    get() {
        var visiblePos = findFirstCompletelyVisibleItemPositions(null)
        if (visiblePos.all { it == RecyclerView.NO_POSITION }) {
            visiblePos = findFirstVisibleItemPositions(null)
        }
        return visiblePos.contains(0)
    }

val StaggeredGridLayoutManager.reachingEnd: Boolean
    get() {
        var visiblePos = findLastCompletelyVisibleItemPositions(null)
        if (visiblePos.all { it == RecyclerView.NO_POSITION }) {
            visiblePos = findLastVisibleItemPositions(null)
        }
        return visiblePos.contains(itemCount - 1)
    }