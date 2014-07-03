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

import static org.mariotaku.twidere.util.ContentValuesCreator.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeStatusContentValues;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkDelete;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.twitter.Extractor;

import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.TwitterWrapper.TwitterListResponse;

import twitter4j.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheUsersStatusesTask extends AsyncTask<Void, Void, Void> implements Constants {

	private final TwitterListResponse<twitter4j.Status>[] all_statuses;
	private final ContentResolver resolver;

	public CacheUsersStatusesTask(final Context context, final TwitterListResponse<twitter4j.Status>... all_statuses) {
		resolver = context.getContentResolver();
		this.all_statuses = all_statuses;
	}

	@Override
	protected Void doInBackground(final Void... args) {
		if (all_statuses == null || all_statuses.length == 0) return null;
		final Extractor extractor = new Extractor();
		final Set<ContentValues> cachedUsersValues = new HashSet<ContentValues>();
		final Set<ContentValues> cached_statuses_values = new HashSet<ContentValues>();
		final Set<ContentValues> hashtag_values = new HashSet<ContentValues>();
		final Set<Long> userIds = new HashSet<Long>();
		final Set<Long> status_ids = new HashSet<Long>();
		final Set<String> hashtags = new HashSet<String>();
		final Set<User> users = new HashSet<User>();

		for (final TwitterListResponse<twitter4j.Status> values : all_statuses) {
			if (values == null || values.list == null) {
				continue;
			}
			final List<twitter4j.Status> list = values.list;
			for (final twitter4j.Status status : list) {
				if (status == null || status.getId() <= 0) {
					continue;
				}
				status_ids.add(status.getId());
				cached_statuses_values.add(makeStatusContentValues(status, values.account_id));
				hashtags.addAll(extractor.extractHashtags(status.getText()));
				final User user = status.getUser();
				if (user != null && user.getId() > 0) {
					users.add(user);
					final ContentValues filtered_users_values = new ContentValues();
					filtered_users_values.put(Filters.Users.NAME, user.getName());
					filtered_users_values.put(Filters.Users.SCREEN_NAME, user.getScreenName());
					final String filtered_users_where = Where.equals(Filters.Users.USER_ID, user.getId()).getSQL();
					resolver.update(Filters.Users.CONTENT_URI, filtered_users_values, filtered_users_where, null);
				}
			}
		}

		bulkDelete(resolver, CachedStatuses.CONTENT_URI, CachedStatuses.STATUS_ID, status_ids, null, false);
		bulkInsert(resolver, CachedStatuses.CONTENT_URI, cached_statuses_values);

		for (final String hashtag : hashtags) {
			final ContentValues hashtag_value = new ContentValues();
			hashtag_value.put(CachedHashtags.NAME, hashtag);
			hashtag_values.add(hashtag_value);
		}
		bulkDelete(resolver, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, hashtags, null, true);
		bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashtag_values);

		for (final User user : users) {
			userIds.add(user.getId());
			cachedUsersValues.add(makeCachedUserContentValues(user));
		}
		bulkDelete(resolver, CachedUsers.CONTENT_URI, CachedUsers.USER_ID, userIds, null, false);
		bulkInsert(resolver, CachedUsers.CONTENT_URI, cachedUsersValues);
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
			new CacheUsersStatusesTask(context, all_statuses).execute();
		}
	}
}
