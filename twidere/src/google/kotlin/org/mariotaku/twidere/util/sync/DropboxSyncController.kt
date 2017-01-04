package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.Intent
import org.mariotaku.twidere.service.DropboxDataSyncService

class DropboxSyncController(val context: Context) : SyncController() {
    override fun cleanupSyncCache() {
        context.syncDataDir.listFiles { file, name -> file.isFile }.forEach { file ->
            file.delete()
        }
    }

    override fun performSync() {
        context.startService(Intent(context, DropboxDataSyncService::class.java))
    }

}