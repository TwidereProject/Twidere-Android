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

package org.mariotaku.twidere.graphic.drawable.userprofile

import android.annotation.TargetApi
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import org.mariotaku.twidere.graphic.ActionBarColorDrawable

internal class ActionBarDrawable(shadow: Drawable) : LayerDrawable(arrayOf(shadow, ActionBarColorDrawable.create(true))) {

    private val shadowDrawable = getDrawable(0)
    private val colorDrawable = getDrawable(1) as ColorDrawable
    private var alphaValue: Int = 0

    var factor: Float = 0f
        set(value) {
            field = value
            updateValue()
        }

    var color: Int = 0
        set(value) {
            field = value
            colorDrawable.color = value
            updateValue()
        }

    var outlineAlphaFactor: Float = 0f
        set(value) {
            field = value
            updateValue()
        }

    init {
        alpha = 0xFF
        updateValue()
    }

    override fun setAlpha(alpha: Int) {
        alphaValue = alpha
        updateValue()
    }

    override fun getAlpha(): Int {
        return alphaValue
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getOutline(outline: Outline) {
        colorDrawable.getOutline(outline)
        outline.alpha = factor * outlineAlphaFactor * 0.99f
    }

    override fun getIntrinsicWidth(): Int {
        return colorDrawable.intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return colorDrawable.intrinsicHeight
    }

    private fun updateValue() {
        val shadowAlpha = Math.round(alpha * (1 - factor).coerceIn(0f, 1f))
        shadowDrawable.alpha = shadowAlpha
        val hasColor = color != 0
        val colorAlpha = if (hasColor) Math.round(alpha * factor.coerceIn(0f, 1f)) else 0
        colorDrawable.alpha = colorAlpha
        invalidateSelf()
    }

}