package org.mariotaku.twidere.model.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.google.GoogleDriveSyncTaskRunner

class GoogleDriveSyncProviderInfo : SyncProviderInfo(GoogleDriveSyncProviderInfo.TYPE) {
    override fun writeToPreferences(editor: SharedPreferences.Editor) {

    }

    override fun newSyncTaskRunner(context: Context): SyncTaskRunner {
        return GoogleDriveSyncTaskRunner(context)
    }

    companion object {
        const val TYPE = "google_drive"

        fun newInstance(preferences: SharedPreferences): GoogleDriveSyncProviderInfo? {
            return GoogleDriveSyncProviderInfo()
        }
    }
}