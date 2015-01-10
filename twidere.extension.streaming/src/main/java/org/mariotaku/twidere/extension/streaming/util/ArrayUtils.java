/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.extension.streaming.util;

import java.util.ArrayList;
import java.util.List;

public final class ArrayUtils {

	private ArrayUtils() {
		throw new AssertionError("You are trying to create an instance for this utility class!");
	}

	public static boolean contains(final long[] array, final long value) {
		if (array == null) return false;
		for (final long item : array) {
			if (item == value) return true;
		}
		return false;
	}

	public static boolean contains(final Object[] array, final Object... values) {
		if (array == null || values == null) return false;
		for (final Object item : array) {
			for (final Object value : values) {
				if (item == null || value == null) {
					if (item == value) return true;
					continue;
				}
				if (item.equals(value)) return true;
			}
		}
		return false;
	}

	public static boolean contentMatch(final Object[] array1, final Object[] array2) {
		if (array1 == null || array2 == null) return array1 == array2;
		if (array1.length != array2.length) return false;
		final int length = array1.length;
		for (int i = 0; i < length; i++) {
			if (!contains(array2, array1[i])) return false;
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

	public static long[] fromString(final String string, final char token) {
		if (string == null) return new long[0];
		final String[] items_string_array = string.split(String.valueOf(token));
		final ArrayList<Long> items_list = new ArrayList<Long>();
		for (final String id_string : items_string_array) {
			try {
				items_list.add(Long.parseLong(id_string));
			} catch (final NumberFormatException e) {
				// Ignore.
			}
		}
		final int list_size = items_list.size();
		final long[] array = new long[list_size];
		for (int i = 0; i < list_size; i++) {
			array[i] = items_list.get(i);
		}
		return array;
	}

	public static int indexOf(final long[] array, final long value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public static int indexOf(final Object[] array, final Object value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i].equals(value)) return i;
		}
		return -1;
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

	public static String mergeArrayToString(final String[] array) {
		if (array == null) return null;
		final StringBuilder builder = new StringBuilder();
		for (final String c : array) {
			builder.append(c);
		}
		return builder.toString();
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

	public static String[] toStringArray(final String s) {
		if (s == null) return null;
		return s.split("(?!^)");
	}
}
