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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;

import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.util.Collection;

import static android.text.TextUtils.isEmpty;

public class ContentResolverUtils {

    public static final int MAX_BULK_COUNT = 128;

    public static <T> int bulkDelete(final ContentResolver resolver, final Uri uri, final String inColumn,
                                     final Collection<T> colValues, final String extraWhere, final boolean valuesIsString) {
        if (colValues == null) return 0;
        return bulkDelete(resolver, uri, inColumn, colValues.toArray(), extraWhere, valuesIsString);
    }

    public static <T> int bulkDelete(final ContentResolver resolver, final Uri uri, final String inColumn,
                                     final T[] colValues, final String extraWhere, final boolean valuesIsString) {
        if (resolver == null || uri == null || isEmpty(inColumn) || colValues == null || colValues.length == 0)
            return 0;
        final int col_values_length = colValues.length, blocks_count = col_values_length / MAX_BULK_COUNT + 1;
        int rows_deleted = 0;
        for (int i = 0; i < blocks_count; i++) {
            final int start = i * MAX_BULK_COUNT, end = Math.min(start + MAX_BULK_COUNT, col_values_length);
            final String[] block = TwidereArrayUtils.toStringArray(TwidereArrayUtils.subArray(colValues, start, end));
            if (valuesIsString) {
                final StringBuilder where = new StringBuilder(inColumn + " IN(" + TwidereArrayUtils.toStringForSQL(block)
                        + ")");
                if (!isEmpty(extraWhere)) {
                    where.append("AND ").append(extraWhere);
                }
                rows_deleted += resolver.delete(uri, where.toString(), block);
            } else {
                final StringBuilder where = new StringBuilder(inColumn + " IN("
                        + TwidereArrayUtils.toString(block, ',', true) + ")");
                if (!isEmpty(extraWhere)) {
                    where.append("AND ").append(extraWhere);
                }
                rows_deleted += resolver.delete(uri, where.toString(), null);
            }
        }
        return rows_deleted;
    }

    public static int bulkInsert(final ContentResolver resolver, final Uri uri, final Collection<ContentValues> values) {
        if (values == null) return 0;
        return bulkInsert(resolver, uri, values.toArray(new ContentValues[values.size()]));
    }

    public static int bulkInsert(final ContentResolver resolver, final Uri uri, final ContentValues[] values) {
        if (resolver == null || uri == null || values == null || values.length == 0) return 0;
        final int colValuesLength = values.length, blocksCount = colValuesLength / MAX_BULK_COUNT + 1;
        int rowsInserted = 0;
        for (int i = 0; i < blocksCount; i++) {
            final int start = i * MAX_BULK_COUNT, end = Math.min(start + MAX_BULK_COUNT, colValuesLength);
            final ContentValues[] block = new ContentValues[end - start];
            System.arraycopy(values, start, block, 0, end - start);
            rowsInserted += resolver.bulkInsert(uri, block);
        }
        return rowsInserted;
    }

    public static Cursor query(final ContentResolver resolver, final Uri uri, final String[] projection,
                               final String selection, final String[] selectionArgs, final String sortOrder) {
        return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Cursor query(final ContentResolver resolver, final Uri uri, final String[] projection,
                               final String selection, final String[] selectionArgs, final String sortOrder,
                               final CancellationSignal cancellationSignal) {
        return resolver.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

}
