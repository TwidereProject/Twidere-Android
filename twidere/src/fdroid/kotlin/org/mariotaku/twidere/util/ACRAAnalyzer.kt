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

package org.mariotaku.twidere.util

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder
import org.acra.config.MailSenderConfigurationBuilder
import org.acra.data.StringFormat
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE
import org.mariotaku.twidere.TwidereConstants.TWIDERE_PROJECT_EMAIL
import org.mariotaku.twidere.activity.CrashReportDialogActivity
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.constant.themeBackgroundOptionKey
import org.mariotaku.twidere.constant.themeKey


/**
 * Created by mariotaku on 2017/5/8.
 */

class ACRAAnalyzer : Analyzer() {
    override fun log(priority: Int, tag: String, msg: String) {

    }

    override fun log(event: Event) {
    }

    override fun logException(throwable: Throwable) {
    }

    override fun init(application: Application) {
        val builder = CoreConfigurationBuilder(application)
        builder.setBuildConfigClass(BuildConfig::class.java).setReportFormat(StringFormat.JSON)
        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder::class.java)
                .setResText(R.string.message_app_crashed)
                .setResTheme(R.style.Theme_Twidere_NoDisplay_DayNight)
                .setReportDialogClass(CrashReportDialogActivity::class.java)
                .setEnabled(true)
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder::class.java)
                .setMailTo(TWIDERE_PROJECT_EMAIL)
                .setEnabled(true)
        ACRA.init(application, builder)
        val reporter = ACRA.getErrorReporter()
        reporter.putCustomData("debug", BuildConfig.DEBUG.toString())
        reporter.putCustomData("build.brand", Build.BRAND)
        reporter.putCustomData("build.device", Build.DEVICE)
        reporter.putCustomData("build.display", Build.DISPLAY)
        reporter.putCustomData("build.hardware", Build.HARDWARE)
        reporter.putCustomData("build.manufacturer", Build.MANUFACTURER)
        reporter.putCustomData("build.model", Build.MODEL)
        reporter.putCustomData("build.product", Build.PRODUCT)
        val am = AccountManager.get(application)
        try {
            am.addOnAccountsUpdatedListenerSafe(OnAccountsUpdateListener { accounts ->
                reporter.putCustomData("twidere.accounts", accounts.filter { it.type == ACCOUNT_TYPE }
                        .joinToString(transform = Account::name))
            }, updateImmediately = true)
        } catch (e: SecurityException) {
            // Permission managers (like some Xposed plugins) may block Twidere from getting accounts
        }
    }

    override fun preferencesChanged(preferences: SharedPreferences) {
        val reporter = ACRA.getErrorReporter()
        reporter.putCustomData("preferences.theme", preferences[themeKey])
        reporter.putCustomData("preferences.theme_bg_option", preferences[themeBackgroundOptionKey])
        reporter.putCustomData("preferences.theme_bg_alpha", preferences[themeBackgroundAlphaKey].toString())
    }

    override fun activityResumed(activity: Activity) {
        val reporter = ACRA.getErrorReporter()
        val intent = activity.intent ?: run {
            reporter.putCustomData("last_activity", "null intent")
            return
        }
        reporter.putCustomData("last_activity", "${intent.action}: ${intent.data} (${activity.javaClass.simpleName})")
    }

}
