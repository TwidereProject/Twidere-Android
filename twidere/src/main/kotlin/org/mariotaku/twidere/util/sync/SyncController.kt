package org.mariotaku.twidere.util.sync

import android.content.Context

abstract class SyncController(val context: Context, val provider: DataSyncProvider) {

    abstract fun appStarted()

}
