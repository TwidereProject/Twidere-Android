package org.mariotaku.twidere.util.sync

/**
 * Created by mariotaku on 2017/1/3.
 */

abstract class SyncController {
    abstract fun performSync()
    abstract fun cleanupSyncCache()
}
