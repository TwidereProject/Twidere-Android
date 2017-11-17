/*
 * Twidere - Twitter client for Android
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

import android.annotation.TargetApi
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.SharedPreferences
import android.os.Build
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.deadline
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.support.JobServiceSupport
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobTaskService : JobService() {

    @Inject
    internal lateinit var taskServiceRunner: TaskServiceRunner
    @Inject
    internal lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        GeneralComponent.get(this).inject(this)
    }

    override fun onStartJob(params: JobParameters): Boolean {
        if (preferences[autoRefreshCompatibilityModeKey]) return false
        val action = getTaskAction(params.jobId) ?: return false
        val promise = taskServiceRunner.createPromise(action) ?: return false
        promise.deadline(3, TimeUnit.MINUTES).successUi {
            jobFinished(params, false)
        }.failUi {
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        try {
            if (JobServiceSupport.handleStopJob(params, false)) {
                JobServiceSupport.removeCallback(params)
            }
        } catch (e: Exception) {
            // Swallow any possible exceptions
            Analyzer.logException(e)
        }
        return false
    }

    companion object {
        // DON'T CHANGE JOB ID ONCE CREATED!
        const val JOB_ID_REFRESH_HOME_TIMELINE = 1
        const val JOB_ID_REFRESH_NOTIFICATIONS = 2
        const val JOB_ID_REFRESH_DIRECT_MESSAGES = 3
        const val JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS = 19
        const val JOB_ID_REFRESH_LAUNCH_PRESENTATIONS = 18
        const val JOB_ID_SYNC_DRAFTS = 21
        const val JOB_ID_SYNC_FILTERS = 22
        const val JOB_ID_SYNC_USER_NICKNAMES = 23
        const val JOB_ID_SYNC_USER_COLORS = 24

        val JOB_IDS_REFRESH = intArrayOf(JOB_ID_REFRESH_HOME_TIMELINE, JOB_ID_REFRESH_NOTIFICATIONS,
                JOB_ID_REFRESH_DIRECT_MESSAGES)
        val JOB_IDS_SYNC = intArrayOf(JOB_ID_SYNC_DRAFTS, JOB_ID_SYNC_FILTERS,
                JOB_ID_SYNC_USER_NICKNAMES, JOB_ID_SYNC_USER_COLORS)

        fun getRefreshJobId(@AutoRefreshType type: String): Int = when (type) {
            AutoRefreshType.HOME_TIMELINE -> JOB_ID_REFRESH_HOME_TIMELINE
            AutoRefreshType.INTERACTIONS_TIMELINE -> JOB_ID_REFRESH_NOTIFICATIONS
            AutoRefreshType.DIRECT_MESSAGES -> JOB_ID_REFRESH_DIRECT_MESSAGES
            else -> 0
        }

        @TaskServiceRunner.Action
        fun getTaskAction(jobId: Int): String? = when (jobId) {
            JOB_ID_REFRESH_HOME_TIMELINE -> TaskServiceRunner.ACTION_REFRESH_HOME_TIMELINE
            JOB_ID_REFRESH_NOTIFICATIONS -> TaskServiceRunner.ACTION_REFRESH_NOTIFICATIONS
            JOB_ID_REFRESH_DIRECT_MESSAGES -> TaskServiceRunner.ACTION_REFRESH_DIRECT_MESSAGES
            JOB_ID_REFRESH_FILTERS_SUBSCRIPTIONS -> TaskServiceRunner.ACTION_REFRESH_FILTERS_SUBSCRIPTIONS
            JOB_ID_REFRESH_LAUNCH_PRESENTATIONS -> TaskServiceRunner.ACTION_REFRESH_LAUNCH_PRESENTATIONS
            JOB_ID_SYNC_DRAFTS -> TaskServiceRunner.ACTION_SYNC_DRAFTS
            JOB_ID_SYNC_FILTERS -> TaskServiceRunner.ACTION_SYNC_FILTERS
            JOB_ID_SYNC_USER_NICKNAMES -> TaskServiceRunner.ACTION_SYNC_USER_NICKNAMES
            JOB_ID_SYNC_USER_COLORS -> TaskServiceRunner.ACTION_SYNC_USER_COLORS
            else -> null
        }

    }

}
