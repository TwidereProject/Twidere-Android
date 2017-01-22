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
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.UserKeyUtils;

import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.TwidereConstants.USER_COLOR_PREFERENCES_NAME;
import static org.mariotaku.twidere.TwidereConstants.USER_NICKNAME_PREFERENCES_NAME;

public class UserColorNameManager {

    private final static String NICKNAME_NULL = ".#NULL#";

    private final SharedPreferences colorPreferences, nicknamePreferences;
    private final LruCache<String, Integer> colorCache;
    private final LruCache<String, String> nicknameCache;
    private final Context context;

    public UserColorNameManager(Context context) {
        this.context = context;
        colorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        nicknamePreferences = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
        colorCache = new LruCache<>(512);
        nicknameCache = new LruCache<>(256);
    }

    public SharedPreferences getColorPreferences() {
        return colorPreferences;
    }

    public SharedPreferences getNicknamePreferences() {
        return nicknamePreferences;
    }

    public static String decideDisplayName(final String nickname, final String name,
                                           final String screenName, final boolean nameFirst) {
        if (!isEmpty(nickname)) return nickname;
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public static String decideNickname(String nick, String name) {
        return isEmpty(nick) ? name : nick;
    }

    public void registerColorChangedListener(final UserColorChangedListener listener) {

        colorPreferences.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public void registerNicknameChangedListener(final UserNicknameChangedListener listener) {

        nicknamePreferences.registerOnSharedPreferenceChangeListener(new OnNickPreferenceChangeListener(listener));
    }

    public void clearUserColor(@NonNull final UserKey userKey) {
        final SharedPreferences.Editor editor = colorPreferences.edit();
        final String userKeyString = userKey.toString();
        colorCache.remove(userKeyString);
        editor.remove(userKeyString);
        editor.apply();
    }

    public void setUserColor(@NonNull final UserKey userKey, final int color) {
        final SharedPreferences.Editor editor = colorPreferences.edit();
        final String userKeyString = userKey.toString();
        colorCache.put(userKeyString, color);
        editor.putInt(userKeyString, color);
        editor.apply();
    }

    public void setUserNickname(@NonNull final UserKey userKey, final String nickname) {
        final SharedPreferences.Editor editor = nicknamePreferences.edit();
        final String userKeyString = userKey.toString();
        nicknameCache.put(userKeyString, nickname);
        editor.putString(userKeyString, nickname);
        editor.apply();
    }

    public void clearUserNickname(@NonNull final UserKey userKey) {
        final SharedPreferences.Editor editor = nicknamePreferences.edit();
        final String userKeyString = userKey.toString();
        nicknameCache.remove(userKeyString);
        editor.remove(userKeyString);
        editor.apply();
    }


    public String getDisplayName(final ParcelableUser user, final boolean nameFirst) {
        return getDisplayName(user.key, user.name, user.screen_name, nameFirst);
    }

    public String getDisplayName(final User user, final boolean nameFirst) {
        return getDisplayName(UserKeyUtils.fromUser(user), user.getName(), user.getScreenName(), nameFirst);
    }

    public String getDisplayName(final ParcelableUserList user, final boolean nameFirst) {
        return getDisplayName(user.user_key, user.user_name, user.user_screen_name, nameFirst);
    }

    public String getDisplayName(final ParcelableStatus status, final boolean nameFirst) {
        return getDisplayName(status.user_key, status.user_name, status.user_screen_name, nameFirst);
    }

    public String getDisplayName(@NonNull final UserKey userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        return getDisplayName(userId.toString(), name, screenName, nameFirst);
    }

    public String getDisplayName(@NonNull final String userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        final String nick = getUserNicknameInternal(userId);
        return decideDisplayName(nick, name, screenName, nameFirst);
    }

    public int getUserColor(@NonNull final UserKey userId) {
        return getUserColor(userId.toString());
    }

    public int getUserColor(@NonNull final String userId) {
        final Integer cached = colorCache.get(userId);
        if (cached != null) return cached;
        final int color = colorPreferences.getInt(userId, Color.TRANSPARENT);
        colorCache.put(userId, color);
        return color;
    }

    @Nullable
    public String getUserNickname(@NonNull final UserKey userKey) {
        final String userKeyString = userKey.toString();
        return getUserNicknameInternal(userKeyString);
    }

    @Nullable
    public String getUserNickname(@NonNull final UserKey userId, final String name) {
        final String nick = getUserNickname(userId);
        return decideNickname(nick, name);
    }

    @Nullable
    public String getUserNickname(@NonNull final String userId, final String name) {
        final String nick = getUserNicknameInternal(userId);
        return decideNickname(nick, name);
    }

    public Set<? extends Map.Entry<String, ?>> getNameEntries() {
        return nicknamePreferences.getAll().entrySet();
    }

    private String getUserNicknameInternal(@NonNull final String userId) {
        final String cached = nicknameCache.get(userId);
        if (NICKNAME_NULL.equals(cached)) return null;
        if (cached != null) return cached;
        final String nickname = nicknamePreferences.getString(userId, null);
        if (nickname != null) {
            nicknameCache.put(userId, nickname);
        } else {
            nicknameCache.put(userId, NICKNAME_NULL);
        }
        return nickname;
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
        public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null) {
                mListener.onUserColorChanged(userId, preferences.getInt(key, 0));
            }
        }

    }

    private static final class OnNickPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final UserNicknameChangedListener mListener;

        OnNickPreferenceChangeListener(final UserNicknameChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null) {
                mListener.onUserNicknameChanged(userId, preferences.getString(key, null));
            }
        }

    }
}
