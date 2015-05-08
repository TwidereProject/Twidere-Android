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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.mariotaku.twidere.adapter.SavedSearchesAdapter;
import org.mariotaku.twidere.loader.support.SavedSearchesLoader;

import java.util.Collections;
import java.util.Comparator;

import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;

import static org.mariotaku.twidere.util.Utils.openTweetSearch;

public class SavedSearchesListFragment extends AbsContentListViewFragment<SavedSearchesAdapter> implements
        LoaderCallbacks<ResponseList<SavedSearch>>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final Comparator<SavedSearch> POSITION_COMPARATOR = new Comparator<SavedSearch>() {

        @Override
        public int compare(final SavedSearch object1, final SavedSearch object2) {
            return object1.getPosition() - object2.getPosition();
        }

    };
    private long mAccountId;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = getListView();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        final Bundle args = getArguments();
        mAccountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
        getLoaderManager().initLoader(0, null, this);
        showProgress();
    }

    @NonNull
    @Override
    protected SavedSearchesAdapter onCreateAdapter(Context context, boolean compact) {
        return new SavedSearchesAdapter(getActivity());
    }

    @Override
    public Loader<ResponseList<SavedSearch>> onCreateLoader(final int id, final Bundle args) {
        return new SavedSearchesLoader(getActivity(), mAccountId);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final SavedSearch item = getAdapter().findItem(id);
        if (item == null) return false;
        DestroySavedSearchDialogFragment.show(getFragmentManager(), mAccountId, item.getId(), item.getName());
        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final SavedSearch item = getAdapter().findItem(id);
        if (item == null) return;
        openTweetSearch(getActivity(), mAccountId, item.getQuery());
    }

    @Override
    public void onLoaderReset(final Loader<ResponseList<SavedSearch>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public void onLoadFinished(final Loader<ResponseList<SavedSearch>> loader, final ResponseList<SavedSearch> data) {
        if (data != null) {
            Collections.sort(data, POSITION_COMPARATOR);
        }
        getAdapter().setData(data);
        showContent();
        setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (isRefreshing()) return;
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }

}
