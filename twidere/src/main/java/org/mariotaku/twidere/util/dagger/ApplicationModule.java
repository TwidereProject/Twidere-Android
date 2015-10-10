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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Network;
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
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.VideoLoader;
import org.mariotaku.twidere.util.imageloader.TwidereImageDownloader;
import org.mariotaku.twidere.util.net.OkHttpRestClient;
import org.mariotaku.twidere.util.net.TwidereNetwork;

import dagger.Module;
import dagger.Provides;
import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
public class ApplicationModule {

    private final SharedPreferencesWrapper sharedPreferences;

    private final ActivityTracker activityTracker;
    private final AsyncTwitterWrapper asyncTwitterWrapper;
    private final ReadStateManager readStateManager;
    private final VideoLoader videoLoader;
    private final ImageLoader imageLoader;
    private final MediaLoaderWrapper mediaLoaderWrapper;
    private final TwidereImageDownloader imageDownloader;
    private final AsyncTaskManager asyncTaskManager;
    private final Network network;
    private final RestHttpClient restHttpClient;
    private final Bus bus;
    private final MultiSelectManager multiSelectManager;
    private final UserColorNameManager userColorNameManager;
    private final KeyboardShortcutsHandler keyboardShortcutsHandler;
    private final HotMobiLogger hotMobiLogger;

    public ApplicationModule(TwidereApplication application) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Module must be created inside main thread");
        }
        sharedPreferences = SharedPreferencesWrapper.getInstance(application, Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE, SharedPreferenceConstants.class);

        if (sharedPreferences == null) {
            throw new RuntimeException("SharedPreferences must not be null");
        }

        activityTracker = new ActivityTracker();
        bus = new Bus(ThreadEnforcer.MAIN);
        asyncTaskManager = AsyncTaskManager.getInstance();
        readStateManager = new ReadStateManager(application);
        network = new TwidereNetwork(application);


        asyncTwitterWrapper = new AsyncTwitterWrapper(application, asyncTaskManager, sharedPreferences, bus);
        restHttpClient = TwitterAPIFactory.getDefaultHttpClient(application, network);
        imageDownloader = new TwidereImageDownloader(application, restHttpClient, true);
        imageLoader = createImageLoader(application, imageDownloader);
        videoLoader = new VideoLoader(application, restHttpClient, asyncTaskManager, bus);
        mediaLoaderWrapper = new MediaLoaderWrapper(imageLoader, videoLoader);
        multiSelectManager = new MultiSelectManager();
        userColorNameManager = new UserColorNameManager(application);
        keyboardShortcutsHandler = new KeyboardShortcutsHandler(application);
        hotMobiLogger = new HotMobiLogger(application);
    }

    public static ApplicationModule get(@NonNull Context context) {
        return TwidereApplication.getInstance(context).getApplicationModule();
    }

    private static ImageLoader createImageLoader(TwidereApplication application, TwidereImageDownloader imageDownloader) {
        final ImageLoader loader = ImageLoader.getInstance();
        final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(application);
        cb.threadPriority(Thread.NORM_PRIORITY - 2);
        cb.denyCacheImageMultipleSizesInMemory();
        cb.tasksProcessingOrder(QueueProcessingType.LIFO);
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(application.getDiskCache());
        cb.imageDownloader(imageDownloader);
        L.writeDebugLogs(BuildConfig.DEBUG);
        loader.init(cb.build());
        return loader;
    }

    @Provides
    public KeyboardShortcutsHandler getKeyboardShortcutsHandler() {
        return keyboardShortcutsHandler;
    }

    @Provides
    public SharedPreferencesWrapper getSharedPreferences() {
        return sharedPreferences;
    }

    @Provides
    public UserColorNameManager getUserColorNameManager() {
        return userColorNameManager;
    }

    @Provides
    public MultiSelectManager getMultiSelectManager() {
        return multiSelectManager;
    }

    @Provides
    public RestHttpClient getRestHttpClient() {
        return restHttpClient;
    }

    @Provides
    public Bus getBus() {
        return bus;
    }

    @Provides
    public AsyncTaskManager getAsyncTaskManager() {
        return asyncTaskManager;
    }

    @Provides
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Provides
    public VideoLoader getVideoLoader() {
        return videoLoader;
    }

    @Provides
    public TwidereImageDownloader getImageDownloader() {
        return imageDownloader;
    }

    @Provides
    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }

    @Provides
    public AsyncTwitterWrapper getAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    @Provides
    public ReadStateManager getReadStateManager() {
        return readStateManager;
    }

    @Provides
    public MediaLoaderWrapper getMediaLoaderWrapper() {
        return mediaLoaderWrapper;
    }

    @Provides
    public Network getNetwork() {
        return network;
    }


    public void reloadConnectivitySettings() {
        imageDownloader.reloadConnectivitySettings();
        if (restHttpClient instanceof OkHttpRestClient) {
            final OkHttpClient okHttpClient = ((OkHttpRestClient) restHttpClient).getClient();
            TwitterAPIFactory.updateHttpClientConfiguration(sharedPreferences, okHttpClient);
        }
    }

    public void onLowMemory() {
        mediaLoaderWrapper.clearMemoryCache();
    }

    public HotMobiLogger getHotMobiLogger() {
        return hotMobiLogger;
    }
}
