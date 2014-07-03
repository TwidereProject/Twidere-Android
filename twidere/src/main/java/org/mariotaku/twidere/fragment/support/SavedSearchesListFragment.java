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

import static org.mariotaku.twidere.util.Utils.openTweetSearch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.loader.support.SavedSearchesLoader;
import org.mariotaku.twidere.model.Panes;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;

import java.util.Collections;
import java.util.Comparator;

public class SavedSearchesListFragment extends BasePullToRefreshListFragment implements
		LoaderCallbacks<ResponseList<SavedSearch>>, OnItemLongClickListener, Panes.Left {

	private SavedSearchesAdapter mAdapter;

	private long mAccountId;
	private ListView mListView;
	private static final Comparator<SavedSearch> POSITION_COMPARATOR = new Comparator<SavedSearch>() {

		@Override
		public int compare(final SavedSearch object1, final SavedSearch object2) {
			return object1.getPosition() - object2.getPosition();
		}

	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new SavedSearchesAdapter(getActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
		final Bundle args = getArguments();
		mAccountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public Loader<ResponseList<SavedSearch>> onCreateLoader(final int id, final Bundle args) {
		return new SavedSearchesLoader(getActivity(), mAccountId);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final SavedSearch item = mAdapter.findItem(id);
		if (item == null) return false;
		DestroySavedSearchDialogFragment.show(getFragmentManager(), mAccountId, item.getId(), item.getName());
		return true;
	}

	@Override
	public void onListItemClick(final ListView view, final View child, final int position, final long id) {
		final SavedSearch item = mAdapter.findItem(id);
		if (item == null) return;
		openTweetSearch(getActivity(), mAccountId, item.getQuery());
	}

	@Override
	public void onLoaderReset(final Loader<ResponseList<SavedSearch>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<ResponseList<SavedSearch>> loader, final ResponseList<SavedSearch> data) {
		if (data != null) {
			Collections.sort(data, POSITION_COMPARATOR);
		}
		mAdapter.setData(data);
		setListShown(true);
		setRefreshComplete();
	}

	@Override
	public void onRefreshFromEnd() {

	}

	@Override
	public void onRefreshFromStart() {
		if (isRefreshing()) return;
		getLoaderManager().restartLoader(0, null, this);
	}

	static class SavedSearchesAdapter extends BaseAdapter {

		private ResponseList<SavedSearch> mData;
		private final LayoutInflater mInflater;

		public SavedSearchesAdapter(final Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public SavedSearch findItem(final long id) {
			for (int i = 0, count = getCount(); i < count; i++) {
				if (id != -1 && id == getItemId(i)) return getItem(i);
			}
			return null;
		}

		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public SavedSearch getItem(final int position) {
			return mData != null ? mData.get(position) : null;
		}

		@Override
		public long getItemId(final int position) {
			return mData != null ? mData.get(position).getId() : -1;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					android.R.layout.simple_list_item_1, null);
			final TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setText(getItem(position).getName());
			return view;
		}

		public void setData(final ResponseList<SavedSearch> data) {
			mData = data;
			notifyDataSetChanged();
		}

	}

}
