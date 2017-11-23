package org.mariotaku.twidere.util.sync

import android.content.Context
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.UserColorNameManager
import java.lang.Exception
import javax.inject.Inject

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
     * @return Task promise, `null` if not suported
     */
    protected abstract fun onCreatePromise(action: String): Promise<Boolean, Exception>?

    fun promise(action: String): Promise<Boolean, Exception> {
        val syncType = SyncTaskRunner.getSyncType(action) ?: return Promise.ofFail(UnsupportedOperationException())
        if (!syncPreferences.isSyncEnabled(syncType)) return Promise.ofFail(UnsupportedOperationException())
        val promise = onCreatePromise(action) ?: return Promise.ofFail(UnsupportedOperationException())
        return promise.success { synced ->
            if (synced) {
                syncPreferences.setLastSynced(syncType, System.currentTimeMillis())
            }
        }.successUi { synced ->
            bus.post(TaskServiceRunner.SyncFinishedEvent(syncType, synced))
        }.failUi {
            bus.post(TaskServiceRunner.SyncFinishedEvent(syncType, false))
        }
    }


    fun cleanupSyncCache(): Promise<Boolean, Exception> {
        return task {
            context.syncDataDir.deleteRecursively()
        }
    }

    fun syncAll(): Promise<List<Boolean>, Exception> {
        return all(TaskServiceRunner.ACTIONS_SYNC.map(this::promise), cancelOthersOnError = false)
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
