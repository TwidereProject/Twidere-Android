package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.message.MessageExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 2017/2/9.
 */
public class MessageExtrasConverter implements CursorFieldConverter<MessageExtras> {
    @Override
    public MessageExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String messageType = cursor.getString(cursor.getColumnIndex(Messages.MESSAGE_TYPE));
        if (TextUtils.isEmpty(messageType)) return null;
        return MessageExtras.parse(messageType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, MessageExtras object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, LoganSquare.serialize(object));
    }
}
