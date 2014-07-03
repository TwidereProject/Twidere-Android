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

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;

import java.util.Map;

public class UserColorNicknameUtils implements TwidereConstants {

	private static LongSparseArray<Integer> sUserColors = new LongSparseArray<Integer>();
	private static LongSparseArray<String> sUserNicknames = new LongSparseArray<String>();

	private UserColorNicknameUtils() {
		throw new AssertionError();
	}

	public static void clearUserColor(final Context context, final long user_id) {
		if (context == null) return;
		sUserColors.remove(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
	}

	public static void clearUserNickname(final Context context, final long user_id) {
		if (context == null) return;
		sUserNicknames.remove(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
	}

	public static int getUserColor(final Context context, final long user_id) {
		return getUserColor(context, user_id, false);
	}

	public static int getUserColor(final Context context, final long user_id, final boolean ignore_cache) {
		if (context == null || user_id == -1) return Color.TRANSPARENT;
		if (!ignore_cache && sUserColors.indexOfKey(user_id) >= 0) return sUserColors.get(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int color = prefs.getInt(Long.toString(user_id), Color.TRANSPARENT);
		sUserColors.put(user_id, color);
		return color;
	}

	public static String getUserNickname(final Context context, final long user_id) {
		return getUserNickname(context, user_id, false);
	}

	public static String getUserNickname(final Context context, final long user_id, final boolean ignore_cache) {
		if (context == null || user_id == -1) return null;
		if (!ignore_cache && LongSparseArrayUtils.hasKey(sUserNicknames, user_id)) return sUserNicknames.get(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final String nickname = prefs.getString(Long.toString(user_id), null);
		sUserNicknames.put(user_id, nickname);
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
		editor.commit();
	}

	public static void setUserNickname(final Context context, final long user_id, final String nickname) {
		if (context == null || user_id == -1) return;
		sUserNicknames.put(user_id, nickname);
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(String.valueOf(user_id), nickname);
		editor.commit();
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
