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
import java.util.*

/**
 * Created by mariotaku on 2017/3/23.
 */

fun Configuration.setLayoutDirectionCompat(locale: Locale?) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return
    ConfigurationExtensionsApi18.setLayoutDirectionCompat(this, locale)
}

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
private object ConfigurationExtensionsApi18 {

    fun setLayoutDirectionCompat(configuration: Configuration, locale: Locale?) {
        configuration.setLayoutDirection(locale)
    }
}