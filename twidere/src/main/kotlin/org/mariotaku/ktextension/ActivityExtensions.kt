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

import android.app.Activity
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat


val Activity.activityIcon: Drawable?
    get() {
        val info = activityInfo
        val activityLabelRes = info.icon
        if (activityLabelRes != 0) return ContextCompat.getDrawable(this, activityLabelRes)
        val appLabelRes = applicationInfo.icon
        if (appLabelRes != 0) return ContextCompat.getDrawable(this, appLabelRes)
        return info.loadIcon(packageManager)
    }

val Activity.activityLabel: CharSequence?
    get() {
        val info = activityInfo
        val activityLabelRes = info.labelRes
        if (activityLabelRes != 0) return getText(activityLabelRes)
        val appLabelRes = applicationInfo.labelRes
        if (appLabelRes != 0) return getText(appLabelRes)
        return info.loadLabel(packageManager)
    }

val Activity.activityInfo: ActivityInfo
    get() = packageManager.getActivityInfo(ComponentName(this, javaClass), 0)