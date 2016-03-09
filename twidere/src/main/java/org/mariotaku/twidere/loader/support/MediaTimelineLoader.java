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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SearchQuery;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;

import java.util.List;


public class MediaTimelineLoader extends TwitterAPIStatusesLoader {

    private final String mUserId;
    private final String mUserScreenName;

    private User mUser;

    public MediaTimelineLoader(final Context context, final UserKey accountKey, final String userId,
                               final String screenName, final long sinceId, final long maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, final boolean fromUser) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
        mUserId = userId;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final Twitter twitter,
                                               @NonNull final ParcelableCredentials credentials,
                                               @NonNull final Paging paging) throws TwitterException {
        final Context context = getContext();
        if (Utils.isOfficialCredentials(context, credentials)) {
            if (mUserId != null)
                return twitter.getMediaTimeline(mUserId, paging);
            if (mUserScreenName != null)
                return twitter.getMediaTimeline(mUserScreenName, paging);
        } else if (TwitterAPIFactory.isStatusNetCredentials(credentials)) {
            throw new TwitterException("Not implemented");
        } else {
            final String screenName;
            if (mUserScreenName != null) {
                screenName = mUserScreenName;
            } else {
                if (mUser == null) {
                    mUser = TwitterWrapper.tryShowUser(twitter, mUserId, null);
                }
                screenName = mUser.getScreenName();
            }
            final SearchQuery query;
            if (TwitterAPIFactory.isTwitterCredentials(credentials)) {
                query = new SearchQuery("from:" + screenName + " filter:media exclude:retweets");
            } else {
                query = new SearchQuery("@" + screenName + " pic.twitter.com -RT");
            }
            query.paging(paging);
            final ResponseList<Status> result = new ResponseList<>();
            for (Status status : twitter.search(query)) {
                final User user = status.getUser();
                if (user.getId() == mUserId || user.getScreenName().equalsIgnoreCase(mUserScreenName)) {
                    result.add(status);
                }
            }
            return result;
        }
        throw new TwitterException("Wrong user");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        final UserKey retweetUserId = status.is_retweet ? status.user_key : null;
        return !isMyTimeline() && InternalTwitterContentUtils.isFiltered(database, retweetUserId, status.text_plain,
                status.text_html, status.source, null, status.quoted_user_id);
    }

    private boolean isMyTimeline() {
        if (mUserId != null) {
            return getAccountKey().check(mUserId, null);
        } else {
            final String accountScreenName = DataStoreUtils.getAccountScreenName(getContext(), getAccountKey());
            return accountScreenName != null && accountScreenName.equalsIgnoreCase(mUserScreenName);
        }
    }
}
