package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.DropboxAuthStarterActivity
import org.mariotaku.twidere.model.sync.DropboxSyncProviderInfo
import org.mariotaku.twidere.model.sync.SyncProviderEntry
import org.mariotaku.twidere.model.sync.SyncProviderInfo

/**
 * Created by mariotaku on 2017/1/2.
 */

class NonFreeSyncProviderInfoFactory : SyncProviderInfoFactory() {
    override fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo? {
        return when (type) {
            DropboxSyncProviderInfo.TYPE -> DropboxSyncProviderInfo.newInstance(preferences)
            else -> null
        }
    }

    override fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
        return listOf(
                SyncProviderEntry(DropboxSyncProviderInfo.TYPE,
                        context.getString(R.string.sync_provider_name_dropbox),
                        Intent(context, DropboxAuthStarterActivity::class.java))
        )
    }
}
