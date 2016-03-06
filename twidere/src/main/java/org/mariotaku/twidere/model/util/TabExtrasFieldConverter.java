package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.tab.Arguments;
import org.mariotaku.twidere.model.tab.Extras;
import org.mariotaku.twidere.model.tab.UserArguments;
import org.mariotaku.twidere.model.tab.UserMentionTabExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.JsonSerializer;

import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/6.
 */
public class TabExtrasFieldConverter implements CursorFieldConverter<Extras> {
    @Override
    public Extras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String tabType = cursor.getString(cursor.getColumnIndex(Tabs.TYPE));
        if (TextUtils.isEmpty(tabType)) return null;
        switch (tabType) {
            case CustomTabType.NOTIFICATIONS_TIMELINE:
                return JsonSerializer.parse(cursor.getString(columnIndex), UserMentionTabExtras.class);
        }
        return null;
    }

    @Override
    public void writeField(ContentValues values, Extras object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, JsonSerializer.serialize(object));
    }
}
