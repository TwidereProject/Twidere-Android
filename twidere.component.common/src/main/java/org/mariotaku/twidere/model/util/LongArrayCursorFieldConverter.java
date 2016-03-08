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

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 15/11/27.
 */
public class LongArrayCursorFieldConverter implements CursorFieldConverter<long[]> {
    @Override
    public long[] parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String string = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(string)) return null;

        long[] temp = new long[0];
        int len = 0;
        int offset = 0;
        try {
            while (true) {
                int index = string.indexOf(',', offset);
                if (index == -1) {
                    temp = putElement(temp, Long.parseLong(string.substring(offset)), len++);
                    long[] out = new long[len];
                    System.arraycopy(temp, 0, out, 0, len);
                    return out;
                } else {
                    temp = putElement(temp, Long.parseLong(string.substring(offset, index)), len++);
                    offset = (index + 1);
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void writeField(ContentValues values, long[] object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, j = object.length; i < j; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(object[i]);
        }
        values.put(columnName, sb.toString());
    }

    private static long[] putElement(long[] array, long element, int index) {
        long[] out;
        if (index < array.length) {
            out = array;
        } else {
            out = new long[Math.max(1, array.length) * 2];
            System.arraycopy(array, 0, out, 0, array.length);
        }
        out[index] = element;
        return out;
    }
}
