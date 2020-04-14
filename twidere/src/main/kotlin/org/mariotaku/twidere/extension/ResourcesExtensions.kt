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

import android.content.res.Resources
import androidx.core.os.LocaleListCompat
import org.mariotaku.ktextension.localesCompat
import java.util.*

fun Resources.setLocale(locale: Locale) {
    Locale.setDefault(locale)
    val config = configuration
    config.localesCompat = LocaleListCompat.create(locale)
    @Suppress("DEPRECATION")
    updateConfiguration(config, displayMetrics)
}

val Resources.firstLanguage: Locale?
    get() = configuration.localesCompat.takeIf { !it.isEmpty }?.get(0)