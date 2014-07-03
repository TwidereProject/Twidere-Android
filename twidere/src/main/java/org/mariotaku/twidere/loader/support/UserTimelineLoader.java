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

package org.mariotaku.twidere.loader.support;

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.isFiltered;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.List;

public class UserTimelineLoader extends Twitter4JStatusesLoader {

	private final long mUserId;
	private final String mUserScreenName;
	private final boolean mIsMyTimeline;
	private int mTotalItemsCount;

	public UserTimelineLoader(final Context context, final long accountId, final long userId, final String screenName,
			final long maxId, final long sinceId, final List<ParcelableStatus> data, final String[] savedStatusesArgs,
			final int tabPosition) {
		super(context, accountId, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mUserId = userId;
		mUserScreenName = screenName;
		mIsMyTimeline = userId > 0 ? accountId == userId : accountId == getAccountId(context, screenName);
	}

	public int getTotalItemsCount() {
		return mTotalItemsCount;
	}

	@Override
	protected ResponseList<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		if (twitter == null) return null;
		final ResponseList<Status> statuses;
		if (mUserId != -1) {
			statuses = twitter.getUserTimeline(mUserId, paging);
		} else if (mUserScreenName != null) {
			statuses = twitter.getUserTimeline(mUserScreenName, paging);
		} else
			return null;
		if (mTotalItemsCount == -1 && !statuses.isEmpty()) {
			final User user = statuses.get(0).getUser();
			if (user != null) {
				mTotalItemsCount = user.getStatusesCount();
			}
		}
		return statuses;
	}

	@Override
	protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
		return !mIsMyTimeline && isFiltered(database, -1, status.text_plain, status.text_html, status.source, -1);
	}
}
