package org.mariotaku.twidere.model.sync

import android.content.SharedPreferences

/**
 * Created by mariotaku on 2017/1/2.
 */

class DropboxSyncProviderInfo(val authToken: String) : SyncProviderInfo(DropboxSyncProviderInfo.TYPE) {

    override fun writeToPreferences(editor: SharedPreferences.Editor) {
        editor.putString(KEY_DROPBOX_AUTH_TOKEN, authToken)
    }

    companion object {
        const val TYPE = "dropbox"

        private const val KEY_DROPBOX_AUTH_TOKEN = "dropbox_auth_token"
        fun newInstance(preferences: SharedPreferences): DropboxSyncProviderInfo? {
            val authToken = preferences.getString(KEY_DROPBOX_AUTH_TOKEN, null) ?: return null
            return DropboxSyncProviderInfo(authToken)
        }
    }

}
