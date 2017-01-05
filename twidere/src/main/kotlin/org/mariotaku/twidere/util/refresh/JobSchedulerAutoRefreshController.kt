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
import java.util.concurrent.TimeUnit
import android.Manifest.permission as AndroidPermissions

/**
 * Created by mariotaku on 2016/12/17.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {
    val scheduler: JobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    override fun appStarted() {
        val allJobs = scheduler.allPendingJobs
        AutoRefreshType.ALL.forEach { type ->
            val jobId = JobTaskService.getJobId(type)
            if (allJobs.none { job -> job.id == jobId }) {
                // Start non existing job
                schedule(type)
            }
        }

    }

    override fun schedule(@AutoRefreshType type: String) {
        val jobId = JobTaskService.getJobId(type)
        scheduler.cancel(jobId)
        scheduleJob(jobId)
    }

    override fun unschedule(type: String) {
        val jobId = JobTaskService.getJobId(type)
        scheduler.cancel(jobId)
    }

    fun scheduleJob(jobId: Int, persisted: Boolean = true) {
        val builder = JobInfo.Builder(jobId, ComponentName(context, JobTaskService::class.java))
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        builder.setPeriodic(TimeUnit.MINUTES.toMillis(kPreferences[refreshIntervalKey]))
        builder.setPersisted(persisted)
        try {
            scheduler.schedule(builder.build())
        } catch (e: IllegalArgumentException) {
            if (persisted) {
                scheduleJob(jobId, false)
            }
        }
    }


}
