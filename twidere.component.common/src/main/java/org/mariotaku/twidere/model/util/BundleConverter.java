package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/2/20.
 */
public class BundleConverter implements CursorFieldConverter<Bundle> {
    @Override
    public Bundle parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        return null;
    }

    @Override
    public void writeField(ContentValues values, Bundle object, String columnName, ParameterizedType fieldType) {

    }
}
