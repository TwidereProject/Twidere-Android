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

package org.mariotaku.twidere.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint.Align
import android.graphics.Rect
import android.support.v4.graphics.ColorUtils
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R

class BadgeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var text: String? = null
        set(value) {
            field = value
            updateTextPosition()
            invalidate()
        }
    var color: Int
        get() = ColorUtils.setAlphaComponent(textPaint.color, textPaint.alpha)
        set(value) {
            textPaint.color = ColorUtils.setAlphaComponent(value, 0xFF)
            textPaint.alpha = Color.alpha(value)
            invalidate()
        }

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private var textX: Float = 0.toFloat()
    private var textY: Float = 0.toFloat()
    private val textBounds: Rect

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BadgeView)
        color = a.getColor(R.styleable.BadgeView_android_textColor, Color.WHITE)
        text = a.getString(R.styleable.BadgeView_android_text)
        a.recycle()
        textPaint.textAlign = Align.CENTER
        textBounds = Rect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        val hPadding = (Math.round(w * (Math.pow(2.0, 0.5) - 1)) / 2).toInt()
        val vPadding = (Math.round(h * (Math.pow(2.0, 0.5) - 1)) / 2).toInt()
        setPadding(hPadding, vPadding, hPadding, vPadding)
        updateTextPosition()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!textBounds.isEmpty) {
            canvas.drawText(text, textX, textY, textPaint)
        }
    }

    private fun updateTextPosition() {
        val width = width
        val height = height
        if (width == 0 || height == 0) return
        val contentWidth = (width - paddingLeft - paddingRight).toFloat()
        val contentHeight = (height - paddingTop - paddingBottom).toFloat()

        val text = this.text
        if (text != null) {
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val scale = Math.min(contentWidth / textBounds.width(), contentHeight / textBounds.height())
            textPaint.textSize = Math.min((height / 2).toFloat(), textPaint.textSize * scale)
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            textX = contentWidth / 2 + paddingLeft
            textY = contentHeight / 2 + paddingTop.toFloat() + (textBounds.height() / 2).toFloat()
        } else {
            textBounds.setEmpty()
        }
    }
}
