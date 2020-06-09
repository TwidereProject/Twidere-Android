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

package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.Resources
import androidx.core.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 15/5/28.
 */
class NameView(context: Context, attrs: AttributeSet? = null) : FixedTextView(context, attrs) {

    var nameFirst: Boolean = false
    var twoLine: Boolean = false
        set(value) {
            field = value
            maxLines = if (value) {
                2
            } else {
                1
            }
        }

    var name: String? = null
    var screenName: String? = null

    private val primaryTextStyle: StyleSpan
    private val secondaryTextStyle: StyleSpan
    private var primaryTextColor: ForegroundColorSpan? = null
    private var secondaryTextColor: ForegroundColorSpan? = null
    private var primaryTextSize: AbsoluteSizeSpan? = null
    private var secondaryTextSize: AbsoluteSizeSpan? = null

    init {
        ellipsize = TextUtils.TruncateAt.END
        val a = context.obtainStyledAttributes(attrs, R.styleable.NameView, 0, 0)
        setPrimaryTextColor(a.getColor(R.styleable.NameView_nv_primaryTextColor, 0))
        setSecondaryTextColor(a.getColor(R.styleable.NameView_nv_secondaryTextColor, 0))
        twoLine = a.getBoolean(R.styleable.NameView_nv_twoLine, false)
        primaryTextStyle = StyleSpan(a.getInt(R.styleable.NameView_nv_primaryTextStyle, 0))
        secondaryTextStyle = StyleSpan(a.getInt(R.styleable.NameView_nv_secondaryTextStyle, 0))
        a.recycle()
        nameFirst = true
        if (isInEditMode && text.isNullOrEmpty()) {
            name = "Name"
            screenName = "@screenname"
            updateText()
        }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }
    }

    fun setPrimaryTextColor(color: Int) {
        primaryTextColor = ForegroundColorSpan(color)
    }

    fun setSecondaryTextColor(color: Int) {
        secondaryTextColor = ForegroundColorSpan(color)
    }

    fun updateText(formatter: BidiFormatter? = null) {
        val sb = SpannableStringBuilder()
        val primaryText = if (nameFirst) name else screenName
        val secondaryText = if (nameFirst) screenName else name
        if (primaryText != null) {
            val start = sb.length
            if (formatter != null && !isInEditMode) {
                sb.append(formatter.unicodeWrap(primaryText))
            } else {
                sb.append(primaryText)
            }
            val end = sb.length
            sb.setSpan(primaryTextColor, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(primaryTextStyle, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(primaryTextSize, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            sb.setSpan(secondaryTextColor, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(secondaryTextStyle, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(secondaryTextSize, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        setText(sb, BufferType.SPANNABLE)
    }

    fun setPrimaryTextSize(textSize: Float) {
        primaryTextSize = AbsoluteSizeSpan(calculateTextSize(TypedValue.COMPLEX_UNIT_SP, textSize).toInt())
    }

    fun setSecondaryTextSize(textSize: Float) {
        secondaryTextSize = AbsoluteSizeSpan(calculateTextSize(TypedValue.COMPLEX_UNIT_SP, textSize).toInt())
    }

    private fun calculateTextSize(unit: Int, size: Float): Float {
        val r = context.resources ?: Resources.getSystem()
        return TypedValue.applyDimension(unit, size, r.displayMetrics)
    }

}
