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

package org.mariotaku.twidere.model.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.annotation.ThemeBackgroundOption
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.constant.themeBackgroundOptionKey
import org.mariotaku.twidere.util.ThemeUtils

class UserTheme : Chameleon.Theme() {
    var backgroundAlpha: Int = 0xFF

    companion object {
        fun get(context: Context, preferences: SharedPreferences): UserTheme {
            val theme = UserTheme()
            theme.init(context)
            val userColor = ThemeUtils.getUserAccentColor(context, preferences)
            theme.colorAccent = userColor
            theme.colorPrimary = userColor
            val backgroundOption = preferences[themeBackgroundOptionKey]
            if (theme.isToolbarColored) {
                theme.colorToolbar = theme.colorPrimary
            } else if (backgroundOption == ThemeBackgroundOption.SOLID) {
                theme.colorToolbar = if (ThemeUtils.isLightTheme(context)) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            }

            if (ThemeBackgroundOption.TRANSPARENT == backgroundOption) {
                theme.backgroundAlpha = (preferences[themeBackgroundAlphaKey] * 1.25f).toInt()
                        .coerceIn(ThemeBackgroundOption.MIN_ALPHA, ThemeBackgroundOption.MAX_ALPHA)
                theme.colorToolbar = ColorUtils.setAlphaComponent(theme.colorToolbar,
                        theme.backgroundAlpha)
            }
            theme.statusBarColor = ChameleonUtils.darkenColor(theme.colorToolbar)
            theme.lightStatusBarMode = Chameleon.Theme.LightStatusBarMode.AUTO
            theme.textColorLink = ThemeUtils.getOptimalAccentColor(theme.colorAccent, theme.colorForeground)
            return theme
        }
    }
}
