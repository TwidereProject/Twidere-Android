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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Constraint;
import org.mariotaku.sqliteqb.library.NewColumn;
import org.mariotaku.sqliteqb.library.OnConflict;
import org.mariotaku.sqliteqb.library.Tables;
import org.mariotaku.sqliteqb.library.query.SQLInsertQuery;
import org.mariotaku.sqliteqb.library.query.SQLSelectQuery;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.collections.ArraysKt;
import kotlin.ranges.IntRange;

import static org.mariotaku.sqliteqb.library.SQLQueryBuilder.alterTable;
import static org.mariotaku.sqliteqb.library.SQLQueryBuilder.createTable;
import static org.mariotaku.sqliteqb.library.SQLQueryBuilder.dropTable;
import static org.mariotaku.sqliteqb.library.SQLQueryBuilder.insertInto;
import static org.mariotaku.sqliteqb.library.SQLQueryBuilder.select;

public final class DatabaseUpgradeHelper {

    private DatabaseUpgradeHelper() {
    }

    public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
                                   final String[] newColTypes, final boolean dropDirectly,
                                   final Map<String, String> colAliases, final OnConflict onConflict,
                                   final Constraint... constraints) {
        if (newColNames == null || newColTypes == null || newColNames.length != newColTypes.length)
            throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
                    + ", length of columns and types not match.");

        // First, create the table if not exists.
        final NewColumn[] newCols = NewColumn.createNewColumns(newColNames, newColTypes);
        final String createQuery = createTable(true, table).columns(newCols).constraint(constraints).buildSQL();
        db.execSQL(createQuery);

        // We need to get all data from old table.
        final String[] oldCols = getColumnNames(db, table);
        if (oldCols == null || TwidereArrayUtils.contentMatch(newColNames, oldCols)) return;
        if (dropDirectly) {
            db.beginTransaction();
            db.execSQL(dropTable(true, table).getSQL());
            db.execSQL(createQuery);
            db.setTransactionSuccessful();
            db.endTransaction();
            return;
        }
        final String tempTable = String.format(Locale.US, "temp_%s_%d", table, System.currentTimeMillis());
        db.beginTransaction();
        db.execSQL(alterTable(table).renameTo(tempTable).buildSQL());
        db.execSQL(createQuery);
        final String[] notNullCols = getNotNullColumns(newCols);
        final String insertQuery = createInsertDataQuery(table, tempTable, newColNames, oldCols, colAliases,
                notNullCols, onConflict);
        if (insertQuery != null) {
            db.execSQL(insertQuery);
        }
        db.execSQL(dropTable(true, tempTable).getSQL());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
                                   final String[] newColTypes, final boolean dropDirectly,
                                   final Map<String, String> colAliases, final Constraint... constraints) {
        safeUpgrade(db, table, newColNames, newColTypes, dropDirectly, colAliases, OnConflict.REPLACE, constraints);
    }

    private static String createInsertDataQuery(final String table, final String tempTable, final String[] newCols,
                                                final String[] oldCols, final Map<String, String> colAliases, final String[] notNullCols,
                                                final OnConflict onConflict) {
        final SQLInsertQuery.Builder qb = insertInto(onConflict, table);
        final List<String> newInsertColsList = new ArrayList<>();
        for (final String newCol : newCols) {
            final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
            if (ArraysKt.contains(oldCols, newCol) || oldAliasedCol != null
                    && ArraysKt.contains(oldCols, oldAliasedCol)) {
                newInsertColsList.add(newCol);
            }
        }
        final String[] newInsertCols = newInsertColsList.toArray(new String[0]);
        if (!TwidereArrayUtils.contains(newInsertCols, notNullCols)) return null;
        qb.columns(newInsertCols);
        final Columns.Column[] oldDataCols = new Columns.Column[newInsertCols.length];
        for (int i = 0, j = oldDataCols.length; i < j; i++) {
            final String newCol = newInsertCols[i];
            final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
            if (oldAliasedCol != null && ArraysKt.contains(oldCols, oldAliasedCol)) {
                oldDataCols[i] = new Columns.Column(oldAliasedCol, newCol);
            } else {
                oldDataCols[i] = new Columns.Column(newCol);
            }
        }
        final SQLSelectQuery.Builder selectOldBuilder = select(new Columns(oldDataCols));
        selectOldBuilder.from(new Tables(tempTable));
        qb.select(selectOldBuilder.build());
        return qb.buildSQL();
    }

    private static String[] getColumnNames(final SQLiteDatabase db, final String table) {
        final Cursor cur = db.query(table, null, null, null, null, null, null, "1");
        if (cur == null) return null;
        try {
            return cur.getColumnNames();
        } finally {
            cur.close();
        }
    }

    private static String[] getNotNullColumns(final NewColumn[] newCols) {
        if (newCols == null) return null;
        final String[] notNullCols = new String[newCols.length];
        int count = 0;
        for (final NewColumn column : newCols) {
            if (column.getType().endsWith(" NOT NULL")) {
                notNullCols[count++] = column.getName();
            }
        }
        return ArraysKt.sliceArray(notNullCols, new IntRange(0, count - 1));
    }

}
