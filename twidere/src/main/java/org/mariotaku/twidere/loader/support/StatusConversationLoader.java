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

import static org.mariotaku.twidere.util.Utils.isOfficialConsumerKeySecret;
import static org.mariotaku.twidere.util.Utils.shouldForceUsingPrivateAPIs;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.XAuthAuthorization;
import twitter4j.conf.Configuration;

import java.util.Collections;
import java.util.List;

public class StatusConversationLoader extends UserMentionsLoader {

	private final long mInReplyToStatusId;

	public StatusConversationLoader(final Context context, final long accountId, final String screenName,
			final long statusId, final long maxId, final long sinceId, final List<ParcelableStatus> data,
			final String[] savedStatusesArgs, final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mInReplyToStatusId = statusId;
	}

	@Override
	public List<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		final Context context = getContext();
		final Configuration conf = twitter.getConfiguration();
		final Authorization auth = twitter.getAuthorization();
		final boolean isOAuth = auth instanceof OAuthAuthorization || auth instanceof XAuthAuthorization;
		final String consumerKey = conf.getOAuthConsumerKey(), consumerSecret = conf.getOAuthConsumerSecret();
		if (shouldForceUsingPrivateAPIs(context) || isOAuth
				&& isOfficialConsumerKeySecret(context, consumerKey, consumerSecret))
			return twitter.showConversation(mInReplyToStatusId, paging);
		return Collections.emptyList();
	}

}
