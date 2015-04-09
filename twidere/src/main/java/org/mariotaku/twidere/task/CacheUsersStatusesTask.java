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

package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.twitter.Extractor;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.util.TwitterWrapper.TwitterListResponse;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mariotaku.twidere.util.ContentValuesCreator.createCachedUser;
import static org.mariotaku.twidere.util.ContentValuesCreator.createStatus;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert;

public class CacheUsersStatusesTask extends AsyncTask<TwitterListResponse<twitter4j.Status>, Object, Object> implements Constants {

    private final Context context;

    public CacheUsersStatusesTask(final Context context) {
        this.context = context;
    }

    @SafeVarargs
    @Override
    protected final Object doInBackground(final TwitterListResponse<twitter4j.Status>... args) {
        if (args == null || args.length == 0) return null;
        final ContentResolver resolver = context.getContentResolver();
        final Extractor extractor = new Extractor();

        for (final TwitterListResponse<twitter4j.Status> response : args) {
            if (response == null || response.list == null) {
                continue;
            }
            final List<twitter4j.Status> list = response.list;
            for (int bulkIdx = 0, totalSize = list.size(); bulkIdx < totalSize; bulkIdx += 100) {
                for (int idx = bulkIdx, end = Math.min(totalSize, bulkIdx + ContentResolverUtils.MAX_BULK_COUNT); idx < end; idx++) {
                    final twitter4j.Status status = list.get(idx);
                    if (status == null || status.getId() <= 0) {
                        continue;
                    }

                    final Set<ContentValues> usersValues = new HashSet<>();
                    final Set<ContentValues> statusesValues = new HashSet<>();
                    final Set<ContentValues> hashTagValues = new HashSet<>();

                    statusesValues.add(createStatus(status, response.accountId));
                    for (final String hashtag : extractor.extractHashtags(status.getText())) {
                        final ContentValues values = new ContentValues();
                        values.put(CachedHashtags.NAME, hashtag);
                        hashTagValues.add(values);
                    }
                    usersValues.add(createCachedUser(status.getUser()));
                    if (status.isRetweet()) {
                        usersValues.add(createCachedUser(status.getRetweetedStatus().getUser()));
                    }

                    bulkInsert(resolver, CachedStatuses.CONTENT_URI, statusesValues);
                    bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashTagValues);
                    bulkInsert(resolver, CachedUsers.CONTENT_URI, usersValues);
                }
            }
        }

        return null;
    }

}
