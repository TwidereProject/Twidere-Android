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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TwidereArrayUtils {

    private TwidereArrayUtils() {
        throw new AssertionError("You are trying to create an instance for this utility class!");
    }

    public static boolean contains(final Object[] array, final Object[] values) {
        if (array == null || values == null) return false;
        for (final Object value : values) {
            if (!ArrayUtils.contains(array, value)) return false;
        }
        return true;
    }

    public static boolean contentMatch(final long[] array1, final long[] array2) {
        if (array1 == null || array2 == null) return array1 == array2;
        if (array1.length != array2.length) return false;
        for (long anArray1 : array1) {
            if (!ArrayUtils.contains(array2, anArray1)) return false;
        }
        return true;
    }

    public static boolean contentMatch(final Object[] array1, final Object[] array2) {
        if (array1 == null || array2 == null) return array1 == array2;
        if (array1.length != array2.length) return false;
        for (Object item : array1) {
            if (!ArrayUtils.contains(array2, item)) return false;
        }
        return true;
    }

    public static long[] fromList(final List<Long> list) {
        if (list == null) return null;
        final int count = list.size();
        final long[] array = new long[count];
        for (int i = 0; i < count; i++) {
            array[i] = list.get(i);
        }
        return array;
    }


    public static long[] intersection(final long[] array1, final long[] array2) {
        if (array1 == null || array2 == null) return new long[0];
        final List<Long> list1 = new ArrayList<>();
        for (final long item : array1) {
            list1.add(item);
        }
        final List<Long> list2 = new ArrayList<>();
        for (final long item : array2) {
            list2.add(item);
        }
        list1.retainAll(list2);
        return fromList(list1);
    }

    public static <T> T[] intersection(@NonNull final T[] array1, @NonNull final T[] array2) {
        final List<T> list1 = new ArrayList<>();
        Collections.addAll(list1, array1);
        final List<T> list2 = new ArrayList<>();
        Collections.addAll(list2, array2);
        list1.retainAll(list2);
        //noinspection unchecked
        return list1.toArray((T[]) Array.newInstance(array1.getClass().getComponentType(), list1.size()));
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

    @NonNull
    public static long[] parseLongArray(final String string, final char token) {
        if (TextUtils.isEmpty(string)) return new long[0];
        final String[] itemsStringArray = string.split(String.valueOf(token));
        final long[] array = new long[itemsStringArray.length];
        for (int i = 0, j = itemsStringArray.length; i < j; i++) {
            try {
                array[i] = Long.parseLong(itemsStringArray[i]);
            } catch (final NumberFormatException e) {
                return new long[0];
            }
        }
        return array;
    }

    public static String toString(final long[] array, final char token, final boolean include_space) {
        final StringBuilder builder = new StringBuilder();
        final int length = array.length;
        for (int i = 0; i < length; i++) {
            final String idString = String.valueOf(array[i]);
            if (i > 0) {
                builder.append(include_space ? token + " " : token);
            }
            builder.append(idString);
        }
        return builder.toString();
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

    public static String[] toStringArray(final Object array) {
        return toStringArray(array, 0, Array.getLength(array));
    }


    public static String toStringForSQL(final String[] array) {
        final int size = array != null ? array.length : 0;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('?');
        }
        return builder.toString();
    }

    public static void offset(long[] array, long offset) {
        for (int i = 0; i < array.length; i++) {
            array[i] += offset;
        }
    }
}
