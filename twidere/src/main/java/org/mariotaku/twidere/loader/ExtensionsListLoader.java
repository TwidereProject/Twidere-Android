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

package org.mariotaku.twidere.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtensionsListLoader extends AsyncTaskLoader<List<ExtensionsListLoader.ExtensionInfo>>
        implements Constants {

    private PackageIntentReceiver mPackageObserver;
    private final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
    private final PackageManager mPackageManager;

    public ExtensionsListLoader(final Context context, final PackageManager pm) {
        super(context);
        mPackageManager = pm;
    }

    @Override
    public List<ExtensionInfo> loadInBackground() {
        final List<ApplicationInfo> apps = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<ExtensionInfo> extensions = new ArrayList<>();
        for (final ApplicationInfo info : apps) {
            final Bundle meta = info.metaData;
            if (meta != null && meta.getBoolean(METADATA_KEY_EXTENSION, false)) {
                extensions.add(new ExtensionInfo(info, mPackageManager));
            }
        }
        return extensions;
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {

        // Start watching for changes in the app data.
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        // Has something interesting in the configuration changed since we
        // last built the app list?
        final boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

        if (takeContentChanged() || configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    public static class ExtensionInfo implements Comparable<ExtensionInfo> {
        public final String[] permissions;
        public final String label, description;
        public final String pname, settings;
        public final Drawable icon;

        ExtensionInfo(final ApplicationInfo info, final PackageManager pm) {
            final Bundle meta = info.metaData;
            final String permissionString = meta.getString(METADATA_KEY_EXTENSION_PERMISSIONS);
            permissions = permissionString != null ? permissionString
                    .split(SEPARATOR_PERMISSION_REGEX) : null;
            settings = meta.getString(METADATA_KEY_EXTENSION_SETTINGS);
            icon = info.loadIcon(pm);
            pname = info.packageName;
            label = ParseUtils.parseString(info.loadLabel(pm), pname);
            description = ParseUtils.parseString(info.loadDescription(pm));
        }

        @Override
        public int compareTo(@NonNull final ExtensionInfo another) {
            return label.compareToIgnoreCase(another.label);
        }

        @Override
        public String toString() {
            return "ExtensionInfo{" +
                    "permissions=" + Arrays.toString(permissions) +
                    ", label='" + label + '\'' +
                    ", description='" + description + '\'' +
                    ", pname='" + pname + '\'' +
                    ", settings='" + settings + '\'' +
                    ", icon=" + icon +
                    '}';
        }
    }

    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
    public static class InterestingConfigChanges {

        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(final Resources res) {
            final int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            final boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged
                    || (configChanges & (ActivityInfo.CONFIG_LOCALE | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps so
     * that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {

        final ExtensionsListLoader mLoader;

        public PackageIntentReceiver(final ExtensionsListLoader loader) {
            mLoader = loader;
            final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                final IntentFilter sdFilter = new IntentFilter();
                sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
                sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
                mLoader.getContext().registerReceiver(this, sdFilter);
            }
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();
        }
    }

}
