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

package org.mariotaku.twidere.taskcontroller.refresh

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.refreshIntervalKey
import org.mariotaku.twidere.service.JobTaskService
import org.mariotaku.twidere.service.JobTaskService.Companion.JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS
import java.util.concurrent.TimeUnit

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerRefreshTaskController(
        context: Context,
        preferences: SharedPreferences
) : RefreshTaskController(context, preferences) {
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

    fun scheduleJob(jobId: Int, periodMillis: Long = TimeUnit.MINUTES.toMillis(preferences[refreshIntervalKey]), persisted: Boolean = true) {
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
