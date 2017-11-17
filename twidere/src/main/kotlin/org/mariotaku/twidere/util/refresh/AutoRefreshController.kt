package org.mariotaku.twidere.util.refresh

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.annotation.AutoRefreshType


abstract class AutoRefreshController(
        val context: Context,
        val preferences: SharedPreferences
) {

    abstract fun appStarted()

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
