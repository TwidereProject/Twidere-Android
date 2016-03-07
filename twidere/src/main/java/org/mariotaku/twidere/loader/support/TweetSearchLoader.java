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
import org.mariotaku.twidere.api.twitter.model.SearchQuery;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import java.util.List;

public class TweetSearchLoader extends TwitterAPIStatusesLoader {

    @NonNull
    private final String mQuery;
    private final boolean mGapEnabled;

    public TweetSearchLoader(final Context context, final UserKey accountKey, @NonNull final String query,
                             final long sinceId, final long maxId, final List<ParcelableStatus> data,
                             final String[] savedStatusesArgs, final int tabPosition, boolean fromUser,
                             boolean makeGap) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
        mQuery = query;
        mGapEnabled = makeGap;
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        final String processedQuery = processQuery(mQuery);
        if (TwitterAPIFactory.isTwitterCredentials(getContext(), getAccountKey())) {
            final SearchQuery query = new SearchQuery(processedQuery);
            query.paging(paging);
            return twitter.search(query);
        }
        return twitter.searchStatuses(processedQuery, paging);
    }

    @NonNull
    protected String processQuery(@NonNull final String query) {
        if (TwitterAPIFactory.isTwitterCredentials(getContext(), getAccountKey())) {
            return String.format("%s exclude:retweets", query);
        }
        return query;
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, status, true);
    }

    @Override
    protected boolean isGapEnabled() {
        return mGapEnabled;
    }

}
