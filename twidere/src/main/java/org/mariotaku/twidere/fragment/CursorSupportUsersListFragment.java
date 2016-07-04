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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.List;

public abstract class CursorSupportUsersListFragment extends ParcelableUsersFragment {

    private long mNextCursor = -1, mPrevCursor = -1;
    private int mNextPage = 1;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mNextCursor = savedInstanceState.getLong(EXTRA_NEXT_CURSOR, -1);
            mPrevCursor = savedInstanceState.getLong(EXTRA_PREV_CURSOR, -1);
            mNextPage = savedInstanceState.getInt(EXTRA_NEXT_PAGE, -1);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onLoaderReset(final Loader<List<ParcelableUser>> loader) {
        super.onLoaderReset(loader);
    }

    @Override
    public void onLoadFinished(final Loader<List<ParcelableUser>> loader, final List<ParcelableUser> data) {
        super.onLoadFinished(loader, data);
        final CursorSupportUsersLoader cursorLoader = (CursorSupportUsersLoader) loader;
        mNextCursor = cursorLoader.getNextCursor();
        mPrevCursor = cursorLoader.getPrevCursor();
        mNextPage = cursorLoader.getNextPage();
    }

    @Override
    public void onLoadMoreContents(@IndicatorPosition int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        loaderArgs.putLong(EXTRA_NEXT_CURSOR, mNextCursor);
        loaderArgs.putLong(EXTRA_PAGE, mNextPage);
        getLoaderManager().restartLoader(0, loaderArgs, this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_NEXT_CURSOR, mNextCursor);
        outState.putLong(EXTRA_PREV_CURSOR, mPrevCursor);
        outState.putLong(EXTRA_NEXT_PAGE, mNextPage);
    }

    protected final long getNextCursor() {
        return mNextCursor;
    }

    protected final int getNextPage() {
        return mNextPage;
    }

    protected final long getPrevCursor() {
        return mPrevCursor;
    }

    @Override
    protected abstract CursorSupportUsersLoader onCreateUsersLoader(final Context context, @NonNull final Bundle args, boolean fromUser);

}
