/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.loader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.twidere.TwidereConstants.*
import java.text.Collator
import java.util.*

class ExtensionsListLoader(
        context: Context
) : FixedAsyncTaskLoader<List<ExtensionsListLoader.ExtensionInfo>>(context) {

    private val packageManager = context.packageManager
    private var packageObserver: PackageIntentReceiver? = null
    private val lastConfig = InterestingConfigChanges()

    override fun loadInBackground(): List<ExtensionInfo> {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val extensions = ArrayList<ExtensionInfo>()
        for (info in apps) {
            val meta = info.metaData
            if (meta != null && meta.getBoolean(METADATA_KEY_EXTENSION, false)) {
                extensions.add(ExtensionInfo(info, packageManager))
            }
        }
        Collections.sort(extensions, ExtensionInfoComparator(Collator.getInstance()))
        return extensions
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()

        // Stop monitoring for changes.
        if (packageObserver != null) {
            context.unregisterReceiver(packageObserver)
            packageObserver = null
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    override fun onStartLoading() {

        // Start watching for changes in the app data.
        if (packageObserver == null) {
            packageObserver = PackageIntentReceiver(this)
        }

        // Has something interesting in the configuration changed since we
        // last built the app list?
        val configChange = lastConfig.applyNewConfig(context.resources)

        if (takeContentChanged() || configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad()
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    data class ExtensionInfo(
            val packageName: String,
            val label: CharSequence,
            val description: CharSequence?,
            val icon: Drawable?,
            val permissions: Array<String>?,
            val settings: String?
    ) {

        constructor(info: ApplicationInfo, pm: PackageManager) : this(
                info.packageName,
                info.loadLabel(pm),
                info.loadDescription(pm),
                info.loadIcon(pm),
                info.metaData?.getString(METADATA_KEY_EXTENSION_PERMISSIONS)?.split('|')?.filterNot(String::isEmpty)?.toTypedArray(),
                info.metaData?.getString(METADATA_KEY_EXTENSION_SETTINGS)
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ExtensionInfo

            if (packageName != other.packageName) return false
            if (label != other.label) return false
            if (description != other.description) return false
            if (icon != other.icon) return false
            if (permissions != null) {
                if (other.permissions == null) return false
                if (!permissions.contentEquals(other.permissions)) return false
            } else if (other.permissions != null) return false
            if (settings != other.settings) return false

            return true
        }

        override fun hashCode(): Int {
            var result = packageName.hashCode()
            result = 31 * result + label.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + (permissions?.contentHashCode() ?: 0)
            result = 31 * result + (settings?.hashCode() ?: 0)
            return result
        }

    }

    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
    class InterestingConfigChanges {

        private val lastConfiguration = Configuration()
        private var lastDensity: Int = 0

        internal fun applyNewConfig(res: Resources): Boolean {
            val configChanges = lastConfiguration.updateFrom(res.configuration)
            val densityChanged = lastDensity != res.displayMetrics.densityDpi
            if (densityChanged || configChanges and (ActivityInfo.CONFIG_LOCALE or ActivityInfo.CONFIG_UI_MODE or ActivityInfo.CONFIG_SCREEN_LAYOUT) != 0) {
                lastDensity = res.displayMetrics.densityDpi
                return true
            }
            return false
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps so
     * that the loader can be updated.
     */
    class PackageIntentReceiver(internal val loader: ExtensionsListLoader) : BroadcastReceiver() {

        init {
            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
            filter.addDataScheme("package")
            loader.context.registerReceiver(this, filter)
            // Register for events related to sdcard installation.
            val sdFilter = IntentFilter()
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)
            loader.context.registerReceiver(this, sdFilter)
        }

        override fun onReceive(context: Context, intent: Intent) {
            // Tell the loader about the change.
            loader.onContentChanged()
        }
    }

    class ExtensionInfoComparator(private val collator: Collator) : Comparator<ExtensionInfo> {
        override fun compare(o1: ExtensionInfo, o2: ExtensionInfo): Int {
            val label1 = o1.label.toString()
            val label2 = o2.label.toString()
            return collator.compare(label1, label2)
        }

    }

}
