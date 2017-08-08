package org.mariotaku.twidere.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteDatabaseWrapper {
    private SQLiteDatabase mDatabase;
    private final SQLiteDatabaseWrapper.LazyLoadCallback mLazyLoadCallback;

    public SQLiteDatabaseWrapper(final LazyLoadCallback callback) {
        mLazyLoadCallback = callback;
    }

    public void beginTransaction() {
        tryCreateDatabase();
        if (mDatabase == null) return;
        mDatabase.beginTransaction();
    }

    public int delete(final String table, final String whereClause, final String[] whereArgs) {
        tryCreateDatabase();
        if (mDatabase == null) return 0;
        return mDatabase.delete(table, whereClause, whereArgs);
    }

    public void endTransaction() {
        tryCreateDatabase();
        if (mDatabase == null) return;
        mDatabase.endTransaction();
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return mDatabase;
    }

    public long insert(final String table, final String nullColumnHack, final ContentValues values) {
        tryCreateDatabase();
        if (mDatabase == null) return -1;
        return mDatabase.insert(table, nullColumnHack, values);
    }

    public long insertWithOnConflict(final String table, final String nullColumnHack,
                                     final ContentValues initialValues, final int conflictAlgorithm) {
        tryCreateDatabase();
        if (mDatabase == null) return -1;
        return mDatabase.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    public void execSQL(String sql) throws SQLException {
        tryCreateDatabase();
        if (mDatabase == null) return;
        mDatabase.execSQL(sql);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        tryCreateDatabase();
        if (mDatabase == null) return;
        mDatabase.execSQL(sql, bindArgs);
    }

    public void prepare() {
        tryCreateDatabase();
    }

    public Cursor query(final String table, final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having, final String orderBy) {
        tryCreateDatabase();
        if (mDatabase == null) return null;
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor rawQuery(final String sql, final String[] selectionArgs) {
        tryCreateDatabase();
        if (mDatabase == null) return null;
        return mDatabase.rawQuery(sql, selectionArgs);
    }

    public void setSQLiteDatabase(final SQLiteDatabase database) {
        mDatabase = database;
    }

    public void setTransactionSuccessful() {
        tryCreateDatabase();
        if (mDatabase == null) return;
        mDatabase.setTransactionSuccessful();
    }


    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        tryCreateDatabase();
        if (mDatabase == null) return null;
        return mDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        tryCreateDatabase();
        if (mDatabase == null) return null;
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public int update(final String table, final ContentValues values, final String whereClause, final String[] whereArgs) {
        tryCreateDatabase();
        if (mDatabase == null) return 0;
        return mDatabase.update(table, values, whereClause, whereArgs);
    }

    private synchronized void tryCreateDatabase() {
        if (mLazyLoadCallback == null || mDatabase != null) return;
        mDatabase = mLazyLoadCallback.onCreateSQLiteDatabase();
        if (mDatabase == null)
            throw new IllegalStateException("Callback must not return null instance!");
    }

    public interface LazyLoadCallback {
        SQLiteDatabase onCreateSQLiteDatabase();
    }

}