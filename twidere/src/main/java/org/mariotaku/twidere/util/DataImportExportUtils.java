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
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.annotation.Preference;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.mariotaku.twidere.annotation.Preference.Type.INVALID;

public class DataImportExportUtils implements Constants {

    public static final String ENTRY_PREFERENCES = "preferences.json";
    public static final String ENTRY_NICKNAMES = "nicknames.json";
    public static final String ENTRY_USER_COLORS = "user_colors.json";
    public static final String ENTRY_HOST_MAPPING = "host_mapping.json";
    public static final String ENTRY_KEYBOARD_SHORTCUTS = "keyboard_shortcuts.json";
    public static final String ENTRY_FILTERS = "filters.json";

    public static final int FLAG_PREFERENCES = 0x1;
    public static final int FLAG_NICKNAMES = 0x2;
    public static final int FLAG_USER_COLORS = 0x4;
    public static final int FLAG_HOST_MAPPING = 0x8;
    public static final int FLAG_KEYBOARD_SHORTCUTS = 0x10;
    public static final int FLAG_FILTERS = 0x20;
    public static final int FLAG_ALL = FLAG_PREFERENCES | FLAG_NICKNAMES | FLAG_USER_COLORS
            | FLAG_HOST_MAPPING | FLAG_KEYBOARD_SHORTCUTS | FLAG_FILTERS;

    public static void exportData(final Context context, final File dst, final int flags) throws IOException {
        if (dst == null) throw new FileNotFoundException();
        dst.delete();
        final FileOutputStream fos = new FileOutputStream(dst);
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
        if (hasFlag(flags, FLAG_PREFERENCES)) {
            exportSharedPreferencesData(zos, context, SHARED_PREFERENCES_NAME, ENTRY_PREFERENCES, new AnnotationProcessStrategy(SharedPreferenceConstants.class));
        }
        if (hasFlag(flags, FLAG_NICKNAMES)) {
            exportSharedPreferencesData(zos, context, USER_NICKNAME_PREFERENCES_NAME, ENTRY_NICKNAMES, ConvertToStringProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_USER_COLORS)) {
            exportSharedPreferencesData(zos, context, USER_COLOR_PREFERENCES_NAME, ENTRY_USER_COLORS, ConvertToIntProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_HOST_MAPPING)) {
            exportSharedPreferencesData(zos, context, HOST_MAPPING_PREFERENCES_NAME, ENTRY_HOST_MAPPING, ConvertToStringProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_KEYBOARD_SHORTCUTS)) {
            exportSharedPreferencesData(zos, context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, ENTRY_KEYBOARD_SHORTCUTS, ConvertToStringProcessStrategy.SINGLETON);
        }
        zos.finish();
        zos.flush();
        Utils.closeSilently(zos);
        Utils.closeSilently(fos);
    }

    public static int getImportedSettingsFlags(final File src) throws IOException {
        if (src == null) return 0;
        final ZipFile zipFile = new ZipFile(src);
        int flags = 0;
        if (zipFile.getEntry(ENTRY_PREFERENCES) != null) {
            flags |= FLAG_PREFERENCES;
        }
        if (zipFile.getEntry(ENTRY_NICKNAMES) != null) {
            flags |= FLAG_NICKNAMES;
        }
        if (zipFile.getEntry(ENTRY_USER_COLORS) != null) {
            flags |= FLAG_USER_COLORS;
        }
        if (zipFile.getEntry(ENTRY_HOST_MAPPING) != null) {
            flags |= FLAG_HOST_MAPPING;
        }
        if (zipFile.getEntry(ENTRY_KEYBOARD_SHORTCUTS) != null) {
            flags |= FLAG_KEYBOARD_SHORTCUTS;
        }
        if (zipFile.getEntry(ENTRY_FILTERS) != null) {
            flags |= FLAG_FILTERS;
        }
        zipFile.close();
        return flags;
    }

    public static HashMap<String, Preference> getSupportedPreferencesMap(Class cls) {
        final Field[] fields = cls.getDeclaredFields();
        final HashMap<String, Preference> supportedPrefsMap = new HashMap<>();
        for (final Field field : fields) {
            final Preference annotation = field.getAnnotation(Preference.class);
            if (Modifier.isStatic(field.getModifiers()) && CompareUtils.classEquals(field.getType(), String.class)
                    && annotation != null && annotation.exportable() && annotation.type() != INVALID) {
                try {
                    supportedPrefsMap.put((String) field.get(null), annotation);
                } catch (final IllegalAccessException | IllegalArgumentException e) {
                    Log.w(LOGTAG, e);
                }
            }
        }
        return supportedPrefsMap;
    }

