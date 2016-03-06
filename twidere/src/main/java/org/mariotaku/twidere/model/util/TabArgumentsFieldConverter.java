package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.tab.Arguments;
import org.mariotaku.twidere.model.tab.UserArguments;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.JsonSerializer;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/6.
 */
public class TabArgumentsFieldConverter implements CursorFieldConverter<Arguments> {
    @Override
    public Arguments parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String tabType = cursor.getString(cursor.getColumnIndex(Tabs.TYPE));
        if (TextUtils.isEmpty(tabType)) return null;
        switch (tabType) {
            case CustomTabType.FAVORITES:
            case CustomTabType.USER_TIMELINE:
                return JsonSerializer.parse(cursor.getString(columnIndex), UserArguments.class);
        }
        return JsonSerializer.parse(cursor.getString(columnIndex), Arguments.class);
    }

    @Override
    public void writeField(ContentValues values, Arguments object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, JsonSerializer.serialize(object));
    }
}
