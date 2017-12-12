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
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import org.mariotaku.twidere.util.Utils.getLocalizedNumber
import java.util.*

class StatusTextCountView(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context,
        attrs, android.R.attr.textViewStyle) {

    @ColorInt
    private val defaultTextColor = currentTextColor

    private val warnLimit = 10

    var remaining: Int? = null
        set(count) {
            field = count
            updateTextCount()
        }

    private fun updateTextCount() {
        val remaining = this.remaining
        if (remaining == null) {
            text = null
            return
        }
        text = getLocalizedNumber(Locale.getDefault(), remaining)
        val exceededLimit = remaining < 0
        val nearLimit = remaining <= warnLimit
        val hue = (if (exceededLimit) if (nearLimit) 5 * remaining else 50 else 0).toFloat()
        val textColorHsv = FloatArray(3)
        Color.colorToHSV(defaultTextColor, textColorHsv)
        val errorColorHsv = FloatArray(3)
        errorColorHsv[0] = hue
        errorColorHsv[1] = 1f
        errorColorHsv[2] = 0.75f + textColorHsv[2] / 4
        if (remaining <= warnLimit) {
            setTextColor(Color.HSVToColor(errorColorHsv))
        } else {
            setTextColor(defaultTextColor)
        }
    }

}
