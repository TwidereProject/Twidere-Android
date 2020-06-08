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

package org.mariotaku.twidere.extension

import android.graphics.Point
import android.graphics.Rect
import androidx.core.view.ViewCompat

/**
 * Created by mariotaku on 2017/2/20.
 */

val Rect.origin: Point get() = Point(left, top)

fun Rect.offsetTopTo(y: Int) {
    offsetTo(left, y)
}

fun Rect.offsetBottomTo(y: Int) {
    offsetTo(left, y - height())
}

fun Rect.offsetLeftTo(x: Int) {
    offsetTo(x, top)
}

fun Rect.offsetRightTo(x: Int) {
    offsetTo(x - width(), top)
}

fun Rect.offsetStartTo(x: Int, layoutDirection: Int) {
    if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        offsetRightTo(x)
    } else {
        offsetLeftTo(x)
    }
}

fun Rect.offsetEndTo(x: Int, layoutDirection: Int) {
    if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        offsetLeftTo(x)
    } else {
        offsetRightTo(x)
    }
}

fun Rect.getStart(layoutDirection: Int): Int {
    return if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        this.right
    } else {
        this.left
    }
}

fun Rect.getEnd(layoutDirection: Int): Int {
    return if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        this.left
    } else {
        this.right
    }
}