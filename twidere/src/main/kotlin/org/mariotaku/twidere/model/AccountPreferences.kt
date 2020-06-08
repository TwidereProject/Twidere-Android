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

package org.mariotaku.twidere.model

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.ContextCompat
import android.text.TextUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_PREFERENCES_NAME_PREFIX
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.constant.defaultAutoRefreshKey
import org.mariotaku.twidere.model.util.AccountUtils

class AccountPreferences(
        private val context: Context,
        private val preferences: SharedPreferences,
        val accountKey: UserKey
) {
    private val accountPreferences = getSharedPreferencesForAccount(context, accountKey)

    val defaultNotificationLightColor: Int
        get() {
            val a = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
            return a?.color ?: ContextCompat.getColor(context, R.color.branding_color)
        }

    val directMessagesNotificationType: Int
        get() = accountPreferences.getInt(KEY_NOTIFICATION_TYPE_DIRECT_MESSAGES,
                DEFAULT_NOTIFICATION_TYPE_DIRECT_MESSAGES)

    val homeTimelineNotificationType: Int
        get() = accountPreferences.getInt(KEY_NOTIFICATION_TYPE_HOME,
                DEFAULT_NOTIFICATION_TYPE_HOME)

    val mentionsNotificationType: Int
        get() = accountPreferences.getInt(KEY_NOTIFICATION_TYPE_MENTIONS,
                DEFAULT_NOTIFICATION_TYPE_MENTIONS)

    val notificationLightColor: Int
        get() = accountPreferences.getInt(KEY_NOTIFICATION_LIGHT_COLOR,
                defaultNotificationLightColor)

    val notificationRingtone: Uri
        get() {
            val ringtone = accountPreferences.getString(KEY_NOTIFICATION_RINGTONE, null)
            return if (TextUtils.isEmpty(ringtone)) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                Uri.parse(ringtone)
            }
        }

    val isAutoRefreshEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_AUTO_REFRESH, preferences[defaultAutoRefreshKey])

    val isAutoRefreshHomeTimelineEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_AUTO_REFRESH_HOME_TIMELINE,
                DEFAULT_AUTO_REFRESH_HOME_TIMELINE)

    val isAutoRefreshMentionsEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_AUTO_REFRESH_MENTIONS,
                DEFAULT_AUTO_REFRESH_MENTIONS)

    val isAutoRefreshDirectMessagesEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_AUTO_REFRESH_DIRECT_MESSAGES,
                DEFAULT_AUTO_REFRESH_DIRECT_MESSAGES)

    val isAutoRefreshTrendsEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_AUTO_REFRESH_TRENDS, DEFAULT_AUTO_REFRESH_TRENDS)

    val isDirectMessagesNotificationEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_DIRECT_MESSAGES_NOTIFICATION,
                DEFAULT_DIRECT_MESSAGES_NOTIFICATION)

    val isHomeTimelineNotificationEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_HOME_TIMELINE_NOTIFICATION,
                DEFAULT_HOME_TIMELINE_NOTIFICATION)

    val isInteractionsNotificationEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_MENTIONS_NOTIFICATION,
                DEFAULT_MENTIONS_NOTIFICATION)

    val isNotificationFollowingOnly: Boolean
        get() = accountPreferences.getBoolean(KEY_NOTIFICATION_FOLLOWING_ONLY, false)

    val isNotificationMentionsOnly: Boolean
        get() = accountPreferences.getBoolean(KEY_NOTIFICATION_MENTIONS_ONLY, false)

    val isNotificationEnabled: Boolean
        get() = accountPreferences.getBoolean(KEY_NOTIFICATION, DEFAULT_NOTIFICATION)

    companion object {

        fun getAccountPreferences(context: Context, preferences: SharedPreferences,
                accountKeys: Array<UserKey>): Array<AccountPreferences> {
            return Array(accountKeys.size) {
                AccountPreferences(context, preferences, accountKeys[it])
            }
        }

        fun isNotificationHasLight(flags: Int): Boolean {
            return VALUE_NOTIFICATION_FLAG_LIGHT in flags
        }

        fun isNotificationHasRingtone(flags: Int): Boolean {
            return VALUE_NOTIFICATION_FLAG_RINGTONE in flags
        }

        fun isNotificationHasVibration(flags: Int): Boolean {
            return VALUE_NOTIFICATION_FLAG_VIBRATION in flags
        }

        fun getSharedPreferencesForAccount(context: Context, accountKey: UserKey): SharedPreferences {
            return context.getSharedPreferences("$ACCOUNT_PREFERENCES_NAME_PREFIX${accountKey.sanitized()}",
                    Context.MODE_PRIVATE)
        }

        private fun UserKey.sanitized(): String {
            return toString().replace(Regex("[^\\w\\d@-_.]"), "_")
        }
    }
}
