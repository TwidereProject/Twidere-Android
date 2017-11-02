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

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.lastWindowInsetsCompat
import android.view.View
import android.view.ViewGroup

fun CoordinatorLayout.measureChildIgnoringInsets(child: View, parentWidthMeasureSpec: Int,
        widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
    val lastInsets = lastWindowInsetsCompat ?: return false

    val lp = child.layoutParams as ViewGroup.MarginLayoutParams

    val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec
            + lastInsets.systemWindowInsetLeft + lastInsets.systemWindowInsetRight,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width)
    val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec
            + lastInsets.systemWindowInsetTop + lastInsets.systemWindowInsetBottom,
            paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height)
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    return true
}
