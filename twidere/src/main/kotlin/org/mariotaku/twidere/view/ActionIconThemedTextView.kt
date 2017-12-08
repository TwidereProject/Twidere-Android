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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff.Mode
import android.support.annotation.ColorInt
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import org.mariotaku.twidere.R

class ActionIconThemedTextView(
        context: Context, attrs: AttributeSet? = null
) : FixedTextView(context, attrs) {

    private var iconWidth: Int = 0
    private var iconHeight: Int = 0

    var defaultColor: Int = 0
        @ColorInt get() {
            if (field != 0) return field
            val colors = textColors
            if (colors != null) return colors.defaultColor
            return currentTextColor
        }
        set(@ColorInt value) {
            field = value
            refreshDrawableState()
        }

    var disabledColor: Int = 0
        @ColorInt get() {
            if (field != 0) return field
            val colors = textColors
            if (colors != null) return colors.getColorForState(IntArray(0), colors.defaultColor)
            return currentTextColor
        }
        set(@ColorInt value) {
            field = value
            refreshDrawableState()
        }

    var activatedColor: Int = 0
        @ColorInt get() {
            if (field != 0) return field
            val colors = linkTextColors
            if (colors != null) return colors.defaultColor
            return currentTextColor
        }
        set(@ColorInt value) {
            field = value
            refreshDrawableState()
        }

    init {
        @SuppressLint("CustomViewStyleable")
        val a = context.obtainStyledAttributes(attrs, R.styleable.ActionIconThemedTextView,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton)
        defaultColor = a.getColor(R.styleable.ActionIconThemedTextView_iabColor, 0)
        disabledColor = a.getColor(R.styleable.ActionIconThemedTextView_iabDisabledColor, 0)
        activatedColor = a.getColor(R.styleable.ActionIconThemedTextView_iabActivatedColor, 0)
        iconWidth = a.getDimensionPixelSize(R.styleable.ActionIconThemedTextView_iabIconWidth, 0)
        iconHeight = a.getDimensionPixelSize(R.styleable.ActionIconThemedTextView_iabIconHeight, 0)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateCompoundDrawables()
    }

    fun updateCompoundDrawables() {
        for (d in TextViewCompat.getCompoundDrawablesRelative(this)) {
            if (d == null) continue
            d.mutate()
            val color = when {
                isActivated -> activatedColor
                isEnabled -> defaultColor
                else -> disabledColor
            }

            if (iconWidth > 0 && iconHeight > 0) {
                val top = (d.intrinsicHeight - iconHeight) / 2
                val left = (d.intrinsicWidth - iconWidth) / 2
                d.setBounds(left, top, left + iconWidth, top + iconHeight)
            }

            d.setColorFilter(color, Mode.SRC_ATOP)
        }
    }


}
