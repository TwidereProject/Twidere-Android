package org.mariotaku.twidere.model.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.google.GoogleDriveSyncTaskRunner

class GoogleDriveSyncProviderInfo(val accessToken: String) : SyncProviderInfo(GoogleDriveSyncProviderInfo.TYPE) {
    override fun writeToPreferences(editor: SharedPreferences.Editor) {
        editor.putString(KEY_GOOGLE_DRIVE_AUTH_TOKEN, accessToken)
    }

    override fun newSyncTaskRunner(context: Context): SyncTaskRunner {
        return GoogleDriveSyncTaskRunner(context, accessToken)
    }

    companion object {
        const val TYPE = "google_drive"
        private const val KEY_GOOGLE_DRIVE_AUTH_TOKEN = "google_drive_auth_token"

        fun newInstance(preferences: SharedPreferences): GoogleDriveSyncProviderInfo? {
            val accessToken = preferences.getString(KEY_GOOGLE_DRIVE_AUTH_TOKEN, null) ?: return null
            return GoogleDriveSyncProviderInfo(accessToken)
        }
    }
}