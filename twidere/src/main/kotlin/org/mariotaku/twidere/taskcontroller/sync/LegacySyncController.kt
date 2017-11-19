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

package org.mariotaku.twidere.taskcontroller.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.support.v4.util.ArrayMap
import org.mariotaku.twidere.service.JobTaskService
import org.mariotaku.twidere.service.LegacyTaskService
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.taskcontroller.refresh.LegacyRefreshTaskController
import org.mariotaku.twidere.util.sync.DataSyncProvider
import java.util.concurrent.TimeUnit

class LegacySyncController(context: Context, provider: DataSyncProvider) : SyncTaskController(context, provider) {

    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntents: ArrayMap<String, PendingIntent> = ArrayMap()

    init {
        TaskServiceRunner.ACTIONS_SYNC.forEach { action ->
            val intent = Intent(context, LegacyTaskService::class.java)
            intent.action = action
            pendingIntents[action] = PendingIntent.getService(context, 0, intent, 0)
        }
    }

    override fun appStarted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LegacyRefreshTaskController.removeAllJobs(context, JobTaskService.JOB_IDS_REFRESH)
        }
        for ((_, pendingIntent) in pendingIntents) {
            alarmManager.cancel(pendingIntent)
            val interval = TimeUnit.HOURS.toMillis(4)
            val triggerAt = SystemClock.elapsedRealtime() + interval
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval, pendingIntent)
        }
    }

}
