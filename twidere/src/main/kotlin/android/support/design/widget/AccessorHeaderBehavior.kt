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

package android.support.design.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View

internal open class AccessorHeaderBehavior<V : View>(context: Context, attrs: AttributeSet? = null) : HeaderBehavior<V>(context, attrs) {

    internal override fun getScrollRangeForDragFling(view: V): Int {
        return super.getScrollRangeForDragFling(view)
    }

    internal override fun getMaxDragOffset(view: V): Int {
        return super.getMaxDragOffset(view)
    }

    internal override fun setHeaderTopBottomOffset(parent: CoordinatorLayout, header: V, newOffset: Int, minOffset: Int, maxOffset: Int): Int {
        return super.setHeaderTopBottomOffset(parent, header, newOffset, minOffset, maxOffset)
    }

    internal override fun setHeaderTopBottomOffset(parent: CoordinatorLayout, header: V, newOffset: Int): Int {
        return super.setHeaderTopBottomOffset(parent, header, newOffset)
    }

    internal override fun getTopBottomOffsetForScrollingSibling(): Int {
        return super.getTopBottomOffsetForScrollingSibling()
    }

    internal override fun onFlingFinished(parent: CoordinatorLayout, layout: V) {
        super.onFlingFinished(parent, layout)
    }

    internal override fun canDragView(view: V): Boolean {
        return super.canDragView(view)
    }

    internal fun scrollAccessor(coordinatorLayout: CoordinatorLayout, header: V,
            dy: Int, minOffset: Int, maxOffset: Int): Int {
        return scroll(coordinatorLayout, header, dy, minOffset, maxOffset)
    }

}
