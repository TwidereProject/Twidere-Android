package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.draft.ActionExtra;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtra;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/2/20.
 */
public class DraftExtrasConverter implements CursorFieldConverter<ActionExtra> {
    @Override
    public ActionExtra parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String actionType = cursor.getString(cursor.getColumnIndex(Drafts.ACTION_TYPE));
        if (TextUtils.isEmpty(actionType)) return null;
        switch (actionType) {
            case "0":
            case "1":
            case Draft.Action.UPDATE_STATUS:
            case Draft.Action.REPLY:
            case Draft.Action.QUOTE: {
                final String string = cursor.getString(columnIndex);
                if (TextUtils.isEmpty(string)) return null;
                return LoganSquare.parse(string, UpdateStatusActionExtra.class);
            }
            case "2":
            case Draft.Action.SEND_DIRECT_MESSAGE: {
                final String string = cursor.getString(columnIndex);
                if (TextUtils.isEmpty(string)) return null;
                return LoganSquare.parse(string, SendDirectMessageActionExtra.class);
            }
        }
        return null;
    }

    @Override
    public void writeField(ContentValues values, ActionExtra object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, LoganSquare.serialize(object));
    }
}
