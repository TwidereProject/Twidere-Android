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

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.isSameAccount;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.support.UserFavoritesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

public class UserFavoritesFragment extends ParcelableStatusesListFragment {

	private long mUserId;
	private String mUserScreenName;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_FAVORITE_CHANGED.equals(action)) {
				final ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
				if (status == null) return;
				if ((isSameAccount(context, status.account_id, mUserId) || isSameAccount(context, status.account_id,
						mUserScreenName)) && status.id > 0 && !intent.getBooleanExtra(EXTRA_FAVORITED, true)) {
					deleteStatus(status.id);
				}
			}
		}

	};

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID);
		final long user_id = args.getLong(EXTRA_USER_ID, -1);
		final long max_id = args.getLong(EXTRA_MAX_ID, -1);
		final long since_id = args.getLong(EXTRA_SINCE_ID, -1);
		final String screen_name = args.getString(EXTRA_SCREEN_NAME);
		final int tab_position = args.getInt(EXTRA_TAB_POSITION, -1);
		mUserId = user_id;
		mUserScreenName = screen_name;
		return new UserFavoritesLoader(getActivity(), account_id, user_id, screen_name, max_id, since_id, getData(),
				getSavedStatusesFileArgs(), tab_position);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final long user_id = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		final String screen_name = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
		final boolean is_my_timeline = user_id > 0 ? account_id == user_id : account_id == getAccountId(getActivity(),
				screen_name);
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		adapter.setFavoritesHightlightDisabled(is_my_timeline);
		adapter.setFiltersEnabled(false);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FAVORITE_CHANGED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	@Override
	protected String[] getSavedStatusesFileArgs() {
		final Bundle args = getArguments();
		if (args == null) return null;
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long user_id = args.getLong(EXTRA_USER_ID, -1);
		final String screen_name = args.getString(EXTRA_SCREEN_NAME);
		return new String[] { AUTHORITY_USER_FAVORITES, "account" + account_id, "user" + user_id, "name" + screen_name };
	}

	@Override
	protected boolean shouldShowAccountColor() {
		return false;
	}

}
