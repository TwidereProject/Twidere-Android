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

import static org.mariotaku.twidere.util.Utils.encodeQueryParams;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ListView;

import org.mariotaku.twidere.adapter.BaseParcelableActivitiesAdapter;
import org.mariotaku.twidere.loader.support.Twitter4JActivitiesLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.IOException;
import java.util.List;

public abstract class BaseActivitiesListFragment extends BasePullToRefreshListFragment implements
		LoaderCallbacks<List<ParcelableActivity>> {

	private BaseParcelableActivitiesAdapter mAdapter;
	private SharedPreferences mPreferences;

	private List<ParcelableActivity> mData;

	public abstract BaseParcelableActivitiesAdapter createListAdapter(Context context, boolean compactCards,
			boolean plainListStyle);

	@Override
	public BaseParcelableActivitiesAdapter getListAdapter() {
		return mAdapter;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean plainListStyle = mPreferences.getBoolean(KEY_PLAIN_LIST_STYLE, false);
		final boolean compactCards = mPreferences.getBoolean(KEY_COMPACT_CARDS, false);
		mAdapter = createListAdapter(getActivity(), compactCards, plainListStyle);
		setListAdapter(mAdapter);
		final ListView lv = getListView();
		if (!plainListStyle) {
			lv.setDivider(null);
		}
		lv.setSelector(android.R.color.transparent);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	@Override
	public void onLoaderReset(final Loader<List<ParcelableActivity>> loader) {
		mAdapter.setData(null);
		mData = null;
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableActivity>> loader, final List<ParcelableActivity> data) {
		setProgressBarIndeterminateVisibility(false);
		setRefreshComplete();
		mData = data;
		mAdapter.setData(data);
		if (loader instanceof Twitter4JActivitiesLoader) {
			final boolean multipleAccounts = ((Twitter4JActivitiesLoader) loader).getAccountIds().length > 1;
			mAdapter.setShowAccountColor(multipleAccounts);
		}
		setRefreshComplete();
		setListShown(true);
	}

	@Override
	public void onRefreshFromEnd() {

	}

	@Override
	public void onRefreshFromStart() {
		if (isRefreshing()) return;
		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onResume() {
		super.onResume();
		final float text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
		final boolean display_profile_image = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean show_absolute_time = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
	}

	protected final long[] getAccountIds() {
		final Bundle args = getArguments();
		if (args != null && args.containsKey(EXTRA_ACCOUNT_ID)) return new long[] { args.getLong(EXTRA_ACCOUNT_ID) };
		return Utils.getActivatedAccountIds(getActivity());
	}

	protected final List<ParcelableActivity> getData() {
		return mData;
	}

	protected final String getPositionKey() {
		final Object[] args = getSavedActivitiesFileArgs();
		if (args == null || args.length <= 0) return null;
		try {
			return encodeQueryParams(ArrayUtils.toString(args, '.', false) + "." + getTabPosition());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract Object[] getSavedActivitiesFileArgs();

}
