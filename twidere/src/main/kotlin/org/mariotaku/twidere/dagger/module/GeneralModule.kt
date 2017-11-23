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

package org.mariotaku.twidere.dagger.module

import android.app.Application
import android.content.*
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
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import com.twitter.Extractor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.mariotaku.kpreferences.get
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.okhttp3.OkHttpRestClient
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import org.mariotaku.twidere.extension.model.load
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.taskcontroller.refresh.JobSchedulerRefreshTaskController
import org.mariotaku.twidere.taskcontroller.refresh.LegacyRefreshTaskController
import org.mariotaku.twidere.taskcontroller.refresh.RefreshTaskController
import org.mariotaku.twidere.taskcontroller.sync.JobSchedulerSyncController
import org.mariotaku.twidere.taskcontroller.sync.LegacySyncController
import org.mariotaku.twidere.taskcontroller.sync.SyncTaskController
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.cache.DiskLRUFileCache
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.lang.SingletonHolder
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.media.TwidereMediaDownloader
import org.mariotaku.twidere.util.net.TwidereDns
import org.mariotaku.twidere.util.notification.ContentNotificationManager
import org.mariotaku.twidere.util.preference.PreferenceChangeNotifier
import org.mariotaku.twidere.util.sync.DataSyncProvider
import org.mariotaku.twidere.util.sync.SyncPreferences
import java.io.File
import javax.inject.Singleton

@Module
class GeneralModule(private val application: Application) {

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
    fun externalThemeManager(preferences: SharedPreferences, notifier: PreferenceChangeNotifier):
            ExternalThemeManager {
        val manager = ExternalThemeManager(application, preferences)
        notifier.register(KEY_EMOJI_SUPPORT) {
            manager.reloadEmojiPreferences()
        }
        val packageFilter = IntentFilter()
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        application.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
                val packages = application.packageManager.getPackagesForUid(uid)
                if (manager.emojiPackageName in packages) {
                    manager.reloadEmojiPreferences()
                }
            }
        }, packageFilter)
        return manager
    }

    @Provides
    @Singleton
    fun notificationManagerWrapper(): NotificationManagerWrapper {
        return NotificationManagerWrapper(application)
    }

    @Provides
    @Singleton
    fun sharedPreferences(): SharedPreferences {
        return application.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun preferenceChangeNotifier(preferences: SharedPreferences): PreferenceChangeNotifier {
        return PreferenceChangeNotifier(preferences)
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
    fun restHttpClient(prefs: SharedPreferences, dns: Dns, connectionPool: ConnectionPool,
            cache: Cache, notifier: PreferenceChangeNotifier): RestHttpClient {
        val conf = HttpClientFactory.HttpClientConfiguration(prefs)
        val client = HttpClientFactory.createRestHttpClient(conf, dns, connectionPool, cache)
        notifier.register(KEY_ENABLE_PROXY, KEY_PROXY_HOST, KEY_PROXY_PORT, KEY_PROXY_TYPE,
                KEY_PROXY_USERNAME, KEY_PROXY_PASSWORD, KEY_CONNECTION_TIMEOUT,
                KEY_RETRY_ON_NETWORK_ISSUE) changed@ {
            if (client !is OkHttpRestClient) return@changed
            val builder = OkHttpClient.Builder()
            HttpClientFactory.initOkHttpClient(HttpClientFactory.HttpClientConfiguration(prefs),
                    builder, dns, connectionPool, cache)
            client.client = builder.build()
        }

        return client
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
        return AsyncTwitterWrapper(application, preferences, notificationManagerWrapper)
    }

    @Provides
    @Singleton
    fun readStateManager(): ReadStateManager {
        return ReadStateManager(application)
    }

    @Provides
    @Singleton
    fun contentNotificationManager(activityTracker: ActivityTracker, userColorNameManager: UserColorNameManager,
            notificationManagerWrapper: NotificationManagerWrapper, preferences: SharedPreferences,
            notifier: PreferenceChangeNotifier): ContentNotificationManager {
        val manager = ContentNotificationManager(application, activityTracker, userColorNameManager,
                notificationManagerWrapper, preferences)
        notifier.register(KEY_NAME_FIRST, KEY_I_WANT_MY_STARS_BACK) {
            manager.updatePreferences()
        }
        return manager
    }

    @Provides
    @Singleton
    fun mediaPreloader(preferences: SharedPreferences, notifier: PreferenceChangeNotifier): MediaPreloader {
        val preloader = MediaPreloader(application)
        preloader.reloadOptions(preferences)
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        preloader.isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        notifier.register(KEY_MEDIA_PRELOAD, KEY_PRELOAD_WIFI_ONLY) {
            preloader.reloadOptions(preferences)
        }
        return preloader
    }

    @Provides
    @Singleton
    fun dns(preferences: SharedPreferences, notifier: PreferenceChangeNotifier): Dns {
        val dns = TwidereDns(application, preferences)
        notifier.register(KEY_DNS_SERVER, KEY_TCP_DNS_QUERY, KEY_BUILTIN_DNS_RESOLVER) {
            dns.reloadDnsSettings()
        }
        return dns
    }

    @Provides
    @Singleton
    fun mediaDownloader(client: RestHttpClient, thumbor: ThumborWrapper): MediaDownloader {
        return TwidereMediaDownloader(application, client, thumbor)
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
    @Singleton
    fun thumborWrapper(preferences: SharedPreferences, notifier: PreferenceChangeNotifier): ThumborWrapper {
        val thumbor = ThumborWrapper()
        thumbor.reloadSettings(preferences)
        notifier.register(KEY_THUMBOR_ENABLED, KEY_THUMBOR_ADDRESS, KEY_THUMBOR_SECURITY_KEY) {
            thumbor.reloadSettings(preferences)
        }
        return thumbor
    }

    @Provides
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }

    @Provides
    @Singleton
    fun autoRefreshController(preferences: SharedPreferences, notifier: PreferenceChangeNotifier): RefreshTaskController {
        val controller = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !preferences[autoRefreshCompatibilityModeKey]) {
            JobSchedulerRefreshTaskController(application, preferences)
        } else {
            LegacyRefreshTaskController(application, preferences)
        }
        notifier.register(KEY_REFRESH_INTERVAL) {
            controller.rescheduleAll()
        }
        return controller
    }

    @Provides
    @Singleton
    fun syncController(provider: DataSyncProvider): SyncTaskController {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return JobSchedulerSyncController(application, provider)
        }
        return LegacySyncController(application, provider)
    }

    @Provides
    @Singleton
    fun syncPreferences(): SyncPreferences {
        return SyncPreferences(application)
    }

    @Provides
    @Singleton
    fun taskCreator(preferences: SharedPreferences, activityTracker: ActivityTracker,
            dataSyncProvider: DataSyncProvider, bus: Bus): TaskServiceRunner {
        return TaskServiceRunner(application, preferences, activityTracker, dataSyncProvider, bus)
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
    fun etagCache(): ETagCache {
        return ETagCache(application)
    }

    @Provides
    fun locationManager(): LocationManager {
        return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    fun connectivityManager(): ConnectivityManager {
        return application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        val userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(application)
        return OkHttpDataSourceFactory(builder.build(), userAgent, null)
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
    fun mastodonApplicationRegistry(): MastodonApplicationRegistry {
        return MastodonApplicationRegistry(application)
    }

    private fun getCacheDir(dirName: String, sizeInBytes: Long): File {
        return Utils.getExternalCacheDir(application, dirName, sizeInBytes) ?:
                Utils.getInternalCacheDir(application, dirName)
    }

    companion object : SingletonHolder<GeneralModule, Application>(::GeneralModule)

}

