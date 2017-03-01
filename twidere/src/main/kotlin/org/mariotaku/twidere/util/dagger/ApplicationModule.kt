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

package org.mariotaku.twidere.util.dagger

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Looper
import android.support.v4.net.ConnectivityManagerCompat
import android.support.v4.text.BidiFormatter
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.nostra13.universalimageloader.cache.disc.DiskCache
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.nostra13.universalimageloader.utils.L
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import com.twitter.Extractor
import com.twitter.Validator
import dagger.Module
import dagger.Provides
import edu.tsinghua.hotmobi.HotMobiLogger
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.imageloader.ReadOnlyDiskLRUNameCache
import org.mariotaku.twidere.util.imageloader.TwidereImageDownloader
import org.mariotaku.twidere.util.imageloader.URLFileNameGenerator
import org.mariotaku.twidere.util.media.TwidereMediaDownloader
import org.mariotaku.twidere.util.media.UILFileCache
import org.mariotaku.twidere.util.net.TwidereDns
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.refresh.AutoRefreshController
import org.mariotaku.twidere.util.refresh.JobSchedulerAutoRefreshController
import org.mariotaku.twidere.util.refresh.LegacyAutoRefreshController
import org.mariotaku.twidere.util.sync.JobSchedulerSyncController
import org.mariotaku.twidere.util.sync.LegacySyncController
import org.mariotaku.twidere.util.sync.SyncController
import org.mariotaku.twidere.util.sync.SyncPreferences
import java.io.File
import java.io.IOException
import javax.inject.Singleton

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
class ApplicationModule(private val application: Application) {

