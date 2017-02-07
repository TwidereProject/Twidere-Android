package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.draft.ActionExtras;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras;
import org.mariotaku.twidere.model.draft.StatusObjectExtras;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/2/20.
 */
public class DraftExtrasConverter implements CursorFieldConverter<ActionExtras> {
    @Override
    public ActionExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String actionType = cursor.getString(cursor.getColumnIndex(Drafts.ACTION_TYPE));
        final String json = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(actionType) || TextUtils.isEmpty(json)) return null;
        switch (actionType) {
            case Draft.Action.UPDATE_STATUS_COMPAT_1:
            case Draft.Action.UPDATE_STATUS_COMPAT_2:
            case Draft.Action.UPDATE_STATUS:
            case Draft.Action.REPLY:
            case Draft.Action.QUOTE: {
                return LoganSquare.parse(json, UpdateStatusActionExtras.class);
            }
            case Draft.Action.SEND_DIRECT_MESSAGE_COMPAT:
            case Draft.Action.SEND_DIRECT_MESSAGE: {
                return LoganSquare.parse(json, SendDirectMessageActionExtras.class);
            }
            case Draft.Action.FAVORITE:
            case Draft.Action.RETWEET: {
                return LoganSquare.parse(json, StatusObjectExtras.class);
            }
        }
        return null;
    }

    @Override
    public void writeField(ContentValues values, ActionExtras object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, LoganSquare.serialize(object));
    }
}
