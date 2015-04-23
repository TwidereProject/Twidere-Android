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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.annotation.Preference;
import org.mariotaku.twidere.util.DataImportExportUtils;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesData implements Constants {


    private final Map<String, Object> preferencesMap = new HashMap<>();

    public SharedPreferencesData(final SharedPreferences preferences) {
        preferencesMap.putAll(preferences.getAll());
    }

    @Override
    public String toString() {
        return preferencesMap.toString();
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