    init {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            throw RuntimeException("Module must be created inside main thread")
        }
    }

    @Provides
    @Singleton
    fun keyboardShortcutsHandler(): KeyboardShortcutsHandler {
        return KeyboardShortcutsHandler(application)
    }

    @Provides
    @Singleton
    fun externalThemeManager(preferences: SharedPreferencesWrapper): ExternalThemeManager {
        return ExternalThemeManager(application, preferences)
    }

    @Provides
    @Singleton
    fun notificationManagerWrapper(): NotificationManagerWrapper {
        return NotificationManagerWrapper(application)
    }

    @Provides
    @Singleton
    fun sharedPreferences(): SharedPreferencesWrapper {
        return SharedPreferencesWrapper.getInstance(application, Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE, SharedPreferenceConstants::class.java)
    }

    @Provides
    @Singleton
    fun kPreferences(sharedPreferences: SharedPreferencesWrapper): KPreferences {
        return KPreferences(sharedPreferences)
    }

    @Provides
    @Singleton
    fun permissionsManager(): PermissionsManager {
        return PermissionsManager(application)
    }

    @Provides
    @Singleton
    fun userColorNameManager(): UserColorNameManager {
        return UserColorNameManager(application)
    }

    @Provides
    @Singleton
    fun multiSelectManager(): MultiSelectManager {
        return MultiSelectManager()
    }

    @Provides
    @Singleton
    fun restHttpClient(prefs: SharedPreferencesWrapper, dns: Dns,
            connectionPool: ConnectionPool, cache: Cache): RestHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(prefs)
        return HttpClientFactory.createRestHttpClient(conf, dns, connectionPool, cache)
    }

    @Provides
    @Singleton
    fun connectionPool(): ConnectionPool {
        return ConnectionPool()
    }

    @Provides
    @Singleton
    fun bus(): Bus {
        return Bus(ThreadEnforcer.MAIN)
    }

    @Provides
    @Singleton
    fun asyncTaskManager(): AsyncTaskManager {
        return AsyncTaskManager()
    }

    @Provides
    @Singleton
    fun imageLoader(preferences: SharedPreferencesWrapper, downloader: MediaDownloader): ImageLoader {
        val loader = ImageLoader.getInstance()
        val cb = ImageLoaderConfiguration.Builder(application)
        cb.threadPriority(Thread.NORM_PRIORITY - 2)
        cb.denyCacheImageMultipleSizesInMemory()
        cb.tasksProcessingOrder(QueueProcessingType.LIFO)
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(createDiskCache("images", preferences))
        cb.imageDownloader(TwidereImageDownloader(application, downloader))
        L.writeDebugLogs(BuildConfig.DEBUG)
        loader.init(cb.build())
        return loader
    }

    @Provides
    @Singleton
    fun activityTracker(): ActivityTracker {
        return ActivityTracker()
    }

    @Provides
    @Singleton
    fun asyncTwitterWrapper(bus: Bus, preferences: SharedPreferencesWrapper,
            asyncTaskManager: AsyncTaskManager, notificationManagerWrapper: NotificationManagerWrapper): AsyncTwitterWrapper {
        return AsyncTwitterWrapper(application, bus, preferences, asyncTaskManager, notificationManagerWrapper)
    }

    @Provides
    @Singleton
    fun readStateManager(): ReadStateManager {
        return ReadStateManager(application)
    }

    @Provides
    @Singleton
    fun contentNotificationManager(activityTracker: ActivityTracker, userColorNameManager: UserColorNameManager,
            notificationManagerWrapper: NotificationManagerWrapper, preferences: SharedPreferencesWrapper): ContentNotificationManager {
        return ContentNotificationManager(application, activityTracker, userColorNameManager, notificationManagerWrapper, preferences)
    }

    @Provides
    @Singleton
    fun mediaLoaderWrapper(loader: ImageLoader, preferences: SharedPreferencesWrapper): MediaLoaderWrapper {
        val wrapper = MediaLoaderWrapper(loader)
        wrapper.reloadOptions(preferences)
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wrapper.isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        return wrapper
    }

    @Provides
    @Singleton
    fun dns(preferences: SharedPreferencesWrapper): Dns {
        return TwidereDns(application, preferences)
    }

    @Provides
    @Singleton
    fun providesDiskCache(preferences: SharedPreferencesWrapper): DiskCache {
        return createDiskCache("files", preferences)
    }

    @Provides
    @Singleton
    fun fileCache(cache: DiskCache): FileCache {
        return UILFileCache(cache)
    }

    @Provides
    @Singleton
    fun mediaDownloader(preferences: SharedPreferencesWrapper, client: RestHttpClient): MediaDownloader {
        return TwidereMediaDownloader(application, preferences, client)
    }

    @Provides
    @Singleton
    fun validator(): Validator {
        return Validator()
    }

    @Provides
    @Singleton
    fun extractor(): Extractor {
        return Extractor()
    }

    @Provides
    @Singleton
    fun errorInfoStore(): ErrorInfoStore {
        return ErrorInfoStore(application)
    }

    @Provides
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }

    @Provides
    @Singleton
    fun autoRefreshController(kPreferences: KPreferences): AutoRefreshController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !kPreferences[autoRefreshCompatibilityModeKey]) {
            return JobSchedulerAutoRefreshController(application, kPreferences)
        }
        return LegacyAutoRefreshController(application, kPreferences)
    }

    @Provides
    @Singleton
    fun syncController(): SyncController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return JobSchedulerSyncController(application)
        }
        return LegacySyncController(application)
    }

    @Provides
    @Singleton
    fun syncPreferences(): SyncPreferences {
        return SyncPreferences(application)
    }

    @Provides
    @Singleton
    fun taskCreator(kPreferences: KPreferences, bus: Bus): TaskServiceRunner {
        return TaskServiceRunner(application, kPreferences, bus)
    }

    @Provides
    @Singleton
    fun defaultFeatures(preferences: SharedPreferencesWrapper): DefaultFeatures {
        val features = DefaultFeatures()
        features.load(preferences)
        return features
    }

    @Provides
    @Singleton
    fun hotMobiLogger(): HotMobiLogger {
        return HotMobiLogger(application)
    }

    @Provides
    @Singleton
    fun extraFeaturesService(): ExtraFeaturesService {
        return ExtraFeaturesService.newInstance(application)
    }

    @Provides
    @Singleton
    fun etagCache(): ETagCache {
        return ETagCache(application)
    }

    @Provides
    fun locationManager(): LocationManager {
        return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    @Singleton
    fun dataSourceFactory(preferences: SharedPreferencesWrapper, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache): DataSource.Factory {
        val conf = HttpClientFactory.HttpClientConfiguration(preferences)
        val builder = OkHttpClient.Builder()
        HttpClientFactory.initOkHttpClient(conf, builder, dns, connectionPool, cache)
        val userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(application)
        return OkHttpDataSourceFactory(builder.build(), userAgent, null)
    }

    @Provides
    @Singleton
    fun cache(preferences: SharedPreferencesWrapper): Cache {
        val cacheSizeMB = preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
        // Convert to bytes
        return Cache(getCacheDir("network"), cacheSizeMB * 1048576L)
    }

    @Provides
    @Singleton
    fun extractorsFactory(): ExtractorsFactory {
        return DefaultExtractorsFactory()
    }

    private fun createDiskCache(dirName: String, preferences: SharedPreferencesWrapper): DiskCache {
        val cacheDir = Utils.getExternalCacheDir(application, dirName)
        val fallbackCacheDir = Utils.getInternalCacheDir(application, dirName)
        val fileNameGenerator = URLFileNameGenerator()
        val cacheSize = preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
        try {
            val cacheMaxSizeBytes = cacheSize * 1024 * 1024
            if (cacheDir != null)
                return LruDiskCache(cacheDir, fallbackCacheDir, fileNameGenerator, cacheMaxSizeBytes.toLong(), 0)
            return LruDiskCache(fallbackCacheDir, null, fileNameGenerator, cacheMaxSizeBytes.toLong(), 0)
        } catch (e: IOException) {
            return ReadOnlyDiskLRUNameCache(cacheDir, fallbackCacheDir, fileNameGenerator)
        }
    }

    private fun getCacheDir(dirName: String): File {
        return Utils.getExternalCacheDir(application, dirName) ?:
                Utils.getInternalCacheDir(application, dirName)
    }

    companion object {

        private var sApplicationModule: ApplicationModule? = null

        fun get(context: Context): ApplicationModule {
            if (sApplicationModule != null) return sApplicationModule!!
            val application = context.applicationContext as Application
            sApplicationModule = ApplicationModule(application)
            return sApplicationModule!!
        }
    }
}

