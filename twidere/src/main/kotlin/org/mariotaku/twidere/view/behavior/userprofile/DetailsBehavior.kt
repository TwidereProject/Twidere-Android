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

package org.mariotaku.twidere.view.behavior.userprofile

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.PublicHeaderBehavior
import android.support.v4.view.WindowInsetsCompat
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.view.measureChildIgnoringInsets

internal class DetailsBehavior(context: Context, attrs: AttributeSet? = null) : PublicHeaderBehavior<View>(context, attrs) {

    override fun onMeasureChild(parent: CoordinatorLayout, child: View,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
            heightUsed: Int): Boolean {
        return parent.measureChildIgnoringInsets(child, parentWidthMeasureSpec, widthUsed,
                parentHeightMeasureSpec, heightUsed)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout, child: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return super.onApplyWindowInsets(coordinatorLayout, child, insets)
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
    }

    override fun getScrollRangeForDragFling(view: View): Int {
        val parent = view.parent as CoordinatorLayout
        val toolbar = parent.findViewById<View>(R.id.toolbar)
        return super.getScrollRangeForDragFling(view) - toolbar.bottom
    }

    override fun getMaxDragOffset(view: View): Int {
        val parent = view.parent as CoordinatorLayout
        val toolbar = parent.findViewById<View>(R.id.toolbar)
        return super.getMaxDragOffset(view) + toolbar.bottom
    }

    override fun canDragView(view: View): Boolean {
        return true
    }

    override fun getTopBottomOffsetForScrollingSibling(): Int {
        return super.getTopBottomOffsetForScrollingSibling()
    }

}