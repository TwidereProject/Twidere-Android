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

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 15/11/27.
 */
public class JSONObjectConverter implements CursorFieldConverter<JSONObject> {
    @Override
    public JSONObject parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String string = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(string)) return null;
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public void writeField(ContentValues values, JSONObject object, String columnName, ParameterizedType fieldType) {
        if (object != null) {
            values.put(columnName, object.toString());
        }
    }
}
