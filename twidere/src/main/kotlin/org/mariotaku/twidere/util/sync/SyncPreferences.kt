package org.mariotaku.twidere.util.sync

import android.content.Context
import org.mariotaku.twidere.TwidereConstants.SYNC_PREFERENCES_NAME

/**
 * Created by mariotaku on 2017/1/6.
 */

class SyncPreferences(val context: Context) {
    private val preferences = context.getSharedPreferences(SYNC_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setLastSynced(type: String, timestamp: Long) {
        preferences.edit().putLong(getLastSyncedKey(type), timestamp).apply()
    }

    fun setSyncEnabled(type: String, enabled: Boolean) {
        preferences.edit().putBoolean(getSyncEnabledKey(type), enabled).apply()
    }

    fun getLastSynced(syncType: String): Long {
        return preferences.getLong(getLastSyncedKey(syncType), -1)
    }

    fun isSyncEnabled(syncType: String): Boolean {
        return preferences.getBoolean(getSyncEnabledKey(syncType), true)
    }

    companion object {

        @JvmStatic
        fun getSyncEnabledKey(type: String) = "sync_enabled_$type"

        @JvmStatic
        fun getLastSyncedKey(type: String) = "last_synced_$type"

    }
}
