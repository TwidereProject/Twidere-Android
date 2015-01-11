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

package org.mariotaku.twidere.util.content;

import android.content.ContentValues;

public class ContentValuesUtils {

	public static boolean getAsBoolean(final ContentValues values, final String key, final boolean def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Boolean.valueOf(value.toString());
	}

	public static long getAsInteger(final ContentValues values, final String key, final int def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Integer.valueOf(value.toString());
	}

	public static long getAsLong(final ContentValues values, final String key, final long def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Long.valueOf(value.toString());
	}
}
