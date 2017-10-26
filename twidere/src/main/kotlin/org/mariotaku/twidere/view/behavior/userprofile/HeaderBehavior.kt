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
import android.support.design.widget.AccessorHeaderBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.math.MathUtils
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.view.measureChildIgnoringInsets

internal class HeaderBehavior(context: Context, attrs: AttributeSet? = null) : AccessorHeaderBehavior<View>(context, attrs) {

    var offsetDelta: Int = 0
        private set

    override fun onMeasureChild(parent: CoordinatorLayout, child: View,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
            heightUsed: Int): Boolean {
        return parent.measureChildIgnoringInsets(child, parentWidthMeasureSpec, widthUsed,
                parentHeightMeasureSpec, heightUsed)
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
    }

    override fun getScrollRangeForDragFling(view: View): Int {
        val parent = view.parent as CoordinatorLayout
        val toolbar = parent.findViewById<View>(R.id.toolbar)
        val toolbarTabs = parent.findViewById<View>(R.id.toolbarTabs)
        return view.height - toolbar.bottom - toolbarTabs.height
    }

    override fun getMaxDragOffset(view: View): Int {
        val parent = view.parent as CoordinatorLayout
        val toolbar = parent.findViewById<View>(R.id.toolbar)
        val toolbarTabs = parent.findViewById<View>(R.id.toolbarTabs)
        return -(view.height - toolbar.bottom - toolbarTabs.height)
    }

    override fun setHeaderTopBottomOffset(parent: CoordinatorLayout, header: View, newOffset: Int, minOffset: Int, maxOffset: Int): Int {
        val curOffset = topBottomOffsetForScrollingSibling
        var consumed = 0

        var newOffset = newOffset
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset)
            if (curOffset != newOffset) {
                val interpolatedOffset = if (header.hasChildWithInterpolator)
                    interpolateOffset(header, newOffset)
                else
                    newOffset

                val offsetChanged = setTopAndBottomOffset(interpolatedOffset)

                // Update how much dy we have consumed
                consumed = curOffset - newOffset
                // Update the stored sibling offset
                offsetDelta = newOffset - interpolatedOffset

                if (!offsetChanged && header.hasChildWithInterpolator) {
                    // If the offset hasn't changed and we're using an interpolated scroll
                    // then we need to keep any dependent views updated. CoL will do this for
                    // us when we move, but we need to do it manually when we don't (as an
                    // interpolated scroll may finish early).
                    parent.dispatchDependentViewsChanged(header)
                }

                // Dispatch the updates to any listeners
//                header.dispatchOffsetUpdates(topAndBottomOffset)

                // Update the AppBarLayout's drawable state (for any elevation changes)
//                updateAppBarLayoutDrawableState(parent, header, newOffset,
//                        if (newOffset < curOffset) -1 else 1, false)
            }
        } else {
            // Reset the offset delta
            offsetDelta = 0
        }

        return consumed
    }

    override fun canDragView(view: View) = true

    private fun interpolateOffset(header: View, offset: Int): Int {
        return offset
    }

    private val View.hasChildWithInterpolator: Boolean
        get() = false

}