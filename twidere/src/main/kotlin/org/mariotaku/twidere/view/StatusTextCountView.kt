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
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import org.mariotaku.twidere.util.Utils.getLocalizedNumber
import java.util.*

class StatusTextCountView(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context,
        attrs, android.R.attr.textViewStyle) {

    @ColorInt
    private val defaultTextColor = currentTextColor

    var textCount: Int = 0
        set(count) {
            field = count
            updateTextCount()
        }
    var maxLength: Int = 0
        set(maxLength) {
            field = maxLength
            updateTextCount()
        }

    fun updateTextCount() {
        if (this.maxLength <= 0) {
            text = null
            return
        }
        val count = this.textCount
        val maxLength = this.maxLength
        text = getLocalizedNumber(Locale.getDefault(), maxLength - count)
        val exceededLimit = count < maxLength
        val nearLimit = count >= maxLength - 10
        val hue = (if (exceededLimit) if (nearLimit) 5 * (maxLength - count) else 50 else 0).toFloat()
        val textColorHsv = FloatArray(3)
        Color.colorToHSV(defaultTextColor, textColorHsv)
        val errorColorHsv = FloatArray(3)
        errorColorHsv[0] = hue
        errorColorHsv[1] = 1f
        errorColorHsv[2] = 0.75f + textColorHsv[2] / 4
        if (count >= maxLength - 10) {
            setTextColor(Color.HSVToColor(errorColorHsv))
        } else {
            setTextColor(defaultTextColor)
        }
    }

}
