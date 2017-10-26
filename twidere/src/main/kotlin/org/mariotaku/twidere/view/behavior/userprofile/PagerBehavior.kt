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
import android.graphics.Rect
import android.support.design.widget.AccessorHeaderScrollingViewBehavior
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.view.measureChildIgnoringInsets

internal class PagerBehavior(context: Context, attrs: AttributeSet? = null) : AccessorHeaderScrollingViewBehavior(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency.id == R.id.profileHeader
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: View,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
            heightUsed: Int): Boolean {
        return parent.measureChildIgnoringInsets(child, parentWidthMeasureSpec, widthUsed,
                parentHeightMeasureSpec, heightUsed)
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        offsetChildAsNeeded(parent, child, dependency)
        return false
    }

    override fun onRequestChildRectangleOnScreen(parent: CoordinatorLayout, child: View,
            rectangle: Rect, immediate: Boolean): Boolean {
        val header = findFirstDependency(parent.getDependencies(child)) ?: return false
        // Offset the rect by the child's left/top
        rectangle.offset(child.left, child.top)

        val parentRect = tempRect1
        parentRect.set(0, 0, parent.width, parent.height)

        if (!parentRect.contains(rectangle)) {
            // If the rectangle can not be fully seen the visible bounds, collapse
            // the AppBarLayout
//            header.setExpanded(false, !immediate)
            return true
        }
        return false
    }

    private fun offsetChildAsNeeded(parent: CoordinatorLayout, child: View, dependency: View) {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior as? HeaderBehavior ?: return
        // Offset the child, pinning it to the bottom the header-dependency, maintaining
        // any vertical gap and overlap

        ViewCompat.offsetTopAndBottom(child, (dependency.bottom - child.top
                + behavior.offsetDelta + verticalLayoutGapAccessor)
                - getOverlapPixelsForOffsetAccessor(dependency))
    }

    override fun getOverlapRatioForOffset(header: View): Float {
//        if (header is AppBarLayout) {
//            val abl = header as AppBarLayout?
//            val totalScrollRange = abl!!.totalScrollRange
//            val preScrollDown = abl.getDownNestedPreScrollRange()
//            val offset = getAppBarLayoutOffset(abl)
//
//            if (preScrollDown != 0 && totalScrollRange + offset <= preScrollDown) {
//                // If we're in a pre-scroll down. Don't use the offset at all.
//                return 0f
//            } else {
//                val availScrollRange = totalScrollRange - preScrollDown
//                if (availScrollRange != 0) {
//                    // Else we'll use a interpolated ratio of the overlap, depending on offset
//                    return 1f + offset / availScrollRange.toFloat()
//                }
//            }
//        }
        return 0f
    }

    private fun getAppBarLayoutOffset(abl: AppBarLayout): Int {
        val lp = abl.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = lp.behavior as? HeaderBehavior ?: return 0
        return behavior.topBottomOffsetForScrollingSibling
    }

    override fun findFirstDependency(views: List<View>): View? {
        return views.firstOrNull { it.id == R.id.profileHeader }
    }

    override fun getScrollRange(v: View): Int {
        return (v as? AppBarLayout)?.totalScrollRange ?: super.getScrollRange(v)
    }

}