package org.mariotaku.twidere.util.sync

import android.content.Context
import nl.komponents.kovenant.Promise
import java.lang.Exception

/**
 * Created by mariotaku on 2017/1/3.
 */

abstract class SyncController(val context: Context) {
    abstract fun appStarted()

    fun performSync(syncProvider: DataSyncProvider) {
        syncProvider.newSyncTaskRunner(context).performSync()
    }

    fun cleanupSyncCache(syncProvider: DataSyncProvider): Promise<Boolean, Exception> {
        return syncProvider.newSyncTaskRunner(context).cleanupSyncCache()
    }
}
