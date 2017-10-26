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
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.header_user.view.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.constant.themeBackgroundOptionKey
import org.mariotaku.twidere.extension.view.measureChildIgnoringInsets
import org.mariotaku.twidere.graphic.drawable.userprofile.ActionBarDrawable
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder

internal class HeaderBehavior(context: Context, attrs: AttributeSet? = null) : AccessorHeaderBehavior<View>(context, attrs) {

    var offsetDelta: Int = 0
        private set

    private val cardBackgroundColor: Int
    private var tabItemIsDark: Int = 0

    init {
        val preferences = DependencyHolder.get(context).preferences
        cardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                preferences[themeBackgroundOptionKey], preferences[themeBackgroundAlphaKey])
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: View,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
            heightUsed: Int): Boolean {
        return parent.measureChildIgnoringInsets(child, parentWidthMeasureSpec, widthUsed,
                parentHeightMeasureSpec, heightUsed)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val result = super.onLayoutChild(parent, child, layoutDirection)
        updateTabColor(parent, child, topAndBottomOffset)
        return result
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

        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            val clampedOffset = newOffset.coerceIn(minOffset, maxOffset)
            if (curOffset != clampedOffset) {

                topAndBottomOffset = clampedOffset
                updateTabColor(parent, header, clampedOffset)
                // Update how much dy we have consumed
                consumed = curOffset - clampedOffset
                // Update the stored sibling offset
                offsetDelta = clampedOffset - clampedOffset

            }
        } else {
            // Reset the offset delta
            offsetDelta = 0
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

    override fun canDragView(view: View) = true

}