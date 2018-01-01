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
import android.graphics.Paint
import android.support.annotation.Dimension
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageButton
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.theme.TextAppearance

class LabeledImageButton(context: Context, attrs: AttributeSet?) : AppCompatImageButton(context, attrs) {

    var textAppearance: TextAppearance

    var text: CharSequence? = null
        set(value) {
            field = value
            updateLayout()
        }

    var textSize: Float
        @Dimension(unit = Dimension.PX)
        get() = textAppearance.size.toFloat()
        set(@Dimension(unit = Dimension.PX) value) {
            textAppearance.size = value.toInt()
            updateLayout()
        }

    var drawablePadding: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    private var textLayout: Layout? = null
    private var imageMeasuredWidth: Int = 0
    private var imageMeasuredHeight: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LabeledImageButton)
        textAppearance = TextAppearance.create(context,
                a.getResourceId(R.styleable.LabeledImageButton_android_textAppearance, 0),
                a,
                R.styleable.LabeledImageButton_android_textColor,
                -1,
                R.styleable.LabeledImageButton_android_textSize,
                R.styleable.LabeledImageButton_android_textStyle
        )
        drawablePadding = a.getDimensionPixelSize(R.styleable.LabeledImageButton_android_drawablePadding, 0)
        text = a.getString(R.styleable.LabeledImageButton_android_text)
        a.recycle()

        updateLayout()
    }

    override fun onDraw(canvas: Canvas) {
        val layout = textLayout
        if (layout != null) {
            // TODO: process RTL case
            if (ViewCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL) {
                // Draw original image content, but translate it back `text.width / 2f` px.
                canvas.save()
                canvas.translate((layout.width + drawablePadding) / 2f, 0f)
                super.onDraw(canvas)
                canvas.restore()

                canvas.save()
                canvas.translate(paddingLeft.toFloat(),
                        (imageMeasuredHeight - layout.height) / 2f)
                layout.draw(canvas)
                canvas.restore()
            } else {
                // Draw original image content, but translate it back `text.width / 2f` px.
                canvas.save()
                canvas.translate((-layout.width - drawablePadding) / 2f, 0f)
                super.onDraw(canvas)
                canvas.restore()

                canvas.save()
                canvas.translate(imageMeasuredWidth.toFloat() - paddingRight + drawablePadding,
                        (imageMeasuredHeight - layout.height) / 2f)
                layout.draw(canvas)
                canvas.restore()
            }
        } else {
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        imageMeasuredWidth = measuredWidth
        imageMeasuredHeight = measuredHeight
        val layout = textLayout
        if (layout != null) {
            setMeasuredDimension(imageMeasuredWidth + layout.width + drawablePadding,
                    measuredHeight)
        }
    }

    fun setTextSize(unit: Int = TypedValue.COMPLEX_UNIT_SP, size: Float) {
        textSize = TypedValue.applyDimension(unit, size, resources.displayMetrics)
    }

    private fun updateLayout() {
        val text = this.text
        if (text == null) {
            textLayout = null
            requestLayout()
            return
        }
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.density = resources.displayMetrics.density
        }
        textAppearance.toSpan().updateDrawState(paint)
        val width = paint.measureText(text, 0, text.length).toInt()
        textLayout = StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL,
                1f, 0f, true)
        requestLayout()
    }
}