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
import org.mariotaku.twidere.annotation.Preference;
import org.mariotaku.twidere.util.DataImportExportUtils;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesData implements JSONParcelable, Constants {

	public static final Creator<SharedPreferencesData> JSON_CREATOR = new Creator<SharedPreferencesData>() {

		@Override
		public SharedPreferencesData createFromParcel(final JSONParcel in) {
			return new SharedPreferencesData(in);
		}

		@Override
		public SharedPreferencesData[] newArray(final int size) {
			return new SharedPreferencesData[size];
		}
	};

	private final Map<String, Object> preferencesMap = new HashMap<String, Object>();

	public SharedPreferencesData(final JSONParcel in) {
		final Map<String, Preference> supportedMap = DataImportExportUtils.getSupportedPreferencesMap();
		for (final String key : supportedMap.keySet()) {
			if (!in.contains(key)) {
				continue;
			}
			final Preference annotation = supportedMap.get(key);
			switch (annotation.type()) {
				case BOOLEAN: {
					preferencesMap.put(key, in.readBoolean(key, annotation.defaultBoolean()));
					break;
				}
				case INT: {
					preferencesMap.put(key, in.readInt(key, annotation.defaultInt()));
					break;
				}
				case LONG: {
					preferencesMap.put(key, in.readLong(key, annotation.defaultLong()));
					break;
				}
				case FLOAT: {
					preferencesMap.put(key, in.readFloat(key, annotation.defaultFloat()));
					break;
				}
				case STRING: {
					preferencesMap.put(key, in.readString(key, annotation.defaultString()));
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	public SharedPreferencesData(final SharedPreferences preferences) {
		preferencesMap.putAll(preferences.getAll());
	}

	@Override
	public String toString() {
		return preferencesMap.toString();
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		final Map<String, Preference> supportedMap = DataImportExportUtils.getSupportedPreferencesMap();
		for (final String key : preferencesMap.keySet()) {
			if (!preferencesMap.containsKey(key)) {
				continue;
			}
			final String value = ParseUtils.parseString(preferencesMap.get(key));
			if (value == null) {
				out.writeValue(key, JSONObject.NULL);
				continue;
			}
			final Preference annotation = supportedMap.get(key);
			if (annotation == null) {
				continue;
			}
			switch (annotation.type()) {
				case BOOLEAN: {
					out.writeBoolean(key, Boolean.parseBoolean(value));
					break;
				}
				case INT: {
					out.writeInt(key, ParseUtils.parseInt(value, annotation.defaultInt()));
					break;
				}
				case LONG: {
					out.writeLong(key, ParseUtils.parseLong(value, annotation.defaultLong()));
					break;
				}
				case FLOAT: {
					out.writeFloat(key, ParseUtils.parseFloat(value, annotation.defaultFloat()));
					break;
				}
				case STRING: {
					out.writeString(key, value);
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	public void writeToSharedPreferences(final SharedPreferences prefs) {
		final Editor editor = prefs.edit();
		final Map<String, Preference> supportedMap = DataImportExportUtils.getSupportedPreferencesMap();
		for (final String key : preferencesMap.keySet()) {
			if (!preferencesMap.containsKey(key)) {
				continue;
			}
			final String value = ParseUtils.parseString(preferencesMap.get(key));
			if (value == null) {
				continue;
			}
			final Preference annotation = supportedMap.get(key);
			switch (annotation.type()) {
				case BOOLEAN: {
					editor.putBoolean(key, Boolean.parseBoolean(value));
					break;
				}
				case INT: {
					editor.putInt(key, ParseUtils.parseInt(value, annotation.defaultInt()));
					break;
				}
				case LONG: {
					editor.putLong(key, ParseUtils.parseLong(value, annotation.defaultLong()));
					break;
				}
				case FLOAT: {
					editor.putFloat(key, ParseUtils.parseFloat(value, annotation.defaultFloat()));
					break;
				}
				case STRING: {
					editor.putString(key, value);
					break;
				}
				default: {
					break;
				}
			}
		}
		editor.apply();
	}

}
