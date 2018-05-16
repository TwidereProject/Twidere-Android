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

package org.mariotaku.twidere.extension

import java.util.*
import java.util.concurrent.TimeUnit

private val epochCalendar = Calendar.getInstance().timeInMillis(0)
val epochWeekDay: Int = epochCalendar.get(Calendar.DAY_OF_WEEK) - epochCalendar.getMinimum(Calendar.DAY_OF_WEEK)
val epochJulianDay: Int = epochCalendar.julianDay()

fun Calendar.isSameDay(that: Calendar): Boolean {
    return this[Calendar.ERA] == that[Calendar.ERA] &&
            this[Calendar.YEAR] == that[Calendar.YEAR] &&
            this[Calendar.DAY_OF_YEAR] == that[Calendar.DAY_OF_YEAR]
}

fun Calendar.timeInMillis(time: Long): Calendar = apply { timeInMillis = time }
fun Calendar.time(time: Date): Calendar = apply { this.time = time }

fun Calendar.daysSinceEpoch(day: Int): Calendar = apply {
    timeInMillis = 0
    add(Calendar.DATE, day)
}

fun Calendar.clearClockTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    clear(Calendar.MINUTE)
    clear(Calendar.SECOND)
    clear(Calendar.MILLISECOND)
}

fun Calendar.dayProgress(): Float {
    val offsetHour = (get(Calendar.HOUR_OF_DAY) - 6 + 24) % 24
    val dayMillis = TimeUnit.HOURS.toMillis(offsetHour.toLong()) +
            TimeUnit.MINUTES.toMillis(get(Calendar.MINUTE).toLong()) +
            TimeUnit.SECONDS.toMillis(get(Calendar.SECOND).toLong()) +
            get(Calendar.MILLISECOND)
    return dayMillis / TimeUnit.HOURS.toMillis(24).toFloat()
}

// http://en.wikipedia.org/wiki/Julian_day
fun Calendar.julianDay(): Int {
    val year = get(Calendar.YEAR)
    val month = get(Calendar.MONTH) + 1
    val day = get(Calendar.DATE)

    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
}

fun Calendar.daysSinceEpoch(): Int = julianDay() - epochJulianDay

fun Calendar.weeksSinceEpoch(): Int = (daysSinceEpoch() + epochWeekDay) / 7