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
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.R

internal class BannerBehavior(context: Context, attrs: AttributeSet? = null) : CoordinatorLayout.Behavior<ViewGroup>(context, attrs) {

    private val bannerAspectRatio: Float

    private var bannerContainerTop: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BannerBehavior)
        bannerAspectRatio = a.getFraction(R.styleable.BannerBehavior_bannerAspectRatio, 1, 1, 1f)
        a.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        return dependency.id == R.id.profileHeader
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: ViewGroup,
            parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        val measuredWidth = View.MeasureSpec.getSize(parentWidthMeasureSpec)
        val measuredHeight = (measuredWidth / bannerAspectRatio).toInt()
        child.measure(parentWidthMeasureSpec, View.MeasureSpec.makeMeasureSpec(measuredHeight,
                View.MeasureSpec.EXACTLY))
        return true
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: ViewGroup, layoutDirection: Int): Boolean {
        child.layout(0, bannerContainerTop, child.measuredWidth, child.measuredHeight)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        bannerContainerTop = dependency.top.coerceIn(-child.height..0)
        ViewCompat.offsetTopAndBottom(child, bannerContainerTop - child.top)
        for (i in 0 until child.childCount) {
            child.getChildAt(i).translationY = bannerContainerTop / -2f
        }
        return true
    }
}
