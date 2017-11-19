/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.deadline
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.util.TaskServiceRunner.Companion.ACTION_REFRESH_DIRECT_MESSAGES
import org.mariotaku.twidere.util.TaskServiceRunner.Companion.ACTION_REFRESH_HOME_TIMELINE
import org.mariotaku.twidere.util.TaskServiceRunner.Companion.ACTION_REFRESH_NOTIFICATIONS
import org.mariotaku.twidere.dagger.component.GeneralComponent
import java.util.concurrent.TimeUnit

class LegacyTaskService : BaseService() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOGTAG, "LegacyTaskService created")
        GeneralComponent.get(this).inject(this)
    }

    override fun onDestroy() {
        Log.d(LOGTAG, "LegacyTaskService destroyed")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOGTAG, "LegacyTaskService received $intent")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                !preferences[autoRefreshCompatibilityModeKey]) return serviceNotHandled(startId)
        val action = intent?.action ?: return serviceNotHandled(startId)
        val promise = taskServiceRunner.createPromise(action) ?: return serviceNotHandled(startId)
        promise.deadline(3, TimeUnit.MINUTES).successUi {
            stopSelfResult(startId)
        }.failUi {
            stopSelfResult(startId)
        }
        return START_NOT_STICKY
    }

    private fun serviceNotHandled(startId: Int): Int {
        stopSelfResult(startId)
        return START_NOT_STICKY
    }


    companion object {

        fun getRefreshAction(@AutoRefreshType type: String): String? = when (type) {
            AutoRefreshType.HOME_TIMELINE -> ACTION_REFRESH_HOME_TIMELINE
            AutoRefreshType.INTERACTIONS_TIMELINE -> ACTION_REFRESH_NOTIFICATIONS
            AutoRefreshType.DIRECT_MESSAGES -> ACTION_REFRESH_DIRECT_MESSAGES
            else -> null
        }

    }
}
