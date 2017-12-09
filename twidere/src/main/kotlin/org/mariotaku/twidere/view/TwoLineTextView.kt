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
import android.content.res.Resources
import android.support.annotation.Dimension
import android.support.v4.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.TypedValue
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.theme.TextAppearance

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

        primaryTextAppearance = TextAppearance.create(context,
                a.getResourceId(R.styleable.TwoLineTextView_tltvPrimaryTextAppearance, 0),
                a,
                R.styleable.TwoLineTextView_tltvPrimaryTextColor,
                R.styleable.TwoLineTextView_tltvPrimaryLinkTextColor,
                R.styleable.TwoLineTextView_tltvPrimaryTextSize,
                R.styleable.TwoLineTextView_tltvPrimaryTextStyle)
        secondaryTextAppearance = TextAppearance.create(context,
                a.getResourceId(R.styleable.TwoLineTextView_tltvSecondaryTextAppearance, 0),
                a,
                R.styleable.TwoLineTextView_tltvSecondaryTextColor,
                R.styleable.TwoLineTextView_tltvSecondaryLinkTextColor,
                R.styleable.TwoLineTextView_tltvSecondaryTextSize,
                R.styleable.TwoLineTextView_tltvSecondaryTextStyle)
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

}