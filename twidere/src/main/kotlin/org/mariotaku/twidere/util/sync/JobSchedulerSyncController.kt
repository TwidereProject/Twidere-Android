package org.mariotaku.twidere.util.sync

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import org.mariotaku.twidere.service.JobTaskService
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2017/1/6.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerSyncController(context: Context) : SyncController(context) {
    val scheduler: JobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    override fun appStarted() {
        val allJobs = scheduler.allPendingJobs
        JobTaskService.JOB_IDS_SYNC.forEach { jobId ->
            if (allJobs.none { job -> job.id == jobId }) {
                // Start non existing job
                scheduleJob(jobId, true)
            }
        }
    }

    fun scheduleJob(jobId: Int, persisted: Boolean = true) {
        val builder = JobInfo.Builder(jobId, ComponentName(context, JobTaskService::class.java))
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        builder.setPeriodic(TimeUnit.HOURS.toMillis(4))
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
