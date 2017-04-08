/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.twidere.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.tab.argument.TabArguments;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by mariotaku on 16/3/6.
 */
public class TabArgumentsFieldConverter implements CursorFieldConverter<TabArguments> {

    @Override
    public TabArguments parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String tabType = Tab.getTypeAlias(cursor.getString(cursor.getColumnIndex(Tabs.TYPE)));
        if (TextUtils.isEmpty(tabType)) return null;
        return TabArguments.parse(tabType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, TabArguments object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        try {
            values.put(columnName, LoganSquare.serialize(object));
        } catch (IOException e) {
            // Ignore
        }
    }
}
