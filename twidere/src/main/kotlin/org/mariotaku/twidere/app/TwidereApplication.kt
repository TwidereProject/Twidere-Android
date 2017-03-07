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

import android.app.Application
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.support.multidex.MultiDex
import com.bumptech.glide.Glide
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.task
import okhttp3.Dns
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.mediaviewer.library.MediaDownloader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.Constants.KEY_USAGE_STATISTICS
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AssistLauncherActivity
import org.mariotaku.twidere.activity.MainActivity
import org.mariotaku.twidere.activity.MainHondaJOJOActivity
import org.mariotaku.twidere.constant.apiLastChangeKey
import org.mariotaku.twidere.constant.bugReportsKey
import org.mariotaku.twidere.constant.defaultFeatureLastUpdated
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.content.TwidereSQLiteOpenHelper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.net.TwidereDns
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.refresh.AutoRefreshController
import org.mariotaku.twidere.util.sync.SyncController
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwidereApplication : Application(), Constants, OnSharedPreferenceChangeListener {

    @Inject
    lateinit internal var activityTracker: ActivityTracker
    @Inject
    lateinit internal var restHttpClient: RestHttpClient
    @Inject
    lateinit internal var dns: Dns
    @Inject
    lateinit internal var mediaDownloader: MediaDownloader
    @Inject
    lateinit internal var defaultFeatures: DefaultFeatures
    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager
    @Inject
    lateinit internal var kPreferences: KPreferences
    @Inject
    lateinit internal var autoRefreshController: AutoRefreshController
    @Inject
    lateinit internal var syncController: SyncController
    @Inject
    lateinit internal var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit internal var mediaPreloader: MediaPreloader
    @Inject
    lateinit internal var contentNotificationManager: ContentNotificationManager
    @Inject
    lateinit internal var thumbor: ThumborWrapper

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


    val sqLiteDatabase: SQLiteDatabase by lazy {
        StrictModeUtils.checkDiskIO()
        sqLiteOpenHelper.writableDatabase
    }

    val sqLiteOpenHelper: SQLiteOpenHelper by lazy {
        TwidereSQLiteOpenHelper(this, Constants.DATABASES_NAME, Constants.DATABASES_VERSION)
    }

    override fun onCreate() {
        instance = this
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
        }
        super.onCreate()
        startKovenant()
        initializeAsyncTask()
        initDebugMode()
        initBugReport()

        updateEasterEggIcon()

        migrateUsageStatisticsPreferences()
        GeneralComponentHelper.build(this).inject(this)

        autoRefreshController.appStarted()
        syncController.appStarted()
        extraFeaturesService.appStarted()

        registerActivityLifecycleCallbacks(activityTracker)

        listenExternalThemeChange()

        loadDefaultFeatures()
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
                val packages = packageManager.getPackagesForUid(uid)
                val manager = externalThemeManager
                if (ArrayUtils.contains(packages, manager.emojiPackageName)) {
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

    private fun migrateUsageStatisticsPreferences() {
        val preferences = sharedPreferences
        val hasUsageStatistics = preferences.contains(KEY_USAGE_STATISTICS)
        if (hasUsageStatistics) return
        if (preferences.contains(KEY_UCD_DATA_PROFILING) || preferences.contains(KEY_SPICE_DATA_PROFILING)) {
            val prevUsageEnabled = preferences.getBoolean(KEY_UCD_DATA_PROFILING, false) || preferences.getBoolean(KEY_SPICE_DATA_PROFILING, false)
            val editor = preferences.edit()
            editor.putBoolean(KEY_USAGE_STATISTICS, prevUsageEnabled)
            editor.remove(KEY_UCD_DATA_PROFILING)
            editor.remove(KEY_SPICE_DATA_PROFILING)
            editor.apply()
        }
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        return@lazy prefs
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
            KEY_CONSUMER_KEY, KEY_CONSUMER_SECRET, KEY_API_URL_FORMAT, KEY_CREDENTIALS_TYPE,
            KEY_SAME_OAUTH_SIGNING_URL, KEY_THUMBOR_ENABLED, KEY_THUMBOR_ADDRESS, KEY_THUMBOR_SECURITY_KEY -> {
                preferences[apiLastChangeKey] = System.currentTimeMillis()
            }
            KEY_EMOJI_SUPPORT -> {
                externalThemeManager.reloadEmojiPreferences()
            }
            KEY_THUMBOR_ADDRESS, KEY_THUMBOR_ENABLED, KEY_THUMBOR_SECURITY_KEY -> {
                thumbor.reloadSettings(preferences)
            }
            KEY_MEDIA_PRELOAD, KEY_PRELOAD_WIFI_ONLY -> {
                mediaPreloader.reloadOptions(preferences)
            }
            KEY_NAME_FIRST, KEY_I_WANT_MY_STARS_BACK -> {
                contentNotificationManager.updatePreferences()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKovenant()
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

    }

    companion object {

        private val KEY_UCD_DATA_PROFILING = "ucd_data_profiling"
        private val KEY_SPICE_DATA_PROFILING = "spice_data_profiling"
        private val KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized"
        var instance: TwidereApplication? = null
            private set

        fun getInstance(context: Context): TwidereApplication {
            return context.applicationContext as TwidereApplication
        }
    }
}
