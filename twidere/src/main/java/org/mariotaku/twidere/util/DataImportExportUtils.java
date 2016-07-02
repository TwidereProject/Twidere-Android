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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.annotation.Preference;
import org.mariotaku.twidere.annotation.PreferenceType;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.model.FiltersData;
import org.mariotaku.twidere.model.FiltersData$BaseItemCursorIndices;
import org.mariotaku.twidere.model.FiltersData$BaseItemValuesCreator;
import org.mariotaku.twidere.model.FiltersData$UserItemCursorIndices;
import org.mariotaku.twidere.model.FiltersData$UserItemValuesCreator;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.TabCursorIndices;
import org.mariotaku.twidere.model.TabValuesCreator;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DataImportExportUtils implements Constants {

    public static final String ENTRY_PREFERENCES = "preferences.json";
    public static final String ENTRY_NICKNAMES = "nicknames.json";
    public static final String ENTRY_USER_COLORS = "user_colors.json";
    public static final String ENTRY_HOST_MAPPING = "host_mapping.json";
    public static final String ENTRY_KEYBOARD_SHORTCUTS = "keyboard_shortcuts.json";
    public static final String ENTRY_FILTERS = "filters.json";
    public static final String ENTRY_TABS = "tabs.json";

    public static final int FLAG_PREFERENCES = 0b1;
    public static final int FLAG_NICKNAMES = 0b10;
    public static final int FLAG_USER_COLORS = 0b100;
    public static final int FLAG_HOST_MAPPING = 0b1000;
    public static final int FLAG_KEYBOARD_SHORTCUTS = 0b10000;
    public static final int FLAG_FILTERS = 0b100000;
    public static final int FLAG_TABS = 0b1000000;
    public static final int FLAG_ALL = FLAG_PREFERENCES | FLAG_NICKNAMES | FLAG_USER_COLORS
            | FLAG_HOST_MAPPING | FLAG_KEYBOARD_SHORTCUTS | FLAG_FILTERS | FLAG_TABS;

    @WorkerThread
    public static void exportData(final Context context, @NonNull final File dst, final int flags) throws IOException {
        dst.delete();
        final FileOutputStream fos = new FileOutputStream(dst);
        final ZipOutputStream zos = new ZipOutputStream(fos);
        try {
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
            if (hasFlag(flags, FLAG_FILTERS)) {
                // TODO export filters
                FiltersData data = new FiltersData();

                final ContentResolver cr = context.getContentResolver();
                data.setUsers(queryAll(cr, Filters.Users.CONTENT_URI, Filters.Users.COLUMNS,
                        FiltersData$UserItemCursorIndices.class));
                data.setKeywords(queryAll(cr, Filters.Keywords.CONTENT_URI, Filters.Keywords.COLUMNS,
                        FiltersData$BaseItemCursorIndices.class));
                data.setSources(queryAll(cr, Filters.Sources.CONTENT_URI, Filters.Sources.COLUMNS,
                        FiltersData$BaseItemCursorIndices.class));
                data.setLinks(queryAll(cr, Filters.Links.CONTENT_URI, Filters.Links.COLUMNS,
                        FiltersData$BaseItemCursorIndices.class));
                exportItem(zos, ENTRY_FILTERS, FiltersData.class, data);
            }
            if (hasFlag(flags, FLAG_TABS)) {
                // TODO export tabs
                final ContentResolver cr = context.getContentResolver();
                final Cursor c = cr.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null);
                if (c != null) {
                    final List<Tab> tabs = new ArrayList<>(c.getCount());
                    try {
                        TabCursorIndices ci = new TabCursorIndices(c);
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            tabs.add(ci.newObject(c));
                            c.moveToNext();
                        }
                    } finally {
                        c.close();
                    }
                    exportItemsList(zos, ENTRY_TABS, Tab.class, tabs);
                }
            }
            zos.finish();
            zos.flush();
        } finally {
            Utils.closeSilently(zos);
            Utils.closeSilently(fos);
        }
    }

    private static <T> List<T> queryAll(ContentResolver cr, Uri uri, String[] projection,
                                        Class<? extends ObjectCursor.CursorIndices<T>> cls) {
        Cursor c = cr.query(uri, projection, null, null, null);
        if (c == null) return null;
        try {
            final ObjectCursor.CursorIndices<T> ci;
            try {
                ci = cls.getConstructor(Cursor.class).newInstance(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<T> items = new ArrayList<>(c.getCount());
            c.moveToFirst();
            while (!c.isAfterLast()) {
                items.add(ci.newObject(c));
                c.moveToNext();
            }
            return items;
        } finally {
            c.close();
        }
    }

    @WorkerThread
    public static int getImportedSettingsFlags(@NonNull final File src) throws IOException {
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
        if (zipFile.getEntry(ENTRY_TABS) != null) {
            flags |= FLAG_TABS;
        }
        zipFile.close();
        return flags;
    }

    public static HashMap<String, Preference> getSupportedPreferencesMap(Class cls) {
        final Field[] fields = cls.getDeclaredFields();
        final HashMap<String, Preference> supportedPrefsMap = new HashMap<>();
        for (final Field field : fields) {
            final Preference annotation = field.getAnnotation(Preference.class);
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class
                    && annotation != null && annotation.exportable() && annotation.type() != PreferenceType.INVALID) {
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
        if (hasFlag(flags, FLAG_FILTERS)) {
            importItem(context, zipFile, ENTRY_FILTERS, FiltersData.class, new ContentResolverProcessStrategy<FiltersData>() {
                @Override
                public boolean importItem(ContentResolver cr, FiltersData filtersData) {
                    if (filtersData == null) return false;
                    insertBase(cr, Filters.Keywords.CONTENT_URI, filtersData.getKeywords());
                    insertBase(cr, Filters.Sources.CONTENT_URI, filtersData.getSources());
                    insertBase(cr, Filters.Links.CONTENT_URI, filtersData.getLinks());
                    insertUser(cr, Filters.Users.CONTENT_URI, filtersData.getUsers());
                    return true;
                }

                void insertBase(ContentResolver cr, Uri uri, List<FiltersData.BaseItem> items) {
                    if (items == null) return;
                    List<ContentValues> values = new ArrayList<>(items.size());
                    for (FiltersData.BaseItem item : items) {
                        values.add(FiltersData$BaseItemValuesCreator.create(item));
                    }
                    ContentResolverUtils.bulkInsert(cr, uri, values);
                }

                void insertUser(ContentResolver cr, Uri uri, List<FiltersData.UserItem> items) {
                    if (items == null) return;
                    List<ContentValues> values = new ArrayList<>(items.size());
                    for (FiltersData.UserItem item : items) {
                        values.add(FiltersData$UserItemValuesCreator.create(item));
                    }
                    ContentResolverUtils.bulkInsert(cr, uri, values);
                }
            });
        }
        if (hasFlag(flags, FLAG_TABS)) {
            importItemsList(context, zipFile, ENTRY_TABS, Tab.class, new ContentResolverProcessStrategy<List<Tab>>() {
                @Override
                public boolean importItem(ContentResolver cr, List<Tab> items) {
                    if (items == null) return false;
                    List<ContentValues> values = new ArrayList<>(items.size());
                    for (Tab item : items) {
                        values.add(TabValuesCreator.create(item));
                    }
                    cr.delete(Tabs.CONTENT_URI, null, null);
                    ContentResolverUtils.bulkInsert(cr, Tabs.CONTENT_URI, values);
                    return true;
                }
            });
        }
        zipFile.close();
    }

    private static boolean hasFlag(final int flags, final int flag) {
        return (flags & flag) != 0;
    }

    private static void importSharedPreferencesData(@NonNull final ZipFile zipFile, @NonNull final Context context,
                                                    @NonNull final String preferencesName, @NonNull final String entryName,
                                                    @NonNull final SharedPreferencesProcessStrategy strategy) throws IOException {
        final ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) return;
        final JsonParser jsonParser = LoganSquare.JSON_FACTORY.createParser(zipFile.getInputStream(entry));
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.skipChildren();
            return;
        }
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String key = jsonParser.getCurrentName();
            strategy.importValue(jsonParser, key, editor);
        }
        editor.apply();
    }

    private static void exportSharedPreferencesData(@NonNull final ZipOutputStream zos, final Context context,
                                                    @NonNull final String preferencesName, @NonNull final String entryName,
                                                    @NonNull final SharedPreferencesProcessStrategy strategy) throws IOException {
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        final Map<String, ?> map = preferences.getAll();
        zos.putNextEntry(new ZipEntry(entryName));
        final JsonGenerator jsonGenerator = LoganSquare.JSON_FACTORY.createGenerator(zos);
        jsonGenerator.writeStartObject();
        for (String key : map.keySet()) {
            strategy.exportValue(jsonGenerator, key, preferences);
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        zos.closeEntry();
    }

    private static <T> void importItemsList(@NonNull final Context context,
                                            @NonNull final ZipFile zipFile,
                                            @NonNull final String entryName,
                                            @NonNull final Class<T> itemCls,
                                            @NonNull final ContentResolverProcessStrategy<List<T>> strategy)
            throws IOException {
        final ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) return;
        final JsonMapper<T> mapper = LoganSquareMapperFinder.mapperFor(itemCls);
        List<T> itemsList = mapper.parseList(zipFile.getInputStream(entry));
        strategy.importItem(context.getContentResolver(), itemsList);
    }


    private static <T> void exportItemsList(@NonNull final ZipOutputStream zos,
                                            @NonNull final String entryName,
                                            @NonNull final Class<T> itemCls,
                                            @NonNull final List<T> itemList) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        final JsonGenerator jsonGenerator = LoganSquare.JSON_FACTORY.createGenerator(zos);
        LoganSquareMapperFinder.mapperFor(itemCls).serialize(itemList, jsonGenerator);
        jsonGenerator.flush();
        zos.closeEntry();
    }

    private static <T> void importItem(@NonNull final Context context,
                                       @NonNull final ZipFile zipFile,
                                       @NonNull final String entryName,
                                       @NonNull final Class<T> itemCls,
                                       @NonNull final ContentResolverProcessStrategy<T> strategy)
            throws IOException {
        final ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) return;
        final JsonMapper<T> mapper = LoganSquareMapperFinder.mapperFor(itemCls);
        T item = mapper.parse(zipFile.getInputStream(entry));
        strategy.importItem(context.getContentResolver(), item);
    }


    private static <T> void exportItem(@NonNull final ZipOutputStream zos,
                                       @NonNull final String entryName,
                                       @NonNull final Class<T> itemCls,
                                       @NonNull final T item) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        final JsonGenerator jsonGenerator = LoganSquare.JSON_FACTORY.createGenerator(zos);
        LoganSquareMapperFinder.mapperFor(itemCls).serialize(item, jsonGenerator, true);
        jsonGenerator.flush();
        zos.closeEntry();
    }

    private interface ContentResolverProcessStrategy<T> {
        boolean importItem(ContentResolver cr, T item);
    }

    private interface SharedPreferencesProcessStrategy {
        boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException;

        boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) throws IOException;
    }

    private static final class ConvertToStringProcessStrategy implements SharedPreferencesProcessStrategy {

        private static final SharedPreferencesProcessStrategy SINGLETON = new ConvertToStringProcessStrategy();

        @Override
        public boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException {
            final JsonToken token = jsonParser.nextToken();
            if (token == null) return false;
            editor.putString(key, jsonParser.getValueAsString());
            return true;
        }

        @Override
        public boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) {
            if (!preferences.contains(key)) return false;
            try {
                jsonGenerator.writeStringField(key, preferences.getString(key, null));
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
    }

    private static final class ConvertToIntProcessStrategy implements SharedPreferencesProcessStrategy {

        private static final SharedPreferencesProcessStrategy SINGLETON = new ConvertToIntProcessStrategy();

        @Override
        public boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException {
            final JsonToken token = jsonParser.nextToken();
            if (token == null) return false;
            editor.putInt(key, jsonParser.getValueAsInt());
            return true;
        }

        @Override
        public boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) {
            if (!preferences.contains(key)) return false;
            try {
                jsonGenerator.writeNumberField(key, preferences.getInt(key, 0));
            } catch (Exception ignore) {
                return false;
            }
            return true;
        }
    }

    private static final class AnnotationProcessStrategy implements SharedPreferencesProcessStrategy {

        private final HashMap<String, Preference> supportedMap;

        AnnotationProcessStrategy(Class cls) {
            this.supportedMap = getSupportedPreferencesMap(cls);
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException {
            final JsonToken token = jsonParser.nextToken();
            if (token == null) return false;
            final Preference preference = supportedMap.get(key);
            if (preference == null || !preference.exportable()) return false;
            switch (preference.type()) {
                case PreferenceType.BOOLEAN: {
                    editor.putBoolean(key, jsonParser.getValueAsBoolean());
                    break;
                }
                case PreferenceType.INT: {
                    editor.putInt(key, jsonParser.getValueAsInt());
                    break;
                }
                case PreferenceType.LONG: {
                    editor.putLong(key, jsonParser.getValueAsLong());
                    break;
                }
                case PreferenceType.FLOAT: {
                    editor.putFloat(key, (float) jsonParser.getValueAsDouble());
                    break;
                }
                case PreferenceType.STRING: {
                    editor.putString(key, jsonParser.getValueAsString());
                    break;
                }
                default: {
                    break;
                }
            }
            return true;
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) throws IOException {
            final Preference preference = supportedMap.get(key);
            if (preference == null || !preference.exportable()) return false;
            try {
                switch (preference.type()) {
                    case PreferenceType.BOOLEAN:
                        jsonGenerator.writeBooleanField(key, preferences.getBoolean(key, preference.defaultBoolean()));
                        break;
                    case PreferenceType.INT:
                        jsonGenerator.writeNumberField(key, preferences.getInt(key, preference.defaultInt()));
                        break;
                    case PreferenceType.LONG:
                        jsonGenerator.writeNumberField(key, preferences.getLong(key, preference.defaultLong()));
                        break;
                    case PreferenceType.FLOAT:
                        jsonGenerator.writeNumberField(key, preferences.getFloat(key, preference.defaultFloat()));
                        break;
                    case PreferenceType.STRING:
                        jsonGenerator.writeStringField(key, preferences.getString(key, preference.defaultString()));
                        break;
                    default:
                        break;
                }
            } catch (ClassCastException e) {
                return false;
            }
            return true;
        }
    }

}
