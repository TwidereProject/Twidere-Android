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

import static org.mariotaku.twidere.util.Utils.isOfficialTwitterInstance;
import static org.mariotaku.twidere.util.Utils.shouldForceUsingPrivateAPIs;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

public class StatusRepliesLoader extends UserMentionsLoader {

	private final long mInReplyToStatusId;

	public StatusRepliesLoader(final Context context, final long accountId, final String screenName,
			final long statusId, final long maxId, final long sinceId, final List<ParcelableStatus> data,
			final String[] savedStatusesArgs, final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mInReplyToStatusId = statusId;
	}

	@Override
	public List<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		final Context context = getContext();
		if (shouldForceUsingPrivateAPIs(context) || isOfficialTwitterInstance(context, twitter)) {
			final List<Status> statuses = twitter.showConversation(mInReplyToStatusId, paging);
			final List<Status> result = new ArrayList<Status>();
			for (final Status status : statuses) {
				if (status.getId() > mInReplyToStatusId) {
					result.add(status);
				}
			}
			return result;
		} else {
			final List<Status> statuses = super.getStatuses(twitter, paging);
			final List<Status> result = new ArrayList<Status>();
			for (final Status status : statuses) {
				if (status.getInReplyToStatusId() == mInReplyToStatusId) {
					result.add(status);
				}
			}
			return result;
		}
	}

}
