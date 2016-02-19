/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.JsonMapper;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.util.LoganSquareMapperFinder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/11/27.
 */
public class LoganSquareCursorFieldConverter implements CursorFieldConverter<Object> {
    @Override
    public Object parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        try {
            return getObject(cursor, columnIndex, fieldType);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void writeField(ContentValues values, Object object, String columnName, ParameterizedType fieldType) {
        try {
            writeObject(values, object, columnName, fieldType);
        } catch (IOException ignored) {
        }
    }

    private <T> void writeObject(ContentValues values, T object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        if (isArray(fieldType)) {
            final Class<?> component = getArrayComponent(fieldType);
            //noinspection unchecked
            JsonMapper<Object> mapper = (JsonMapper<Object>) LoganSquareMapperFinder.mapperFor(component);
            values.put(columnName, mapper.serialize(Arrays.asList((Object[]) object)));
        } else if (fieldType.getRawType() == List.class) {
            JsonMapper<Object> mapper = LoganSquareMapperFinder.mapperFor(fieldType.getActualTypeArguments()[0]);
            //noinspection unchecked
            values.put(columnName, mapper.serialize((List) object));
        } else if (fieldType.getRawType() == Map.class) {
            JsonMapper<Object> mapper = LoganSquareMapperFinder.mapperFor(fieldType.getActualTypeArguments()[1]);
            //noinspection unchecked
            values.put(columnName, mapper.serialize((Map) object));
        } else {
            JsonMapper<T> mapper = LoganSquareMapperFinder.mapperFor(fieldType);
            values.put(columnName, mapper.serialize(object));
        }
    }

    private <T> T getObject(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String string = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(string)) return null;
        if (isArray(fieldType)) {
            final Class<?> component = getArrayComponent(fieldType);
            //noinspection unchecked
            JsonMapper<Object> mapper = (JsonMapper<Object>) LoganSquareMapperFinder.mapperFor(component);
            final List<Object> list = mapper.parseList(string);
            //noinspection unchecked
            return (T) list.toArray((Object[]) Array.newInstance(component, list.size()));
        } else if (fieldType.getRawType() == List.class) {
            JsonMapper<Object> mapper = LoganSquareMapperFinder.mapperFor(fieldType.getActualTypeArguments()[0]);
            //noinspection unchecked
            return (T) mapper.parseList(string);
        } else if (fieldType.getRawType() == Map.class) {
            JsonMapper<Object> mapper = LoganSquareMapperFinder.mapperFor(fieldType.getActualTypeArguments()[1]);
            //noinspection unchecked
            return (T) mapper.parseMap(string);
        } else {
            JsonMapper<T> mapper = LoganSquareMapperFinder.mapperFor(fieldType);
            return mapper.parse(string);
        }
    }

    private boolean isArray(Type type) {
        if (type instanceof Class) {
            return ((Class) type).isArray();
        } else if (type instanceof ParameterizedType) {
            return isArray(((ParameterizedType) type).getRawType());
        }
        return false;
    }

    private Class getArrayComponent(Type type) {
        if (type instanceof Class) {
            if (((Class) type).isArray()) {
                return ((Class) type).getComponentType();
            }
        } else if (type instanceof ParameterizedType) {
            return getArrayComponent(((ParameterizedType) type).getRawType());
        }
        throw new UnsupportedOperationException();
    }
}
