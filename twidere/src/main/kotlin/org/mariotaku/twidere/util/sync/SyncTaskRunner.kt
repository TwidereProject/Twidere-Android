package org.mariotaku.twidere.util.sync

import android.content.Context
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponent
import java.lang.Exception
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 2017/1/3.
 */

abstract class SyncTaskRunner(val context: Context) {
    @Inject
    protected lateinit var userColorNameManager: UserColorNameManager
    @Inject
    protected lateinit var bus: Bus
    @Inject
    protected lateinit var syncPreferences: SyncPreferences

    init {
        @Suppress("LeakingThis")
        GeneralComponent.get(context).inject(this)
    }

    /**
     * @param action Action of `TaskServiceRunner.Action`
     * @param callback When task finished, true if task finished successfully
     * @return True if task actually executed, false otherwise
     */
    protected abstract fun onRunningTask(action: String, callback: ((Boolean) -> Unit)): Boolean

    fun runTask(action: String, callback: ((Boolean) -> Unit)? = null): Boolean {
        val syncType = getSyncType(action) ?: return false
        if (!syncPreferences.isSyncEnabled(syncType)) return false
        return onRunningTask(action) { success ->
            callback?.invoke(success)
            if (success) {
                syncPreferences.setLastSynced(syncType, System.currentTimeMillis())
            }
            bus.post(TaskServiceRunner.SyncFinishedEvent(syncType, success))
        }
    }


    fun cleanupSyncCache(): Promise<Boolean, Exception> {
        return task {
            context.syncDataDir.deleteRecursively()
        }
    }

    fun performSync() {
        val actions = TaskServiceRunner.ACTIONS_SYNC.toCollection(LinkedList())
        val runnable = object : Runnable {
            override fun run() {
                val action = actions.poll() ?: return
                runTask(action) {
                    this.run()
                }
            }
        }
        runnable.run()
    }

    companion object {
        const val SYNC_TYPE_DRAFTS = "drafts"
        const val SYNC_TYPE_FILTERS = "filters"
        const val SYNC_TYPE_USER_COLORS = "user_colors"
        const val SYNC_TYPE_USER_NICKNAMES = "user_nicknames"
        const val SYNC_TYPE_TIMELINE_POSITIONS = "timeline_positions"

        @JvmStatic
        fun getSyncType(action: String): String? {
            return when (action) {
                TaskServiceRunner.ACTION_SYNC_DRAFTS -> SYNC_TYPE_DRAFTS
                TaskServiceRunner.ACTION_SYNC_FILTERS -> SYNC_TYPE_FILTERS
                TaskServiceRunner.ACTION_SYNC_USER_COLORS -> SYNC_TYPE_USER_COLORS
                TaskServiceRunner.ACTION_SYNC_USER_NICKNAMES -> SYNC_TYPE_USER_NICKNAMES
                else -> null
            }
        }
    }
}
