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
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.message.conversation.ConversationExtras;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 2017/2/9.
 */
public class ConversationExtrasConverter implements CursorFieldConverter<ConversationExtras> {
    @Override
    public ConversationExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String extrasType = cursor.getString(cursor.getColumnIndex(Messages.Conversations.CONVERSATION_EXTRAS_TYPE));
        if (TextUtils.isEmpty(extrasType)) return null;
        return ConversationExtras.parse(extrasType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, ConversationExtras object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, LoganSquare.serialize(object));
    }
}
