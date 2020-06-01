package org.mariotaku.twidere.util.refresh

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.refreshIntervalKey
import org.mariotaku.twidere.service.JobTaskService
import org.mariotaku.twidere.service.JobTaskService.Companion.JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS
import org.mariotaku.twidere.service.JobTaskService.Companion.JOB_ID_REFRESH_LAUNCH_PRESENTATIONS
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2016/12/17.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {
    private val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    override fun appStarted() {
        val allJobs = scheduler.allPendingJobs
        AutoRefreshType.ALL.forEach { type ->
            val jobId = JobTaskService.getRefreshJobId(type)
            if (allJobs.none { job -> job.id == jobId }) {
                // Start non existing job
                schedule(type)
            }
        }
        if (allJobs.none { job -> job.id == JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS }) {
            scheduleJob(JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS, TimeUnit.HOURS.toMillis(4))
        }
        if (allJobs.none { job -> job.id == JOB_ID_REFRESH_LAUNCH_PRESENTATIONS }) {
            scheduleJob(JOB_ID_REFRESH_LAUNCH_PRESENTATIONS, TimeUnit.HOURS.toMillis(6))
        }
    }

    override fun schedule(@AutoRefreshType type: String) {
        val jobId = JobTaskService.getRefreshJobId(type)
        scheduler.cancel(jobId)
        scheduleJob(jobId)
    }

    override fun unschedule(type: String) {
        val jobId = JobTaskService.getRefreshJobId(type)
        scheduler.cancel(jobId)
    }

    fun scheduleJob(jobId: Int, periodMillis: Long = TimeUnit.MINUTES.toMillis(kPreferences[refreshIntervalKey]), persisted: Boolean = true) {
        val builder = JobInfo.Builder(jobId, ComponentName(context, JobTaskService::class.java))
        builder.setPeriodic(periodMillis)
        builder.setPersisted(persisted)
        try {
            scheduler.schedule(builder.build())
        } catch (e: IllegalArgumentException) {
            if (persisted) {
                scheduleJob(jobId, periodMillis, false)
            }
        }
    }


}
