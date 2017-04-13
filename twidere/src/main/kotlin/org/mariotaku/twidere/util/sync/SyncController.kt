package org.mariotaku.twidere.util.sync

import android.content.Context

/**
 * Created by mariotaku on 2017/1/3.
 */

abstract class SyncController(val context: Context) {
    abstract fun appStarted()

    fun performSync(syncProvider: DataSyncProvider) {
        syncProvider.newSyncTaskRunner(context).performSync()
    }

    fun cleanupSyncCache(syncProvider: DataSyncProvider) {
        syncProvider.newSyncTaskRunner(context).cleanupSyncCache()
    }
}
