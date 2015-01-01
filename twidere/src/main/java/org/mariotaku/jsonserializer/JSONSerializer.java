/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.jsonserializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class JSONSerializer {
	private static boolean debugMode = false;

	public static <T extends JSONParcelable> T[] createArray(final JSONParcelable.Creator<T> creator,
			final JSONArray json) {
		if (creator == null) throw new NullPointerException("JSON_CREATOR must not be null!");
		if (json == null) return null;
		final int size = json.length();
		final T[] array = creator.newArray(size);
		for (int i = 0; i < size; i++) {
			array[i] = creator.createFromParcel(new JSONParcel(json.optJSONObject(i)));
		}
		return array;
	}

	public static <T extends JSONParcelable> ArrayList<T> createArrayList(final JSONParcelable.Creator<T> creator,
			final JSONArray json) {
		if (creator == null) throw new NullPointerException("JSON_CREATOR must not be null!");
		if (json == null) return null;
		final int size = json.length();
		final ArrayList<T> list = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			list.add(creator.createFromParcel(new JSONParcel(json.optJSONObject(i))));
		}
		return list;
	}

	public static <T extends JSONParcelable> T createObject(final JSONParcelable.Creator<T> creator,
			final JSONObject json) {
		if (creator == null) throw new NullPointerException("JSON_CREATOR must not be null!");
		if (json == null) return null;
		return creator.createFromParcel(new JSONParcel(json));
	}

	public static <T extends JSONParcelable> byte[] getByteArray(final T parcelable) {
		final JSONObject json = toJSONObject(parcelable);
		final String string = jsonToString(json);
		if (string == null) return null;
		return string.getBytes(Charset.defaultCharset());
	}

	public static <T extends JSONParcelable> byte[] getByteArray(final T[] array) {
		final JSONArray json = toJSONArray(array);
		final String string = jsonToString(json);
		if (string == null) return null;
		return string.getBytes(Charset.defaultCharset());
	}

	public static boolean isDebugMode() {
		return debugMode;
	}

	public static void setIsDebugMode(final boolean debug) {
		debugMode = debug;
	}

	public static <T extends JSONParcelable> byte[] toByteArray(final T parcelable) throws IOException {
		final String jsonString = jsonToString(toJSONObject(parcelable));
		if (jsonString == null) return null;
		return jsonString.getBytes(Charset.defaultCharset());
	}

	public static <T extends JSONParcelable> byte[] toByteArray(final T[] array) throws IOException {
		final String jsonString = jsonToString(toJSONArray(array));
		if (jsonString == null) return null;
		return jsonString.getBytes(Charset.defaultCharset());
	}

	public static <T extends JSONParcelable> JSONArray toJSONArray(final T[] array) {
		if (array == null) return null;
		final JSONArray json = new JSONArray();
		for (final T parcelable : array) {
			json.put(toJSONObject(parcelable));
		}
		return json;
	}

	public static <T extends JSONParcelable> String toJSONArrayString(final T[] array) {
		return jsonToString(toJSONArray(array));
	}

	public static <T extends JSONParcelable> JSONObject toJSONObject(final T parcelable) {
		if (parcelable == null) return null;
		final JSONObject json = new JSONObject();
		parcelable.writeToParcel(new JSONParcel(json));
		return json;
	}

	public static <T extends JSONParcelable> String toJSONObjectString(final T parcelable) {
		return jsonToString(toJSONObject(parcelable));
	}

	static String jsonToString(final JSONArray json) {
		if (json == null) return null;
		if (debugMode) {
			try {
				return json.toString(4);
			} catch (final JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		} else
			return json.toString();
	}

	static String jsonToString(final JSONObject json) {
		if (json == null) return null;
		if (debugMode) {
			try {
				return json.toString(4);
			} catch (final JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		} else
			return json.toString();
	}

}
