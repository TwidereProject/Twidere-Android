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
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.documentfile.provider.DocumentFile;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.library.exportablepreferences.PreferencesExporter;
import org.mariotaku.library.exportablepreferences.annotation.PreferenceType;
import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.model.FiltersData;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
    public static void exportData(final Context context, @NonNull final Uri dst, final int flags) throws IOException {
        try (OutputStream fos = context.getContentResolver().openOutputStream(dst);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            if (hasFlag(flags, FLAG_PREFERENCES)) {
                exportSharedPreferencesData(zos, context, SHARED_PREFERENCES_NAME, ENTRY_PREFERENCES,
                        new PreferencesExporterStrategy(SharedPreferenceConstants.class));
            }
            if (hasFlag(flags, FLAG_NICKNAMES)) {
                exportSharedPreferencesData(zos, context, USER_NICKNAME_PREFERENCES_NAME, ENTRY_NICKNAMES,
                        ConvertToStringProcessStrategy.SINGLETON);
            }
            if (hasFlag(flags, FLAG_USER_COLORS)) {
                exportSharedPreferencesData(zos, context, USER_COLOR_PREFERENCES_NAME, ENTRY_USER_COLORS,
                        ConvertToIntProcessStrategy.SINGLETON);
            }
            if (hasFlag(flags, FLAG_HOST_MAPPING)) {
                exportSharedPreferencesData(zos, context, HOST_MAPPING_PREFERENCES_NAME, ENTRY_HOST_MAPPING,
                        ConvertToStringProcessStrategy.SINGLETON);
            }
            if (hasFlag(flags, FLAG_KEYBOARD_SHORTCUTS)) {
                exportSharedPreferencesData(zos, context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, ENTRY_KEYBOARD_SHORTCUTS,
                        ConvertToStringProcessStrategy.SINGLETON);
            }
            if (hasFlag(flags, FLAG_FILTERS)) {
                // TODO export filters
                FiltersData data = new FiltersData();

                final ContentResolver cr = context.getContentResolver();
                data.setUsers(queryAll(cr, Filters.Users.CONTENT_URI, Filters.Users.COLUMNS,
                        FiltersData.UserItem.class));
                data.setKeywords(queryAll(cr, Filters.Keywords.CONTENT_URI, Filters.Keywords.COLUMNS,
                        FiltersData.BaseItem.class));
                data.setSources(queryAll(cr, Filters.Sources.CONTENT_URI, Filters.Sources.COLUMNS,
                        FiltersData.BaseItem.class));
                data.setLinks(queryAll(cr, Filters.Links.CONTENT_URI, Filters.Links.COLUMNS,
                        FiltersData.BaseItem.class));
                exportItem(zos, ENTRY_FILTERS, FiltersData.class, data);
            }
            if (hasFlag(flags, FLAG_TABS)) {
                // TODO export tabs
                final ContentResolver cr = context.getContentResolver();
                final Cursor c = cr.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null);
                if (c != null) {
                    final List<Tab> tabs = new ArrayList<>(c.getCount());
                    try {
                        final ObjectCursor.CursorIndices<Tab> ci = ObjectCursor.indicesFrom(c, Tab.class);
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
        }
    }

    private static <T> List<T> queryAll(ContentResolver cr, Uri uri, String[] projection,
            Class<T> cls) throws IOException {
        Cursor c = cr.query(uri, projection, null, null, null);
        if (c == null) return null;
        try {
            final ObjectCursor.CursorIndices<T> ci = ObjectCursor.indicesFrom(c, cls);
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
    public static int getImportedSettingsFlags(@NonNull final Context context, @NonNull final DocumentFile src) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(src.getUri());
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            int flags = 0;
            List<String> entryNames = new ArrayList<>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryNames.add(entry.getName());
            }
            if (entryNames.contains(ENTRY_PREFERENCES)) {
                flags |= FLAG_PREFERENCES;
            }
            if (entryNames.contains(ENTRY_NICKNAMES)) {
                flags |= FLAG_NICKNAMES;
            }
            if (entryNames.contains(ENTRY_USER_COLORS)) {
                flags |= FLAG_USER_COLORS;
            }
            if (entryNames.contains(ENTRY_HOST_MAPPING)) {
                flags |= FLAG_HOST_MAPPING;
            }
            if (entryNames.contains(ENTRY_KEYBOARD_SHORTCUTS)) {
                flags |= FLAG_KEYBOARD_SHORTCUTS;
            }
            if (entryNames.contains(ENTRY_FILTERS)) {
                flags |= FLAG_FILTERS;
            }
            if (entryNames.contains(ENTRY_TABS)) {
                flags |= FLAG_TABS;
            }
            return flags;
        }
    }

    public static void importData(final Context context, final Uri src, final int flags) throws IOException {
        if (src == null) throw new FileNotFoundException();
        try (InputStream inputStream = context.getContentResolver().openInputStream(src);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)
        ) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = zipInputStream.read(buffer, 0, 1024)) >= 0) {
                    stringBuilder.append(new String(buffer, 0, read));
                }
                String data = stringBuilder.toString();
                if (hasFlag(flags, FLAG_PREFERENCES)) {
                    importSharedPreferencesData(entry, context, SHARED_PREFERENCES_NAME, ENTRY_PREFERENCES,
                            new PreferencesExporterStrategy(SharedPreferenceConstants.class), data);
                }
                if (hasFlag(flags, FLAG_NICKNAMES)) {
                    importSharedPreferencesData(entry, context, USER_NICKNAME_PREFERENCES_NAME, ENTRY_NICKNAMES,
                            ConvertToStringProcessStrategy.SINGLETON, data);
                }
                if (hasFlag(flags, FLAG_USER_COLORS)) {
                    importSharedPreferencesData(entry, context, USER_COLOR_PREFERENCES_NAME, ENTRY_USER_COLORS,
                            ConvertToIntProcessStrategy.SINGLETON, data);
                }
                if (hasFlag(flags, FLAG_HOST_MAPPING)) {
                    importSharedPreferencesData(entry, context, HOST_MAPPING_PREFERENCES_NAME, ENTRY_HOST_MAPPING,
                            ConvertToStringProcessStrategy.SINGLETON, data);
                }
                if (hasFlag(flags, FLAG_KEYBOARD_SHORTCUTS)) {
                    importSharedPreferencesData(entry, context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME,
                            ENTRY_KEYBOARD_SHORTCUTS, ConvertToStringProcessStrategy.SINGLETON, data);
                }
                if (hasFlag(flags, FLAG_FILTERS)) {
                    importItem(context, entry, ENTRY_FILTERS, FiltersData.class, data, new ContentResolverProcessStrategy<FiltersData>() {
                        @Override
                        public boolean importItem(ContentResolver cr, FiltersData filtersData) throws IOException {
                            if (filtersData == null) return false;
                            insertBase(cr, Filters.Keywords.CONTENT_URI, filtersData.getKeywords());
                            insertBase(cr, Filters.Sources.CONTENT_URI, filtersData.getSources());
                            insertBase(cr, Filters.Links.CONTENT_URI, filtersData.getLinks());
                            insertUser(cr, Filters.Users.CONTENT_URI, filtersData.getUsers());
                            return true;
                        }

                        void insertBase(ContentResolver cr, Uri uri, List<FiltersData.BaseItem> items) throws IOException {
                            if (items == null) return;
                            final ObjectCursor.ValuesCreator<FiltersData.BaseItem> baseItemCreator =
                                    ObjectCursor.valuesCreatorFrom(FiltersData.BaseItem.class);
                            List<ContentValues> values = new ArrayList<>(items.size());
                            for (FiltersData.BaseItem item : items) {
                                values.add(baseItemCreator.create(item));
                            }
                            ContentResolverUtils.bulkInsert(cr, uri, values);
                        }

                        void insertUser(ContentResolver cr, Uri uri, List<FiltersData.UserItem> items) throws IOException {
                            if (items == null) return;
                            final ObjectCursor.ValuesCreator<FiltersData.UserItem> userItemCreator =
                                    ObjectCursor.valuesCreatorFrom(FiltersData.UserItem.class);
                            List<ContentValues> values = new ArrayList<>(items.size());
                            for (FiltersData.UserItem item : items) {
                                values.add(userItemCreator.create(item));
                            }
                            ContentResolverUtils.bulkInsert(cr, uri, values);
                        }
                    });
                }
                if (hasFlag(flags, FLAG_TABS)) {
                    final ObjectCursor.ValuesCreator<Tab> creator = ObjectCursor.valuesCreatorFrom(Tab.class);
                    importItemsList(context, entry, ENTRY_TABS, Tab.class, data, (cr, items) -> {
                        if (items == null) return false;
                        List<ContentValues> values = new ArrayList<>(items.size());
                        for (Tab item : items) {
                            values.add(creator.create(item));
                        }
                        cr.delete(Tabs.CONTENT_URI, null, null);
                        ContentResolverUtils.bulkInsert(cr, Tabs.CONTENT_URI, values);
                        return true;
                    });
                }
            }
        }
    }

    private static boolean hasFlag(final int flags, final int flag) {
        return (flags & flag) != 0;
    }

    private static void importSharedPreferencesData(@NonNull final ZipEntry entry, @NonNull final Context context,
            @NonNull final String preferencesName, @NonNull final String entryName,
            @NonNull final SharedPreferencesProcessStrategy strategy,
            @NonNull final String data) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Objects.equals(entry.getName(), entryName)) {
                return;
            }
        } else {
            if (entry.getName().equals(entryName)) {
                return;
            }
        }
        final JsonParser jsonParser = LoganSquare.JSON_FACTORY.createParser(data);
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.skipChildren();
            return;
        }
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        strategy.importAll(jsonParser, editor);
        editor.apply();
    }

    private static void exportSharedPreferencesData(@NonNull final ZipOutputStream zos, final Context context,
            @NonNull final String preferencesName, @NonNull final String entryName,
            @NonNull final SharedPreferencesProcessStrategy strategy) throws IOException {
        final SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        zos.putNextEntry(new ZipEntry(entryName));
        final JsonGenerator jsonGenerator = LoganSquare.JSON_FACTORY.createGenerator(zos);
        jsonGenerator.writeStartObject();
        strategy.exportAll(jsonGenerator, preferences);
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        zos.closeEntry();
    }

    private static <T> void importItemsList(@NonNull final Context context,
            @NonNull final ZipEntry entry,
            @NonNull final String entryName,
            @NonNull final Class<T> itemCls,
            @NonNull final String data,
            @NonNull final ContentResolverProcessStrategy<List<T>> strategy)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Objects.equals(entry.getName(), entryName)) {
                return;
            }
        } else {
            if (entry.getName().equals(entryName)) {
                return;
            }
        }
        List<T> itemsList = JsonSerializer.parseList(data, itemCls);
        strategy.importItem(context.getContentResolver(), itemsList);
    }


    private static <T> void exportItemsList(@NonNull final ZipOutputStream zos,
            @NonNull final String entryName,
            @NonNull final Class<T> itemCls,
            @NonNull final List<T> itemList) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        String json = LoganSquare.serialize(itemList);
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write(json);
        writer.flush();
        zos.closeEntry();
    }

    private static <T> void importItem(@NonNull final Context context,
            @NonNull final ZipEntry entry,
            @NonNull final String entryName,
            @NonNull final Class<T> itemCls,
            @NonNull final String data,
            @NonNull final ContentResolverProcessStrategy<T> strategy)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Objects.equals(entry.getName(), entryName)) {
                return;
            }
        } else {
            if (entry.getName().equals(entryName)) {
                return;
            }
        }
        T item = JsonSerializer.parse(data, itemCls);
        strategy.importItem(context.getContentResolver(), item);
    }


    private static <T> void exportItem(@NonNull final ZipOutputStream zos,
            @NonNull final String entryName,
            @NonNull final Class<T> itemCls,
            @NonNull final T item) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        String json = LoganSquare.serialize(item);
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write(json);
        writer.flush();
        zos.closeEntry();
    }

    private interface ContentResolverProcessStrategy<T> {
        boolean importItem(ContentResolver cr, T item) throws IOException;
    }

    private abstract static class SharedPreferencesProcessStrategy {

        public boolean importAll(JsonParser jsonParser, SharedPreferences.Editor editor) throws IOException {
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String key = jsonParser.getCurrentName();
                importValue(jsonParser, key, editor);
            }
            return true;
        }

        public boolean exportAll(JsonGenerator jsonGenerator, SharedPreferences preferences) throws IOException {
            final Map<String, ?> map = preferences.getAll();
            for (String key : map.keySet()) {
                exportValue(jsonGenerator, key, preferences);
            }
            return true;
        }

        public abstract boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException;

        public abstract boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) throws IOException;
    }

    private static final class ConvertToStringProcessStrategy extends SharedPreferencesProcessStrategy {

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

    private static final class ConvertToIntProcessStrategy extends SharedPreferencesProcessStrategy {

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

    private static final class PreferencesExporterStrategy extends SharedPreferencesProcessStrategy {

        private final Class<?> cls;

        PreferencesExporterStrategy(Class<?> cls) {
            this.cls = cls;
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public boolean importAll(JsonParser jsonParser, SharedPreferences.Editor editor) throws IOException {
            if (jsonParser.getCurrentToken() == null) {
                jsonParser.nextToken();
            }
            if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
                return false;
            }
            PreferencesExporter exporter = PreferencesExporter.get(cls);
            PreferencesExporter.ImportHandler handler = exporter.importTo(editor);
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                switch (handler.getType(fieldName)) {
                    case PreferenceType.BOOLEAN: {
                        handler.onBoolean(fieldName, jsonParser.getValueAsBoolean());
                        break;
                    }
                    case PreferenceType.INT: {
                        handler.onInt(fieldName, jsonParser.getValueAsInt());
                        break;
                    }
                    case PreferenceType.LONG: {
                        handler.onLong(fieldName, jsonParser.getValueAsLong());
                        break;
                    }
                    case PreferenceType.FLOAT: {
                        handler.onFloat(fieldName, (float) jsonParser.getValueAsDouble());
                        break;
                    }
                    case PreferenceType.STRING: {
                        handler.onString(fieldName, jsonParser.getValueAsString());
                        break;
                    }
                    case PreferenceType.STRING_SET: {
                        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                            Set<String> stringSet = new HashSet<>();
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                stringSet.add(jsonParser.getValueAsString(null));
                            }
                            handler.onStringSet(fieldName, stringSet);
                        } else {
                            handler.onStringSet(fieldName, null);
                        }
                        break;
                    }
                }

                jsonParser.skipChildren();
            }
            return true;
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public boolean exportAll(final JsonGenerator jsonGenerator, SharedPreferences preferences) throws IOException {
            PreferencesExporter exporter = PreferencesExporter.get(cls);
            exporter.exportTo(preferences, new PreferencesExporter.ExportHandler() {
                @Override
                public void onBoolean(String key, boolean value) throws IOException {
                    jsonGenerator.writeBooleanField(key, value);
                }

                @Override
                public void onInt(String key, int value) throws IOException {
                    jsonGenerator.writeNumberField(key, value);
                }

                @Override
                public void onLong(String key, long value) throws IOException {
                    jsonGenerator.writeNumberField(key, value);
                }

                @Override
                public void onFloat(String key, float value) throws IOException {
                    jsonGenerator.writeNumberField(key, value);
                }

                @Override
                public void onString(String key, String value) throws IOException {
                    jsonGenerator.writeStringField(key, value);
                }

                @Override
                public void onStringSet(String key, Set<String> value) throws IOException {
                    if (value != null) {
                        jsonGenerator.writeArrayFieldStart(key);
                        for (String s : value) {
                            jsonGenerator.writeString(s);
                        }
                        jsonGenerator.writeEndArray();
                    } else {
                        jsonGenerator.writeNullField(key);
                    }
                }

            });
            return true;
        }

        @Override
        public boolean importValue(JsonParser jsonParser, String key, SharedPreferences.Editor editor) throws IOException {
            return false;
        }

        @Override
        public boolean exportValue(JsonGenerator jsonGenerator, String key, SharedPreferences preferences) throws IOException {
            return false;
        }
    }

}
