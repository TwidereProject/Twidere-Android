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

package org.mariotaku.twidere.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.api.MicroBlog;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SearchQuery;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;

import java.util.List;


public class MediaTimelineLoader extends MicroBlogAPIStatusesLoader {

    @Nullable
    private final UserKey mUserKey;
    @Nullable
    private final String mUserScreenName;

    private User mUser;

    public MediaTimelineLoader(final Context context, final UserKey accountKey, @Nullable final UserKey userKey,
                               @Nullable final String screenName, final String sinceId, final String maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, final boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser,
                loadingMore);
        mUserKey = userKey;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final MicroBlog microBlog,
                                               @NonNull final ParcelableCredentials credentials,
                                               @NonNull final Paging paging) throws TwitterException {
        final Context context = getContext();
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                if (Utils.isOfficialCredentials(context, credentials)) {
                    if (mUserKey != null) {
                        return microBlog.getMediaTimeline(mUserKey.getId(), paging);
                    }
                    if (mUserScreenName != null) {
                        return microBlog.getMediaTimelineByScreenName(mUserScreenName, paging);
                    }
                } else {
                    final String screenName;
                    if (mUserScreenName != null) {
                        screenName = mUserScreenName;
                    } else if (mUserKey != null) {
                        if (mUser == null) {
                            mUser = TwitterWrapper.tryShowUser(microBlog, mUserKey.getId(), null,
                                    credentials.account_type);
                        }
                        screenName = mUser.getScreenName();
                    } else {
                        throw new TwitterException("Invalid parameters");
                    }
                    final SearchQuery query;
                    if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
                        query = new SearchQuery("from:" + screenName + " filter:media exclude:retweets");
                    } else {
                        query = new SearchQuery("@" + screenName + " pic.twitter.com -RT");
                    }
                    query.paging(paging);
                    final ResponseList<Status> result = new ResponseList<>();
                    for (Status status : microBlog.search(query)) {
                        final User user = status.getUser();
                        if ((mUserKey != null && TextUtils.equals(user.getId(), mUserKey.getId())) ||
                                StringUtils.endsWithIgnoreCase(user.getScreenName(), mUserScreenName)) {
                            result.add(status);
                        }
                    }
                    return result;
                }
                throw new TwitterException("Wrong user");
            }
            case ParcelableAccount.Type.FANFOU: {
                if (mUserKey != null) {
                    return microBlog.getPhotosUserTimeline(mUserKey.getId(), paging);
                }
                if (mUserScreenName != null) {
                    return microBlog.getPhotosUserTimeline(mUserScreenName, paging);
                }
                throw new TwitterException("Wrong user");
            }
        }
        throw new TwitterException("Not implemented");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        final UserKey retweetUserId = status.is_retweet ? status.user_key : null;
        return !isMyTimeline() && InternalTwitterContentUtils.isFiltered(database, retweetUserId,
                status.text_plain, status.quoted_text_plain, status.spans, status.quoted_spans,
                status.source, status.quoted_source, null, status.quoted_user_key);
    }

    private boolean isMyTimeline() {
        final UserKey accountKey = getAccountKey();
        if (accountKey == null) return false;
        if (mUserKey != null) {
            return mUserKey.maybeEquals(accountKey);
        } else {
            final String accountScreenName = DataStoreUtils.getAccountScreenName(getContext(), accountKey);
            return accountScreenName != null && accountScreenName.equalsIgnoreCase(mUserScreenName);
        }
    }
}
