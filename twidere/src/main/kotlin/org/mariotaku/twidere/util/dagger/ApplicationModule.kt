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

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Looper
import androidx.core.net.ConnectivityManagerCompat
import androidx.core.text.BidiFormatter
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import com.twitter.twittertext.Extractor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.extension.model.load
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.cache.DiskLRUFileCache
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.gifshare.GifShareProvider
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.media.TwidereMediaDownloader
import org.mariotaku.twidere.util.net.TwidereDns
import org.mariotaku.twidere.util.notification.ContentNotificationManager
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.refresh.AutoRefreshController
import org.mariotaku.twidere.util.refresh.JobSchedulerAutoRefreshController
import org.mariotaku.twidere.util.refresh.LegacyAutoRefreshController
import org.mariotaku.twidere.util.schedule.StatusScheduleProvider
import org.mariotaku.twidere.util.sync.*
import java.io.File
import javax.inject.Singleton

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
class ApplicationModule(private val context: Context) {

    init {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            throw RuntimeException("Module must be created inside main thread")
        }
    }

    @Provides
    @Singleton
    fun keyboardShortcutsHandler(): KeyboardShortcutsHandler {
        return KeyboardShortcutsHandler(context)
    }

    @Provides
    @Singleton
    fun externalThemeManager(preferences: SharedPreferences): ExternalThemeManager {
        return ExternalThemeManager(context, preferences)
    }

    @Provides
    @Singleton
    fun notificationManagerWrapper(): NotificationManagerWrapper {
        return NotificationManagerWrapper(context)
    }

    @Provides
    @Singleton
    fun sharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun kPreferences(sharedPreferences: SharedPreferences): KPreferences {
        return KPreferences(sharedPreferences)
    }

    @Provides
    @Singleton
    fun permissionsManager(): PermissionsManager {
        return PermissionsManager(context)
    }

    @Provides
    @Singleton
    fun userColorNameManager(): UserColorNameManager {
        return UserColorNameManager(context)
    }

    @Provides
    @Singleton
    fun multiSelectManager(): MultiSelectManager {
        return MultiSelectManager()
    }

    @Provides
    @Singleton
    fun restHttpClient(prefs: SharedPreferences, dns: Dns,
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
    fun activityTracker(): ActivityTracker {
        return ActivityTracker()
    }

    @Provides
    @Singleton
    fun asyncTwitterWrapper(bus: Bus, preferences: SharedPreferences,
            notificationManagerWrapper: NotificationManagerWrapper): AsyncTwitterWrapper {
        return AsyncTwitterWrapper(context, bus, preferences, notificationManagerWrapper)
    }

    @Provides
    @Singleton
    fun readStateManager(): ReadStateManager {
        return ReadStateManager(context)
    }

    @Provides
    @Singleton
    fun contentNotificationManager(activityTracker: ActivityTracker, userColorNameManager: UserColorNameManager,
            notificationManagerWrapper: NotificationManagerWrapper,
            preferences: SharedPreferences): ContentNotificationManager {
        return ContentNotificationManager(context, activityTracker, userColorNameManager, notificationManagerWrapper, preferences)
    }

    @Provides
    @Singleton
    fun mediaLoaderWrapper(preferences: SharedPreferences): MediaPreloader {
        val preloader = MediaPreloader(context)
        preloader.reloadOptions(preferences)
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        preloader.isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        return preloader
    }

    @Provides
    @Singleton
    fun dns(preferences: SharedPreferences): Dns {
        return TwidereDns(context, preferences)
    }

    @Provides
    @Singleton
    fun mediaDownloader(client: RestHttpClient, thumbor: ThumborWrapper): MediaDownloader {
        return TwidereMediaDownloader(context, client, thumbor)
    }

    @Provides
    @Singleton
    fun extractor(): Extractor {
        return Extractor()
    }

    @Provides
    @Singleton
    fun errorInfoStore(): ErrorInfoStore {
        return ErrorInfoStore(context)
    }

    @Provides
    @Singleton
    fun thumborWrapper(preferences: SharedPreferences): ThumborWrapper {
        val thumbor = ThumborWrapper()
        thumbor.reloadSettings(preferences)
        return thumbor
    }

    @Provides
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }

    @Provides
    @Singleton
    fun autoRefreshController(kPreferences: KPreferences): AutoRefreshController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !kPreferences[autoRefreshCompatibilityModeKey]) {
            return JobSchedulerAutoRefreshController(context, kPreferences)
        }
        return LegacyAutoRefreshController(context, kPreferences)
    }

    @Provides
    @Singleton
    fun syncController(): SyncController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return JobSchedulerSyncController(context)
        }
        return LegacySyncController(context)
    }

    @Provides
    @Singleton
    fun syncPreferences(): SyncPreferences {
        return SyncPreferences(context)
    }

    @Provides
    @Singleton
    fun taskCreator(preferences: SharedPreferences, activityTracker: ActivityTracker,
            bus: Bus): TaskServiceRunner {
        return TaskServiceRunner(context, preferences, activityTracker, bus)
    }

    @Provides
    @Singleton
    fun defaultFeatures(preferences: SharedPreferences): DefaultFeatures {
        val features = DefaultFeatures()
        features.load(preferences)
        return features
    }

    @Provides
    @Singleton
    fun extraFeaturesService(): ExtraFeaturesService {
        return ExtraFeaturesService.newInstance(context)
    }

    @Provides
    @Singleton
    fun etagCache(): ETagCache {
        return ETagCache(context)
    }

    @Provides
    fun locationManager(): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    fun connectivityManager(): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    fun okHttpClient(preferences: SharedPreferences, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache): OkHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(preferences)
        val builder = OkHttpClient.Builder()
        HttpClientFactory.initOkHttpClient(conf, builder, dns, connectionPool, cache)
        return builder.build()
    }

    @Provides
    @Singleton
    fun dataSourceFactory(preferences: SharedPreferences, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache): DataSource.Factory {
        val conf = HttpClientFactory.HttpClientConfiguration(preferences)
        val builder = OkHttpClient.Builder()
        HttpClientFactory.initOkHttpClient(conf, builder, dns, connectionPool, cache)
        val userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context)
        return OkHttpDataSourceFactory(builder.build(), userAgent)
    }

    @Provides
    @Singleton
    fun cache(preferences: SharedPreferences): Cache {
        val cacheSizeMB = preferences.getInt(KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
        // Convert to bytes
        return Cache(getCacheDir("network", cacheSizeMB * 1048576L), cacheSizeMB * 1048576L)
    }

    @Provides
    @Singleton
    fun extractorsFactory(): ExtractorsFactory {
        return DefaultExtractorsFactory()
    }

    @Provides
    @Singleton
    fun jsonCache(): JsonCache {
        return JsonCache(getCacheDir("json", 100 * 1048576L))
    }

    @Provides
    @Singleton
    fun fileCache(): FileCache {
        return DiskLRUFileCache(getCacheDir("media", 100 * 1048576L))
    }

    @Provides
    @Singleton
    fun statusScheduleProviderFactory(): StatusScheduleProvider.Factory {
        return StatusScheduleProvider.newFactory()
    }

    @Provides
    @Singleton
    fun gifShareProviderFactory(): GifShareProvider.Factory {
        return GifShareProvider.newFactory()
    }

    @Provides
    @Singleton
    fun timelineSyncManagerFactory(): TimelineSyncManager.Factory {
        return TimelineSyncManager.newFactory()
    }

    @Provides
    @Singleton
    fun mastodonApplicationRegistry(): MastodonApplicationRegistry {
        return MastodonApplicationRegistry(context)
    }

    @Provides
    @Singleton
    fun promotionService(preferences: SharedPreferences): PromotionService {
        return PromotionService.newInstance(context, preferences)
    }

    private fun getCacheDir(dirName: String, sizeInBytes: Long): File {
        return Utils.getExternalCacheDir(context, dirName, sizeInBytes) ?:
                Utils.getInternalCacheDir(context, dirName)
    }

    companion object {

        fun get(context: Context): ApplicationModule {
            val appContext = context.applicationContext
            if (appContext is TwidereApplication) {
                return appContext.applicationModule
            }
            return ApplicationModule(appContext)
        }
    }
}

