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

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.support.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

public class SearchStatusesFragment extends ParcelableStatusesListFragment {

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long maxId = args.getLong(EXTRA_MAX_ID, -1);
		final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
		final String query = args.getString(EXTRA_QUERY);
		final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		adapter.setHighlightKeyword(query.split(" "));
		return new TweetSearchLoader(getActivity(), accountId, query, maxId, sinceId, getData(),
				getSavedStatusesFileArgs(), tabPosition);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		adapter.setFiltersEnabled(true);
		adapter.setIgnoredFilterFields(false, false, false, false, false);
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			final Fragment parent = getParentFragment();
			if (parent instanceof SearchFragment) {
				((SearchFragment) parent).hideIndicator();
			}
		}
	}

	@Override
	protected String[] getSavedStatusesFileArgs() {
		final Bundle args = getArguments();
		if (args == null) return null;
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final String query = args.getString(EXTRA_QUERY);
		return new String[] { AUTHORITY_SEARCH_TWEETS, "account" + account_id, "query" + query };
	}

	@Override
	protected boolean shouldShowAccountColor() {
		return getActivatedAccountIds(getActivity()).length > 1;
	}

}
