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

package org.mariotaku.twidere.util.content;

import static android.text.TextUtils.isEmpty;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.StrictModeUtils;

import java.util.Collection;

public class ContentResolverUtils {

	private static final int MAX_DELETE_COUNT = 128;

	public static <T> int bulkDelete(final ContentResolver resolver, final Uri uri, final String in_column,
			final Collection<T> col_values, final String extra_where, final boolean values_is_string) {
		StrictModeUtils.checkDiskIO();
		if (col_values == null) return 0;
		return bulkDelete(resolver, uri, in_column, col_values.toArray(), extra_where, values_is_string);
	}

	public static <T> int bulkDelete(final ContentResolver resolver, final Uri uri, final String in_column,
			final T[] col_values, final String extra_where, final boolean values_is_string) {
		StrictModeUtils.checkDiskIO();
		if (resolver == null || uri == null || isEmpty(in_column) || col_values == null || col_values.length == 0)
			return 0;
		final int col_values_length = col_values.length, blocks_count = col_values_length / MAX_DELETE_COUNT + 1;
		int rows_deleted = 0;
		for (int i = 0; i < blocks_count; i++) {
			final int start = i * MAX_DELETE_COUNT, end = Math.min(start + MAX_DELETE_COUNT, col_values_length);
			final String[] block = ArrayUtils.toStringArray(ArrayUtils.subArray(col_values, start, end));
			if (values_is_string) {
				final StringBuilder where = new StringBuilder(in_column + " IN(" + ArrayUtils.toStringForSQL(block)
						+ ")");
				if (!isEmpty(extra_where)) {
					where.append("AND " + extra_where);
				}
				rows_deleted += resolver.delete(uri, where.toString(), block);
			} else {
				final StringBuilder where = new StringBuilder(in_column + " IN("
						+ ArrayUtils.toString(block, ',', true) + ")");
				if (!isEmpty(extra_where)) {
					where.append("AND " + extra_where);
				}
				rows_deleted += resolver.delete(uri, where.toString(), null);
			}
		}
		return rows_deleted;
	}

	public static int bulkInsert(final ContentResolver resolver, final Uri uri, final Collection<ContentValues> values) {
		StrictModeUtils.checkDiskIO();
		if (values == null) return 0;
		return bulkInsert(resolver, uri, values.toArray(new ContentValues[values.size()]));
	}

	public static int bulkInsert(final ContentResolver resolver, final Uri uri, final ContentValues[] values) {
		StrictModeUtils.checkDiskIO();
		if (resolver == null || uri == null || values == null || values.length == 0) return 0;
		final int col_values_length = values.length, blocks_count = col_values_length / MAX_DELETE_COUNT + 1;
		int rows_inserted = 0;
		for (int i = 0; i < blocks_count; i++) {
			final int start = i * MAX_DELETE_COUNT, end = Math.min(start + MAX_DELETE_COUNT, col_values_length);
			final ContentValues[] block = new ContentValues[end - start];
			System.arraycopy(values, start, block, 0, end - start);
			rows_inserted += resolver.bulkInsert(uri, block);
		}
		return rows_inserted;
	}

	public static Cursor query(final ContentResolver resolver, final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs, final String sortOrder) {
		StrictModeUtils.checkDiskIO();
		return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
	}

	public static Cursor query(final ContentResolver resolver, final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs, final String sortOrder,
			final CancellationSignal cancellationSignal) {
		StrictModeUtils.checkDiskIO();
		return resolver.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
	}

}
