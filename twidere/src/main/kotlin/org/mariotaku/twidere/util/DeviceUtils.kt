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

package org.mariotaku.twidere.util

import android.content.Context
import android.os.Build
import androidx.appcompat.view.menu.MenuBuilder
import android.util.DisplayMetrics
import android.view.Menu
import android.view.WindowManager
import kotlin.math.min

/**
 * Created by mariotaku on 2017/4/17.
 */

object DeviceUtils {

    fun checkCompatibility(): Boolean {
        try {
            Menu::class.java.isAssignableFrom(MenuBuilder::class.java)
        } catch (e: Error) {
            Analyzer.logException(e)
            return false
        }

        return true
    }

    /**
     * Detect whether screen minimum width is not smaller than 600dp, regardless split screen mode
     */
    fun isDeviceTablet(context: Context): Boolean {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = wm.defaultDisplay
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultDisplay.getMetrics(metrics)
        } else {
            defaultDisplay.getRealMetrics(metrics)
        }
        val mw = min(metrics.widthPixels / metrics.density, metrics.heightPixels / metrics.density)
        return mw >= 600
    }

    /*
     * May return false on tablets when using split window
     */
    fun isScreenTablet(context: Context): Boolean {
        val metrics = context.resources.displayMetrics
        return metrics.widthPixels / metrics.density >= 600
    }
}
