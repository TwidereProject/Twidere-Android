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

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.dagger.ApplicationModule;

import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public class UserColorNameManager implements TwidereConstants {

    private final LongSparseArray<Integer> mUserColors = new LongSparseArray<>();
    private final LongSparseArray<String> mUserNicknames = new LongSparseArray<>();
    private final SharedPreferences mColorPreferences, mNicknamePreferences;

    public UserColorNameManager(Context context) {
        mColorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNicknamePreferences = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
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

    public static UserColorNameManager getInstance(Context context) {
        return ApplicationModule.get(context).getUserColorNameManager();
    }

    public void clearUserColor(final long userId) {
        if (userId < 0) return;
        mUserColors.remove(userId);
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.remove(Long.toString(userId));
        editor.apply();
    }

    public void setUserColor(final long userId, final int color) {
        if (userId < 0) return;
        mUserColors.put(userId, color);
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.putInt(String.valueOf(userId), color);
        editor.apply();
    }

    public void setUserNickname(final long userId, final String nickname) {
        if (userId < 0) return;
        mUserNicknames.put(userId, nickname);
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        editor.putString(String.valueOf(userId), nickname);
        editor.apply();
    }

    public void clearUserNickname(final long userId) {
        if (userId < 0) return;
        mUserNicknames.remove(userId);
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        editor.remove(Long.toString(userId));
        editor.apply();
    }

    public String getDisplayName(final ParcelableUser user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(user.id, user.name, user.screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(final User user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(user.getId(), user.getName(), user.getScreenName(), nameFirst, ignoreCache);
    }

    public String getDisplayName(final ParcelableUserList user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(user.user_id, user.user_name, user.user_screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(final ParcelableStatus status, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(status.user_id, status.user_name, status.user_screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(final long userId, final String name,
                                 final String screenName, final boolean nameFirst,
                                 final boolean ignoreCache) {
        final String nick = getUserNickname(userId, ignoreCache);
        if (!isEmpty(nick)) return nick;
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public int getUserColor(final long userId, final boolean ignoreCache) {
        if (userId == -1) return Color.TRANSPARENT;
        if (!ignoreCache && mUserColors.indexOfKey(userId) >= 0) return mUserColors.get(userId);
        final int color = mColorPreferences.getInt(Long.toString(userId), Color.TRANSPARENT);
        mUserColors.put(userId, color);
        return color;
    }

    public String getUserNickname(final long userId) {
        return getUserNickname(userId, false);
    }

    public String getUserNickname(final long userId, final boolean ignoreCache) {
        if (userId == -1) return null;
        if (!ignoreCache && LongSparseArrayUtils.hasKey(mUserNicknames, userId))
            return mUserNicknames.get(userId);
        final String nickname = mNicknamePreferences.getString(Long.toString(userId), null);
        mUserNicknames.put(userId, nickname);
        return nickname;
    }

    public String getUserNickname(final long userId, final String name) {
        return getUserNickname(userId, name, false);
    }

    public String getUserNickname(final long userId, final String name, final boolean ignoreCache) {
        final String nick = getUserNickname(userId, ignoreCache);
        return isEmpty(nick) ? name : nick;
    }

    public Set<? extends Map.Entry<String, ?>> getNameEntries() {
        return mNicknamePreferences.getAll().entrySet();
    }

    public interface OnUserColorChangedListener {
        void onUserColorChanged(long userId, int color);
    }

    public interface OnUserNicknameChangedListener {
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
