package org.mariotaku.twidere.model.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.google.GoogleDriveSyncTaskRunner

class GoogleDriveSyncProviderInfo(val refreshToken: String) : SyncProviderInfo(GoogleDriveSyncProviderInfo.TYPE) {
    override fun writeToPreferences(editor: SharedPreferences.Editor) {
        editor.putString(KEY_GOOGLE_DRIVE_REFRESH_TOKEN, refreshToken)
    }

    override fun newSyncTaskRunner(context: Context): SyncTaskRunner {
        return GoogleDriveSyncTaskRunner(context, refreshToken)
    }

    companion object {
        const val TYPE = "google_drive"
        private const val KEY_GOOGLE_DRIVE_REFRESH_TOKEN = "google_drive_refresh_token"

        const val WEB_CLIENT_ID = "223623398518-0sc2i5fsqliidcdoogn53iqltpktfnff.apps.googleusercontent.com"
        const val WEB_CLIENT_SECRET = "BsZ0a06UgJf5hJOTI3fcxI2u"

        fun newInstance(preferences: SharedPreferences): GoogleDriveSyncProviderInfo? {
            val accessToken = preferences.getString(KEY_GOOGLE_DRIVE_REFRESH_TOKEN, null) ?: return null
            return GoogleDriveSyncProviderInfo(accessToken)
        }
    }
}