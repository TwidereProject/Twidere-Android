package org.mariotaku.twidere.taskcontroller.refresh

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.taskcontroller.TaskController


abstract class RefreshTaskController(
        val context: Context,
        val preferences: SharedPreferences
) : TaskController {

    abstract fun schedule(@AutoRefreshType type: String)

    abstract fun unschedule(@AutoRefreshType type: String)

    open fun reschedule(@AutoRefreshType type: String) {
        unschedule(type)
        schedule(type)
    }

    open fun rescheduleAll() {
        AutoRefreshType.ALL.forEach { reschedule(it) }
    }

}
