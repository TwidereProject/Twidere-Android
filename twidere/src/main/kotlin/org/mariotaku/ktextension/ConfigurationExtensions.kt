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

package org.mariotaku.ktextension

import android.annotation.TargetApi
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.*

var Configuration.localesCompat: LocaleListCompat
    get() = ConfigurationCompat.getLocales(this)
    set(value) {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                @Suppress("DEPRECATION")
                this.locale = value[0]
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> {
                ConfigurationExtensionsApi18.setLocaleCompat(this, value[0])
            }
            else -> {
                ConfigurationExtensionsApi24.setLocalesCompat(this, value)
            }
        }
    }

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
private object ConfigurationExtensionsApi18 {

    fun setLocaleCompat(configuration: Configuration, locale: Locale?) {
        configuration.setLocale(locale)
    }
}

@TargetApi(Build.VERSION_CODES.N)
private object ConfigurationExtensionsApi24 {

    fun setLocalesCompat(configuration: Configuration, locales: LocaleListCompat) {
        configuration.setLocales(locales.unwrap() as? LocaleList)
    }
}