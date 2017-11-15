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

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import org.junit.Assert
import org.junit.Test

class DrawableExtensionsKtTest {
    @Test
    fun setBoundsFitCenter() {
        val drawable = TestDrawable()
        drawable.setBoundsFitCenter(0, 0, 100, 100)
        Assert.assertEquals(Rect(25, 0, 75, 100), drawable.bounds)

        drawable.setBoundsFitCenter(0, 0, 50, 100)
        Assert.assertEquals(Rect(0, 0, 50, 100), drawable.bounds)

        drawable.setBoundsFitCenter(0, 0, 10, 100)
        Assert.assertEquals(Rect(0, 40, 10, 60), drawable.bounds)
    }

    private class TestDrawable : Drawable() {
        override fun draw(canvas: Canvas) {
        }

        override fun setAlpha(alpha: Int) {
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
        }

        override fun getIntrinsicWidth(): Int {
            return 100
        }

        override fun getIntrinsicHeight(): Int {
            return 200
        }
    }

}