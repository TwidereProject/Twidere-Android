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

import org.mariotaku.library.logansquare.extension.LoganSquareWrapper;
import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

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

    private void writeObject(ContentValues values, Object object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        final Type rawType = fieldType.getRawType();
        if (!(rawType instanceof Class)) throw new UnsupportedOperationException();
        final Class rawCls = (Class) rawType;
        if (List.class.isAssignableFrom(rawCls)) {
            values.put(columnName, LoganSquareWrapper.serialize((List) object,
                    (Class) fieldType.getActualTypeArguments()[0]));
        } else if (Map.class.isAssignableFrom(rawCls)) {
            //noinspection unchecked
            values.put(columnName, LoganSquareWrapper.serialize((Map) object,
                    (Class) fieldType.getActualTypeArguments()[1]));
        } else if (rawCls.isArray()) {
            final Class componentType = rawCls.getComponentType();
            values.put(columnName, LoganSquareWrapper.serialize((List) Arrays.asList((Object[]) object),
                    componentType));
        } else {
            values.put(columnName, LoganSquareWrapper.serialize(object));
        }
    }

    private Object getObject(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final Type rawType = fieldType.getRawType();
        if (!(rawType instanceof Class)) throw new UnsupportedOperationException();
        final Class rawCls = (Class) rawType;
        final String string = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(string)) return null;
        if (List.class.isAssignableFrom(rawCls)) {
            // Parse list
            return LoganSquareWrapper.parseList(string,
                    (Class) fieldType.getActualTypeArguments()[0]);
        } else if (Map.class.isAssignableFrom(rawCls)) {
            return LoganSquareWrapper.parseMap(string,
                    (Class) fieldType.getActualTypeArguments()[1]);
        } else if (rawCls.isArray()) {
            final Class componentType = rawCls.getComponentType();
            List<?> list = LoganSquareWrapper.parseList(string, componentType);
            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        }
        return LoganSquareWrapper.parse(string, rawCls);
    }
}
