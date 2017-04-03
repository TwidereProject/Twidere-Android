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

package org.mariotaku.twidere.graphic

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import io.nayuki.qrcodegen.QrCode

/**
 * Created by mariotaku on 2017/4/3.
 */

class QrCodeDrawable(val qrCode: QrCode) : Drawable() {
    override fun draw(canvas: Canvas) {
        for (x in 0 until qrCode.size) {
            for (y in 0 until qrCode.size) {

            }
        }
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setAlpha(alpha: Int) {
        // NO-OP
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // NO-OP
    }

}
