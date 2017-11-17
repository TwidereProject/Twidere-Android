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
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.multidex.MultiDex
import com.bumptech.glide.Glide
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.task
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
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
import org.mariotaku.twidere.service.StreamingService
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.emoji.EmojiOneShortCodeMap
import org.mariotaku.twidere.util.notification.NotificationChannelsManager
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.refresh.AutoRefreshController
import org.mariotaku.twidere.util.sync.DataSyncProvider
import org.mariotaku.twidere.util.sync.SyncController
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwidereApplication : Application(), OnSharedPreferenceChangeListener {

    @Inject
    lateinit internal var activityTracker: ActivityTracker
    @Inject
    lateinit internal var restHttpClient: RestHttpClient
    @Inject
    lateinit internal var defaultFeatures: DefaultFeatures
    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager
    @Inject
    lateinit internal var autoRefreshController: AutoRefreshController
    @Inject
    lateinit internal var syncController: SyncController
    @Inject
    lateinit internal var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit internal var promotionService: PromotionService

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
        EmojiOneShortCodeMap.init(this)
        NotificationChannelsManager.initialize(this)

        updateEasterEggIcon()
        GeneralComponent.get(this).inject(this)

        autoRefreshController.appStarted()
        syncController.appStarted()
        extraFeaturesService.appStarted()
        promotionService.appStarted()

        registerActivityLifecycleCallbacks(activityTracker)
        registerReceiver(ConnectivityStateReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        loadDefaultFeatures()

        Analyzer.preferencesChanged(sharedPreferences)
        DataSyncProvider.Factory.notifyUpdate(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
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
            KEY_CREDENTIALS_TYPE, KEY_API_URL_FORMAT, KEY_CONSUMER_KEY, KEY_CONSUMER_SECRET,
            KEY_SAME_OAUTH_SIGNING_URL -> {
                preferences[apiLastChangeKey] = System.currentTimeMillis()
            }
            streamingEnabledKey.key, streamingPowerSavingKey.key,
            streamingNonMeteredNetworkKey.key -> {
                val streamingIntent = Intent(this, StreamingService::class.java)
                if (activityTracker.isHomeActivityLaunched) {
                    startService(streamingIntent)
                } else {
                    stopService(streamingIntent)
                }
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
        val lastUpdated = sharedPreferences[defaultFeatureLastUpdated]
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
            sharedPreferences[defaultFeatureLastUpdated] = System.currentTimeMillis()
        }
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

    private fun initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask::class.java.name)
        } catch (ignore: ClassNotFoundException) {
        }
    }

    companion object {

        var instance: TwidereApplication? = null
            private set

        fun getInstance(context: Context) = context.applicationContext as TwidereApplication
    }

}
