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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.lang.reflect.Array;
import java.util.Collection;

public class ContentResolverUtils {

    public static final int MAX_BULK_COUNT = 128;

    private ContentResolverUtils() {
    }

    public static <T> int bulkDelete(@NonNull final ContentResolver resolver, @NonNull final Uri uri,
                                     @NonNull final String inColumn, final Collection<T> colValues,
                                     final String extraWhere) {
        if (colValues == null) return 0;
        return bulkDelete(resolver, uri, inColumn, colValues.toArray(), extraWhere);
    }

    public static int bulkDelete(@NonNull final ContentResolver resolver, @NonNull final Uri uri,
                                 @NonNull final String inColumn, final Object colValues,
                                 final String extraWhere) {
        if (colValues == null) return 0;
        final int colValuesLength = Array.getLength(colValues), blocksCount = colValuesLength / MAX_BULK_COUNT + 1;
        int rowsDeleted = 0;
        for (int i = 0; i < blocksCount; i++) {
            final int start = i * MAX_BULK_COUNT, end = Math.min(start + MAX_BULK_COUNT,
                    colValuesLength);
            final String[] block = TwidereArrayUtils.toStringArray(colValues, start, end);
            final StringBuilder where = new StringBuilder(Expression.in(new Columns.Column(inColumn),
                    new ArgsArray(block.length)).getSQL());
            if (!TextUtils.isEmpty(extraWhere)) {
                where.append(" AND ").append(extraWhere);
            }
            rowsDeleted += resolver.delete(uri, where.toString(), block);
        }
        return rowsDeleted;
    }

    public static int bulkInsert(@NonNull final ContentResolver resolver, @NonNull final Uri uri,
                                 @NonNull final Collection<ContentValues> values) {
        return bulkInsert(resolver, uri, values.toArray(new ContentValues[values.size()]));
    }

    public static int bulkInsert(@NonNull final ContentResolver resolver, @NonNull final Uri uri,
                                 @NonNull final ContentValues[] values) {
        if (values.length == 0) return 0;
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

}
