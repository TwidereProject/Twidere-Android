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

package org.mariotaku.twidere.extension.model

import android.content.Context
import androidx.core.os.LocaleListCompat
import org.mariotaku.ktextension.localesCompat
import org.mariotaku.twidere.model.presentation.LaunchPresentation
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

fun LaunchPresentation.shouldShow(context: Context): Boolean {
    // Check language
    val userLocales = context.resources.configuration.localesCompat
    if (locales != null && locales.none { it.matchesAny(userLocales) }) {
        return false
    }
    // Check date/time
    val date = Date()
    if (schedule != null && !schedule.matches(date)) {
        return false
    }
    return true
}

fun LaunchPresentation.Schedule.matches(date: Date): Boolean {
    val cal = Calendar.getInstance()
    cal.time = date
    return cron.matches(cal)
}


fun LaunchPresentation.Locale.matches(locale: Locale): Boolean {
    if (language != locale.language) return false
    if (country == null) {
        return locale.country.isNullOrEmpty()
    }
    return country == locale.country
}

fun LaunchPresentation.Locale.matchesAny(locales: LocaleListCompat): Boolean {
    return (0 until locales.size()).any { matches(locales[it]) }
}

fun LaunchPresentation.Image.displayingScore(viewDensity: Float, viewWidth: Int,
        viewHeight: Int): Int {
    if (viewWidth == 0 || viewHeight == 0) return 0
    var score = 0
    // Compute size scores
    score += when {
        viewWidth == width && viewHeight <= height -> 100
        viewHeight == height && viewWidth <= width -> 100
        viewWidth < width && viewHeight < height -> {
            val diffW = (width / viewWidth.toFloat() - 1).coerceAtMost(0.5f)
            val diffH = (height / viewHeight.toFloat() - 1).coerceAtMost(0.5f)
            100 - (diffH * 100).roundToInt() - (diffW * 100).roundToInt()
        }
        else -> {
            val diffW = (width / viewWidth.toFloat() - 1).coerceAtMost(0.5f)
            val diffH = (height / viewHeight.toFloat() - 1).coerceAtMost(0.5f)
            100 - (diffH * 50).roundToInt() - (diffW * 50).roundToInt()
        }
    }
    if (this.density != 0f) {
        score += 100 - abs(this.density / viewDensity - 1).coerceAtMost(1f).roundToInt()
    }
    return score
}

