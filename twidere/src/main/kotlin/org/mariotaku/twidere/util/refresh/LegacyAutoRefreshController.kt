package org.mariotaku.twidere.util.refresh

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.collection.ArrayMap
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.refreshIntervalKey
import org.mariotaku.twidere.service.JobTaskService.Companion.JOB_IDS_REFRESH
import org.mariotaku.twidere.service.LegacyTaskService
import org.mariotaku.twidere.util.TaskServiceRunner.Companion.ACTION_REFRESH_FILTERS_SUBSCRIPTIONS
import org.mariotaku.twidere.util.TaskServiceRunner.Companion.ACTION_REFRESH_LAUNCH_PRESENTATIONS
import java.util.concurrent.TimeUnit

class LegacyAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {

    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntents: ArrayMap<String, PendingIntent> = ArrayMap()

    init {
        AutoRefreshType.ALL.forEach { type ->
            val action = LegacyTaskService.getRefreshAction(type) ?: return@forEach
            val intent = Intent(context, LegacyTaskService::class.java)
            intent.action = action
            pendingIntents[type] = PendingIntent.getService(context, 0, intent, 0)
        }
    }

    override fun appStarted() {
        rescheduleAll()
        rescheduleFiltersSubscriptionsRefresh()
        rescheduleLaunchPresentationsRefresh()
    }

    override fun rescheduleAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeAllJobs(context, JOB_IDS_REFRESH)
        }
        super.rescheduleAll()
    }

    override fun unschedule(type: String) {
        val pendingIntent = pendingIntents[type] ?: return
        alarmManager.cancel(pendingIntent)
    }

    override fun schedule(type: String) {
        val pendingIntent = pendingIntents[type] ?: return
        val interval = TimeUnit.MINUTES.toMillis(kPreferences[refreshIntervalKey])
        if (interval > 0) {
            val triggerAt = SystemClock.elapsedRealtime() + interval
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval, pendingIntent)
        }
    }

    private fun rescheduleFiltersSubscriptionsRefresh() {
        val interval = TimeUnit.HOURS.toMillis(4)
        val triggerAt = SystemClock.elapsedRealtime() + interval
        val intent = Intent(context, LegacyTaskService::class.java)
        intent.action = ACTION_REFRESH_FILTERS_SUBSCRIPTIONS
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval,
                PendingIntent.getService(context, 0, intent, 0))
    }

    private fun rescheduleLaunchPresentationsRefresh() {
        val interval = TimeUnit.HOURS.toMillis(6)
        val triggerAt = SystemClock.elapsedRealtime() + interval
        val intent = Intent(context, LegacyTaskService::class.java)
        intent.action = ACTION_REFRESH_LAUNCH_PRESENTATIONS
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval,
                PendingIntent.getService(context, 0, intent, 0))
    }

    companion object {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun removeAllJobs(context: Context, jobIds: IntArray) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
            val jobService = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobIds.forEach { id ->
                jobService.cancel(id)
            }
        }
    }

}