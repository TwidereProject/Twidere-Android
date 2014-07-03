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

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTableNameByUri;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;

public class TrendsSuggectionsFragment extends BasePullToRefreshListFragment implements LoaderCallbacks<Cursor>,
		Panes.Left {

	private MultiSelectManager mMultiSelectManager;
	private SharedPreferences mPreferences;

	private TrendsAdapter mTrendsAdapter;

	private long mAccountId;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateRefreshState();
			}
		}
	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
		mMultiSelectManager = getMultiSelectManager();
		mAccountId = getDefaultAccountId(getActivity());
		mTrendsAdapter = new TrendsAdapter(getActivity());
		setListAdapter(mTrendsAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = CachedTrends.Local.CONTENT_URI;
		final String table = getTableNameByUri(uri);
		final String where = table != null ? CachedTrends.TIMESTAMP + " = " + "(SELECT " + CachedTrends.TIMESTAMP
				+ " FROM " + table + " ORDER BY " + CachedTrends.TIMESTAMP + " DESC LIMIT 1)" : null;
		return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mMultiSelectManager.isActive()) return;
		final Cursor cur = (Cursor) mTrendsAdapter.getItem(position - l.getHeaderViewsCount());
		if (cur == null) return;
		openTweetSearch(getActivity(), mAccountId, cur.getString(cur.getColumnIndex(CachedTrends.NAME)));
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mTrendsAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mTrendsAdapter.swapCursor(cursor);
	}

	@Override
	public void onRefreshFromEnd() {

	}

	@Override
	public void onRefreshFromStart() {
		if (isRefreshing()) return;
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter == null) return;
		twitter.getLocalTrendsAsync(mAccountId, mPreferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1));
	}

	@Override
	public void onStart() {
		super.onStart();
		getLoaderManager().restartLoader(0, null, this);
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	protected void updateRefreshState() {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter == null || !getUserVisibleHint()) return;
		setRefreshing(twitter.isLocalTrendsRefreshing());
	}

	static class TrendsAdapter extends SimpleCursorAdapter {

		public TrendsAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1, null, new String[] { CachedTrends.NAME },
					new int[] { android.R.id.text1 }, 0);
		}

	}

}
