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

package org.mariotaku.twidere.service;

import android.content.Intent;
import android.content.res.Resources;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.provider.TweetStore.UnreadCounts;
import org.mariotaku.twidere.util.UnreadCountUtils;

public class DashClockMentionsUnreadCountService extends DashClockExtension implements TwidereConstants {

	private static final String[] URIS = { UnreadCounts.CONTENT_URI.toString() };

	@Override
	protected void onInitialize(final boolean isReconnect) {
		super.onInitialize(isReconnect);
		addWatchContentUris(URIS);
	}

	@Override
	protected void onUpdateData(final int reason) {
		final ExtensionData data = new ExtensionData();
		final int count = UnreadCountUtils.getUnreadCount(this, TAB_TYPE_MENTIONS_TIMELINE);
		final Resources res = getResources();
		data.visible(count > 0);
		data.icon(R.drawable.ic_extension_mentions);
		data.status(Integer.toString(count));
		data.expandedTitle(res.getQuantityString(R.plurals.N_new_mentions, count, count));
		data.clickIntent(new Intent(this, HomeActivity.class));
		publishUpdate(data);
	}
}
