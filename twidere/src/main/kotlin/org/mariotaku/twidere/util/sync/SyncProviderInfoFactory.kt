package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.model.sync.SyncProviderEntry
import org.mariotaku.twidere.model.sync.SyncProviderInfo
import java.util.*

/**
 * Created by mariotaku on 2017/1/2.
 */

abstract class SyncProviderInfoFactory {
    abstract fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo?

    abstract fun getSupportedProviders(context: Context): List<SyncProviderEntry>

    companion object {
        fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo? {
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                val info = factory.getInfoForType(type, preferences)
                if (info != null) return info
            }
            return null
        }

        fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
            val result = ArrayList<SyncProviderEntry>()
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                result.addAll(factory.getSupportedProviders(context))
            }
            return result
        }

        fun getProviderEntry(context: Context, type: String): SyncProviderEntry? {
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                factory.getSupportedProviders(context).forEach { entry ->
                    if (entry.type == type) return entry
                }
            }
            return null
        }
    }
}
