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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.util.TwidereArrayUtils;

/**
 * Created by mariotaku on 15/3/25.
 */
public class StringLongPair {
    @NonNull
    private final String key;
    private long value;

    public StringLongPair(@NonNull String key, long value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringLongPair that = (StringLongPair) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }

    @NonNull
    public static StringLongPair valueOf(@NonNull String s) throws NumberFormatException {
        final String[] segs = s.split(":");
        if (segs.length != 2) throw new NumberFormatException();
        return new StringLongPair(segs[0], Long.parseLong(segs[1]));
    }

    public static String toString(StringLongPair[] pairs) {
        if (pairs == null) return null;
        return TwidereArrayUtils.toString(pairs, ';', false);
    }

    public static StringLongPair[] valuesOf(String s) throws NumberFormatException {
        if (s == null) return null;
        final String[] segs = s.split(";");
        final StringLongPair[] pairs = new StringLongPair[segs.length];
        for (int i = 0, j = segs.length; i < j; i++) {
            pairs[i] = valueOf(segs[i]);
        }
        return pairs;
    }
}
