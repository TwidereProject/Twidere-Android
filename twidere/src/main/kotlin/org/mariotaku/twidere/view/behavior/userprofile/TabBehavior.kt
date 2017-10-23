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
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.TabPagerIndicator

internal class TabBehavior(context: Context, attrs: AttributeSet? = null) : PublicHeaderBehavior<TabPagerIndicator>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: TabPagerIndicator, dependency: View): Boolean {
        return dependency.id == R.id.profileHeader
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: TabPagerIndicator, layoutDirection: Int): Boolean {
        val profileHeader = parent.getDependencies(child).first()
        child.layout(0, profileHeader.bottom, child.measuredWidth, profileHeader.bottom + child.measuredHeight)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: TabPagerIndicator, dependency: View): Boolean {
        child.requestLayout()
        return true
    }
}