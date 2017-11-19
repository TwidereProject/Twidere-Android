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

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.AccessorHeaderBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.graphics.drawable.ArgbEvaluator
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.header_user.view.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.constant.themeBackgroundOptionKey
import org.mariotaku.twidere.extension.view.measureChildIgnoringInsets
import org.mariotaku.twidere.graphic.drawable.userprofile.ActionBarDrawable
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.dagger.DependencyHolder

internal class HeaderBehavior(context: Context, attrs: AttributeSet? = null) :
        AccessorHeaderBehavior<ViewGroup>(context, attrs) {

    private val cardBackgroundColor: Int
    private var tabItemIsDark: Int = 0

    private var lastNestedScrollingChild by weak<View>()

    init {
        val preferences = DependencyHolder.get(context).preferences
        cardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                preferences[themeBackgroundOptionKey], preferences[themeBackgroundAlphaKey])
    }

    override fun onStartNestedScroll(parent: CoordinatorLayout, child: ViewGroup,
            directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        // Return true if we're nested scrolling vertically, and we have scrollable children
        // and the scrolling view is big enough to scroll
        val started = (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
                && child.hasScrollableChildren
                && parent.height - directTargetChild.height <= child.height)

        // A new nested scroll has started so clear out the previous ref
        lastNestedScrollingChild = null

        return started
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: ViewGroup,
            target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy != 0) {
            val min: Int
            val max: Int
            if (dy < 0) {
                // We're scrolling down
                min = -child.totalScrollRange
                max = min + child.downNestedPreScrollRange
            } else {
                // We're scrolling up
                min = -child.upNestedPreScrollRange
                max = 0
            }
            if (min != max) {
                consumed[1] = scrollAccessor(coordinatorLayout, child, dy, min, max)
            }
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ViewGroup,
            target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
            type: Int) {
        if (dyUnconsumed < 0) {
            // If the scrolling view is scrolling down but not consuming, it's probably be at
            // the top of it's content
            if (scrollAccessor(coordinatorLayout, child, dyUnconsumed, -child.downNestedScrollRange,
                    0) == 0) {
                if (target is RecyclerView) {
                    target.stopScroll()
                } else {
                    ViewCompat.stopNestedScroll(target)
                }
            }
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: ViewGroup, target: View,
            type: Int) {
        // Keep a reference to the previous nested scrolling child
        lastNestedScrollingChild = target
        scroller?.forceFinished(true)
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: ViewGroup,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
            heightUsed: Int): Boolean {
        return parent.measureChildIgnoringInsets(child, parentWidthMeasureSpec, widthUsed,
                parentHeightMeasureSpec, heightUsed)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: ViewGroup, layoutDirection: Int): Boolean {
        val handled = super.onLayoutChild(parent, child, layoutDirection)
        updateTabColor(parent, child, topAndBottomOffset)
        return handled
    }

    override fun layoutChild(parent: CoordinatorLayout, child: ViewGroup, layoutDirection: Int) {
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
    }

    override fun canDragView(view: ViewGroup): Boolean {
        if (view.translationY != 0f) return false
        // Else we'll use the default behaviour of seeing if it can scroll down
        val scrollingView = lastNestedScrollingChild
        return if (scrollingView != null) {
            // If we have a reference to a scrolling view, check it
            scrollingView.isShown && !scrollingView.canScrollVertically(-1)
        } else {
            // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
            true
        }
    }

    override fun onFlingFinished(parent: CoordinatorLayout, layout: ViewGroup) {
        // At the end of a manual fling, check to see if we need to snap to the edge-child
    }

    override fun getMaxDragOffset(view: ViewGroup): Int {
        return -view.downNestedScrollRange
    }

    override fun getScrollRangeForDragFling(view: ViewGroup): Int {
        return view.totalScrollRange
    }

    override fun setHeaderTopBottomOffset(parent: CoordinatorLayout, header: ViewGroup, newOffset: Int, minOffset: Int, maxOffset: Int): Int {
        val consumed = super.setHeaderTopBottomOffset(parent, header, newOffset, minOffset, maxOffset)
        if (consumed != 0) {
            updateTabColor(parent, header, topAndBottomOffset)
        }
        return consumed
    }

    @SuppressLint("RestrictedApi")
    private fun updateTabColor(parent: CoordinatorLayout, header: View, offset: Int) {
        val actionBarBackground = parent.toolbar.background as? ActionBarDrawable ?: return
        val profileHeaderBackground = parent.profileHeaderBackground

        val toolbarBottom = parent.toolbar.bottom
        val headerBackgroundOffset = offset + profileHeaderBackground.top

        val factor = ((toolbarBottom - headerBackgroundOffset) / profileHeaderBackground.height.toFloat()).coerceIn(0f, 1f)

        val toolbarTabs = header.toolbarTabs

        val colorPrimary = actionBarBackground.color
        val currentTabColor = ArgbEvaluator.getInstance().evaluate(factor,
                cardBackgroundColor, colorPrimary) as Int

        toolbarTabs.setBackgroundColor(currentTabColor)

        val tabItemIsDark = if (ThemeUtils.isLightColor(currentTabColor)) 1 else -1
        if (this.tabItemIsDark != tabItemIsDark) {
            val context = parent.context
            val activity = ChameleonUtils.getActivity(context)
            val tabContrastColor = ThemeUtils.getColorDependent(currentTabColor)
            toolbarTabs.setIconColor(tabContrastColor)
            toolbarTabs.setLabelColor(tabContrastColor)
            val theme = Chameleon.getOverrideTheme(context, activity)
            if (theme.isToolbarColored) {
                toolbarTabs.setStripColor(tabContrastColor)
            } else {
                toolbarTabs.setStripColor(ThemeUtils.getOptimalAccentColor(colorPrimary, tabContrastColor))
            }
            toolbarTabs.updateAppearance()
        }
        this.tabItemIsDark = tabItemIsDark
    }


}