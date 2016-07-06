/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.app.Application
import android.os.Build
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants

/**
 * Created by mariotaku on 15/7/8.
 */
class TwidereBugReporter : BugReporter(), Constants {

    override fun logImpl(priority: Int, tag: String, msg: String) {
        Crashlytics.log(priority, tag, msg)
    }

    override fun logExceptionImpl(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    override fun initImpl(application: Application) {
        Fabric.with(application, Crashlytics())
        Crashlytics.setBool("debug", BuildConfig.DEBUG)
        Crashlytics.setString("build.brand", Build.BRAND)
        Crashlytics.setString("build.device", Build.DEVICE)
        Crashlytics.setString("build.display", Build.DISPLAY)
        Crashlytics.setString("build.hardware", Build.HARDWARE)
        Crashlytics.setString("build.manufacturer", Build.MANUFACTURER)
        Crashlytics.setString("build.model", Build.MODEL)
        Crashlytics.setString("build.product", Build.PRODUCT)
    }

}
