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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.util.Log
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.stopAutoRefreshWhenBatteryLowKey
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

/**
 * Created by mariotaku on 14/12/12.
 */
@SuppressLint("Registered")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class JobTaskService : JobService() {

    @Inject
    internal lateinit var preferences: KPreferences

    override fun onCreate() {
        super.onCreate()
        GeneralComponentHelper.build(this).inject(this)
    }

    override fun onStartJob(params: JobParameters): Boolean {
        if (!Utils.isBatteryOkay(this) && preferences[stopAutoRefreshWhenBatteryLowKey]) {
            // Low battery, don't refresh
            return false
        }
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Running job ${params.jobId}")
        }

        val task = run {
            val type = getRefreshType(params.jobId) ?: return@run null
            return@run LegacyTaskService.createJobTask(this, type)
        } ?: return false
        task.callback = {
            this.jobFinished(params, false)
        }
        TaskStarter.execute(task)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    companion object {
        const val JOB_ID_REFRESH_HOME_TIMELINE = 1
        const val JOB_ID_REFRESH_NOTIFICATIONS = 2
        const val JOB_ID_REFRESH_DIRECT_MESSAGES = 3

        fun getJobId(@AutoRefreshType type: String): Int = when (type) {
            AutoRefreshType.HOME_TIMELINE -> JOB_ID_REFRESH_HOME_TIMELINE
            AutoRefreshType.INTERACTIONS_TIMELINE -> JOB_ID_REFRESH_NOTIFICATIONS
            AutoRefreshType.DIRECT_MESSAGES -> JOB_ID_REFRESH_DIRECT_MESSAGES
            else -> 0
        }

        @LegacyTaskService.Action
        fun getRefreshType(jobId: Int): String? = when (jobId) {
            JOB_ID_REFRESH_HOME_TIMELINE -> LegacyTaskService.ACTION_REFRESH_HOME_TIMELINE
            JOB_ID_REFRESH_NOTIFICATIONS -> LegacyTaskService.ACTION_REFRESH_NOTIFICATIONS
            JOB_ID_REFRESH_DIRECT_MESSAGES -> LegacyTaskService.ACTION_REFRESH_DIRECT_MESSAGES
            else -> null
        }
    }
}
