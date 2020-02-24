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

package org.mariotaku.ktextension

import android.annotation.TargetApi
import android.graphics.Rect
import android.os.Build
import androidx.core.view.WindowInsetsCompat
import android.view.WindowInsets

inline val WindowInsetsCompat.systemWindowInsets: Rect
    get() = Rect(systemWindowInsetLeft, systemWindowInsetTop, systemWindowInsetRight,
            systemWindowInsetBottom)

fun WindowInsetsCompat.getSystemWindowInsets(rect: Rect) {
    rect.set(systemWindowInsetLeft, systemWindowInsetTop, systemWindowInsetRight,
            systemWindowInsetBottom)
}

inline val WindowInsets.systemWindowInsets: Rect
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() = Rect(systemWindowInsetLeft, systemWindowInsetTop, systemWindowInsetRight,
            systemWindowInsetBottom)