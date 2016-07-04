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
import android.support.annotation.WorkerThread;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;

import java.util.List;


public class RetweetsOfMeLoader extends MicroBlogAPIStatusesLoader {

    public RetweetsOfMeLoader(final Context context, final UserKey accountKey,
                              final String sinceId, final String maxId,
                              final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                              final int tabPosition, boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, -1, data, savedStatusesArgs, tabPosition, fromUser,
                loadingMore);
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final MicroBlog microBlog, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging) throws MicroBlogException {
        return microBlog.getRetweetsOfMe(paging);
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, null, status.text_plain,
                status.quoted_text_plain, status.spans, status.quoted_spans, status.source,
                status.quoted_source, status.retweeted_by_user_key, status.quoted_user_key);
    }
}
