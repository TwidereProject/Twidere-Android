/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;

import kotlin.collections.ArraysKt;

public final class TwidereArrayUtils {

    private TwidereArrayUtils() {
        throw new AssertionError("You are trying to create an instance for this utility class!");
    }

    public static boolean contains(final Object[] array, final Object[] values) {
        if (array == null || values == null) return false;
        for (final Object value : values) {
            if (!ArraysKt.contains(array, value)) return false;
        }
        return true;
    }

    public static boolean contentMatch(final Object[] array1, final Object[] array2) {
        if (array1 == null || array2 == null) return array1 == array2;
        if (array1.length != array2.length) return false;
        for (Object item : array1) {
            if (!ArraysKt.contains(array2, item)) return false;
        }
        return true;
    }


    public static int arraysLength(@NonNull final Object... arrays) {
        int length = 0;
        for (Object array : arrays) {
            if (array == null) continue;
            length += Array.getLength(array);
        }
        return length;
    }

    public static void mergeArray(final Object dest, @NonNull final Object... arrays) {
        for (int i = 0, j = arrays.length, k = 0; i < j; i++) {
            final Object array = arrays[i];
            if (array == null) continue;
            final int length = Array.getLength(array);
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(array, 0, dest, k, length);
            k += length;
        }
    }

    public static String toString(final Object[] array, final char token, final boolean include_space) {
        final StringBuilder builder = new StringBuilder();
        final int length = array.length;
        for (int i = 0; i < length; i++) {
            final String id_string = String.valueOf(array[i]);
            if (id_string != null) {
                if (i > 0) {
                    builder.append(include_space ? token + " " : token);
                }
                builder.append(id_string);
            }
        }
        return builder.toString();
    }

    public static String[] toStringArray(final Object array, int start, int end) {
        if (array == null) return null;
        final String[] stringArray = new String[end - start];
        for (int i = start; i < end; i++) {
            stringArray[i - start] = ParseUtils.parseString(Array.get(array, i));
        }
        return stringArray;
    }

}
