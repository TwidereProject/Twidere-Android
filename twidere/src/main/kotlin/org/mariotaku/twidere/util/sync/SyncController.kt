package org.mariotaku.twidere.util.sync

import android.content.Context
import org.mariotaku.twidere.model.sync.SyncProviderInfo

/**
 * Created by mariotaku on 2017/1/3.
 */

abstract class SyncController(val context: Context) {
    abstract fun appStarted()

    fun performSync(providerInfo: SyncProviderInfo) {
        providerInfo.newSyncTaskRunner(context).performSync()
    }

    fun cleanupSyncCache(providerInfo: SyncProviderInfo) {
        providerInfo.newSyncTaskRunner(context).cleanupSyncCache()
    }
}
