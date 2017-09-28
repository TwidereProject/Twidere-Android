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

import org.mariotaku.commons.objectcursor.AbsArrayCursorFieldConverter;

public class FilterStringsFieldConverter extends AbsArrayCursorFieldConverter<String> {
    @Override
    protected String[] newArray(int size) {
        return new String[size];
    }

    @Override
    protected String convertToString(String item) {
        if (item == null || item.isEmpty()) return "";
        return '\\' + item + '\\';
    }

    @Override
    protected String parseItem(String str) {
        if (str == null || str.isEmpty()) return null;
        final int len = str.length();
        if (str.charAt(0) != '\\' || str.charAt(len - 1) != '\\') return str;
        return str.substring(1, len - 1);
    }

    @Override
    protected char separatorChar() {
        return '\n';
    }
}
