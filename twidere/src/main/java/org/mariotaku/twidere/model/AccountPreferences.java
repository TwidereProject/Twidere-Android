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

package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;

public class AccountPreferences implements Constants {

    private final Context mContext;
    private final AccountId mAccountId;
    private final SharedPreferences mPreferences;

    public AccountPreferences(final Context context, final AccountId accountId) {
        mContext = context;
        mAccountId = accountId;
        final String name = ACCOUNT_PREFERENCES_NAME_PREFIX + accountId;
        mPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public AccountId getAccountId() {
        return mAccountId;
    }

    public int getDefaultNotificationLightColor() {
        final ParcelableAccount a = ParcelableAccountUtils.getAccount(mContext, mAccountId);
        if (a != null) {
            return a.color;
        } else {
            return ContextCompat.getColor(mContext, R.color.branding_color);
        }
    }

    public int getDirectMessagesNotificationType() {
        return mPreferences.getInt(KEY_NOTIFICATION_TYPE_DIRECT_MESSAGES, DEFAULT_NOTIFICATION_TYPE_DIRECT_MESSAGES);
    }

    public int getHomeTimelineNotificationType() {
        return mPreferences.getInt(KEY_NOTIFICATION_TYPE_HOME, DEFAULT_NOTIFICATION_TYPE_HOME);
    }

    public int getMentionsNotificationType() {
        return mPreferences.getInt(KEY_NOTIFICATION_TYPE_MENTIONS, DEFAULT_NOTIFICATION_TYPE_MENTIONS);
    }

    public int getNotificationLightColor() {
        return mPreferences.getInt(KEY_NOTIFICATION_LIGHT_COLOR, getDefaultNotificationLightColor());
    }

    public Uri getNotificationRingtone() {
        final String ringtone = mPreferences.getString(KEY_NOTIFICATION_RINGTONE, null);
        if (TextUtils.isEmpty(ringtone)) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            return Uri.parse(ringtone);
        }
    }

    public boolean isAutoRefreshDirectMessagesEnabled() {
        return mPreferences.getBoolean(KEY_AUTO_REFRESH_DIRECT_MESSAGES, DEFAULT_AUTO_REFRESH_DIRECT_MESSAGES);
    }

    public boolean isAutoRefreshEnabled() {
        return mPreferences.getBoolean(KEY_AUTO_REFRESH, DEFAULT_AUTO_REFRESH);
    }

    public boolean isAutoRefreshHomeTimelineEnabled() {
        return mPreferences.getBoolean(KEY_AUTO_REFRESH_HOME_TIMELINE, DEFAULT_AUTO_REFRESH_HOME_TIMELINE);
    }

    public boolean isAutoRefreshMentionsEnabled() {
        return mPreferences.getBoolean(KEY_AUTO_REFRESH_MENTIONS, DEFAULT_AUTO_REFRESH_MENTIONS);
    }

    public boolean isAutoRefreshTrendsEnabled() {
        return mPreferences.getBoolean(KEY_AUTO_REFRESH_TRENDS, DEFAULT_AUTO_REFRESH_TRENDS);
    }

    public boolean isStreamingEnabled() {
        return mPreferences.getBoolean(KEY_ENABLE_STREAMING, false);
    }

    public boolean isDirectMessagesNotificationEnabled() {
        return mPreferences.getBoolean(KEY_DIRECT_MESSAGES_NOTIFICATION, DEFAULT_DIRECT_MESSAGES_NOTIFICATION);
    }

    public boolean isHomeTimelineNotificationEnabled() {
        return mPreferences.getBoolean(KEY_HOME_TIMELINE_NOTIFICATION, DEFAULT_HOME_TIMELINE_NOTIFICATION);
    }

    public boolean isInteractionsNotificationEnabled() {
        return mPreferences.getBoolean(KEY_MENTIONS_NOTIFICATION, DEFAULT_MENTIONS_NOTIFICATION);
    }

    public boolean isNotificationFollowingOnly() {
        return mPreferences.getBoolean(KEY_NOTIFICATION_FOLLOWING_ONLY, false);
    }

    public boolean isNotificationMentionsOnly() {
        return mPreferences.getBoolean(KEY_NOTIFICATION_MENTIONS_ONLY, false);
    }

    public boolean isNotificationEnabled() {
        return mPreferences.getBoolean(KEY_NOTIFICATION, DEFAULT_NOTIFICATION);
    }

    public static AccountPreferences getAccountPreferences(final AccountPreferences[] prefs, final AccountId accountId) {
        for (final AccountPreferences pref : prefs) {
            if (pref.getAccountId() == accountId) return pref;
        }
        return null;
    }

    public static AccountPreferences[] getAccountPreferences(final Context context, final AccountId[] accountIds) {
        if (context == null || accountIds == null) return null;
        final AccountPreferences[] preferences = new AccountPreferences[accountIds.length];
        for (int i = 0, j = preferences.length; i < j; i++) {
            preferences[i] = new AccountPreferences(context, accountIds[i]);
        }
        return preferences;
    }

    @NonNull
    public static AccountId[] getAutoRefreshEnabledAccountIds(final Context context, final AccountId[] accountIds) {
        if (context == null || accountIds == null) return new AccountId[0];
        final AccountId[] temp = new AccountId[accountIds.length];
        int i = 0;
        for (final AccountId accountId : accountIds) {
            if (new AccountPreferences(context, accountId).isAutoRefreshEnabled()) {
                temp[i++] = accountId;
            }
        }
        final AccountId[] enabledIds = new AccountId[i];
        System.arraycopy(temp, 0, enabledIds, 0, i);
        return enabledIds;
    }

    @NonNull
    public static AccountPreferences[] getNotificationEnabledPreferences(final Context context, final AccountId[] accountIds) {
        if (context == null || accountIds == null) return new AccountPreferences[0];
        final AccountPreferences[] temp = new AccountPreferences[accountIds.length];
        int i = 0;
        for (final AccountId accountId : accountIds) {
            final AccountPreferences preference = new AccountPreferences(context, accountId);
            if (preference.isNotificationEnabled()) {
                temp[i++] = preference;
            }
        }
        final AccountPreferences[] enabledIds = new AccountPreferences[i];
        System.arraycopy(temp, 0, enabledIds, 0, i);
        return enabledIds;
    }

    public static boolean isNotificationHasLight(final int flags) {
        return (flags & VALUE_NOTIFICATION_FLAG_LIGHT) != 0;
    }

    public static boolean isNotificationHasRingtone(final int flags) {
        return (flags & VALUE_NOTIFICATION_FLAG_RINGTONE) != 0;
    }

    public static boolean isNotificationHasVibration(final int flags) {
        return (flags & VALUE_NOTIFICATION_FLAG_VIBRATION) != 0;
    }
}
