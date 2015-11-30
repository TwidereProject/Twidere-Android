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

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 15/11/27.
 */
public class LongArrayConverter implements CursorFieldConverter<long[]> {
    @Override
    public long[] parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        return TwidereArrayUtils.parseLongArray(cursor.getString(columnIndex), ',');
    }

    @Override
    public void writeField(ContentValues values, long[] object, String columnName, ParameterizedType fieldType) {
        values.put(columnName, TwidereArrayUtils.toString(object, ',', false));
    }
}
