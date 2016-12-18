package org.mariotaku.twidere.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.support.v4.util.ArrayMap
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.refreshIntervalKey
import org.mariotaku.twidere.service.RefreshService
import java.util.concurrent.TimeUnit

class LegacyAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {

    private val alarmManager: AlarmManager

    private val pendingIntents: ArrayMap<String, PendingIntent>

    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        pendingIntents = ArrayMap()

        AutoRefreshType.ALL.forEach { type ->
            val action = RefreshService.getRefreshAction(type) ?: return@forEach
            val intent = Intent(context, RefreshService::class.java)
            intent.action = action
            pendingIntents[type] = PendingIntent.getService(context, 0, intent, 0)
        }
    }

    override fun appStarted() {
        rescheduleAll()
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

}