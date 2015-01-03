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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class UserColorNameUtils implements TwidereConstants {

    private static LongSparseArray<Integer> sUserColors = new LongSparseArray<>();
    private static LongSparseArray<String> sUserNicknames = new LongSparseArray<>();

    private UserColorNameUtils() {
        throw new AssertionError();
    }

    public static void clearUserColor(final Context context, final long user_id) {
        if (context == null) return;
        sUserColors.remove(user_id);
        final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Long.toString(user_id));
        editor.apply();
    }

    public static void clearUserNickname(final Context context, final long user_id) {
        if (context == null) return;
        sUserNicknames.remove(user_id);
        final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Long.toString(user_id));
        editor.apply();
    }

    public static String getDisplayName(final Context context, final long userId, final String name,
                                        final String screenName) {
        return getDisplayName(context, userId, name, screenName, false);
    }

    public static String getDisplayName(final Context context, final ParcelableUser user) {
        return getDisplayName(context, user, false);
    }

    public static String getDisplayName(final Context context, final ParcelableUser user, final boolean ignoreCache) {
        return getDisplayName(context, user.id, user.name, user.screen_name, ignoreCache);
    }

    public static String getDisplayName(final Context context, final long userId, final String name,
                                        final String screenName, final boolean ignoreCache) {
        if (context == null) return null;
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final boolean nameFirst = prefs.getBoolean(KEY_NAME_FIRST, true);
        final boolean nicknameOnly = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NICKNAME_ONLY, false);
        return getDisplayName(context, userId, name, screenName, nameFirst, nicknameOnly, ignoreCache);
    }

    public static String getDisplayName(final Context context, final long user_id, final String name,
                                        final String screen_name, final boolean name_first, final boolean nickname_only) {
        return getDisplayName(context, user_id, name, screen_name, name_first, nickname_only, false);
    }

    public static String getDisplayName(final Context context, final ParcelableUser user,
                                        final boolean nameFirst, final boolean nicknameOnly,
                                        final boolean ignoreCache) {
        return getDisplayName(context, user.id, user.name, user.screen_name,
                nameFirst, nicknameOnly, ignoreCache);
    }


    public static String getDisplayName(final Context context, final ParcelableStatus status,
                                        final boolean nameFirst, final boolean nicknameOnly,
                                        final boolean ignoreCache) {
        return getDisplayName(context, status.user_id, status.user_name, status.user_screen_name,
                nameFirst, nicknameOnly, ignoreCache);
    }

    public static String getDisplayName(final Context context, final long userId, final String name,
                                        final String screenName, final boolean nameFirst,
                                        final boolean nicknameOnly, final boolean ignoreCache) {
        if (context == null) return null;
        final String nick = getUserNickname(context, userId, ignoreCache);
        final boolean nick_available = !isEmpty(nick);
        if (nicknameOnly && nick_available) return nick;
        if (!nick_available) return nameFirst && !isEmpty(name) ? name : "@" + screenName;
        return context.getString(R.string.name_with_nickname, nameFirst && !isEmpty(name) ? name : "@" + screenName,
                nick);
    }

    public static int getUserColor(final Context context, final long user_id) {
        return getUserColor(context, user_id, false);
    }

    public static int getUserColor(final Context context, final long userId, final boolean ignoreCache) {
        if (context == null || userId == -1) return Color.TRANSPARENT;
        if (!ignoreCache && sUserColors.indexOfKey(userId) >= 0) return sUserColors.get(userId);
        final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int color = prefs.getInt(Long.toString(userId), Color.TRANSPARENT);
        sUserColors.put(userId, color);
        return color;
    }

    public static String getUserNickname(final Context context, final long userId) {
        return getUserNickname(context, userId, false);
    }

    public static String getUserNickname(final Context context, final long userId, final boolean ignoreCache) {
        if (context == null || userId == -1) return null;
        if (!ignoreCache && LongSparseArrayUtils.hasKey(sUserNicknames, userId))
            return sUserNicknames.get(userId);
        final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final String nickname = prefs.getString(Long.toString(userId), null);
        sUserNicknames.put(userId, nickname);
        return nickname;
    }

    public static String getUserNickname(final Context context, final long user_id, final String name) {
        final String nick = getUserNickname(context, user_id);
        if (isEmpty(nick)) return name;
        final boolean nickname_only = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NICKNAME_ONLY, false);
        return nickname_only ? nick : context.getString(R.string.name_with_nickname, name, nick);
    }

    public static void initUserColor(final Context context) {
        if (context == null) return;
        final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        for (final Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            sUserColors.put(ParseUtils.parseLong(entry.getKey()),
                    ParseUtils.parseInt(ParseUtils.parseString(entry.getValue())));
        }
    }

    public static void registerOnUserColorChangedListener(final Context context,
                                                          final OnUserColorChangedListener listener) {

        final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public static void registerOnUserNicknameChangedListener(final Context context,
                                                             final OnUserNicknameChangedListener listener) {

        final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(new OnNickPreferenceChangeListener(listener));
    }

    public static void setUserColor(final Context context, final long user_id, final int color) {
        if (context == null || user_id == -1) return;
        sUserColors.put(user_id, color);
        final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(String.valueOf(user_id), color);
        editor.apply();
    }

    public static void setUserNickname(final Context context, final long user_id, final String nickname) {
        if (context == null || user_id == -1) return;
        sUserNicknames.put(user_id, nickname);
        final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(String.valueOf(user_id), nickname);
        editor.apply();
    }

    public static interface OnUserColorChangedListener {
        void onUserColorChanged(long userId, int color);
    }

    public static interface OnUserNicknameChangedListener {
        void onUserNicknameChanged(long userId, String nick);
    }

    private static final class OnColorPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final OnUserColorChangedListener mListener;

        OnColorPreferenceChangeListener(final OnUserColorChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            final long userId = ParseUtils.parseLong(key, -1);
            if (mListener != null) {
                mListener.onUserColorChanged(userId, sharedPreferences.getInt(key, 0));
            }
        }

    }

    private static final class OnNickPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final OnUserNicknameChangedListener mListener;

        OnNickPreferenceChangeListener(final OnUserNicknameChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            final long userId = ParseUtils.parseLong(key, -1);
            if (mListener != null) {
                mListener.onUserNicknameChanged(userId, sharedPreferences.getString(key, null));
            }
        }

    }
}
