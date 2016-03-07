package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.UserKey;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UserKeyCursorFieldConverter implements CursorFieldConverter<UserKey> {
    @Override
    public UserKey parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        return UserKey.valueOf(cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, UserKey object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, object.toString());
    }

}
