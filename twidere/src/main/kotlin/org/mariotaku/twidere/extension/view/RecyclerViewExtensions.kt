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

package org.mariotaku.twidere.extension.view

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.recyclerview.widget.recyclerView
import kotlin.math.max

fun RecyclerView.LayoutManager.calculateSpaceItemHeight(child: View, spaceViewType: Int, typeStart: Int): Int {
    val recyclerView = recyclerView ?: return 0
    var heightBeforeSpace = 0
    for (i in 0 until childCount) {
        val childToMeasure = getChildAt(i)
        val typeToMeasure = getItemViewType(childToMeasure!!)
        if (typeToMeasure == spaceViewType) {
            break
        }
        if (typeToMeasure == typeStart || heightBeforeSpace != 0 && child != childToMeasure) {
            heightBeforeSpace += getDecoratedMeasuredHeight(childToMeasure)
        }
    }
    if (heightBeforeSpace != 0) {
        val spaceHeight = recyclerView.measuredHeight - recyclerView.paddingTop -
                recyclerView.paddingBottom - heightBeforeSpace
        return max(0, spaceHeight)
    }
    return -1
}