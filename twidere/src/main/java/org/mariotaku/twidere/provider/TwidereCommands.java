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

package org.mariotaku.twidere.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ParseUtils;

public class TwidereCommands {

	public static final String AUTHORITY = "twidere.command";

	public static final Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
			.authority(AUTHORITY).build();
	public static final String EXTRA_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
	public static final String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";
	public static final String EXTRA_DELETE_IMAGE = "delete_image";

	public static class DirectMessage {

		public static final String ACTION_SEND_DIRECT_MESSAGE = "send_direct_message";
	}

	public static class Refresh {

		public static final String ACTION_REFRESH_ALL = "refresh_all";
		public static final String ACTION_REFRESH_HOME_TIMELINE = "refresh_home_timeline";
		public static final String ACTION_REFRESH_MENTIONS = "refresh_mentions";
		public static final String ACTION_REFRESH_INBOX = "refresh_inbox";
		public static final String ACTION_REFRESH_OUTBOX = "refresh_inbox";

		public static boolean isHomeTimelineRefreshing(final Context context) {
			return Utils.isQueryCommandTrue(context, ACTION_REFRESH_HOME_TIMELINE);
		}

		public static boolean isInboxRefreshing(final Context context) {
			return Utils.isQueryCommandTrue(context, ACTION_REFRESH_INBOX);
		}

		public static boolean isMentionsRefreshing(final Context context) {
			return Utils.isQueryCommandTrue(context, ACTION_REFRESH_MENTIONS);
		}

		public static boolean isOutboxRefreshing(final Context context) {
			return Utils.isQueryCommandTrue(context, ACTION_REFRESH_OUTBOX);
		}

		public static void refreshAll(final Context context) {
			Utils.sendInsertCommand(context, ACTION_REFRESH_ALL, null);
		}

		public static void refreshHomeTimeline(final Context context) {
			Utils.sendInsertCommand(context, ACTION_REFRESH_HOME_TIMELINE, null);
		}

		public static void refreshInbox(final Context context) {
			Utils.sendInsertCommand(context, ACTION_REFRESH_INBOX, null);
		}

		public static void refreshMentions(final Context context) {
			Utils.sendInsertCommand(context, ACTION_REFRESH_MENTIONS, null);
		}

		public static void refreshOutbox(final Context context) {
			Utils.sendInsertCommand(context, ACTION_REFRESH_OUTBOX, null);
		}
	}

	public static class Send {

		public static final String ACTION_UPDATE_STATUS = "update_status";

		public void updateStatus(final Context context, final long[] account_ids, final String content,
				final ParcelableLocation location, final Uri image_uri, final long in_reply_to_status_id,
				final boolean is_possibly_sensitive, final boolean delete_image) {
			final ContentValues values = new ContentValues();
			values.put(Constants.EXTRA_ACCOUNT_IDS, ArrayUtils.toString(account_ids, ',', false));
			values.put(Constants.EXTRA_TEXT, content);
			values.put(Constants.EXTRA_LOCATION, ParcelableLocation.toString(location));
			values.put(Constants.EXTRA_URI, ParseUtils.parseString(image_uri));
			values.put(EXTRA_IN_REPLY_TO_STATUS_ID, in_reply_to_status_id);
			values.put(EXTRA_IS_POSSIBLY_SENSITIVE, is_possibly_sensitive);
			values.put(EXTRA_DELETE_IMAGE, delete_image);
			Utils.sendInsertCommand(context, ACTION_UPDATE_STATUS, values);
		}
	}

	private final static class Utils {

		private static boolean isQueryCommandTrue(final Context context, final String action) {
			final Cursor cur = sendQueryCommand(context, action);
			if (cur == null) return false;
			cur.close();
			return true;
		}

		private static Uri sendInsertCommand(final Context context, final String action, final ContentValues values) {
			final ContentResolver resolver = context.getContentResolver();
			final Uri uri = Uri.withAppendedPath(BASE_CONTENT_URI, action);
			return resolver.insert(uri, values != null ? values : new ContentValues());
		}

		private static Cursor sendQueryCommand(final Context context, final String action) {
			final ContentResolver resolver = context.getContentResolver();
			final Uri uri = Uri.withAppendedPath(BASE_CONTENT_URI, action);
			return resolver.query(uri, null, null, null, null);
		}
	}
}
