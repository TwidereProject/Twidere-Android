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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RawSharedPreferencesData implements JSONParcelable, Constants {

	public static final Creator<RawSharedPreferencesData> JSON_CREATOR = new Creator<RawSharedPreferencesData>() {

		@Override
		public RawSharedPreferencesData createFromParcel(final JSONParcel in) {
			return new RawSharedPreferencesData(in);
		}

		@Override
		public RawSharedPreferencesData[] newArray(final int size) {
			return new RawSharedPreferencesData[size];
		}
	};

	private final Map<String, Object> preferencesMap = new HashMap<String, Object>();

	public RawSharedPreferencesData(final JSONParcel in) {
		final JSONIterable json = new JSONIterable(in.getJSON());
		for (final String key : json) {
			if (!in.contains(key)) {
				continue;
			}
			final Object value = in.readObject(key);
			if (value == null) {
				continue;
			}
			if (value instanceof Boolean) {
				preferencesMap.put(key, value);
			} else if (value instanceof Integer) {
				preferencesMap.put(key, value);
			} else if (value instanceof Long) {
				preferencesMap.put(key, value);
			} else if (value instanceof Float) {
				preferencesMap.put(key, value);
			} else if (value instanceof String) {
				preferencesMap.put(key, value);
			}
		}
	}

	public RawSharedPreferencesData(final SharedPreferences preferences) {
		preferencesMap.putAll(preferences.getAll());
	}

	@Override
	public String toString() {
		return preferencesMap.toString();
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		for (final String key : preferencesMap.keySet()) {
			if (!preferencesMap.containsKey(key)) {
				continue;
			}
			final Object value = preferencesMap.get(key);
			if (value == null) {
				out.writeValue(key, JSONObject.NULL);
				continue;
			}
			if (value instanceof Boolean) {
				out.writeBoolean(key, (Boolean) value);
			} else if (value instanceof Integer) {
				out.writeInt(key, (Integer) value);
			} else if (value instanceof Long) {
				out.writeLong(key, (Long) value);
			} else if (value instanceof Float) {
				out.writeFloat(key, (Float) value);
			} else if (value instanceof String) {
				out.writeString(key, (String) value);
			}
		}
	}

	public void writeToSharedPreferences(final SharedPreferences prefs) {
		final Editor editor = prefs.edit();
		for (final String key : preferencesMap.keySet()) {
			if (!preferencesMap.containsKey(key)) {
				continue;
			}
			final Object value = preferencesMap.get(key);
			if (value == null) {
				continue;
			}
			if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean) value);
			} else if (value instanceof Integer) {
				editor.putInt(key, (Integer) value);
			} else if (value instanceof Long) {
				editor.putLong(key, (Long) value);
			} else if (value instanceof Float) {
				editor.putFloat(key, (Float) value);
			} else if (value instanceof String) {
				editor.putString(key, (String) value);
			}
		}
		editor.apply();
	}

	private static class JSONIterable implements Iterable<String> {
		private final JSONObject jsonObject;

		JSONIterable(final JSONObject json) {
			jsonObject = json;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Iterator<String> iterator() {
			return jsonObject.keys();
		}

	}

}
