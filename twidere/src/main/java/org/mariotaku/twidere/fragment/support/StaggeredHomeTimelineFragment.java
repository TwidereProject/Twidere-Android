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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;

import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

public class StaggeredHomeTimelineFragment extends CursorStatusesStaggeredGridFragment {

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				setRefreshComplete();
			} else if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateRefreshState();
			}
		}
	};

	@Override
	public int getStatuses(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter == null) return 0;
		if (maxIds == null) return twitter.refreshAll(accountIds);
		return twitter.getHomeTimelineAsync(accountIds, maxIds, sinceIds);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_REFRESHED);
		filter.addAction(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	protected Uri getContentUri() {
		return Statuses.CONTENT_URI;
	}

	@Override
	protected int getNotificationType() {
		return NOTIFICATION_ID_HOME_TIMELINE;
	}

	@Override
	protected String getPositionKey() {
		return "home_timeline" + getTabPosition();
	}

	@Override
	protected boolean isFiltersEnabled() {
		final SharedPreferences pref = getSharedPreferences();
		return pref != null && pref.getBoolean(KEY_FILTERS_IN_HOME_TIMELINE, true);
	}

	@Override
	protected void updateRefreshState() {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter == null || !getUserVisibleHint() || getActivity() == null) return;
		setRefreshing(twitter.isHomeTimelineRefreshing());
	}

}
