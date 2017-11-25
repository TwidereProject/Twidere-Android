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
import android.content.res.Resources
import android.graphics.Typeface
import android.support.annotation.Dimension
import android.support.annotation.StyleableRes
import android.support.v4.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.TypedValue
import org.mariotaku.twidere.R

open class TwoLineTextView(context: Context, attrs: AttributeSet? = null) : FixedTextView(context, attrs) {

    var twoLine: Boolean = false
        set(value) {
            field = value
            maxLines = if (value) 2 else 1
        }

    val primaryTextAppearance: TextAppearance
    val secondaryTextAppearance: TextAppearance

    var primaryText: CharSequence? = null
    var secondaryText: CharSequence? = null

    protected open val displayPrimaryText: CharSequence?
        get() = primaryText

    protected open val displaySecondaryText: CharSequence?
        get() = secondaryText

    private var primaryTextSpan: TextAppearanceSpan
    private var secondaryTextSpan: TextAppearanceSpan

    init {
        ellipsize = TextUtils.TruncateAt.END
        val a = context.obtainStyledAttributes(attrs, R.styleable.TwoLineTextView, 0, 0)
        twoLine = a.getBoolean(R.styleable.TwoLineTextView_tltvTwoLine, false)
        primaryText = a.getText(R.styleable.TwoLineTextView_tltvPrimaryText)
        secondaryText = a.getText(R.styleable.TwoLineTextView_tltvSecondaryText)

        primaryTextAppearance = createTextAppearance(a.getResourceId(R.styleable.TwoLineTextView_tltvPrimaryTextAppearance, 0),
                a.getColorStateList(R.styleable.TwoLineTextView_tltvPrimaryTextColor),
                a.getColorStateList(R.styleable.TwoLineTextView_tltvPrimaryLinkTextColor),
                a.getDimensionPixelSize(R.styleable.TwoLineTextView_tltvPrimaryTextSize, 0),
                a.getInt(R.styleable.TwoLineTextView_tltvPrimaryTextStyle, -1))
        secondaryTextAppearance = createTextAppearance(a.getResourceId(R.styleable.TwoLineTextView_tltvSecondaryTextAppearance, 0),
                a.getColorStateList(R.styleable.TwoLineTextView_tltvSecondaryTextColor),
                a.getColorStateList(R.styleable.TwoLineTextView_tltvSecondaryLinkTextColor),
                a.getDimensionPixelSize(R.styleable.TwoLineTextView_tltvSecondaryTextSize, 0),
                a.getInt(R.styleable.TwoLineTextView_tltvSecondaryTextStyle, -1))
        a.recycle()

        primaryTextSpan = primaryTextAppearance.toSpan()
        secondaryTextSpan = secondaryTextAppearance.toSpan()
        updateText()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }
    }

    fun setPrimaryTextSize(size: Float, unit: Int = TypedValue.COMPLEX_UNIT_SP) {
        primaryTextAppearance.size = calculateTextSize(size, unit).toInt()
    }

    fun setSecondaryTextSize(size: Float, unit: Int = TypedValue.COMPLEX_UNIT_SP) {
        secondaryTextAppearance.size = calculateTextSize(size, unit).toInt()
    }

    fun updateText(formatter: BidiFormatter? = null) {
        val sb = SpannableStringBuilder()
        val primaryText = displayPrimaryText
        val secondaryText = displaySecondaryText
        if (primaryText != null) {
            val start = sb.length
            if (formatter != null && !isInEditMode) {
                sb.append(formatter.unicodeWrap(primaryText))
            } else {
                sb.append(primaryText)
            }
            val end = sb.length
            sb.setSpan(primaryTextSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        sb.append(if (twoLine) '\n' else ' ')
        if (secondaryText != null) {
            val start = sb.length
            if (formatter != null && !isInEditMode) {
                sb.append(formatter.unicodeWrap(secondaryText))
            } else {
                sb.append(secondaryText)
            }
            val end = sb.length
            sb.setSpan(secondaryTextSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        setText(sb, BufferType.SPANNABLE)
    }

    fun updateTextAppearance() {
        primaryTextSpan = primaryTextAppearance.toSpan()
        secondaryTextSpan = secondaryTextAppearance.toSpan()
    }

    @Dimension(unit = Dimension.PX)
    private fun calculateTextSize(size: Float, unit: Int): Float {
        val r = context.resources ?: Resources.getSystem()
        return TypedValue.applyDimension(unit, size, r.displayMetrics)
    }

    private fun updatePrimaryTextSpan() {
        primaryTextSpan = primaryTextAppearance.toSpan()
    }

    private fun updateSecondaryTextSpan() {
        secondaryTextSpan = secondaryTextAppearance.toSpan()
    }

    private fun createTextAppearance(appearanceId: Int, overrideColor: ColorStateList?,
            overrideColorLink: ColorStateList?, overrideSize: Int, overrideStyle: Int): TextAppearance {
        val a = context.obtainStyledAttributes(appearanceId, Styleable.TextAppearance)

        val textColor = overrideColor ?: a.getColorStateList(Styleable.TextAppearance_textColor)
        val textColorLink = overrideColorLink ?: a.getColorStateList(Styleable.TextAppearance_textColorLink) ?: overrideColorLink
        val textSize = if (overrideSize > 0) overrideSize else a.getDimensionPixelSize(Styleable.TextAppearance_textSize, 0)
        val style = if (overrideStyle != -1) overrideStyle else a.getInt(Styleable.TextAppearance_textStyle, Typeface.NORMAL)
        val typeface = a.getString(Styleable.TextAppearance_fontFamily) ?: mapTypeface(a.getInt(Styleable.TextAppearance_typeface, 0))

        a.recycle()
        return TextAppearance(textColor, textColorLink, textSize, style, typeface)
    }

    private fun TextAppearance.toSpan(): TextAppearanceSpan = TextAppearanceSpan(typeface, style, size, color, colorLink)

    data class TextAppearance(
            var color: ColorStateList?,
            var colorLink: ColorStateList?,
            var size: Int,
            var style: Int,
            var typeface: String?
    )

    private object Styleable {

        @StyleableRes
        val TextAppearance = intArrayOf(android.R.attr.textColor, android.R.attr.textColorLink,
                android.R.attr.textSize, android.R.attr.textStyle, android.R.attr.fontFamily,
                android.R.attr.typeface)

        @StyleableRes const val TextAppearance_textColor = 0
        @StyleableRes const val TextAppearance_textColorLink = 1
        @StyleableRes const val TextAppearance_textSize = 2
        @StyleableRes const val TextAppearance_textStyle = 3
        @StyleableRes const val TextAppearance_fontFamily = 4
        @StyleableRes const val TextAppearance_typeface = 5

    }

    companion object {


        private fun mapTypeface(tf: Int) = when (tf) {
            1 -> "sans"
            2 -> "serif"
            3 -> "monospace"
            else -> null
        }
    }

}