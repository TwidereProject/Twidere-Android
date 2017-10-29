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

import android.graphics.drawable.Drawable

fun Drawable.setBoundsFitCenter(left: Int, top: Int, right: Int, bottom: Int) {
    val boundsWidth = right - left
    val boundsHeight = bottom - top
    if (intrinsicWidth <= 0 || intrinsicHeight <= 0 || boundsWidth <= 0 || boundsHeight <= 0) {
        setBounds(left, top, right, bottom)
        return
    }
    val intrinsicAspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
    val boundsAspectRatio = boundsWidth / boundsHeight.toFloat()
    if (intrinsicAspectRatio < boundsAspectRatio) {
        // Match height
        val actualWidth = (boundsWidth * (intrinsicAspectRatio / boundsAspectRatio)).toInt()
        setBounds(boundsWidth / 2 - actualWidth / 2, top,
                boundsWidth / 2 + actualWidth / 2, bottom)
    } else {
        // Match width
        val actualHeight = (boundsHeight * (boundsAspectRatio / intrinsicAspectRatio)).toInt()
        setBounds(left, boundsHeight / 2 - actualHeight / 2, right,
                boundsHeight / 2 + actualHeight / 2)
    }
}