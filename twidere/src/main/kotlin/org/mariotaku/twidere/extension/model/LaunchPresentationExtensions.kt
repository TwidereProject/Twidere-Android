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

import org.mariotaku.twidere.model.presentation.LaunchPresentation
import java.util.*

/**
 * Created by mariotaku on 2017/8/20.
 */

fun LaunchPresentation.shouldShow(): Boolean {
    // Check language
    val locale = Locale.getDefault()
    if (locales != null && locales.none { it.matches(locale) }) {
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