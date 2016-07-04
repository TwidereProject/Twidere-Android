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

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.SearchQuery;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;

import java.util.List;

public class TweetSearchLoader extends MicroBlogAPIStatusesLoader {

    @Nullable
    private final String mQuery;
    private final boolean mGapEnabled;

    public TweetSearchLoader(final Context context, final UserKey accountKey, @Nullable final String query,
                             final String sinceId, final String maxId, final int page,
                             final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                             final int tabPosition, final boolean fromUser, final boolean makeGap,
                             boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, page, data, savedStatusesArgs, tabPosition,
                fromUser, loadingMore);
        mQuery = query;
        mGapEnabled = makeGap;
    }

    @NonNull
    @Override
    public List<? extends Status> getStatuses(@NonNull final MicroBlog microBlog,
                                              @NonNull final ParcelableCredentials credentials,
                                              @NonNull final Paging paging) throws MicroBlogException {
        if (mQuery == null) throw new MicroBlogException("Empty query");
        final String processedQuery = processQuery(credentials, mQuery);
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                final SearchQuery query = new SearchQuery(processedQuery);
                query.paging(paging);
                return microBlog.search(query);
            }
            case ParcelableAccount.Type.STATUSNET: {
                return microBlog.searchStatuses(processedQuery, paging);
            }
            case ParcelableAccount.Type.FANFOU: {
                return microBlog.searchPublicTimeline(processedQuery, paging);
            }
        }
        throw new MicroBlogException("Not implemented");
    }

    @NonNull
    protected String processQuery(ParcelableCredentials credentials, @NonNull final String query) {
        if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
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
    protected void processPaging(@NonNull ParcelableCredentials credentials, int loadItemLimit, @NonNull Paging paging) {
        if (MicroBlogAPIFactory.isStatusNetCredentials(credentials)) {
            paging.setRpp(loadItemLimit);
            final int page = getPage();
            if (page > 0) {
                paging.setPage(page);
            }
        } else {
            super.processPaging(credentials, loadItemLimit, paging);
        }
    }

    @Override
    protected boolean isGapEnabled() {
        return mGapEnabled;
    }

}
