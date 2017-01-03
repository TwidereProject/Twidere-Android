package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.model.sync.SyncProviderEntry
import org.mariotaku.twidere.model.sync.SyncProviderInfo

/**
 * Created by mariotaku on 2017/1/2.
 */

class OpenSourceSyncProviderInfoFactory : SyncProviderInfoFactory() {
    override fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo? {
        return null
    }

    override fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
        return emptyList()
    }

}
