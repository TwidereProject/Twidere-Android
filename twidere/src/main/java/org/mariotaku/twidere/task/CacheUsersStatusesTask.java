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

import com.twitter.Extractor;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.TwitterWrapper.TwitterListResponse;
import org.mariotaku.twidere.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.mariotaku.twidere.util.ContentValuesCreator.createCachedUser;
import static org.mariotaku.twidere.util.ContentValuesCreator.createStatus;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkDelete;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert;

public class CacheUsersStatusesTask extends TwidereAsyncTask<Void, Void, Void> implements Constants {

    private final TwitterListResponse<twitter4j.Status>[] responses;
    private final Context context;

    public CacheUsersStatusesTask(final Context context, final TwitterListResponse<twitter4j.Status>... responses) {
        this.context = context;
        this.responses = responses;
    }

    @Override
    protected Void doInBackground(final Void... args) {
        if (responses == null || responses.length == 0) return null;
        final ContentResolver resolver = context.getContentResolver();
        final Extractor extractor = new Extractor();
        final Set<ContentValues> usersValues = new HashSet<>();
        final Set<ContentValues> statusesValues = new HashSet<>();
        final Set<ContentValues> hashTagValues = new HashSet<>();
        final Set<Long> allStatusIds = new HashSet<>();
        final Set<String> allHashTags = new HashSet<>();
        final Set<User> users = new HashSet<>();

        for (final TwitterListResponse<twitter4j.Status> response : responses) {
            if (response == null || response.list == null) {
                continue;
            }
            final List<twitter4j.Status> list = response.list;
            final Set<Long> userIds = new HashSet<>();
            for (final twitter4j.Status status : list) {
                if (status == null || status.getId() <= 0) {
                    continue;
                }
                if (status.isRetweet()) {
                    final User retweetUser = status.getRetweetedStatus().getUser();
                    userIds.add(retweetUser.getId());
                }
                allStatusIds.add(status.getId());
                statusesValues.add(createStatus(status, response.account_id));
                allHashTags.addAll(extractor.extractHashtags(status.getText()));
                final User user = status.getUser();
                users.add(user);
                userIds.add(user.getId());
                final ContentValues filtered_users_values = new ContentValues();
                filtered_users_values.put(Filters.Users.NAME, user.getName());
                filtered_users_values.put(Filters.Users.SCREEN_NAME, user.getScreenName());
                final String filtered_users_where = Expression.equals(Filters.Users.USER_ID, user.getId()).getSQL();
                resolver.update(Filters.Users.CONTENT_URI, filtered_users_values, filtered_users_where, null);
            }
        }

        bulkDelete(resolver, CachedStatuses.CONTENT_URI, CachedStatuses.STATUS_ID, allStatusIds, null, false);
        bulkInsert(resolver, CachedStatuses.CONTENT_URI, statusesValues);

        for (final String hashtag : allHashTags) {
            final ContentValues values = new ContentValues();
            values.put(CachedHashtags.NAME, hashtag);
            hashTagValues.add(values);
        }
        bulkDelete(resolver, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, allHashTags, null, true);
        bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashTagValues);

        for (final User user : users) {
            usersValues.add(createCachedUser(user));
        }
        bulkInsert(resolver, CachedUsers.CONTENT_URI, usersValues);
        return null;
    }

    public static Runnable getRunnable(final Context context,
                                       final TwitterListResponse<twitter4j.Status>... all_statuses) {
        return new ExecuteCacheUserStatusesTaskRunnable(context, all_statuses);
    }

    static class ExecuteCacheUserStatusesTaskRunnable implements Runnable {
        final Context context;
        final TwitterListResponse<twitter4j.Status>[] all_statuses;

        ExecuteCacheUserStatusesTaskRunnable(final Context context,
                                             final TwitterListResponse<twitter4j.Status>... all_statuses) {
            this.context = context;
            this.all_statuses = all_statuses;
        }

        @Override
        public void run() {
            new CacheUsersStatusesTask(context, all_statuses).executeTask();
        }
    }
}
