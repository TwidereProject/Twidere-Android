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

import android.animation.ArgbEvaluator
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.lastWindowInsetsCompat
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.twidere.R
import org.mariotaku.twidere.graphic.drawable.userprofile.ActionBarDrawable
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.support.WindowSupport

internal class StatusBarBehavior(context: Context, attrs: AttributeSet? = null) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private val argbEvaluator = ArgbEvaluator()

    private var lightStatusBar: Int = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency.id == R.id.profileHeader
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val lastInsets = parent.lastWindowInsetsCompat ?: return true
        val height = lastInsets.systemWindowInsetTop
        child.layout(0, 0, child.measuredWidth, height)
        updateStatusBarColor(parent, child, parent.getDependencies(child).first())
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        updateStatusBarColor(parent, child, dependency)
        return true
    }

    private fun updateStatusBarColor(parent: CoordinatorLayout, child: View, dependency: View) {
        if (child.height == 0) return
        val toolbar = parent.toolbar
        val actionBarBackground = toolbar.background as? ActionBarDrawable ?: return
        val factor = ToolbarBehavior.colorFactor(dependency, parent.profileBannerContainer, toolbar)
        val primaryColorDark = ChameleonUtils.darkenColor(actionBarBackground.color)
        val statusBarColor = argbEvaluator.evaluate(factor, 0xA0000000.toInt(),
                ChameleonUtils.darkenColor(primaryColorDark))
        child.setBackgroundColor(statusBarColor as Int)
        val lightStatusBar = if (ThemeUtils.isLightColor(statusBarColor)) 1 else -1
        if (this.lightStatusBar != lightStatusBar) {
            val window = ChameleonUtils.getActivity(parent.context)?.window
            if (window != null) {
                WindowSupport.setLightStatusBar(window, lightStatusBar == 1)
            }
        }
        this.lightStatusBar = lightStatusBar
    }

}