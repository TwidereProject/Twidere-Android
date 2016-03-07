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
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.UserKeyUtils;

import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public class UserColorNameManager implements TwidereConstants {

    private final SimpleArrayMap<String, Integer> mUserColors = new SimpleArrayMap<>();
    private final SimpleArrayMap<String, String> mUserNicknames = new SimpleArrayMap<>();
    private final SharedPreferences mColorPreferences, mNicknamePreferences;

    public UserColorNameManager(Context context) {
        mColorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNicknamePreferences = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void registerColorChangedListener(final UserColorChangedListener listener) {

        mColorPreferences.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public void registerNicknameChangedListener(final UserNicknameChangedListener listener) {

        mNicknamePreferences.registerOnSharedPreferenceChangeListener(new OnNickPreferenceChangeListener(listener));
    }

    public void clearUserColor(@NonNull final UserKey userId) {
        mUserColors.remove(userId);
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.remove(userId.toString());
        editor.apply();
    }

    public void setUserColor(@NonNull final UserKey userId, final int color) {
        setUserColor(userId.toString(), color);
    }

    public void setUserColor(@NonNull final String userId, final int color) {
        mUserColors.put(userId, color);
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.putInt(userId, color);
        editor.apply();
    }

    public void setUserNickname(@NonNull final UserKey userId, final String nickname) {
        setUserNickname(userId.toString(), nickname);
    }

    public void setUserNickname(@NonNull final String userId, final String nickname) {
        mUserNicknames.put(userId, nickname);
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        editor.putString(userId, nickname);
        editor.apply();
    }

    public void clearUserNickname(@NonNull final UserKey userId) {
        mUserNicknames.remove(userId);
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        editor.remove(userId.toString());
        editor.apply();
    }

    public String getDisplayName(final ParcelableUser user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(user.key, user.name, user.screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(final User user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(UserKeyUtils.fromUser(user), user.getName(), user.getScreenName(), nameFirst, ignoreCache);
    }

    public String getDisplayName(final ParcelableUserList user, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(user.user_key, user.user_name, user.user_screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(final ParcelableStatus status, final boolean nameFirst, final boolean ignoreCache) {
        return getDisplayName(status.user_key, status.user_name, status.user_screen_name, nameFirst, ignoreCache);
    }

    public String getDisplayName(@NonNull final UserKey userId, final String name,
                                 final String screenName, final boolean nameFirst,
                                 final boolean ignoreCache) {
        return getDisplayName(userId.toString(), name, screenName, nameFirst, ignoreCache);
    }

    public String getDisplayName(@NonNull final String userId, final String name,
                                 final String screenName, final boolean nameFirst,
                                 final boolean ignoreCache) {
        final String nick = getUserNickname(userId, ignoreCache);
        if (!isEmpty(nick)) return nick;
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public static String getDisplayName(final String name, final String screenName, final boolean nameFirst) {
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public int getUserColor(@NonNull final UserKey userId, final boolean ignoreCache) {
        return getUserColor(userId.toString(), ignoreCache);
    }

    public int getUserColor(@NonNull final String userId, final boolean ignoreCache) {
        if (!ignoreCache && mUserColors.indexOfKey(userId) >= 0) return mUserColors.get(userId);
        final int color = mColorPreferences.getInt(userId, Color.TRANSPARENT);
        mUserColors.put(userId, color);
        return color;
    }

    public String getUserNickname(@NonNull final UserKey userId) {
        return getUserNickname(userId, false);
    }

    public String getUserNickname(@NonNull final UserKey userId, final boolean ignoreCache) {
        return getUserNickname(userId.toString(), ignoreCache);
    }

    public String getUserNickname(@NonNull final String userId, final boolean ignoreCache) {
        if (!ignoreCache && mUserNicknames.containsKey(userId))
            return mUserNicknames.get(userId);
        final String nickname = mNicknamePreferences.getString(userId, null);
        mUserNicknames.put(userId, nickname);
        return nickname;
    }

    public String getUserNickname(@NonNull final UserKey userId, final String name) {
        return getUserNickname(userId, name, false);
    }

    public String getUserNickname(@NonNull final String userId, final String name) {
        return getUserNickname(userId, name, false);
    }

    public String getUserNickname(@NonNull final UserKey userId, final String name, final boolean ignoreCache) {
        return getUserNickname(userId.toString(), name, ignoreCache);
    }

    public String getUserNickname(@NonNull final String userId, final String name, final boolean ignoreCache) {
        final String nick = getUserNickname(userId, ignoreCache);
        return isEmpty(nick) ? name : nick;
    }

    public Set<? extends Map.Entry<String, ?>> getNameEntries() {
        return mNicknamePreferences.getAll().entrySet();
    }

    public String getName(@NonNull final UserKey id, String name) {
        final String nick = getUserNickname(id, true);
        return TextUtils.isEmpty(nick) ? name : nick;
    }

    public interface UserColorChangedListener {
        void onUserColorChanged(@NonNull UserKey userId, int color);
    }

    public interface UserNicknameChangedListener {
        void onUserNicknameChanged(@NonNull UserKey userId, String nick);
    }

    private static final class OnColorPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final UserColorChangedListener mListener;

        OnColorPreferenceChangeListener(final UserColorChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null && userId != null) {
                mListener.onUserColorChanged(userId, sharedPreferences.getInt(key, 0));
            }
        }

    }

    private static final class OnNickPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final UserNicknameChangedListener mListener;

        OnNickPreferenceChangeListener(final UserNicknameChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null && userId != null) {
                mListener.onUserNicknameChanged(userId, sharedPreferences.getString(key, null));
            }
        }

    }
}
