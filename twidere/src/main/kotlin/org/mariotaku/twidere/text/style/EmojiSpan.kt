/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 15/12/22.
 */
class EmojiSpan(private val drawable: Drawable) : DynamicDrawableSpan(ALIGN_BOTTOM) {
    private val fontMetrics: Paint.FontMetrics = Paint.FontMetrics()

    override fun getDrawable(): Drawable? {
        return drawable
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int,
            fm: Paint.FontMetricsInt?): Int {
        val drawable = getDrawable() ?: return 0
        paint.getFontMetrics(fontMetrics)
        val textHeightPx = (fontMetrics.descent - fontMetrics.ascent).roundToInt()
        val intrinsicWidth = drawable.intrinsicWidth.toFloat()
        val intrinsicHeight = drawable.intrinsicHeight.toFloat()
        val scaledWidth: Int
        scaledWidth = if (intrinsicWidth > intrinsicHeight) {
            (textHeightPx * (intrinsicWidth / intrinsicHeight)).roundToInt()
        } else {
            (intrinsicWidth * (textHeightPx / intrinsicHeight)).roundToInt()
        }
        val top = fontMetrics.bottom.roundToInt() - textHeightPx
        val left = 0
        drawable.setBounds(left, top, left + scaledWidth, top + textHeightPx)
        return scaledWidth
    }


    override fun draw(canvas: Canvas, text: CharSequence, start: Int,
            end: Int, x: Float, top: Int, y: Int, bottom: Int,
            paint: Paint) {
        val b = getDrawable() ?: return
        canvas.save()
        canvas.translate(x, y.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

}
