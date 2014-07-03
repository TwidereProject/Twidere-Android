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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.Context;
import android.net.Uri;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.Notifications;
import org.mariotaku.twidere.provider.TweetStore.UnreadCounts;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TwitterWrapper implements Constants {

	public static int clearNotification(final Context context, final int notificationType, final long accountId) {
		final Uri.Builder builder = Notifications.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(notificationType));
		if (accountId > 0) {
			builder.appendPath(String.valueOf(accountId));
		}
		return context.getContentResolver().delete(builder.build(), null, null);
	}

	public static int clearUnreadCount(final Context context, final int position) {
		if (context == null || position < 0) return 0;
		final Uri uri = UnreadCounts.CONTENT_URI.buildUpon().appendPath(String.valueOf(position)).build();
		return context.getContentResolver().delete(uri, null, null);
	}

	public static SingleResponse<Boolean> deleteProfileBannerImage(final Context context, final long account_id) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter == null) return new SingleResponse<Boolean>(false, null);
		try {
			twitter.removeProfileBannerImage();
			return new SingleResponse<Boolean>(true, null);
		} catch (final TwitterException e) {
			return new SingleResponse<Boolean>(false, e);
		}
	}

	public static int removeUnreadCounts(final Context context, final int position, final long account_id,
			final long... status_ids) {
		if (context == null || position < 0 || status_ids == null || status_ids.length == 0) return 0;
		int result = 0;
		final Uri.Builder builder = UnreadCounts.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(position));
		builder.appendPath(String.valueOf(account_id));
		builder.appendPath(ArrayUtils.toString(status_ids, ',', false));
		result += context.getContentResolver().delete(builder.build(), null, null);
		return result;
	}

	public static int removeUnreadCounts(final Context context, final int position, final Map<Long, Set<Long>> counts) {
		if (context == null || position < 0 || counts == null) return 0;
		int result = 0;
		for (final Entry<Long, Set<Long>> entry : counts.entrySet()) {
			final Uri.Builder builder = UnreadCounts.CONTENT_URI.buildUpon();
			builder.appendPath(String.valueOf(position));
			builder.appendPath(String.valueOf(entry.getKey()));
			builder.appendPath(ListUtils.toString(new ArrayList<Long>(entry.getValue()), ',', false));
			result += context.getContentResolver().delete(builder.build(), null, null);
		}
		return result;
	}

	public static SingleResponse<ParcelableUser> updateProfile(final Context context, final long account_id,
			final String name, final String url, final String location, final String description) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null) {
			try {
				final User user = twitter.updateProfile(name, url, location, description);
				return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id), null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			}
		}
		return SingleResponse.getInstance();
	}

	public static SingleResponse<Boolean> updateProfileBannerImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final File file = new File(image_uri.getPath());
				twitter.updateProfileBannerImage(file);
				// Wait for 5 seconds, see
				// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
				Thread.sleep(5000L);
				if (delete_image) {
					file.delete();
				}
				return new SingleResponse<Boolean>(true, null);
			} catch (final TwitterException e) {
				return new SingleResponse<Boolean>(false, e);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new SingleResponse<Boolean>(false, null);
	}

	public static SingleResponse<ParcelableUser> updateProfileImage(final Context context, final long account_id,
			final Uri image_uri, final boolean delete_image) {
		final Twitter twitter = getTwitterInstance(context, account_id, false);
		if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
			try {
				final User user = twitter.updateProfileImage(new File(image_uri.getPath()));
				// Wait for 5 seconds, see
				// https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
				Thread.sleep(5000L);
				return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id), null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			} catch (final InterruptedException e) {
				return new SingleResponse<ParcelableUser>(null, e);
			}
		}
		return SingleResponse.getInstance();
	}

	public static final class MessageListResponse extends TwitterListResponse<DirectMessage> {

		public final boolean truncated;

		public MessageListResponse(final long account_id, final Exception exception) {
			this(account_id, -1, -1, null, false, exception);
		}

		public MessageListResponse(final long account_id, final List<DirectMessage> list) {
			this(account_id, -1, -1, list, false, null);
		}

		public MessageListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<DirectMessage> list, final boolean truncated) {
			this(account_id, max_id, since_id, list, truncated, null);
		}

		MessageListResponse(final long account_id, final long max_id, final long since_id,
				final List<DirectMessage> list, final boolean truncated, final Exception exception) {
			super(account_id, max_id, since_id, list, exception);
			this.truncated = truncated;
		}

	}

	public static final class StatusListResponse extends TwitterListResponse<Status> {

		public final boolean truncated;

		public StatusListResponse(final long account_id, final Exception exception) {
			this(account_id, -1, -1, null, false, exception);
		}

		public StatusListResponse(final long account_id, final List<Status> list) {
			this(account_id, -1, -1, list, false, null);
		}

		public StatusListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<Status> list, final boolean truncated) {
			this(account_id, max_id, since_id, list, truncated, null);
		}

		StatusListResponse(final long account_id, final long max_id, final long since_id, final List<Status> list,
				final boolean truncated, final Exception exception) {
			super(account_id, max_id, since_id, list, exception);
			this.truncated = truncated;
		}

	}

	public static class TwitterListResponse<Data> extends ListResponse<Data> {

		public final long account_id, max_id, since_id;

		public TwitterListResponse(final long account_id, final Exception exception) {
			this(account_id, -1, -1, null, exception);
		}

		public TwitterListResponse(final long account_id, final long max_id, final long since_id, final List<Data> list) {
			this(account_id, max_id, since_id, list, null);
		}

		TwitterListResponse(final long account_id, final long max_id, final long since_id, final List<Data> list,
				final Exception exception) {
			super(list, exception);
			this.account_id = account_id;
			this.max_id = max_id;
			this.since_id = since_id;
		}

	}
}
