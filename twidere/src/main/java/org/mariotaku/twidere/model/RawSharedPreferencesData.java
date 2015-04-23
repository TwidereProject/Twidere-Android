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

import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.Constants;

import java.util.HashMap;
import java.util.Map;

@JsonObject
public class RawSharedPreferencesData implements Constants {

    private final Map<String, Object> preferencesMap = new HashMap<>();


    public RawSharedPreferencesData(final SharedPreferences preferences) {
        preferencesMap.putAll(preferences.getAll());
    }

    @Override
    public String toString() {
        return preferencesMap.toString();
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

}
