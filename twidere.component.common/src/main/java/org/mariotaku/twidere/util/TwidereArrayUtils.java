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

import java.util.ArrayList;
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
        final int length = array1.length;
        for (int i = 0; i < length; i++) {
            if (!ArrayUtils.contains(array2, array1[i])) return false;
        }
        return true;
    }

    public static boolean contentMatch(final Object[] array1, final Object[] array2) {
        if (array1 == null || array2 == null) return array1 == array2;
        if (array1.length != array2.length) return false;
        final int length = array1.length;
        for (int i = 0; i < length; i++) {
            if (!ArrayUtils.contains(array2, array1[i])) return false;
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
        final List<Long> list1 = new ArrayList<Long>();
        for (final long item : array1) {
            list1.add(item);
        }
        final List<Long> list2 = new ArrayList<Long>();
        for (final long item : array2) {
            list2.add(item);
        }
        list1.retainAll(list2);
        return fromList(list1);
    }

    public static void mergeArray(final Object[] dest, final Object[]... arrays) {
        if (arrays == null || arrays.length == 0) return;
        if (arrays.length == 1) {
            final Object[] array = arrays[0];
            System.arraycopy(array, 0, dest, 0, array.length);
            return;
        }
        for (int i = 0, j = arrays.length - 1; i < j; i++) {
            final Object[] array1 = arrays[i], array2 = arrays[i + 1];
            System.arraycopy(array1, 0, dest, 0, array1.length);
            System.arraycopy(array2, 0, dest, array1.length, array2.length);
        }
    }

    public static String mergeArrayToString(final String[] array) {
        if (array == null) return null;
        final StringBuilder builder = new StringBuilder();
        for (final String c : array) {
            builder.append(c);
        }
        return builder.toString();
    }

    public static long min(final long[] array) {
        if (array == null || array.length == 0) throw new IllegalArgumentException();
        long min = array[0];
        for (int i = 1, j = array.length; i < j; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
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

    public static void reverse(@NonNull Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public static int[] subArray(final int[] array, final int start, final int end) {
        final int length = end - start;
        if (length < 0) throw new IllegalArgumentException();
        final int[] result = new int[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static long[] subArray(final long[] array, final int start, final int end) {
        final int length = end - start;
        if (length < 0) throw new IllegalArgumentException();
        final long[] result = new long[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static Object[] subArray(final Object[] array, final int start, final int end) {
        final int length = end - start;
        if (length < 0) throw new IllegalArgumentException();
        final Object[] result = new Object[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static String[] subArray(final String[] array, final int start, final int end) {
        final int length = end - start;
        if (length < 0) throw new IllegalArgumentException();
        final String[] result = new String[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
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

    public static String[] toStringArray(final Object[] array) {
        if (array == null) return null;
        final int length = array.length;
        final String[] string_array = new String[length];
        for (int i = 0; i < length; i++) {
            string_array[i] = ParseUtils.parseString(array[i]);
        }
        return string_array;
    }


    public static String[] toStringArray(final List<?> list) {
        if (list == null) return null;
        final int length = list.size();
        final String[] stringArray = new String[length];
        for (int i = 0; i < length; i++) {
            stringArray[i] = ParseUtils.parseString(list.get(i));
        }
        return stringArray;
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
}
