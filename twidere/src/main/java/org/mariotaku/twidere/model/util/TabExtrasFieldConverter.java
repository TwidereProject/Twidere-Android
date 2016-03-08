package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.tab.extra.TabExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.JsonSerializer;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/6.
 */
public class TabExtrasFieldConverter implements CursorFieldConverter<TabExtras> {
    @Override
    public TabExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String tabType = CustomTabUtils.getTabTypeAlias(cursor.getString(cursor.getColumnIndex(Tabs.TYPE)));
        if (TextUtils.isEmpty(tabType)) return null;
        return CustomTabUtils.parseTabExtras(tabType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, TabExtras object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, JsonSerializer.serialize(object));
    }
}
