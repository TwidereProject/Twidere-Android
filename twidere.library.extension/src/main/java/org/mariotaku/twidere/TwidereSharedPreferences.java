/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.twidere;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import org.mariotaku.twidere.provider.TwidereDataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class TwidereSharedPreferences implements SharedPreferences {

    private final ContentResolver mResolver;

    TwidereSharedPreferences(final Context context) {
        mResolver = context.getContentResolver();
    }

    @Override
    public boolean contains(final String key) {
        if (key == null) throw new NullPointerException();
        final Uri uri = Uri.withAppendedPath(TwidereDataStore.Preferences.CONTENT_URI, key);
        final Cursor cur = mResolver.query(uri, TwidereDataStore.Preferences.MATRIX_COLUMNS, null, null, null);
        if (cur == null) return false;
        try {
            return cur.getCount() > 0;
        } finally {
            cur.close();
        }
    }

    @Override
    public Editor edit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ?> getAll() {
        final Cursor cur = mResolver.query(TwidereDataStore.Preferences.CONTENT_URI, TwidereDataStore.Preferences.MATRIX_COLUMNS,
                null, null, null);
        if (cur == null) return null;
        final HashMap<String, Object> map = new HashMap<>();
        final int key_idx = cur.getColumnIndex(TwidereDataStore.Preferences.KEY);
        final int value_idx = cur.getColumnIndex(TwidereDataStore.Preferences.VALUE);
        final int type_idx = cur.getColumnIndex(TwidereDataStore.Preferences.TYPE);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            final Object value;
            switch (cur.getInt(type_idx)) {
                case TwidereDataStore.Preferences.TYPE_BOOLEAN:
                    value = Boolean.valueOf(cur.getString(value_idx));
                    break;
                case TwidereDataStore.Preferences.TYPE_INTEGER:
                    value = cur.getInt(value_idx);
                    break;
                case TwidereDataStore.Preferences.TYPE_LONG:
                    value = cur.getLong(value_idx);
                    break;
                case TwidereDataStore.Preferences.TYPE_FLOAT:
                    value = cur.getFloat(value_idx);
                    break;
                case TwidereDataStore.Preferences.TYPE_STRING:
                    value = cur.getString(value_idx);
                    break;
                case TwidereDataStore.Preferences.TYPE_NULL:
                    value = null;
                    break;
                default:
                    continue;
            }
            map.put(cur.getString(key_idx), value);
            cur.moveToNext();
        }
        cur.close();
        return map;
    }

    @Override
    public boolean getBoolean(final String key, final boolean defValue) {
        if (key == null) throw new NullPointerException();
        try {
            return (Boolean) getPreferenceObject(key);
        } catch (final NullPointerException e) {
            return defValue;
        }
    }

    @Override
    public float getFloat(final String key, final float defValue) {
        if (key == null) throw new NullPointerException();
        try {
            return (Float) getPreferenceObject(key);
        } catch (final NullPointerException e) {
            return defValue;
        }
    }

    @Override
    public int getInt(final String key, final int defValue) {
        if (key == null) throw new NullPointerException();
        try {
            return (Integer) getPreferenceObject(key);
        } catch (final NullPointerException e) {
            return defValue;
        }
    }

    @Override
    public long getLong(final String key, final long defValue) {
        if (key == null) throw new NullPointerException();
        try {
            return (Long) getPreferenceObject(key);
        } catch (final NullPointerException e) {
            return defValue;
        }
    }

    public Object getPreferenceObject(final String key) {
        final Uri uri = Uri.withAppendedPath(TwidereDataStore.Preferences.CONTENT_URI, key);
        final Cursor cur = mResolver.query(uri, TwidereDataStore.Preferences.MATRIX_COLUMNS, null, null, null);
        if (cur == null) throw new NullPointerException();
        try {
            final int key_idx = cur.getColumnIndex(TwidereDataStore.Preferences.KEY);
            final int value_idx = cur.getColumnIndex(TwidereDataStore.Preferences.VALUE);
            final int type_idx = cur.getColumnIndex(TwidereDataStore.Preferences.TYPE);
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                if (!key.equals(cur.getString(key_idx))) {
                    cur.moveToNext();
                    continue;
                }
                final Object value;
                switch (cur.getInt(type_idx)) {
                    case TwidereDataStore.Preferences.TYPE_BOOLEAN:
                        value = Boolean.valueOf(cur.getString(value_idx));
                        break;
                    case TwidereDataStore.Preferences.TYPE_INTEGER:
                        value = cur.getInt(value_idx);
                        break;
                    case TwidereDataStore.Preferences.TYPE_LONG:
                        value = cur.getLong(value_idx);
                        break;
                    case TwidereDataStore.Preferences.TYPE_FLOAT:
                        value = cur.getFloat(value_idx);
                        break;
                    case TwidereDataStore.Preferences.TYPE_STRING:
                        value = cur.getString(value_idx);
                        break;
                    case TwidereDataStore.Preferences.TYPE_NULL:
                        value = null;
                        break;
                    default:
                        continue;
                }
                return value;
            }
            throw new NullPointerException();
        } finally {
            cur.close();
        }
    }

    @Override
    public String getString(final String key, final String defValue) {
        if (key == null) throw new NullPointerException();
        try {
            return (String) getPreferenceObject(key);
        } catch (final NullPointerException e) {
            return defValue;
        }
    }

    @Override
    public Set<String> getStringSet(final String key, final Set<String> defValue) {
        if (key == null) throw new NullPointerException();
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException();
    }
}
