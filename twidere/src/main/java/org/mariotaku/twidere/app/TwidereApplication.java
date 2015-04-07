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

package org.mariotaku.twidere.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.otto.Bus;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.AssistLauncherActivity;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.activity.MainHondaJOJOActivity;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.VideoLoader;
import org.mariotaku.twidere.util.content.TwidereSQLiteOpenHelper;
import org.mariotaku.twidere.util.imageloader.TwidereImageDownloader;
import org.mariotaku.twidere.util.imageloader.URLFileNameGenerator;
import org.mariotaku.twidere.util.net.TwidereHostAddressResolver;

import java.io.File;

import edu.tsinghua.spice.SpiceService;
import edu.ucdavis.earlybird.UCDService;
import twitter4j.http.HostAddressResolver;

import static org.mariotaku.twidere.util.UserColorNameUtils.initUserColor;
import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getInternalCacheDir;
import static org.mariotaku.twidere.util.Utils.initAccountColor;
import static org.mariotaku.twidere.util.Utils.startRefreshServiceIfNeeded;
import static org.mariotaku.twidere.util.Utils.startUsageStatisticsServiceIfNeeded;

public class TwidereApplication extends MultiDexApplication implements Constants,
        OnSharedPreferenceChangeListener {

    private static final String KEY_UCD_DATA_PROFILING = "ucd_data_profiling";
    private static final String KEY_SPICE_DATA_PROFILING = "spice_data_profiling";

    private Handler mHandler;
    private MediaLoaderWrapper mMediaLoaderWrapper;
    private ImageLoader mImageLoader;
    private AsyncTaskManager mAsyncTaskManager;
    private SharedPreferences mPreferences;
    private AsyncTwitterWrapper mTwitterWrapper;
    private MultiSelectManager mMultiSelectManager;
    private TwidereImageDownloader mImageDownloader, mFullImageDownloader;
    private DiskCache mDiskCache, mFullDiskCache;
    private MessagesManager mCroutonsManager;
    private SQLiteOpenHelper mSQLiteOpenHelper;
    private HostAddressResolver mResolver;
    private SQLiteDatabase mDatabase;
    private Bus mMessageBus;
    private VideoLoader mVideoLoader;
    private ReadStateManager mReadStateManager;

    public AsyncTaskManager getAsyncTaskManager() {
        if (mAsyncTaskManager != null) return mAsyncTaskManager;
        return mAsyncTaskManager = AsyncTaskManager.getInstance();
    }

    public DiskCache getDiskCache() {
        if (mDiskCache != null) return mDiskCache;
        return mDiskCache = createDiskCache(DIR_NAME_IMAGE_CACHE);
    }

    public DiskCache getFullDiskCache() {
        if (mFullDiskCache != null) return mFullDiskCache;
        return mFullDiskCache = createDiskCache(DIR_NAME_FULL_IMAGE_CACHE);
    }

    public ImageDownloader getFullImageDownloader() {
        if (mFullImageDownloader != null) return mFullImageDownloader;
        return mFullImageDownloader = new TwidereImageDownloader(this, true, true);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public HostAddressResolver getHostAddressResolver() {
        if (mResolver != null) return mResolver;
        return mResolver = new TwidereHostAddressResolver(this);
    }

    public ReadStateManager getReadStateManager() {
        if (mReadStateManager != null) return mReadStateManager;
        return mReadStateManager = new ReadStateManager(this);
    }

    public ImageDownloader getImageDownloader() {
        if (mImageDownloader != null) return mImageDownloader;
        return mImageDownloader = new TwidereImageDownloader(this, false, true);
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader != null) return mImageLoader;
        final ImageLoader loader = ImageLoader.getInstance();
        final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(this);
        cb.threadPriority(Thread.NORM_PRIORITY - 2);
        cb.denyCacheImageMultipleSizesInMemory();
        cb.tasksProcessingOrder(QueueProcessingType.LIFO);
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(getDiskCache());
        cb.imageDownloader(getImageDownloader());
        L.writeDebugLogs(Utils.isDebugBuild());
        loader.init(cb.build());
        return mImageLoader = loader;
    }

    public VideoLoader getVideoLoader() {
        if (mVideoLoader != null) return mVideoLoader;
        final VideoLoader loader = new VideoLoader(this);
        return mVideoLoader = loader;
    }

    public MediaLoaderWrapper getMediaLoaderWrapper() {
        if (mMediaLoaderWrapper != null) return mMediaLoaderWrapper;
        return mMediaLoaderWrapper = new MediaLoaderWrapper(getImageLoader(), getVideoLoader());
    }

    @NonNull
    public static TwidereApplication getInstance(@NonNull final Context context) {
        return (TwidereApplication) context.getApplicationContext();
    }

    public Bus getMessageBus() {
        return mMessageBus;
    }

    public MessagesManager getMessagesManager() {
        if (mCroutonsManager != null) return mCroutonsManager;
        return mCroutonsManager = new MessagesManager(this);
    }

    public MultiSelectManager getMultiSelectManager() {
        if (mMultiSelectManager != null) return mMultiSelectManager;
        return mMultiSelectManager = new MultiSelectManager();
    }

    public SQLiteDatabase getSQLiteDatabase() {
        if (mDatabase != null) return mDatabase;

        StrictModeUtils.checkDiskIO();
        return mDatabase = getSQLiteOpenHelper().getWritableDatabase();
    }

    public SQLiteOpenHelper getSQLiteOpenHelper() {
        if (mSQLiteOpenHelper != null) return mSQLiteOpenHelper;
        return mSQLiteOpenHelper = new TwidereSQLiteOpenHelper(this, DATABASES_NAME, DATABASES_VERSION);
    }

    public AsyncTwitterWrapper getTwitterWrapper() {
        if (mTwitterWrapper != null) return mTwitterWrapper;
        return mTwitterWrapper = AsyncTwitterWrapper.getInstance(this);
    }

    @Override
    public void onCreate() {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
        }
        super.onCreate();
        mHandler = new Handler();
        mMessageBus = new Bus();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        initializeAsyncTask();
        initAccountColor(this);
        initUserColor(this);

        final PackageManager pm = getPackageManager();
        final ComponentName main = new ComponentName(this, MainActivity.class);
        final ComponentName main2 = new ComponentName(this, MainHondaJOJOActivity.class);
        final boolean mainDisabled = pm.getComponentEnabledSetting(main) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        final boolean main2Disabled = pm.getComponentEnabledSetting(main2) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        final boolean noEntry = mainDisabled && main2Disabled;
        if (noEntry) {
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else if (!mainDisabled) {
            pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        if (!Utils.isComposeNowSupported()) {
            final ComponentName assist = new ComponentName(this, AssistLauncherActivity.class);
            pm.setComponentEnabledSetting(assist, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        migrateUsageStatisticsPreferences();
        startUsageStatisticsServiceIfNeeded(this);
        startRefreshServiceIfNeeded(this);
    }

    private void migrateUsageStatisticsPreferences() {
        final boolean hasUsageStatistics = mPreferences.contains(KEY_USAGE_STATISTICS);
        if (hasUsageStatistics) return;
        if (mPreferences.contains(KEY_UCD_DATA_PROFILING) || mPreferences.contains(KEY_SPICE_DATA_PROFILING)) {
            final boolean prevUsageEnabled = mPreferences.getBoolean(KEY_UCD_DATA_PROFILING, false)
                    || mPreferences.getBoolean(KEY_SPICE_DATA_PROFILING, false);
            final Editor editor = mPreferences.edit();
            editor.putBoolean(KEY_USAGE_STATISTICS, prevUsageEnabled);
            editor.remove(KEY_UCD_DATA_PROFILING);
            editor.remove(KEY_SPICE_DATA_PROFILING);
            editor.apply();
        }
    }

    @Override
    public void onLowMemory() {
        if (mMediaLoaderWrapper != null) {
            mMediaLoaderWrapper.clearMemoryCache();
        }
        super.onLowMemory();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        if (KEY_REFRESH_INTERVAL.equals(key)) {
            stopService(new Intent(this, RefreshService.class));
            startRefreshServiceIfNeeded(this);
        } else if (KEY_ENABLE_PROXY.equals(key) || KEY_CONNECTION_TIMEOUT.equals(key) || KEY_PROXY_HOST.equals(key)
                || KEY_PROXY_PORT.equals(key) || KEY_FAST_IMAGE_LOADING.equals(key)) {
            reloadConnectivitySettings();
        } else if (KEY_USAGE_STATISTICS.equals(key)) {
            stopService(new Intent(this, UCDService.class));
            startUsageStatisticsServiceIfNeeded(this);
            //spice
            stopService(new Intent(this, SpiceService.class));
            startUsageStatisticsServiceIfNeeded(this);
            //end
        } else if (KEY_CONSUMER_KEY.equals(key) || KEY_CONSUMER_SECRET.equals(key) || KEY_API_URL_FORMAT.equals(key)
                || KEY_AUTH_TYPE.equals(key) || KEY_SAME_OAUTH_SIGNING_URL.equals(key) || KEY_THUMBOR_ENABLED.equals(key)
                || KEY_THUMBOR_ADDRESS.equals(key) || KEY_THUMBOR_SECURITY_KEY.equals(key)) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(KEY_API_LAST_CHANGE, System.currentTimeMillis());
            editor.apply();
        }
    }

    public void reloadConnectivitySettings() {
        if (mImageDownloader != null) {
            mImageDownloader.reloadConnectivitySettings();
        }
        if (mFullImageDownloader != null) {
            mFullImageDownloader.reloadConnectivitySettings();
        }
    }

    private DiskCache createDiskCache(final String dirName) {
        final File cacheDir = getBestCacheDir(this, dirName);
        final File fallbackCacheDir = getInternalCacheDir(this, dirName);
//        final LruDiscCache discCache = new LruDiscCache(cacheDir, new URLFileNameGenerator(), 384 *
//                1024 * 1024);
//        discCache.setReserveCacheDir(fallbackCacheDir);
//        return discCache;
        return new UnlimitedDiscCache(cacheDir, fallbackCacheDir, new URLFileNameGenerator());
    }

    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (final ClassNotFoundException ignore) {
        }
    }

}
