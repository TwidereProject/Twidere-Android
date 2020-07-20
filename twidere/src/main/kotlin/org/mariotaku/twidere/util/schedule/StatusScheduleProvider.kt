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

package org.mariotaku.twidere.util.schedule

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import org.mariotaku.twidere.model.ParcelableStatusUpdate
import org.mariotaku.twidere.model.schedule.ScheduleInfo
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.task.twitter.UpdateStatusTask.PendingStatusUpdate
import java.util.*

/**
 * Created by mariotaku on 2017/3/24.
 */

interface StatusScheduleProvider {

    @WorkerThread
    @Throws(ScheduleException::class)
    fun scheduleStatus(statusUpdate: ParcelableStatusUpdate, pendingUpdate: PendingStatusUpdate,
            scheduleInfo: ScheduleInfo)

    fun createSetScheduleIntent(): Intent

    fun createSettingsIntent(): Intent?

    fun createManageIntent(): Intent?

    class ScheduleException : UpdateStatusTask.UpdateStatusException {

        constructor() : super()

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String) : super(message)
    }

    interface Factory {
        fun newInstance(context: Context): StatusScheduleProvider?

        fun parseInfo(json: String): ScheduleInfo?

    }

    private object NullFactory : Factory {
        override fun newInstance(context: Context): Nothing? = null

        override fun parseInfo(json: String): ScheduleInfo? = null

    }

    companion object {
        fun newFactory(): Factory = ServiceLoader.load(Factory::class.java)?.firstOrNull() ?: NullFactory
    }
}
