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

package org.mariotaku.twidere.app

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Application
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Looper
import androidx.multidex.MultiDex
import com.bumptech.glide.Glide
import nl.komponents.kovenant.task
import okhttp3.Dns
import org.apache.commons.lang3.concurrent.ConcurrentUtils
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.isCurrentThreadCompat
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AssistLauncherActivity
import org.mariotaku.twidere.activity.MainActivity
import org.mariotaku.twidere.activity.MainHondaJOJOActivity
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.extension.firstLanguage
import org.mariotaku.twidere.extension.model.loadRemoteSettings
import org.mariotaku.twidere.extension.model.save
import org.mariotaku.twidere.extension.setLocale
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.receiver.ConnectivityStateReceiver
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.content.TwidereSQLiteOpenHelper
import org.mariotaku.twidere.util.dagger.ApplicationModule
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.emoji.EmojioneTranslator
import org.mariotaku.twidere.util.kovenant.startKovenant
import org.mariotaku.twidere.util.kovenant.stopKovenant
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.net.TwidereDns
import org.mariotaku.twidere.util.notification.ContentNotificationManager
import org.mariotaku.twidere.util.notification.NotificationChannelsManager
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.refresh.AutoRefreshController
import org.mariotaku.twidere.util.sync.DataSyncProvider
import org.mariotaku.twidere.util.sync.SyncController
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwidereApplication : Application(), OnSharedPreferenceChangeListener {

    @Inject
    internal lateinit var activityTracker: ActivityTracker
    @Inject
    internal lateinit var restHttpClient: RestHttpClient
    @Inject
    internal lateinit var dns: Dns
    @Inject
    internal lateinit var mediaDownloader: MediaDownloader
    @Inject
    internal lateinit var defaultFeatures: DefaultFeatures
    @Inject
    internal lateinit var externalThemeManager: ExternalThemeManager
    @Inject
    internal lateinit var kPreferences: KPreferences
    @Inject
    internal lateinit var autoRefreshController: AutoRefreshController
    @Inject
    internal lateinit var syncController: SyncController
    @Inject
    internal lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    internal lateinit var promotionService: PromotionService
    @Inject
    internal lateinit var mediaPreloader: MediaPreloader
    @Inject
    internal lateinit var contentNotificationManager: ContentNotificationManager
    @Inject
    internal lateinit var thumbor: ThumborWrapper

    val sqLiteDatabase: SQLiteDatabase by lazy {
        StrictModeUtils.checkDiskIO()
        sqLiteOpenHelper.writableDatabase
    }

    val sqLiteOpenHelper: SQLiteOpenHelper by lazy {
        TwidereSQLiteOpenHelper(this, Constants.DATABASES_NAME, Constants.DATABASES_VERSION)
    }

    val applicationModule: ApplicationModule by lazy {
        ApplicationModule(this)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        return@lazy prefs
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        instance = this
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
        }
        super.onCreate()
        applyLanguageSettings()
        startKovenant()
        initializeAsyncTask()
        initDebugMode()
        initBugReport()
        EmojioneTranslator.init(this)
        NotificationChannelsManager.initialize(this)

        updateEasterEggIcon()

        GeneralComponent.get(this).inject(this)

        autoRefreshController.appStarted()
        syncController.appStarted()
        extraFeaturesService.appStarted()
        promotionService.appStarted()

        registerActivityLifecycleCallbacks(activityTracker)
        registerReceiver(ConnectivityStateReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        listenExternalThemeChange()

        loadDefaultFeatures()

        Analyzer.preferencesChanged(sharedPreferences)
        DataSyncProvider.Factory.notifyUpdate(this)

        AccountManager.get(this).addOnAccountsUpdatedListenerSafe(OnAccountsUpdateListener {
            NotificationChannelsManager.updateAccountChannelsAndGroups(this)
        }, updateImmediately = true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        applyLanguageSettings()
        super.onConfigurationChanged(newConfig)
    }

    override fun onTrimMemory(level: Int) {
        Glide.with(this).onTrimMemory(level)
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        Glide.with(this).onLowMemory()
        super.onLowMemory()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_REFRESH_INTERVAL -> {
                autoRefreshController.rescheduleAll()
            }
            KEY_ENABLE_PROXY, KEY_PROXY_HOST, KEY_PROXY_PORT, KEY_PROXY_TYPE, KEY_PROXY_USERNAME,
            KEY_PROXY_PASSWORD, KEY_CONNECTION_TIMEOUT, KEY_RETRY_ON_NETWORK_ISSUE -> {
                HttpClientFactory.reloadConnectivitySettings(this)
            }
            KEY_DNS_SERVER, KEY_TCP_DNS_QUERY, KEY_BUILTIN_DNS_RESOLVER -> {
                reloadDnsSettings()
            }
            KEY_CREDENTIALS_TYPE, KEY_API_URL_FORMAT, KEY_CONSUMER_KEY, KEY_CONSUMER_SECRET,
            KEY_SAME_OAUTH_SIGNING_URL -> {
                preferences[apiLastChangeKey] = System.currentTimeMillis()
            }
            KEY_EMOJI_SUPPORT -> {
                externalThemeManager.reloadEmojiPreferences()
            }
            KEY_THUMBOR_ENABLED, KEY_THUMBOR_ADDRESS, KEY_THUMBOR_SECURITY_KEY -> {
                thumbor.reloadSettings(preferences)
            }
            KEY_MEDIA_PRELOAD, KEY_PRELOAD_WIFI_ONLY -> {
                mediaPreloader.reloadOptions(preferences)
            }
            KEY_NAME_FIRST, KEY_I_WANT_MY_STARS_BACK -> {
                contentNotificationManager.updatePreferences()
            }
            KEY_OVERRIDE_LANGUAGE -> {
                applyLanguageSettings()
            }
        }
        Analyzer.preferencesChanged(preferences)
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKovenant()
    }

    private fun applyLanguageSettings() {
        val locale = sharedPreferences[overrideLanguageKey] ?: Resources.getSystem().
                firstLanguage ?: return
        resources.setLocale(locale)
    }

    private fun loadDefaultFeatures() {
        val lastUpdated = kPreferences[defaultFeatureLastUpdated]
        if (lastUpdated > 0 && TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastUpdated) < 12) {
            return
        }
        task {
            defaultFeatures.loadRemoteSettings(restHttpClient)
        }.success {
            defaultFeatures.save(sharedPreferences)
            DebugLog.d(LOGTAG, "Loaded remote features")
        }.fail {
            DebugLog.w(LOGTAG, "Unable to load remote features", it)
        }.always {
            kPreferences[defaultFeatureLastUpdated] = System.currentTimeMillis()
        }
    }

    private fun listenExternalThemeChange() {
        val packageFilter = IntentFilter()
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
                val packages = packageManager.getPackagesForUid(uid).orEmpty()
                val manager = externalThemeManager
                if (manager.emojiPackageName in packages) {
                    manager.reloadEmojiPreferences()
                }
            }
        }, packageFilter)
    }

    private fun updateEasterEggIcon() {
        val pm = packageManager
        val main = ComponentName(this, MainActivity::class.java)
        val main2 = ComponentName(this, MainHondaJOJOActivity::class.java)
        val mainDisabled = pm.getComponentEnabledSetting(main) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        val main2Disabled = pm.getComponentEnabledSetting(main2) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        val noEntry = mainDisabled && main2Disabled
        if (noEntry) {
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
        } else if (!mainDisabled) {
            pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
        }
        if (!Utils.isComposeNowSupported(this)) {
            val assist = ComponentName(this, AssistLauncherActivity::class.java)
            pm.setComponentEnabledSetting(assist, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
        }
    }


    private fun initDebugMode() {
        DebugModeUtils.initForApplication(this)
    }

    private fun initBugReport() {
        if (!sharedPreferences[bugReportsKey]) return
        Analyzer.implementation = ServiceLoader.load(Analyzer::class.java).firstOrNull()
        Analyzer.init(this)
    }

    private fun reloadDnsSettings() {
        (dns as? TwidereDns)?.reloadDnsSettings()
    }

    private fun initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask::class.java.name)
        } catch (ignore: ClassNotFoundException) {
        }
        TaskStarter.setDefaultExecutor(AsyncTask.SERIAL_EXECUTOR)
        val executor = Executors.newSingleThreadExecutor()
        LoganSquareMapperFinder.setDefaultExecutor(object : LoganSquareMapperFinder.FutureExecutor {
            override fun <T> submit(callable: Callable<T>): Future<T> {
                if (Looper.getMainLooper().isCurrentThreadCompat) {
                    return ConcurrentUtils.constantFuture(callable.call())
                }
                return executor.submit(callable)
            }
        })
    }

    companion object {

        private const val KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized"
        var instance: TwidereApplication? = null
            private set

        fun getInstance(context: Context): TwidereApplication {
            return context.applicationContext as TwidereApplication
        }
    }
}
