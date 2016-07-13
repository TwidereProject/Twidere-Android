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

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.ActionBarContextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.AssistLauncherActivity;
import org.mariotaku.twidere.activity.MainActivity;
import org.mariotaku.twidere.activity.MainHondaJOJOActivity;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.BugReporter;
import org.mariotaku.twidere.util.DebugModeUtils;
import org.mariotaku.twidere.util.ExternalThemeManager;
import org.mariotaku.twidere.util.HttpClientFactory;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereBugReporter;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.TwidereSQLiteOpenHelper;
import org.mariotaku.twidere.util.dagger.DependencyHolder;
import org.mariotaku.twidere.util.net.TwidereDns;
import org.mariotaku.twidere.util.theme.ActionBarContextViewViewProcessor;
import org.mariotaku.twidere.util.theme.FloatingActionButtonViewProcessor;
import org.mariotaku.twidere.util.theme.FontFamilyTagProcessor;
import org.mariotaku.twidere.util.theme.IconActionButtonTagProcessor;
import org.mariotaku.twidere.util.theme.ImageViewViewProcessor;
import org.mariotaku.twidere.util.theme.MaterialEditTextViewProcessor;
import org.mariotaku.twidere.util.theme.OptimalLinkColorTagProcessor;
import org.mariotaku.twidere.util.theme.ProfileImageViewViewProcessor;
import org.mariotaku.twidere.util.theme.ProgressWheelViewProcessor;
import org.mariotaku.twidere.util.theme.SwipeRefreshLayoutViewProcessor;
import org.mariotaku.twidere.util.theme.TabPagerIndicatorViewProcessor;
import org.mariotaku.twidere.util.theme.TextViewViewProcessor;
import org.mariotaku.twidere.util.theme.TimelineContentTextViewViewProcessor;
import org.mariotaku.twidere.view.ProfileImageView;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.ThemedMultiValueSwitch;
import org.mariotaku.twidere.view.TimelineContentTextView;

