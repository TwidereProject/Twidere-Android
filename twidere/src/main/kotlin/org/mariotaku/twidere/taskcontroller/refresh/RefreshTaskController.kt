package org.mariotaku.twidere.taskcontroller.refresh

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.taskcontroller.TaskController
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import org.mariotaku.twidere.util.preference.PreferenceChangeNotifier


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

    companion object : ApplicationContextSingletonHolder<RefreshTaskController>(creator@{
        val preferences = PreferencesSingleton.get(it)
        val controller = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !preferences[autoRefreshCompatibilityModeKey]) {
            JobSchedulerRefreshTaskController(it, preferences)
        } else {
            LegacyRefreshTaskController(it, preferences)
        }
        PreferenceChangeNotifier.get(it).register(Constants.KEY_REFRESH_INTERVAL) {
            controller.rescheduleAll()
        }
        return@creator controller
    })

}
