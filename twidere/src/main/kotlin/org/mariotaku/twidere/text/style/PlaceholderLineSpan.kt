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

package org.mariotaku.twidere.text.style

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class PlaceholderLineSpan(val width: Float, val widthRelativeToChar: Boolean = false) : ReplacementSpan() {

    private val fontMetrics = Paint.FontMetrics()

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        paint.getFontMetrics(fontMetrics)
        if (fm != null) {
            paint.getFontMetricsInt(fm)
        }
        return if (widthRelativeToChar) {
            (width * paint.textSize).toInt()
        } else {
            100
        }
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width = if (widthRelativeToChar) {
            width * paint.textSize
        } else {
            (canvas.width - x) * width
        }
        val paintAlphaBackup = paint.alpha
        paint.alpha = placeholderAlpha
        canvas.drawRect(x, top + fontMetrics.leading, x + width, bottom - fontMetrics.descent,
                paint)
        paint.alpha = paintAlphaBackup
    }

    companion object {
        const val placeholderAlpha = 0x20
    }
}
