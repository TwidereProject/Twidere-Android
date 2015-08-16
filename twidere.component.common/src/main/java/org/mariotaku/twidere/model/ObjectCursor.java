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

package org.mariotaku.twidere.model;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.AbstractList;

/**
 * Created by mariotaku on 15/7/5.
 */
public class ObjectCursor<E> extends AbstractList<E> {

    private final Cursor mCursor;
    private final CursorIndices<E> mIndices;
    private final SparseArray<E> mCache;

    public ObjectCursor(@NonNull Cursor cursor, @NonNull CursorIndices<E> indies) {
        mCursor = cursor;
        mIndices = indies;
        mCache = new SparseArray<>();
    }

    @Override
    public E get(final int location) {
        ensureCursor();
        final int idxOfCache = mCache.indexOfKey(location);
        if (idxOfCache >= 0) return mCache.valueAt(idxOfCache);
        if (mCursor.moveToPosition(location)) {
            final E object = get(mCursor, mIndices);
            mCache.put(location, object);
            return object;
        }
        throw new ArrayIndexOutOfBoundsException("length=" + mCursor.getCount() + "; index=" + location);
    }

    private void ensureCursor() {
        if (mCursor.isClosed()) throw new IllegalStateException("Cursor is closed");
    }

    protected E get(final Cursor cursor, final CursorIndices<E> indices) {
        return indices.newObject(cursor);
    }

    @Override
    public int size() {
        return mCursor.getCount();
    }

    public boolean isClosed() {
        return mCursor.isClosed();
    }

    public void close() {
        mCursor.close();
    }

    public static abstract class CursorIndices<T> {

        public CursorIndices(@NonNull Cursor cursor) {

        }

        public abstract T newObject(Cursor cursor);
    }
}