public class TwidereApplication extends Application implements Constants,
        OnSharedPreferenceChangeListener {

    private static final String KEY_UCD_DATA_PROFILING = "ucd_data_profiling";
    private static final String KEY_SPICE_DATA_PROFILING = "spice_data_profiling";
    private static final String KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized";
    private static TwidereApplication sInstance;

    private Handler mHandler;
    private SharedPreferences mPreferences;
    private SQLiteOpenHelper mSQLiteOpenHelper;
    private SQLiteDatabase mDatabase;

    private ProfileImageViewViewProcessor mProfileImageViewViewProcessor;
    private FontFamilyTagProcessor mFontFamilyTagProcessor;

    @NonNull
    public static TwidereApplication getInstance(@NonNull final Context context) {
        return (TwidereApplication) context.getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void initKeyboardShortcuts() {
        final SharedPreferences preferences = getSharedPreferences();
        if (!preferences.getBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, false)) {
//            getApplicationModule().getKeyboardShortcutsHandler().reset();
            preferences.edit().putBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, true).apply();
        }
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

    @Override
    public void onCreate() {
        sInstance = this;
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy();
        }
        final SharedPreferences preferences = getSharedPreferences();
        resetTheme(preferences);
        super.onCreate();

        mProfileImageViewViewProcessor = new ProfileImageViewViewProcessor();
        mFontFamilyTagProcessor = new FontFamilyTagProcessor();

        ATE.registerViewProcessor(TabPagerIndicator.class, new TabPagerIndicatorViewProcessor());
        ATE.registerViewProcessor(FloatingActionButton.class, new FloatingActionButtonViewProcessor());
        ATE.registerViewProcessor(ActionBarContextView.class, new ActionBarContextViewViewProcessor());
        ATE.registerViewProcessor(SwipeRefreshLayout.class, new SwipeRefreshLayoutViewProcessor());
        ATE.registerViewProcessor(TimelineContentTextView.class, new TimelineContentTextViewViewProcessor());
        ATE.registerViewProcessor(TextView.class, new TextViewViewProcessor());
        ATE.registerViewProcessor(ImageView.class, new ImageViewViewProcessor());
        ATE.registerViewProcessor(MaterialEditText.class, new MaterialEditTextViewProcessor());
        ATE.registerViewProcessor(ProgressWheel.class, new ProgressWheelViewProcessor());
        ATE.registerViewProcessor(ProfileImageView.class, mProfileImageViewViewProcessor);
        ATE.registerTagProcessor(OptimalLinkColorTagProcessor.TAG, new OptimalLinkColorTagProcessor());
        ATE.registerTagProcessor(FontFamilyTagProcessor.TAG, mFontFamilyTagProcessor);
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR,
                new IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR));
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_ACTIVATED,
                new IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_ACTIVATED));
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_DISABLED,
                new IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_DISABLED));
        ATE.registerTagProcessor(ThemedMultiValueSwitch.PREFIX_TINT, new ThemedMultiValueSwitch.TintTagProcessor());


        mProfileImageViewViewProcessor.setStyle(Utils.getProfileImageStyle(preferences));
        mFontFamilyTagProcessor.setFontFamily(ThemeUtils.getThemeFontFamily(preferences));

        final int themeColor = preferences.getInt(KEY_THEME_COLOR, ContextCompat.getColor(this,
                R.color.branding_color));
        if (!ATE.config(this, VALUE_THEME_NAME_LIGHT).isConfigured()) {
            //noinspection WrongConstant
            ATE.config(this, VALUE_THEME_NAME_LIGHT)
                    .primaryColor(themeColor)
                    .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK))
                    .coloredActionBar(true)
                    .coloredStatusBar(true)
                    .commit();
        }
        if (!ATE.config(this, VALUE_THEME_NAME_DARK).isConfigured()) {
            ATE.config(this, VALUE_THEME_NAME_DARK)
                    .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE))
                    .coloredActionBar(false)
                    .coloredStatusBar(true)
                    .statusBarColor(Color.BLACK)
                    .commit();
        }
        if (!ATE.config(this, null).isConfigured()) {
            ATE.config(this, null)
                    .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE))
                    .coloredActionBar(false)
                    .coloredStatusBar(false)
                    .commit();
        }
        initializeAsyncTask();
        initDebugMode();
        initBugReport();
        mHandler = new Handler();

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
        if (!Utils.isComposeNowSupported(this)) {
            final ComponentName assist = new ComponentName(this, AssistLauncherActivity.class);
            pm.setComponentEnabledSetting(assist, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        Utils.startRefreshServiceIfNeeded(this);

        DependencyHolder holder = DependencyHolder.get(this);
        registerActivityLifecycleCallbacks(holder.getActivityTracker());

        final IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                final String[] packages = getPackageManager().getPackagesForUid(uid);
                DependencyHolder holder = DependencyHolder.get(context);
                final ExternalThemeManager manager = holder.getExternalThemeManager();
                if (ArrayUtils.contains(packages, manager.getEmojiPackageName())) {
                    manager.reloadEmojiPreferences();
                }
            }
        }, packageFilter);
    }

    private void initDebugMode() {
        DebugModeUtils.initForApplication(this);
    }

    private void initBugReport() {
        final SharedPreferences preferences = getSharedPreferences();
        if (!preferences.getBoolean(KEY_BUG_REPORTS, BuildConfig.DEBUG)) return;
        BugReporter.setImplementation(new TwidereBugReporter());
        BugReporter.init(this);
    }

    private SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        return mPreferences;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        final DependencyHolder holder = DependencyHolder.get(this);
        super.onLowMemory();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        switch (key) {
            case KEY_REFRESH_INTERVAL: {
                stopService(new Intent(this, RefreshService.class));
                Utils.startRefreshServiceIfNeeded(this);
                break;
            }
            case KEY_ENABLE_PROXY:
            case KEY_PROXY_HOST:
            case KEY_PROXY_PORT:
            case KEY_PROXY_TYPE:
            case KEY_PROXY_USERNAME:
            case KEY_PROXY_PASSWORD:
            case KEY_CONNECTION_TIMEOUT:
            case KEY_RETRY_ON_NETWORK_ISSUE: {
                HttpClientFactory.reloadConnectivitySettings(this);
                break;
            }
            case KEY_DNS_SERVER:
            case KEY_TCP_DNS_QUERY:
            case KEY_BUILTIN_DNS_RESOLVER: {
                reloadDnsSettings();
                break;
            }
            case KEY_CONSUMER_KEY:
            case KEY_CONSUMER_SECRET:
            case KEY_API_URL_FORMAT:
            case KEY_AUTH_TYPE:
            case KEY_SAME_OAUTH_SIGNING_URL:
            case KEY_THUMBOR_ENABLED:
            case KEY_THUMBOR_ADDRESS:
            case KEY_THUMBOR_SECURITY_KEY: {
                final Editor editor = preferences.edit();
                editor.putLong(KEY_API_LAST_CHANGE, System.currentTimeMillis());
                editor.apply();
                break;
            }
            case KEY_EMOJI_SUPPORT: {
                DependencyHolder.get(this).getExternalThemeManager().reloadEmojiPreferences();
                break;
            }
            case KEY_THEME: {
                resetTheme(preferences);
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK);
                break;
            }
            case KEY_THEME_BACKGROUND: {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK);
                break;
            }
            case KEY_PROFILE_IMAGE_STYLE: {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK);
                mProfileImageViewViewProcessor.setStyle(Utils.getProfileImageStyle(preferences.getString(key, null)));
                break;
            }
            case KEY_THEME_FONT_FAMILY: {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK);
                mFontFamilyTagProcessor.setFontFamily(ThemeUtils.getThemeFontFamily(preferences));
                break;
            }
            case KEY_THEME_COLOR: {
                final int themeColor = preferences.getInt(key, ContextCompat.getColor(this,
                        R.color.branding_color));
                //noinspection WrongConstant
                ATE.config(this, VALUE_THEME_NAME_LIGHT)
                        .primaryColor(themeColor)
                        .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK))
                        .coloredActionBar(true)
                        .coloredStatusBar(true)
                        .commit();
                ATE.config(this, VALUE_THEME_NAME_DARK)
                        .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE))
                        .coloredActionBar(false)
                        .coloredStatusBar(true)
                        .statusBarColor(Color.BLACK)
                        .commit();
                ATE.config(this, null)
                        .accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK))
                        .coloredActionBar(false)
                        .coloredStatusBar(false)
                        .commit();
                break;
            }
        }
    }

    private void resetTheme(SharedPreferences preferences) {
        switch (ThemeUtils.getLocalNightMode(preferences)) {
            case AppCompatDelegate.MODE_NIGHT_AUTO: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
            }
            case AppCompatDelegate.MODE_NIGHT_YES: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            }
            default: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            }
        }
    }

    private void reloadDnsSettings() {
        DependencyHolder holder = DependencyHolder.get(this);
        final TwidereDns dns = holder.getDns();
        dns.reloadDnsSettings();
    }


    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (final ClassNotFoundException ignore) {
        }
    }

    @Nullable
    public static TwidereApplication getInstance() {
        return sInstance;
    }
}
