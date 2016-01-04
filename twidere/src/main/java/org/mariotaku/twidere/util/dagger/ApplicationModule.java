/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.dagger;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.text.BidiFormatter;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.okhttp.Dns;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ExternalThemeManager;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.NotificationManagerWrapper;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.imageloader.ReadOnlyDiskLRUNameCache;
import org.mariotaku.twidere.util.imageloader.TwidereImageDownloader;
import org.mariotaku.twidere.util.imageloader.URLFileNameGenerator;
import org.mariotaku.twidere.util.net.TwidereDns;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.tsinghua.hotmobi.HotMobiLogger;

import static org.mariotaku.twidere.util.Utils.getInternalCacheDir;

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
public class ApplicationModule implements Constants {

    private final TwidereApplication application;

    public ApplicationModule(TwidereApplication application) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Module must be created inside main thread");
        }
        this.application = application;
    }

    static ApplicationModule get(@NonNull Context context) {
        return TwidereApplication.getInstance(context).getApplicationModule();
    }

    @Provides
    @Singleton
    public KeyboardShortcutsHandler keyboardShortcutsHandler() {
        return new KeyboardShortcutsHandler(application);
    }

    @Provides
    @Singleton
    public ExternalThemeManager externalThemeManager(SharedPreferencesWrapper preferences) {
        return new ExternalThemeManager(application, preferences);
    }

    @Provides
    @Singleton
    public NotificationManagerWrapper notificationManagerWrapper() {
        return new NotificationManagerWrapper(application);
    }

    @Provides
    @Singleton
    public SharedPreferencesWrapper sharedPreferences() {
        return SharedPreferencesWrapper.getInstance(application, Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE, SharedPreferenceConstants.class);
    }

    @Provides
    @Singleton
    public UserColorNameManager userColorNameManager() {
        return new UserColorNameManager(application);
    }

    @Provides
    @Singleton
    public MultiSelectManager multiSelectManager() {
        return new MultiSelectManager();
    }

    @Provides
    @Singleton
    public RestHttpClient restHttpClient() {
        return TwitterAPIFactory.getDefaultHttpClient(application);
    }

    @Provides
    @Singleton
    public Bus bus() {
        return new Bus(ThreadEnforcer.MAIN);
    }

    @Provides
    @Singleton
    public AsyncTaskManager asyncTaskManager() {
        return new AsyncTaskManager();
    }

    @Provides
    @Singleton
    public ImageLoader imageLoader(SharedPreferencesWrapper preferences, RestHttpClient client) {
        final ImageLoader loader = ImageLoader.getInstance();
        final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(application);
        cb.threadPriority(Thread.NORM_PRIORITY - 2);
        cb.denyCacheImageMultipleSizesInMemory();
        cb.tasksProcessingOrder(QueueProcessingType.LIFO);
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(createDiskCache("images", preferences));
        cb.imageDownloader(new TwidereImageDownloader(application, preferences, client, true));
        L.writeDebugLogs(BuildConfig.DEBUG);
        loader.init(cb.build());
        return loader;
    }

    @Provides
    @Singleton
    public ActivityTracker activityTracker() {
        return new ActivityTracker();
    }

    @Provides
    @Singleton
    public AsyncTwitterWrapper asyncTwitterWrapper(UserColorNameManager userColorNameManager,
                                                   ReadStateManager readStateManager,
                                                   Bus bus, SharedPreferencesWrapper preferences,
                                                   AsyncTaskManager asyncTaskManager) {
        return new AsyncTwitterWrapper(application, userColorNameManager, readStateManager, bus,
                preferences, asyncTaskManager);
    }

    @Provides
    @Singleton
    public ReadStateManager readStateManager() {
        return new ReadStateManager(application);
    }

    @Provides
    @Singleton
    public MediaLoaderWrapper mediaLoaderWrapper(ImageLoader loader) {
        return new MediaLoaderWrapper(loader);
    }

    @Provides
    @Singleton
    public Dns dns() {
        return new TwidereDns(application);
    }

    @Provides
    @Singleton
    public DiskCache providesDiskCache(SharedPreferencesWrapper preferences) {
        return createDiskCache("files", preferences);
    }

    @Provides
    public BidiFormatter provideBidiFormatter() {
        return BidiFormatter.getInstance();
    }

    private DiskCache createDiskCache(final String dirName, SharedPreferencesWrapper preferences) {
        final File cacheDir = Utils.getExternalCacheDir(application, dirName);
        final File fallbackCacheDir = getInternalCacheDir(application, dirName);
        final URLFileNameGenerator fileNameGenerator = new URLFileNameGenerator();
        final int cacheSize = TwidereMathUtils.clamp(preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300), 100, 500);
        try {
            final int cacheMaxSizeBytes = cacheSize * 1024 * 1024;
            if (cacheDir != null)
                return new LruDiskCache(cacheDir, fallbackCacheDir, fileNameGenerator, cacheMaxSizeBytes, 0);
            return new LruDiskCache(fallbackCacheDir, null, fileNameGenerator, cacheMaxSizeBytes, 0);
        } catch (IOException e) {
            return new ReadOnlyDiskLRUNameCache(cacheDir, fallbackCacheDir, fileNameGenerator);
        }
    }

    public void reloadConnectivitySettings() {
//        imageDownloader.reloadConnectivitySettings();
//        if (restHttpClient instanceof OkHttpRestClient) {
//             OkHttpClient okHttpClient = ((OkHttpRestClient) restHttpClient).getClient();
//            TwitterAPIFactory.updateHttpClientConfiguration(application, sharedPreferences, okHttpClient);
//        }
    }

    public void onLowMemory() {
    }

    @Provides
    @Singleton
    public HotMobiLogger hotMobiLogger() {
        return new HotMobiLogger(application);
    }
}
