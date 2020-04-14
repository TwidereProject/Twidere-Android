package org.mariotaku.twidere.util.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.collection.ArrayMap
import org.mariotaku.twidere.service.JobTaskService
import org.mariotaku.twidere.service.LegacyTaskService
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.refresh.LegacyAutoRefreshController
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2017/1/6.
 */

class LegacySyncController(context: Context) : SyncController(context) {

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
            LegacyAutoRefreshController.removeAllJobs(context, JobTaskService.JOB_IDS_REFRESH)
        }
        for ((_, pendingIntent) in pendingIntents) {
            alarmManager.cancel(pendingIntent)
            val interval = TimeUnit.HOURS.toMillis(4)
            val triggerAt = SystemClock.elapsedRealtime() + interval
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval, pendingIntent)
        }
    }

}
