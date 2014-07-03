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

import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import org.mariotaku.twidere.loader.support.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.support.UserFollowersLoader;

public class UserFollowersFragment extends CursorSupportUsersListFragment {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_MULTI_BLOCKSTATE_CHANGED.equals(action)) {
				final long account_id = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
				final String screen_name = getAccountScreenName(getActivity(), account_id);
				final Bundle args = getArguments();
				if (args == null) return;
				if (account_id > 0 && args.getLong(EXTRA_USER_ID, -1) == account_id || screen_name != null
						&& screen_name.equalsIgnoreCase(args.getString(EXTRA_SCREEN_NAME))) {
					removeUsers(intent.getLongArrayExtra(EXTRA_USER_IDS));
				}
			}
		}

	};

	@Override
	public CursorSupportUsersLoader newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long user_id = args.getLong(EXTRA_USER_ID, -1);
		final String screen_name = args.getString(EXTRA_SCREEN_NAME);
		return new UserFollowersLoader(context, account_id, user_id, screen_name, getNextCursor(), getData());
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
		registerReceiver(mStateReceiver, filter);

	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

}
