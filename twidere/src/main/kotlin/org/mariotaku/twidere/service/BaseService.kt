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

package org.mariotaku.twidere.service

import android.app.Service
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.twitter.Extractor
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.util.ActivityTracker
import org.mariotaku.twidere.util.NotificationManagerWrapper
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.notification.ContentNotificationManager
import javax.inject.Inject

abstract class BaseService : Service() {

    @Inject
    @Deprecated(message = "Deprecated", replaceWith = ReplaceWith("PreferencesSingleton.get(this)", imports = ["org.mariotaku.twidere.singleton.PreferencesSingleton"]))
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var extractor: Extractor
    @Inject
    @Deprecated(message = "Deprecated", replaceWith = ReplaceWith("UserColorNameManager.get(this)", imports = ["org.mariotaku.twidere.util.UserColorNameManager"]))
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var taskServiceRunner: TaskServiceRunner
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    @Inject
    lateinit var activityTracker: ActivityTracker
    @Inject
    lateinit var contentNotificationManager: ContentNotificationManager

    override fun onCreate() {
        super.onCreate()
        GeneralComponent.get(this).inject(this)
    }
}