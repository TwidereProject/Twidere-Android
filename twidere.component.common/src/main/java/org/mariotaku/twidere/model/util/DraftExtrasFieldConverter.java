/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.draft.ActionExtras;
import org.mariotaku.twidere.model.draft.QuoteStatusActionExtras;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras;
import org.mariotaku.twidere.model.draft.StatusObjectActionExtras;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/2/20.
 */
public class DraftExtrasFieldConverter implements CursorFieldConverter<ActionExtras> {
    @Override
    public ActionExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String actionType = cursor.getString(cursor.getColumnIndex(Drafts.ACTION_TYPE));
        final String json = cursor.getString(columnIndex);
        return parseExtras(actionType, json);
    }

    @Override
    public void writeField(ContentValues values, ActionExtras object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, serializeExtras(object));
    }

    @Nullable
    public static ActionExtras parseExtras(final String actionType, final String json) throws IOException {
        if (TextUtils.isEmpty(actionType) || TextUtils.isEmpty(json)) return null;
        switch (actionType) {
            case Draft.Action.UPDATE_STATUS_COMPAT_1:
            case Draft.Action.UPDATE_STATUS_COMPAT_2:
            case Draft.Action.UPDATE_STATUS:
            case Draft.Action.REPLY: {
                return LoganSquare.parse(json, UpdateStatusActionExtras.class);
            }
            case Draft.Action.SEND_DIRECT_MESSAGE_COMPAT:
            case Draft.Action.SEND_DIRECT_MESSAGE: {
                return LoganSquare.parse(json, SendDirectMessageActionExtras.class);
            }
            case Draft.Action.FAVORITE:
            case Draft.Action.RETWEET: {
                return LoganSquare.parse(json, StatusObjectActionExtras.class);
            }
            case Draft.Action.QUOTE: {
                return LoganSquare.parse(json, QuoteStatusActionExtras.class);
            }
        }
        return null;
    }

    public static String serializeExtras(final ActionExtras object) throws IOException {
        if (object == null) return null;
        return LoganSquare.serialize(object);
    }
}
