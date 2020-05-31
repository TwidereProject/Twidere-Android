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

package org.mariotaku.twidere.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.loader.content.FixedAsyncTaskLoader;
import androidx.loader.content.LoaderAccessor;

import org.mariotaku.library.objectcursor.ObjectCursor;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mariotaku on 15-7-5.
 */
public class ObjectCursorLoader<T> extends FixedAsyncTaskLoader<List<T>> {

    final ForceLoadContentObserver mObserver;
    final Class<T> mObjectClass;

    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
    boolean mUseCache;

    ObjectCursor<T> mObjects;

    /* Runs on a worker thread */
    @Override
    public ObjectCursor<T> loadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder);
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }
        if (cursor == null) throw new NullPointerException("Cursor is null");
        return createObjectCursor(cursor, createIndices(cursor));
    }

    protected ObjectCursor<T> createObjectCursor(Cursor cursor, ObjectCursor.CursorIndices<T> indices) {
        return new ObjectCursor<>(cursor, indices, mUseCache);
    }

    @NonNull
    private ObjectCursor.CursorIndices<T> createIndices(final Cursor cursor) {
        return ObjectCursor.indicesFrom(cursor, mObjectClass);
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(List<T> data) {
        final ObjectCursor<T> cursor = (ObjectCursor<T>) data;
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        ObjectCursor<T> oldCursor = mObjects;
        mObjects = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public ObjectCursorLoader(Context context, Class<T> objectClass) {
        super(context);
        mObjectClass = objectClass;
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public ObjectCursorLoader(Context context, Class<T> objectClass, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        super(context);
        mObjectClass = objectClass;
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mObjects != null) {
            deliverResult(mObjects);
        }
        if (takeContentChanged() || mObjects == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(List<T> data) {
        final ObjectCursor<T> cursor = (ObjectCursor<T>) data;
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mObjects != null && !mObjects.isClosed()) {
            mObjects.close();
        }
        mObjects = null;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    public boolean isUseCache() {
        return mUseCache;
    }

    public void setUseCache(boolean mUseCache) {
        this.mUseCache = mUseCache;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(mSortOrder);
        writer.print(prefix);
        writer.print("mObjects=");
        if (mObjects != null) {
            writer.println(mObjects.getCursor());
        } else {
            writer.println("null");
        }
        writer.print(prefix);
        writer.print("mContentChanged=");
        writer.println(LoaderAccessor.isContentChanged(this));
    }

}
