/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.model.sync.SyncProviderEntry
import java.util.*

/**
 * Created by mariotaku on 2017/1/2.
 */
abstract class DataSyncProvider(val type: String) {

    abstract fun writeToPreferences(editor: SharedPreferences.Editor)

    abstract fun newSyncTaskRunner(context: Context): SyncTaskRunner

    open fun newTimelineSyncManager(context: Context): TimelineSyncManager? = null

    abstract class Factory {
        abstract fun createForType(type: String, preferences: SharedPreferences): DataSyncProvider?

        abstract fun getSupportedProviders(context: Context): List<SyncProviderEntry>

        abstract fun notifyUpdate(context: Context)

        companion object {
            fun notifyUpdate(context: Context) {
                ServiceLoader.load(Factory::class.java).forEach { factory ->
                    factory.notifyUpdate(context)
                }
            }

            fun createForType(type: String, preferences: SharedPreferences): DataSyncProvider? {
                ServiceLoader.load(Factory::class.java).forEach { factory ->
                    val info = factory.createForType(type, preferences)
                    if (info != null) return info
                }
                return null
            }

            fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
                val result = ArrayList<SyncProviderEntry>()
                ServiceLoader.load(Factory::class.java).forEach { factory ->
                    result.addAll(factory.getSupportedProviders(context))
                }
                return result
            }

            fun getProviderEntry(context: Context, type: String): SyncProviderEntry? {
                ServiceLoader.load(Factory::class.java).forEach { factory ->
                    factory.getSupportedProviders(context).forEach { entry ->
                        if (entry.type == type) return entry
                    }
                }
                return null
            }
        }
    }

}
