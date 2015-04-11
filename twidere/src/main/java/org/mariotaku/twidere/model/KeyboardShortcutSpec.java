/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.content.Context;

import org.mariotaku.twidere.util.KeyboardShortcutsHandler;

import java.util.Locale;

/**
 * Created by mariotaku on 15/4/11.
 */
public class KeyboardShortcutSpec {

    private String rawKey;
    private String value;
    private String contextTag;
    private int keyMeta;
    private String keyName;

    public KeyboardShortcutSpec(String key, String value) {
        rawKey = key;
        final int contextDotIdx = key.indexOf('.');
        if (contextDotIdx != -1) {
            contextTag = key.substring(0, contextDotIdx);
        }
        int idx = contextDotIdx, previousIdx = idx;
        while ((idx = key.indexOf('+', idx + 1)) != -1) {
            keyMeta |= KeyboardShortcutsHandler.getKeyEventMeta(key.substring(previousIdx + 1, idx));
            previousIdx = idx;
        }
        if (previousIdx != -1) {
            keyName = key.substring(previousIdx + 1);
        }
        this.value = value;
    }

    public String getContextTag() {
        return contextTag;
    }

    public int getKeyMeta() {
        return keyMeta;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getRawKey() {
        return rawKey;
    }

    public String getValue() {
        return value;
    }

    public String getValueName(Context context) {
        return KeyboardShortcutsHandler.getActionLabel(context, value);
    }

    public boolean isValid() {
        return keyName != null;
    }

    public String toKeyString() {
        return KeyboardShortcutsHandler.metaToHumanReadableString(keyMeta) + keyName.toUpperCase(Locale.US);
    }

    @Override
    public String toString() {
        return "KeyboardShortcutSpec{" +
                "value='" + value + '\'' +
                ", contextTag='" + contextTag + '\'' +
                ", keyMeta=" + keyMeta +
                ", keyName='" + keyName + '\'' +
                '}';
    }
}
