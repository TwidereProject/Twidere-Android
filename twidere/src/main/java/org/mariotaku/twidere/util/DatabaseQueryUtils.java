/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.sqliteqb.library.SQLFunctions;

public class DatabaseQueryUtils {

    public static int count(final SQLiteDatabase db, final String table, final String selection,
                            final String[] selectionArgs, final String groupBy, final String having, final String orderBy) {
        if (db == null) return -1;
        final Cursor c = db.query(table, new String[]{SQLFunctions.COUNT()}, selection, selectionArgs, groupBy, having, orderBy);
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return -1;
        } finally {
            c.close();
        }
    }

    public static int count(final SQLiteDatabase db, boolean distinct, String table, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        if (db == null) return -1;
        final Cursor c = db.query(distinct, table, new String[]{SQLFunctions.COUNT()}, selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return -1;
        } finally {
            c.close();
        }
    }

    public static int count(final SQLiteDatabase db, String table, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        if (db == null) return -1;
        final Cursor c = db.query(table, new String[]{SQLFunctions.COUNT()}, selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return -1;
        } finally {
            c.close();
        }
    }


}