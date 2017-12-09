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

package org.mariotaku.twidere.model.theme

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.support.annotation.Px
import android.support.annotation.StyleableRes
import android.text.style.TextAppearanceSpan

data class TextAppearance(
        var color: ColorStateList?,
        var colorLink: ColorStateList?,
        @Px var size: Int,
        var style: Int,
        var typeface: String?
) {
    fun toSpan(): TextAppearanceSpan = TextAppearanceSpan(typeface, style, size, color, colorLink)

    companion object {

        @StyleableRes
        private val TextAppearance = intArrayOf(android.R.attr.textColor, android.R.attr.textColorLink,
                android.R.attr.textSize, android.R.attr.textStyle, android.R.attr.fontFamily,
                android.R.attr.typeface)

        @StyleableRes private const val TextAppearance_textColor = 0
        @StyleableRes private const val TextAppearance_textColorLink = 1
        @StyleableRes private const val TextAppearance_textSize = 2
        @StyleableRes private const val TextAppearance_textStyle = 3
        @StyleableRes private const val TextAppearance_fontFamily = 4
        @StyleableRes private const val TextAppearance_typeface = 5

        fun create(context: Context, appearanceId: Int): TextAppearance {
            val a = context.obtainStyledAttributes(appearanceId, TextAppearance)
            try {
                return TextAppearance(
                        color = a.getColorStateList(TextAppearance_textColor),
                        colorLink = a.getColorStateList(TextAppearance_textColorLink),
                        size = a.getDimensionPixelSize(TextAppearance_textSize, 0),
                        style = a.getInt(TextAppearance_textStyle, Typeface.NORMAL),
                        typeface = a.getString(TextAppearance_fontFamily) ?: mapTypeface(a.getInt(TextAppearance_typeface, 0))
                )
            } finally {
                a.recycle()
            }
        }

        fun create(context: Context, appearanceId: Int, overrideAttrs: TypedArray,
                colorAttr: Int, colorLinkAttr: Int, sizeAttr: Int, styleAttr: Int): TextAppearance {
            val appearance = create(context, appearanceId)
            if (colorAttr != -1 && overrideAttrs.hasValue(colorAttr)) {
                appearance.color = overrideAttrs.getColorStateList(colorAttr)
            }
            if (colorLinkAttr != -1 && overrideAttrs.hasValue(colorLinkAttr)) {
                appearance.colorLink = overrideAttrs.getColorStateList(colorLinkAttr)
            }
            if (sizeAttr != -1 && overrideAttrs.hasValue(sizeAttr)) {
                appearance.size = overrideAttrs.getDimensionPixelSize(sizeAttr, appearance.size)
            }
            if (styleAttr != -1 && overrideAttrs.hasValue(styleAttr)) {
                appearance.style = overrideAttrs.getInt(styleAttr, appearance.style)
            }
            return appearance
        }


        private fun mapTypeface(tf: Int) = when (tf) {
            1 -> "sans"
            2 -> "serif"
            3 -> "monospace"
            else -> null
        }

    }
}