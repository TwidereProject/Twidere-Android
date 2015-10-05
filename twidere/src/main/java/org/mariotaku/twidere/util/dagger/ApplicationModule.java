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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.VideoLoader;
import org.mariotaku.twidere.util.imageloader.TwidereImageDownloader;
import org.mariotaku.twidere.util.net.TwidereNetwork;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
public class ApplicationModule {

    private final ActivityTracker activityTracker;
    private final AsyncTwitterWrapper asyncTwitterWrapper;
    private final ReadStateManager readStateManager;
    private final VideoLoader videoLoader;
    private final ImageLoader imageLoader;
    private final MediaLoaderWrapper mediaLoaderWrapper;
    private final TwidereImageDownloader imageDownloader;
    private final AsyncTaskManager asyncTaskManager;
    private final Network network;

    public ApplicationModule(TwidereApplication application) {
        activityTracker = new ActivityTracker();
        asyncTwitterWrapper = new AsyncTwitterWrapper(application);
        readStateManager = new ReadStateManager(application);
        imageDownloader = new TwidereImageDownloader(application, true);
        imageLoader = createImageLoader(application, imageDownloader);
        videoLoader = new VideoLoader(application);
        mediaLoaderWrapper = new MediaLoaderWrapper(imageLoader, videoLoader);
        asyncTaskManager = AsyncTaskManager.getInstance();
        network = new TwidereNetwork(application);
    }

    public static ApplicationModule get(Context context) {
        return TwidereApplication.getInstance(context).getApplicationModule();
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
    public Network getNetwork() {
        return network;
    }
}
