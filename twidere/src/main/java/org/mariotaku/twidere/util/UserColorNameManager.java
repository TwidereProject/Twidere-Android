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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;

import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public class UserColorNameManager implements TwidereConstants {

    private final SharedPreferences mColorPreferences, mNicknamePreferences;
    private final Context mContext;

    public UserColorNameManager(Context context) {
        mContext = context;
        mColorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNicknamePreferences = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
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

        mColorPreferences.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public void registerNicknameChangedListener(final UserNicknameChangedListener listener) {

        mNicknamePreferences.registerOnSharedPreferenceChangeListener(new OnNickPreferenceChangeListener(listener));
    }

    public void clearUserColor(@NonNull final UserKey userKey) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        final String userKeyString = userKey.toString();
        updateColor(userKeyString, 0);
        editor.remove(userKeyString);
        editor.apply();
    }

    public void setUserColor(@NonNull final UserKey userKey, final int color) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        final String userKeyString = userKey.toString();
        updateColor(userKeyString, color);
        editor.putInt(userKeyString, color);
        editor.apply();
    }

    public void setUserNickname(@NonNull final UserKey userKey, final String nickname) {
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        final String userKeyString = userKey.toString();
        updateNickname(userKeyString, null);
        editor.putString(userKeyString, nickname);
        editor.apply();
    }

    public void clearUserNickname(@NonNull final UserKey userKey) {
        final SharedPreferences.Editor editor = mNicknamePreferences.edit();
        final String userKeyString = userKey.toString();
        updateNickname(userKeyString, null);
        editor.remove(userKeyString);
        editor.apply();
    }

    private void updateColor(String userKey, int color) {
        final ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.USER_COLOR, Statuses.USER_KEY,
                color, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.QUOTED_USER_COLOR,
                Statuses.QUOTED_USER_KEY, color, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.RETWEET_USER_COLOR,
                Statuses.RETWEETED_BY_USER_KEY, color, cv);

        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_USER_COLOR,
                Activities.STATUS_USER_KEY, color, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_RETWEET_USER_COLOR,
                Activities.STATUS_RETWEETED_BY_USER_KEY, color, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_QUOTED_USER_COLOR,
                Activities.STATUS_QUOTED_USER_KEY, color, cv);
    }

    private void updateNickname(String userKey, String nickname) {
        final ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.USER_NICKNAME, Statuses.USER_KEY,
                nickname, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.QUOTED_USER_NICKNAME,
                Statuses.QUOTED_USER_KEY, nickname, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.RETWEET_USER_NICKNAME,
                Statuses.RETWEETED_BY_USER_KEY, nickname, cv);

        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_USER_NICKNAME,
                Activities.STATUS_USER_KEY, nickname, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_RETWEET_USER_NICKNAME,
                Activities.STATUS_RETWEETED_BY_USER_KEY, nickname, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_QUOTED_USER_NICKNAME,
                Activities.STATUS_QUOTED_USER_KEY, nickname, cv);
    }

    private static void updateColumn(ContentResolver cr, Uri uri, String userKey, String valueColumn,
                                     String whereColumn, int value, ContentValues temp) {
        temp.clear();
        temp.put(valueColumn, value);
        cr.update(uri, temp, Expression.equalsArgs(whereColumn).getSQL(),
                new String[]{userKey});
    }


    private static void updateColumn(ContentResolver cr, Uri uri, String userKey, String valueColumn,
                                     String whereColumn, String value, ContentValues temp) {
        temp.clear();
        temp.put(valueColumn, value);
        cr.update(uri, temp, Expression.equalsArgs(whereColumn).getSQL(),
                new String[]{userKey});
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
        return mColorPreferences.getInt(userId, Color.TRANSPARENT);
    }

    @Nullable
    public String getUserNickname(@NonNull final UserKey userKey) {
        final String userKeyString = userKey.toString();
        if (mNicknamePreferences.contains(userKey.getId())) {
            String nick = mNicknamePreferences.getString(userKey.getId(), null);
            SharedPreferences.Editor editor = mNicknamePreferences.edit();
            editor.remove(userKey.getId());
            editor.putString(userKeyString, nick);
            editor.apply();
            return nick;
        }
        return mNicknamePreferences.getString(userKeyString, null);
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
        return mNicknamePreferences.getAll().entrySet();
    }

    private String getUserNicknameInternal(@NonNull final String userId) {
        return mNicknamePreferences.getString(userId, null);
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
            if (mListener != null && userId != null) {
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
            if (mListener != null && userId != null) {
                mListener.onUserNicknameChanged(userId, preferences.getString(key, null));
            }
        }

    }
}