    public static void importData(final Context context, final File src, final int flags) throws IOException {
        if (src == null) throw new FileNotFoundException();
        final ZipFile zipFile = new ZipFile(src);
        if (hasFlag(flags, FLAG_PREFERENCES)) {
            importSharedPreferencesData(zipFile, context, SHARED_PREFERENCES_NAME, ENTRY_PREFERENCES, new AnnotationProcessStrategy(SharedPreferenceConstants.class));
        }
        if (hasFlag(flags, FLAG_NICKNAMES)) {
            importSharedPreferencesData(zipFile, context, USER_NICKNAME_PREFERENCES_NAME, ENTRY_NICKNAMES, ConvertToStringProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_USER_COLORS)) {
            importSharedPreferencesData(zipFile, context, USER_COLOR_PREFERENCES_NAME, ENTRY_USER_COLORS, ConvertToIntProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_HOST_MAPPING)) {
            importSharedPreferencesData(zipFile, context, HOST_MAPPING_PREFERENCES_NAME, ENTRY_HOST_MAPPING, ConvertToStringProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags, FLAG_KEYBOARD_SHORTCUTS)) {
            importSharedPreferencesData(zipFile, context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, ENTRY_KEYBOARD_SHORTCUTS, ConvertToStringProcessStrategy.SINGLETON);
        }
        if (hasFlag(flags,FLAG_FILTERS)) {

        }
        zipFile.close();
    }

    private static boolean hasFlag(final int flags, final int flag) {
        return (flags & flag) != 0;
    }

    private static void importSharedPreferencesData(@NonNull final ZipFile zipFile, @NonNull final Context context,
                                                    @NonNull final String preferencesName, @NonNull final String entryName,
                                                    @NonNull final ProcessStrategy strategy) throws IOException {
        final ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) return;
        final JSONObject json = JsonSerializer.convertJSONObject(zipFile.getInputStream(entry));
        final Iterator<String> keys = json.keys();
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        while (keys.hasNext()) {
            strategy.importValue(json, keys.next(), editor);
        }
        editor.apply();
    }

    private static void exportSharedPreferencesData(@NonNull final ZipOutputStream zos, final Context context,
                                                    @NonNull final String preferencesName, @NonNull final String entryName,
                                                    @NonNull final ProcessStrategy strategy) throws IOException {
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        final Map<String, ?> map = preferences.getAll();
        zos.putNextEntry(new ZipEntry(entryName));
        final JSONObject json = new JSONObject();
        for (String key : map.keySet()) {
            strategy.exportValue(json, key, preferences);
        }
        zos.write(json.toString().getBytes(Charset.defaultCharset()));
        zos.closeEntry();
    }

    private interface ProcessStrategy {
        boolean importValue(JSONObject json, String key, SharedPreferences.Editor editor);

        boolean exportValue(JSONObject json, String key, SharedPreferences preferences);
    }

    private static final class ConvertToStringProcessStrategy implements ProcessStrategy {

        private static final ProcessStrategy SINGLETON = new ConvertToStringProcessStrategy();

        @Override
        public boolean importValue(JSONObject json, String key, SharedPreferences.Editor editor) {
            if (!json.has(key)) return false;
            editor.putString(key, json.optString(key));
            return true;
        }

        @Override
        public boolean exportValue(JSONObject json, String key, SharedPreferences preferences) {
            if (!preferences.contains(key)) return false;
            try {
                json.putOpt(key, preferences.getString(key, null));
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
    }

    private static final class ConvertToIntProcessStrategy implements ProcessStrategy {

        private static final ProcessStrategy SINGLETON = new ConvertToIntProcessStrategy();

        @Override
        public boolean importValue(JSONObject json, String key, SharedPreferences.Editor editor) {
            if (!json.has(key)) return false;
            editor.putInt(key, json.optInt(key));
            return true;
        }

        @Override
        public boolean exportValue(JSONObject json, String key, SharedPreferences preferences) {
            if (!preferences.contains(key)) return false;
            try {
                json.put(key, preferences.getInt(key, 0));
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
    }

    private static final class AnnotationProcessStrategy implements ProcessStrategy {

        private final HashMap<String, Preference> supportedMap;

        AnnotationProcessStrategy(Class cls) {
            this.supportedMap = getSupportedPreferencesMap(cls);
        }

        @Override
        public boolean importValue(JSONObject json, String key, SharedPreferences.Editor editor) {
            final Preference preference = supportedMap.get(key);
            if (preference == null || !preference.exportable()) return false;
            switch (preference.type()) {
                case BOOLEAN:
                    editor.putBoolean(key, json.optBoolean(key, preference.defaultBoolean()));
                    break;
                case INT:
                    editor.putInt(key, json.optInt(key, preference.defaultInt()));
                    break;
                case LONG:
                    editor.putLong(key, json.optLong(key, preference.defaultLong()));
                    break;
                case FLOAT:
                    editor.putFloat(key, (float) json.optDouble(key, preference.defaultFloat()));
                    break;
                case STRING:
                    editor.putString(key, json.optString(key, preference.defaultString()));
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public boolean exportValue(JSONObject json, String key, SharedPreferences preferences) {
            final Preference preference = supportedMap.get(key);
            if (preference == null || !preference.exportable()) return false;
            try {
                switch (preference.type()) {
                    case BOOLEAN:
                        json.put(key, preferences.getBoolean(key, preference.defaultBoolean()));
                        break;
                    case INT:
                        json.put(key, preferences.getInt(key, preference.defaultInt()));
                        break;
                    case LONG:
                        json.put(key, preferences.getLong(key, preference.defaultLong()));
                        break;
                    case FLOAT:
                        json.put(key, preferences.getFloat(key, preference.defaultFloat()));
                        break;
                    case STRING:
                        json.put(key, preferences.getString(key, preference.defaultString()));
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                return false;
            }
            return true;
        }
    }

}
