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
import android.content.res.ColorStateList
import android.os.Build
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import org.mariotaku.twidere.R

class DrawableTintTextView(
        context: Context, attrs: AttributeSet? = null
) : FixedTextView(context, attrs) {

    private var iconWidth: Int = 0
    private var iconHeight: Int = 0

    var compoundDrawableTintListCompat: ColorStateList? = null
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) field else compoundDrawableTintList
        set(value) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                field = value
            } else {
                compoundDrawableTintList = value
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DrawableTintTextView)
        if (a.hasValue(R.styleable.DrawableTintTextView_drawableTint)) {
            compoundDrawableTintListCompat = a.getColorStateList(R.styleable.DrawableTintTextView_drawableTint)
            iconWidth = a.getDimensionPixelSize(R.styleable.DrawableTintTextView_iabIconWidth, 0)
            iconHeight = a.getDimensionPixelSize(R.styleable.DrawableTintTextView_iabIconHeight, 0)
        }
        a.recycle()
        updateDrawableCompat()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableCompat()
    }

    private fun updateDrawableCompat() {
        compoundDrawables.forEach { drawable ->
            if (drawable == null) return@forEach
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                DrawableCompat.setTintList(drawable, compoundDrawableTintListCompat)
            }
            if (iconWidth > 0 && iconHeight > 0) {
                val top = (drawable.intrinsicHeight - iconHeight) / 2
                val left = (drawable.intrinsicWidth - iconWidth) / 2
                drawable.setBounds(left, top, left + iconWidth, top + iconHeight)
            }
        }
    }
}
