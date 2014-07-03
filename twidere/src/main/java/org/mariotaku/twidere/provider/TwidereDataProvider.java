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

import static org.mariotaku.twidere.util.Utils.clearAccountColor;
import static org.mariotaku.twidere.util.Utils.clearAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountDisplayName;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getAccountNotificationId;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getNotificationUri;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameById;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.isNotificationsSilent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import org.mariotaku.jsonserializer.JSONFileIO;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.UnreadItem;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Preferences;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.UnreadCounts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.ImagePreloader;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.PermissionsManager;
import org.mariotaku.twidere.util.SQLiteDatabaseWrapper;
import org.mariotaku.twidere.util.SQLiteDatabaseWrapper.LazyLoadCallback;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereQueryBuilder;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.collection.NoDuplicatesCopyOnWriteArrayList;

import twitter4j.http.HostAddressResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TwidereDataProvider extends ContentProvider implements Constants, OnSharedPreferenceChangeListener,
		LazyLoadCallback {

	private static final String UNREAD_STATUSES_FILE_NAME = "unread_statuses";
	private static final String UNREAD_MENTIONS_FILE_NAME = "unread_mentions";
	private static final String UNREAD_MESSAGES_FILE_NAME = "unread_messages";

	private ContentResolver mContentResolver;
	private SQLiteDatabaseWrapper mDatabaseWrapper;
	private PermissionsManager mPermissionsManager;
	private NotificationManager mNotificationManager;
	private SharedPreferencesWrapper mPreferences;
	private ImagePreloader mImagePreloader;
	private HostAddressResolver mHostAddressResolver;

	private final List<ParcelableStatus> mNewStatuses = new CopyOnWriteArrayList<ParcelableStatus>();
	private final List<ParcelableStatus> mNewMentions = new CopyOnWriteArrayList<ParcelableStatus>();
	private final List<ParcelableDirectMessage> mNewMessages = new CopyOnWriteArrayList<ParcelableDirectMessage>();

	private final List<UnreadItem> mUnreadStatuses = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();
	private final List<UnreadItem> mUnreadMentions = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();
	private final List<UnreadItem> mUnreadMessages = new NoDuplicatesCopyOnWriteArrayList<UnreadItem>();

	private boolean mHomeActivityInBackground;

	private boolean mNameFirst, mNickOnly;

	private final BroadcastReceiver mHomeActivityStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_HOME_ACTIVITY_ONSTART.equals(action)) {
				mHomeActivityInBackground = false;
			} else if (BROADCAST_HOME_ACTIVITY_ONSTOP.equals(action)) {
				mHomeActivityInBackground = true;
			}
		}

	};

	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			checkWritePermission(tableId, table);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return 0;
			}
			int result = 0;
			if (table != null && values != null) {
				mDatabaseWrapper.beginTransaction();
				final boolean replaceOnConflict = shouldReplaceOnConflict(tableId);
				for (final ContentValues contentValues : values) {
					if (replaceOnConflict) {
						mDatabaseWrapper.insertWithOnConflict(table, null, contentValues,
								SQLiteDatabase.CONFLICT_REPLACE);
					} else {
						mDatabaseWrapper.insert(table, null, contentValues);
					}
					result++;
				}
				mDatabaseWrapper.setTransactionSuccessful();
				mDatabaseWrapper.endTransaction();
			}
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			onNewItemsInserted(uri, values);
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			checkWritePermission(tableId, table);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return 0;
				case VIRTUAL_TABLE_ID_NOTIFICATIONS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 1) {
						clearNotification();
					} else if (segments.size() == 2) {
						final int notificationType = ParseUtils.parseInt(segments.get(1));
						clearNotification(notificationType, 0);
					} else if (segments.size() == 3) {
						final int notificationType = ParseUtils.parseInt(segments.get(1));
						final long accountId = ParseUtils.parseLong(segments.get(2));
						clearNotification(notificationType, accountId);
					}
					return 1;
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS: {
					final List<String> segments = uri.getPathSegments();
					final int segmentsSize = segments.size();
					if (segmentsSize == 1)
						return clearUnreadCount();
					else if (segmentsSize == 2)
						return clearUnreadCount(ParseUtils.parseInt(segments.get(1)));
					else if (segmentsSize == 4)
						return removeUnreadItems(ParseUtils.parseInt(segments.get(1)),
								ParseUtils.parseLong(segments.get(2)), ArrayUtils.parseLongArray(segments.get(3), ','));
					return 0;
				}
			}
			if (table == null) return 0;
			final int result = mDatabaseWrapper.delete(table, selection, selectionArgs);
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			checkWritePermission(tableId, table);
			switch (tableId) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
					return null;
			}
			if (table == null) return null;
			final boolean replaceOnConflict = shouldReplaceOnConflict(tableId);
			final long rowId;
			if (replaceOnConflict) {
				rowId = mDatabaseWrapper.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} else {
				rowId = mDatabaseWrapper.insert(table, null, values);
			}
			onDatabaseUpdated(tableId, uri);
			onNewItemsInserted(uri, values);
			return Uri.withAppendedPath(uri, String.valueOf(rowId));
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mDatabaseWrapper = new SQLiteDatabaseWrapper(this);
		mHostAddressResolver = app.getHostAddressResolver();
		mPreferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		updatePreferences();
		mPermissionsManager = new PermissionsManager(context);
		mImagePreloader = new ImagePreloader(context, app.getImageLoader());
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTART);
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTOP);
		context.registerReceiver(mHomeActivityStateReceiver, filter);
		restoreUnreadItems();
		// final GetWritableDatabaseTask task = new
		// GetWritableDatabaseTask(context, helper, mDatabaseWrapper);
		// task.execute();
		return true;
	}

	@Override
	public SQLiteDatabase onCreateSQLiteDatabase() {
		final TwidereApplication app = TwidereApplication.getInstance(getContext());
		final SQLiteOpenHelper helper = app.getSQLiteOpenHelper();
		return helper.getWritableDatabase();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		updatePreferences();
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) throws FileNotFoundException {
		if (uri == null || mode == null) throw new IllegalArgumentException();
		final int table_id = getTableId(uri);
		final String table = getTableNameById(table_id);
		final int mode_code;
		if ("r".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_ONLY;
		} else if ("rw".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE;
		} else if ("rwt".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_TRUNCATE;
		} else
			throw new IllegalArgumentException();
		if (mode_code == ParcelFileDescriptor.MODE_READ_ONLY) {
			checkReadPermission(table_id, table, null);
		} else if ((mode_code & ParcelFileDescriptor.MODE_READ_WRITE) != 0) {
			checkReadPermission(table_id, table, null);
			checkWritePermission(table_id, table);
		}
		switch (table_id) {
			case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
				return getCachedImageFd(uri.getQueryParameter(QUERY_PARAM_URL));
			}
			case VIRTUAL_TABLE_ID_CACHE_FILES: {
				return getCacheFileFd(uri.getLastPathSegment());
			}
		}
		return null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			checkReadPermission(tableId, table, projection);
			switch (tableId) {
				case VIRTUAL_TABLE_ID_DATABASE_READY: {
					if (mDatabaseWrapper.isReady())
						return new MatrixCursor(projection != null ? projection : new String[0]);
					return null;
				}
				case VIRTUAL_TABLE_ID_PERMISSIONS: {
					final MatrixCursor c = new MatrixCursor(TweetStore.Permissions.MATRIX_COLUMNS);
					final Map<String, String> map = mPermissionsManager.getAll();
					for (final Map.Entry<String, String> item : map.entrySet()) {
						c.addRow(new Object[] { item.getKey(), item.getValue() });
					}
					return c;
				}
				case VIRTUAL_TABLE_ID_ALL_PREFERENCES: {
					return getPreferencesCursor(mPreferences, null);
				}
				case VIRTUAL_TABLE_ID_PREFERENCES: {
					return getPreferencesCursor(mPreferences, uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_DNS: {
					return getDNSCursor(uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
					return getCachedImageCursor(uri.getQueryParameter(QUERY_PARAM_URL));
				}
				case VIRTUAL_TABLE_ID_NOTIFICATIONS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 2)
						return getNotificationsCursor(ParseUtils.parseInt(segments.get(1), -1));
					else
						return getNotificationsCursor();
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() == 2)
						return getUnreadCountsCursor(ParseUtils.parseInt(segments.get(1), -1));
					else
						return getUnreadCountsCursor();
				}
				case VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 3) return null;
					return getUnreadCountsCursorByType(segments.get(2));
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 4) return null;
					final long accountId = ParseUtils.parseLong(segments.get(2));
					final long conversationId = ParseUtils.parseLong(segments.get(3));
					final String query = TwidereQueryBuilder.ConversationQueryBuilder.buildByConversationId(projection,
							accountId, conversationId, selection, sortOrder);
					final Cursor c = mDatabaseWrapper.rawQuery(query, selectionArgs);
					setNotificationUri(c, DirectMessages.CONTENT_URI);
					return c;
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 4) return null;
					final long accountId = ParseUtils.parseLong(segments.get(2));
					final String screenName = segments.get(3);
					final String query = TwidereQueryBuilder.ConversationQueryBuilder.buildByScreenName(projection,
							accountId, screenName, selection, sortOrder);
					final Cursor c = mDatabaseWrapper.rawQuery(query, selectionArgs);
					setNotificationUri(c, DirectMessages.CONTENT_URI);
					return c;
				}
			}
			if (table == null) return null;
			final Cursor c = mDatabaseWrapper.query(table, projection, selection, selectionArgs, null, null, sortOrder);
			setNotificationUri(c, getNotificationUri(tableId, uri));
			return c;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		try {
			final int tableId = getTableId(uri);
			final String table = getTableNameById(tableId);
			int result = 0;
			if (table != null) {
				switch (tableId) {
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
					case TABLE_ID_DIRECT_MESSAGES:
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
						return 0;
				}
				result = mDatabaseWrapper.update(table, values, selection, selectionArgs);
			}
			if (result > 0) {
				onDatabaseUpdated(tableId, uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private void buildNotification(final NotificationCompat.Builder builder, final AccountPreferences accountPrefs,
			final int notificationType, final String ticker, final String title, final String message, final long when,
			final int icon, final Bitmap largeIcon, final Intent contentIntent, final Intent deleteIntent) {
		final Context context = getContext();
		builder.setTicker(ticker);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		if (largeIcon != null) {
			builder.setLargeIcon(largeIcon);
		}
		if (deleteIntent != null) {
			builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		if (contentIntent != null) {
			builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		int defaults = 0;
		if (isNotificationAudible()) {
			if (AccountPreferences.isNotificationHasRingtone(notificationType)) {
				final Uri ringtone = accountPrefs.getNotificationRingtone();
				builder.setSound(ringtone, Notification.STREAM_DEFAULT);
			}
			if (AccountPreferences.isNotificationHasVibration(notificationType)) {
				defaults |= Notification.DEFAULT_VIBRATE;
			} else {
				defaults &= ~Notification.DEFAULT_VIBRATE;
			}
		}
		if (AccountPreferences.isNotificationHasLight(notificationType)) {
			final int color = accountPrefs.getNotificationLightColor();
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
	}

	private boolean checkPermission(final String... permissions) {
		return mPermissionsManager.checkCallingPermission(permissions);
	}

	private void checkReadPermission(final int id, final String table, final String[] projection) {
		switch (id) {
			case VIRTUAL_TABLE_ID_PREFERENCES:
			case VIRTUAL_TABLE_ID_DNS: {
				if (!checkPermission(PERMISSION_PREFERENCES))
					throw new SecurityException("Access preferences requires level PERMISSION_LEVEL_PREFERENCES");
				break;
			}
			case TABLE_ID_ACCOUNTS: {
				// Reading some infomation like user_id, screen_name etc is
				// okay, but reading columns like password requires higher
				// permission level.
				final String[] credentialsCols = { Accounts.BASIC_AUTH_PASSWORD, Accounts.OAUTH_TOKEN,
						Accounts.OAUTH_TOKEN_SECRET, Accounts.CONSUMER_KEY, Accounts.CONSUMER_SECRET };
				if (projection == null || ArrayUtils.contains(projection, credentialsCols)
						&& !checkPermission(PERMISSION_ACCOUNTS))
					throw new SecurityException("Access column " + ArrayUtils.toString(projection, ',', true)
							+ " in database accounts requires level PERMISSION_LEVEL_ACCOUNTS");
				if (!checkPermission(PERMISSION_READ))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_READ");
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES: {
				if (!checkPermission(PERMISSION_DIRECT_MESSAGES))
					throw new SecurityException("Access database " + table
							+ " requires level PERMISSION_LEVEL_DIRECT_MESSAGES");
				break;
			}
			case TABLE_ID_STATUSES:
			case TABLE_ID_MENTIONS:
			case TABLE_ID_TABS:
			case TABLE_ID_DRAFTS:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
			case TABLE_ID_TRENDS_LOCAL:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_HASHTAGS: {
				if (!checkPermission(PERMISSION_READ))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_READ");
				break;
			}
		}
	}

	private void checkWritePermission(final int id, final String table) {
		switch (id) {
			case TABLE_ID_ACCOUNTS: {
				// Writing to accounts database is not allowed for third-party
				// applications.
				if (!mPermissionsManager.checkSignature(Binder.getCallingUid()))
					throw new SecurityException(
							"Writing to accounts database is not allowed for third-party applications");
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES: {
				if (!checkPermission(PERMISSION_DIRECT_MESSAGES))
					throw new SecurityException("Access database " + table
							+ " requires level PERMISSION_LEVEL_DIRECT_MESSAGES");
				break;
			}
			case TABLE_ID_STATUSES:
			case TABLE_ID_MENTIONS:
			case TABLE_ID_TABS:
			case TABLE_ID_DRAFTS:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
			case TABLE_ID_TRENDS_LOCAL:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_HASHTAGS: {
				if (!checkPermission(PERMISSION_WRITE))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_WRITE");
				break;
			}
		}
	}

	private void clearNotification() {
		mNewStatuses.clear();
		mNewMentions.clear();
		mNewMessages.clear();
		getNotificationManager().cancelAll();
	}

	private void clearNotification(final int notificationType, final long accountId) {
		final NotificationManager nm = getNotificationManager();
		final boolean isAccountSpecific;
		switch (notificationType) {
			case NOTIFICATION_ID_HOME_TIMELINE: {
				mNewStatuses.clear();
				isAccountSpecific = true;
				break;
			}
			case NOTIFICATION_ID_MENTIONS: {
				mNewMentions.clear();
				isAccountSpecific = true;
				break;
			}
			case NOTIFICATION_ID_DIRECT_MESSAGES: {
				mNewMessages.clear();
				isAccountSpecific = true;
				break;
			}
			default: {
				isAccountSpecific = false;
			}
		}
		if (isAccountSpecific) {
			if (accountId > 0) {
				nm.cancel(getAccountNotificationId(notificationType, accountId));
			} else {
				for (final long id : getAccountIds(getContext())) {
					nm.cancel(getAccountNotificationId(notificationType, id));
				}
			}
		} else {
			nm.cancel(notificationType);
		}
	}

	private int clearUnreadCount() {
		int result = 0;
		result += mUnreadStatuses.size();
		result += mUnreadMentions.size();
		result += mUnreadMentions.size();
		mUnreadStatuses.clear();
		mUnreadMentions.clear();
		mUnreadMessages.clear();
		saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		notifyContentObserver(UnreadCounts.CONTENT_URI);
		return result;
	}

	private int clearUnreadCount(final int position) {
		final Context context = getContext();
		final int result;
		final SupportTabSpec tab = CustomTabUtils.getAddedTabAt(context, position);
		final String type = tab.type;
		if (TAB_TYPE_HOME_TIMELINE.equals(type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadStatuses, account_ids);
			saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadMentions, account_ids);
			mUnreadMentions.clear();
			saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			result = clearUnreadCount(mUnreadMessages, account_ids);
			mUnreadMessages.clear();
			saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		} else
			return 0;
		if (result > 0) {
			notifyUnreadCountChanged(position);
		}
		return result;
	}

	private void displayMessagesNotification(final int notifiedCount, final AccountPreferences accountPrefs,
			final int notificationType, final int icon, final List<ParcelableDirectMessage> messages) {
		final NotificationManager nm = getNotificationManager();
		if (notifiedCount == 0 || accountPrefs == null || messages.isEmpty()) return;
		final long accountId = accountPrefs.getAccountId();
		final Context context = getContext();
		final Resources resources = context.getResources();
		final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context);
		final ParcelableDirectMessage firstItem = messages.get(0);
		final int messagesCount = messages.size();
		final Intent deleteIntent = new Intent(BROADCAST_NOTIFICATION_DELETED);
		deleteIntent.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_DIRECT_MESSAGES);
		deleteIntent.putExtra(EXTRA_NOTIFICATION_ACCOUNT, accountId);
		final Intent contentIntent;
		final String title;
		if (messagesCount > 1) {
			notifBuilder.setNumber(messagesCount);
		}
		final int usersCount = getSendersCount(messages);
		contentIntent = new Intent(context, HomeActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		contentIntent.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_DIRECT_MESSAGES);
		if (messagesCount == 1) {
			final Uri.Builder uriBuilder = new Uri.Builder();
			uriBuilder.scheme(SCHEME_TWIDERE);
			uriBuilder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
			uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(firstItem.account_id));
			uriBuilder.appendQueryParameter(QUERY_PARAM_RECIPIENT_ID, String.valueOf(firstItem.sender_id));
			final Intent statusIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
			statusIntent.setExtrasClassLoader(context.getClassLoader());
			contentIntent.putExtra(EXTRA_EXTRA_INTENT, statusIntent);
		}

		final String displayName = getDisplayName(context, firstItem.sender_id, firstItem.sender_name,
				firstItem.sender_screen_name, mNameFirst, mNickOnly);
		if (usersCount > 1) {
			title = resources.getString(R.string.notification_direct_message_multiple_users, displayName,
					usersCount - 1, messagesCount);
		} else if (messagesCount > 1) {
			title = resources.getString(R.string.notification_direct_message_multiple_messages, displayName,
					messagesCount);
		} else {
			title = resources.getString(R.string.notification_direct_message, displayName);
		}
		notifBuilder.setLargeIcon(getProfileImageForNotification(firstItem.sender_profile_image_url));
		buildNotification(notifBuilder, accountPrefs, notificationType, title, title, firstItem.text_plain,
				firstItem.timestamp, R.drawable.ic_stat_direct_message, null, contentIntent, deleteIntent);
		final NotificationCompat.Style notifStyle;
		if (messagesCount > 1) {
			final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notifBuilder);
			final int max = Math.min(4, messagesCount);
			for (int i = 0; i < max; i++) {
				final ParcelableDirectMessage item = messages.get(i);
				if (item == null) return;
				final String nameEscaped = HtmlEscapeHelper.escape(getDisplayName(context, item.sender_id,
						item.sender_name, item.sender_name, mNameFirst, mNickOnly));
				final String textEscaped = HtmlEscapeHelper.escape(stripMentionText(item.text_unescaped,
						getAccountScreenName(context, item.account_id)));
				inboxStyle.addLine(Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped)));
			}
			if (max == 4 && messagesCount - max > 0) {
				inboxStyle.addLine(context.getString(R.string.and_more, messagesCount - max));
			}
			inboxStyle.setSummaryText(getAccountDisplayName(context, accountId, mNameFirst));
			notifStyle = inboxStyle;
		} else {
			final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(notifBuilder);
			bigTextStyle.bigText(firstItem.text_unescaped);
			bigTextStyle.setSummaryText(getAccountDisplayName(context, accountId, mNameFirst));
			notifStyle = bigTextStyle;
		}
		final int accountNotificationId = getAccountNotificationId(NOTIFICATION_ID_DIRECT_MESSAGES, accountId);
		nm.notify(accountNotificationId, notifStyle.build());
	}

	private void displayStatusesNotification(final int notifiedCount, final AccountPreferences accountPreferences,
			final int notificationType, final int notificationId, final List<ParcelableStatus> statuses,
			final int titleSingle, final int titleMutiple, final int icon) {
		final NotificationManager nm = getNotificationManager();
		if (notifiedCount == 0 || accountPreferences == null || statuses.isEmpty()) return;
		final long accountId = accountPreferences.getAccountId();
		final Context context = getContext();
		final Resources resources = context.getResources();
		final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context);
		final ParcelableStatus firstItem = statuses.get(0);
		final int statusesSize = statuses.size();
		final Intent deleteIntent = new Intent(BROADCAST_NOTIFICATION_DELETED);
		deleteIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		deleteIntent.putExtra(EXTRA_NOTIFICATION_ACCOUNT, accountId);
		final Intent contentIntent;
		final String title;
		if (statusesSize > 1) {
			notifBuilder.setNumber(statusesSize);
		}
		final int usersCount = getUsersCount(statuses);
		contentIntent = new Intent(context, HomeActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		contentIntent.putExtra(EXTRA_TAB_TYPE, TAB_TYPE_MENTIONS_TIMELINE);
		if (statusesSize == 1) {
			final Uri.Builder uriBuilder = new Uri.Builder();
			uriBuilder.scheme(SCHEME_TWIDERE);
			uriBuilder.authority(AUTHORITY_STATUS);
			uriBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(firstItem.account_id));
			uriBuilder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(firstItem.id));
			final Intent statusIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build());
			statusIntent.setExtrasClassLoader(context.getClassLoader());
			statusIntent.putExtra(EXTRA_STATUS, firstItem);
			contentIntent.putExtra(EXTRA_EXTRA_INTENT, statusIntent);
		}

		final String displayName = getDisplayName(context, firstItem.user_id, firstItem.user_name,
				firstItem.user_screen_name, mNameFirst, mNickOnly);
		if (usersCount > 1) {
			title = resources.getString(titleMutiple, displayName, usersCount - 1);
		} else {
			title = resources.getString(titleSingle, displayName);
		}
		notifBuilder.setLargeIcon(getProfileImageForNotification(firstItem.user_profile_image_url));
		buildNotification(notifBuilder, accountPreferences, notificationType, title, title, firstItem.text_plain,
				firstItem.timestamp, icon, null, contentIntent, deleteIntent);
		final NotificationCompat.Style notifStyle;
		if (statusesSize > 1) {
			final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notifBuilder);
			final int max = Math.min(4, statusesSize);
			for (int i = 0; i < max; i++) {
				final ParcelableStatus s = safeGet(statuses, i);
				if (s == null) return;
				final String nameEscaped = HtmlEscapeHelper.escape(getDisplayName(context, s.user_id, s.user_name,
						s.user_screen_name, mNameFirst, mNickOnly));
				final String textEscaped = HtmlEscapeHelper.escape(stripMentionText(s.text_unescaped,
						getAccountScreenName(context, s.account_id)));
				inboxStyle.addLine(Html.fromHtml(String.format("<b>%s</b>: %s", nameEscaped, textEscaped)));
			}
			if (max == 4 && statusesSize - max > 0) {
				inboxStyle.addLine(context.getString(R.string.and_more, statusesSize - max));
			}
			inboxStyle.setSummaryText(getAccountDisplayName(context, accountId, mNameFirst));
			notifStyle = inboxStyle;
		} else {
			final Intent replyIntent = new Intent(INTENT_ACTION_REPLY);
			replyIntent.setExtrasClassLoader(context.getClassLoader());
			replyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationType);
			replyIntent.putExtra(EXTRA_NOTIFICATION_ACCOUNT, accountId);
			replyIntent.putExtra(EXTRA_STATUS, firstItem);
			replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			final Uri.Builder viewProfileBuilder = new Uri.Builder();
			viewProfileBuilder.scheme(SCHEME_TWIDERE);
			viewProfileBuilder.authority(AUTHORITY_USER);
			viewProfileBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(firstItem.account_id));
			viewProfileBuilder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(firstItem.user_id));
			final Intent viewProfileIntent = new Intent(Intent.ACTION_VIEW, viewProfileBuilder.build());
			viewProfileIntent.setPackage(APP_PACKAGE_NAME);
			notifBuilder.addAction(R.drawable.ic_action_reply, context.getString(R.string.reply),
					PendingIntent.getActivity(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			notifBuilder.addAction(R.drawable.ic_action_profile, context.getString(R.string.view_user_profile),
					PendingIntent.getActivity(context, 0, viewProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(notifBuilder);
			bigTextStyle.bigText(stripMentionText(firstItem.text_unescaped,
					getAccountScreenName(context, firstItem.account_id)));
			bigTextStyle.setSummaryText(getAccountDisplayName(context, accountId, mNameFirst));
			notifStyle = bigTextStyle;
		}
		final int accountNotificationId = getAccountNotificationId(notificationId, accountId);
		nm.notify(accountNotificationId, notifStyle.build());
	}

	private Cursor getCachedImageCursor(final String url) {
		if (Utils.isDebugBuild()) {
			Log.d(LOGTAG, String.format("getCachedImageCursor(%s)", url));
		}
		final MatrixCursor c = new MatrixCursor(TweetStore.CachedImages.MATRIX_COLUMNS);
		final File file = mImagePreloader.getCachedImageFile(url);
		if (url != null && file != null) {
			c.addRow(new String[] { url, file.getPath() });
		}
		return c;
	}

	private ParcelFileDescriptor getCachedImageFd(final String url) throws FileNotFoundException {
		if (Utils.isDebugBuild()) {
			Log.d(LOGTAG, String.format("getCachedImageFd(%s)", url));
		}
		final File file = mImagePreloader.getCachedImageFile(url);
		if (file == null) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private ParcelFileDescriptor getCacheFileFd(final String name) throws FileNotFoundException {
		if (name == null) return null;
		final Context mContext = getContext();
		final File cacheDir = mContext.getCacheDir();
		final File file = new File(cacheDir, name);
		if (!file.exists()) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private ContentResolver getContentResolver() {
		if (mContentResolver != null) return mContentResolver;
		final Context context = getContext();
		return mContentResolver = context.getContentResolver();
	}

	private Cursor getDNSCursor(final String host) {
		final MatrixCursor c = new MatrixCursor(TweetStore.DNS.MATRIX_COLUMNS);
		try {
			final String address = mHostAddressResolver.resolve(host);
			if (host != null && address != null) {
				c.addRow(new String[] { host, address });
			}
		} catch (final IOException e) {

		}
		return c;
	}

	private NotificationManager getNotificationManager() {
		if (mNotificationManager != null) return mNotificationManager;
		final Context context = getContext();
		return mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private Cursor getNotificationsCursor() {
		final MatrixCursor c = new MatrixCursor(TweetStore.Notifications.MATRIX_COLUMNS);
		c.addRow(new Integer[] { NOTIFICATION_ID_HOME_TIMELINE, mUnreadStatuses.size() });
		c.addRow(new Integer[] { NOTIFICATION_ID_MENTIONS, mNewMentions.size() });
		c.addRow(new Integer[] { NOTIFICATION_ID_DIRECT_MESSAGES, mNewMessages.size() });
		return c;
	}

	private Cursor getNotificationsCursor(final int id) {
		final MatrixCursor c = new MatrixCursor(TweetStore.Notifications.MATRIX_COLUMNS);
		if (id == NOTIFICATION_ID_HOME_TIMELINE) {
			c.addRow(new Integer[] { id, mNewStatuses.size() });
		} else if (id == NOTIFICATION_ID_MENTIONS) {
			c.addRow(new Integer[] { id, mNewMentions.size() });
		} else if (id == NOTIFICATION_ID_DIRECT_MESSAGES) {
			c.addRow(new Integer[] { id, mNewMessages.size() });
		}
		return c;
	}

	private Bitmap getProfileImageForNotification(final String profile_image_url) {
		final Context context = getContext();
		final Resources res = context.getResources();
		final int w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
		final int h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
		final File profile_image_file = mImagePreloader.getCachedImageFile(profile_image_url);
		final Bitmap profile_image = profile_image_file != null && profile_image_file.isFile() ? BitmapFactory
				.decodeFile(profile_image_file.getPath()) : null;
		if (profile_image != null) return Bitmap.createScaledBitmap(profile_image, w, h, true);
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.ic_profile_image_default), w, h,
				true);
	}

	private int getSendersCount(final List<ParcelableDirectMessage> items) {
		if (items == null || items.isEmpty()) return 0;
		final Set<Long> ids = new HashSet<Long>();
		for (final ParcelableDirectMessage item : items.toArray(new ParcelableDirectMessage[items.size()])) {
			ids.add(item.sender_id);
		}
		return ids.size();
	}

	private Cursor getUnreadCountsCursor() {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		return c;
	}

	private Cursor getUnreadCountsCursor(final int position) {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		final Context context = getContext();
		final SupportTabSpec tab = CustomTabUtils.getAddedTabAt(context, position);
		if (tab == null) return c;
		final int count;
		if (TAB_TYPE_HOME_TIMELINE.equals(tab.type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadStatuses, account_ids);
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadMentions, account_ids);
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(tab.type)) {
			final long account_id = tab.args != null ? tab.args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(context);
			count = getUnreadCount(mUnreadMessages, account_ids);
		} else {
			count = 0;
		}
		if (tab.type != null) {
			c.addRow(new Object[] { position, tab.type, count });
		}
		return c;
	}

	private Cursor getUnreadCountsCursorByType(final String type) {
		final MatrixCursor c = new MatrixCursor(TweetStore.UnreadCounts.MATRIX_COLUMNS);
		final int count;
		if (TAB_TYPE_HOME_TIMELINE.equals(type) || TAB_TYPE_STAGGERED_HOME_TIMELINE.equals(type)) {
			count = mUnreadStatuses.size();
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			count = mUnreadMentions.size();
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			count = mUnreadMessages.size();
		} else {
			count = 0;
		}
		if (type != null) {
			c.addRow(new Object[] { -1, type, count });
		}
		return c;
	}

	private int getUsersCount(final List<ParcelableStatus> items) {
		if (items == null || items.isEmpty()) return 0;
		final Set<Long> ids = new HashSet<Long>();
		for (final ParcelableStatus item : items.toArray(new ParcelableStatus[items.size()])) {
			ids.add(item.user_id);
		}
		return ids.size();
	}

	private boolean isNotificationAudible() {
		return mHomeActivityInBackground && !isNotificationsSilent(getContext());
	}

	private void notifyContentObserver(final Uri uri) {
		final ContentResolver cr = getContentResolver();
		if (uri == null || cr == null) return;
		cr.notifyChange(uri, null);
	}

	private int notifyIncomingMessagesInserted(final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		for (final ContentValues value : values) {
			final ParcelableDirectMessage message = new ParcelableDirectMessage(value);
			mNewMessages.add(message);
			if (mUnreadMessages.add(new UnreadItem(message.sender_id, message.account_id))) {
				result++;
			}
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
		}
		return result;
	}

	private int notifyMentionsInserted(final AccountPreferences[] prefs, final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		final boolean enabled = mPreferences.getBoolean(KEY_FILTERS_IN_MENTIONS, true);
		final boolean filtersForRts = mPreferences.getBoolean(KEY_FILTERS_FOR_RTS, true);
		for (final ContentValues value : values) {
			final ParcelableStatus status = new ParcelableStatus(value);
			if (!enabled || !isFiltered(mDatabaseWrapper.getSQLiteDatabase(), status, filtersForRts)) {
				final AccountPreferences pref = AccountPreferences.getAccountPreferences(prefs, status.account_id);
				if (pref == null || status.user_is_following || !pref.isMyFollowingOnly()) {
					mNewMentions.add(status);
				}
				if (mUnreadMentions.add(new UnreadItem(status.id, status.account_id))) {
					result++;
				}
			}
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		}
		return result;
	}

	private int notifyStatusesInserted(final ContentValues... values) {
		if (values == null || values.length == 0) return 0;
		// Add statuses that not filtered to list for future use.
		int result = 0;
		final boolean enabled = mPreferences.getBoolean(KEY_FILTERS_IN_HOME_TIMELINE, true);
		final boolean filtersForRts = mPreferences.getBoolean(KEY_FILTERS_FOR_RTS, true);
		for (final ContentValues value : values) {
			final ParcelableStatus status = new ParcelableStatus(value);
			if (!enabled || !isFiltered(mDatabaseWrapper.getSQLiteDatabase(), status, filtersForRts)) {
				mNewStatuses.add(status);
				if (mUnreadStatuses.add(new UnreadItem(status.id, status.account_id))) {
					result++;
				}
			}
		}
		if (result > 0) {
			saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		}
		return result;
	}

	private void notifyUnreadCountChanged(final int position) {
		final Intent intent = new Intent(BROADCAST_UNREAD_COUNT_UPDATED);
		intent.putExtra(EXTRA_TAB_POSITION, position);
		final Context context = getContext();
		context.sendBroadcast(intent);
		notifyContentObserver(UnreadCounts.CONTENT_URI);
	}

	private void onDatabaseUpdated(final int tableId, final Uri uri) {
		if (uri == null) return;
		switch (tableId) {
			case TABLE_ID_ACCOUNTS: {
				clearAccountColor();
				clearAccountName();
				break;
			}
		}
		notifyContentObserver(getNotificationUri(tableId, uri));
	}

	private void onNewItemsInserted(final Uri uri, final ContentValues... values) {
		if (uri == null || values == null || values.length == 0) return;
		preloadImages(values);
		if (!uri.getBooleanQueryParameter(QUERY_PARAM_NOTIFY, true)) return;
		switch (getTableId(uri)) {
			case TABLE_ID_STATUSES: {
				final int notifiedCount = notifyStatusesInserted(values);
				final List<ParcelableStatus> items = new ArrayList<ParcelableStatus>(mNewStatuses);
				Collections.sort(items);
				final AccountPreferences[] prefs = AccountPreferences.getNotificationEnabledPreferences(getContext(),
						getAccountIds(getContext()));
				for (final AccountPreferences pref : prefs) {
					if (pref.isHomeTimelineNotificationEnabled()) {
						final long accountId = pref.getAccountId();
						displayStatusesNotification(notifiedCount, pref, pref.getHomeTimelineNotificationType(),
								NOTIFICATION_ID_HOME_TIMELINE, getStatusesForAccounts(items, accountId),
								R.string.notification_status, R.string.notification_status_multiple,
								R.drawable.ic_stat_twitter);
					}
				}
				notifyUnreadCountChanged(NOTIFICATION_ID_HOME_TIMELINE);
				break;
			}
			case TABLE_ID_MENTIONS: {
				final AccountPreferences[] prefs = AccountPreferences.getNotificationEnabledPreferences(getContext(),
						getAccountIds(getContext()));
				final int notifiedCount = notifyMentionsInserted(prefs, values);
				final List<ParcelableStatus> items = new ArrayList<ParcelableStatus>(mNewMentions);
				Collections.sort(items);
				for (final AccountPreferences pref : prefs) {
					if (pref.isMentionsNotificationEnabled()) {
						final long accountId = pref.getAccountId();
						displayStatusesNotification(notifiedCount, pref, pref.getMentionsNotificationType(),
								NOTIFICATION_ID_MENTIONS, getStatusesForAccounts(items, accountId),
								R.string.notification_mention, R.string.notification_mention_multiple,
								R.drawable.ic_stat_mention);
					}
				}
				notifyUnreadCountChanged(NOTIFICATION_ID_MENTIONS);
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_INBOX: {
				final int notifiedCount = notifyIncomingMessagesInserted(values);
				final List<ParcelableDirectMessage> items = new ArrayList<ParcelableDirectMessage>(mNewMessages);
				Collections.sort(items);
				final AccountPreferences[] prefs = AccountPreferences.getNotificationEnabledPreferences(getContext(),
						getAccountIds(getContext()));
				for (final AccountPreferences pref : prefs) {
					if (pref.isDirectMessagesNotificationEnabled()) {
						final long accountId = pref.getAccountId();
						displayMessagesNotification(notifiedCount, pref, pref.getDirectMessagesNotificationType(),
								R.drawable.ic_stat_mention, getMessagesForAccounts(items, accountId));
					}
				}
				notifyUnreadCountChanged(NOTIFICATION_ID_DIRECT_MESSAGES);
				break;
			}
		}
	}

	private void preloadImages(final ContentValues... values) {
		if (values == null) return;
		for (final ContentValues v : values) {
			if (mPreferences.getBoolean(KEY_PRELOAD_PROFILE_IMAGES, false)) {
				mImagePreloader.preloadImage(v.getAsString(Statuses.USER_PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL));
			}
			if (mPreferences.getBoolean(KEY_PRELOAD_PREVIEW_IMAGES, false)) {
				final String textHtml = v.getAsString(Statuses.TEXT_HTML);
				for (final String link : MediaPreviewUtils.getSupportedLinksInStatus(textHtml)) {
					mImagePreloader.preloadImage(link);
				}
			}
		}
	}

	private int removeUnreadItems(final int tab_position, final long account_id, final long... ids) {
		if (tab_position < 0 || account_id == 0 || ids == null || ids.length == 0) return 0;
		final UnreadItem[] items = new UnreadItem[ids.length];
		for (int i = 0, j = ids.length; i < j; i++) {
			items[i] = new UnreadItem(ids[i], account_id);
		}
		return removeUnreadItems(tab_position, items);
	}

	private synchronized int removeUnreadItems(final int tab_position, final UnreadItem... items) {
		if (tab_position < 0 || items == null || items.length == 0) return 0;
		final int result;
		final String type = CustomTabUtils.getAddedTabTypeAt(getContext(), tab_position);
		if (TAB_TYPE_HOME_TIMELINE.equals(type)) {
			final int size = mUnreadStatuses.size();
			mUnreadStatuses.removeAll(Arrays.asList(items));
			result = size - mUnreadStatuses.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
			}
		} else if (TAB_TYPE_MENTIONS_TIMELINE.equals(type)) {
			final int size = mUnreadMentions.size();
			mUnreadMentions.removeAll(Arrays.asList(items));
			result = size - mUnreadMentions.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
			}
		} else if (TAB_TYPE_DIRECT_MESSAGES.equals(type)) {
			final int size = mUnreadMessages.size();
			mUnreadMessages.removeAll(Arrays.asList(items));
			result = size - mUnreadMessages.size();
			if (result != 0) {
				saveUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
			}
		} else
			return 0;
		if (result != 0) {
			notifyUnreadCountChanged(tab_position);
		}
		return result;
	}

	private void restoreUnreadItems() {
		restoreUnreadItemsFile(mUnreadStatuses, UNREAD_STATUSES_FILE_NAME);
		restoreUnreadItemsFile(mUnreadMentions, UNREAD_MENTIONS_FILE_NAME);
		restoreUnreadItemsFile(mUnreadMessages, UNREAD_MESSAGES_FILE_NAME);
	}

	private void restoreUnreadItemsFile(final Collection<UnreadItem> items, final String name) {
		if (items == null || name == null) return;
		try {
			final File file = JSONFileIO.getSerializationFile(getContext(), name);
			final List<UnreadItem> restored = JSONFileIO.readArrayList(file);
			if (restored != null) {
				items.addAll(restored);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void saveUnreadItemsFile(final Collection<UnreadItem> items, final String name) {
		if (items == null || name == null) return;
		try {
			final File file = JSONFileIO.getSerializationFile(getContext(), name);
			JSONFileIO.writeArray(file, items.toArray(new UnreadItem[items.size()]));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void setNotificationUri(final Cursor c, final Uri uri) {
		final ContentResolver cr = getContentResolver();
		if (cr == null || c == null || uri == null) return;
		c.setNotificationUri(cr, uri);
	}

	private void updatePreferences() {
		mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, false);
		mNickOnly = mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
	}

	private static int clearUnreadCount(final List<UnreadItem> set, final long[] accountIds) {
		if (accountIds == null) return 0;
		int count = 0;
		for (final UnreadItem item : set.toArray(new UnreadItem[set.size()])) {
			if (item != null && ArrayUtils.contains(accountIds, item.account_id) && set.remove(item)) {
				count++;
			}
		}
		return count;
	}

	private static List<ParcelableDirectMessage> getMessagesForAccounts(final List<ParcelableDirectMessage> items,
			final long accountId) {
		if (items == null) return Collections.emptyList();
		final List<ParcelableDirectMessage> result = new ArrayList<ParcelableDirectMessage>();
		for (final ParcelableDirectMessage item : items.toArray(new ParcelableDirectMessage[items.size()])) {
			if (item.account_id == accountId) {
				result.add(item);
			}
		}
		return result;
	}

	private static Cursor getPreferencesCursor(final SharedPreferencesWrapper preferences, final String key) {
		final MatrixCursor c = new MatrixCursor(TweetStore.Preferences.MATRIX_COLUMNS);
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, ?> all = preferences.getAll();
		if (key == null) {
			map.putAll(all);
		} else {
			map.put(key, all.get(key));
		}
		for (final Map.Entry<String, ?> item : map.entrySet()) {
			final Object value = item.getValue();
			final int type = getPreferenceType(value);
			c.addRow(new Object[] { item.getKey(), ParseUtils.parseString(value), type });
		}
		return c;
	}

	private static int getPreferenceType(final Object object) {
		if (object == null)
			return Preferences.TYPE_NULL;
		else if (object instanceof Boolean)
			return Preferences.TYPE_BOOLEAN;
		else if (object instanceof Integer)
			return Preferences.TYPE_INTEGER;
		else if (object instanceof Long)
			return Preferences.TYPE_LONG;
		else if (object instanceof Float)
			return Preferences.TYPE_FLOAT;
		else if (object instanceof String) return Preferences.TYPE_STRING;
		return Preferences.TYPE_INVALID;
	}

	private static List<ParcelableStatus> getStatusesForAccounts(final List<ParcelableStatus> items,
			final long accountId) {
		if (items == null) return Collections.emptyList();
		final List<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
		for (final ParcelableStatus item : items.toArray(new ParcelableStatus[items.size()])) {
			if (item.account_id == accountId) {
				result.add(item);
			}
		}
		return result;
	}

	private static int getUnreadCount(final List<UnreadItem> set, final long... accountIds) {
		if (set == null || set.isEmpty()) return 0;
		int count = 0;
		for (final UnreadItem item : set.toArray(new UnreadItem[set.size()])) {
			if (item != null && ArrayUtils.contains(accountIds, item.account_id)) {
				count++;
			}
		}
		return count;
	}

	private static <T> T safeGet(final List<T> list, final int index) {
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	private static boolean shouldReplaceOnConflict(final int table_id) {
		switch (table_id) {
			case TABLE_ID_CACHED_HASHTAGS:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
				return true;
		}
		return false;
	}

	private static String stripMentionText(final String text, final String my_screen_name) {
		if (text == null || my_screen_name == null) return text;
		final String temp = "@" + my_screen_name + " ";
		if (text.startsWith(temp)) return text.substring(temp.length());
		return text;
	}

	@SuppressWarnings("unused")
	private static class GetWritableDatabaseTask extends AsyncTask<Void, Void, SQLiteDatabase> {
		private final Context mContext;
		private final SQLiteOpenHelper mHelper;
		private final SQLiteDatabaseWrapper mWrapper;

		GetWritableDatabaseTask(final Context context, final SQLiteOpenHelper helper,
				final SQLiteDatabaseWrapper wrapper) {
			mContext = context;
			mHelper = helper;
			mWrapper = wrapper;
		}

		@Override
		protected SQLiteDatabase doInBackground(final Void... params) {
			return mHelper.getWritableDatabase();
		}

		@Override
		protected void onPostExecute(final SQLiteDatabase result) {
			mWrapper.setSQLiteDatabase(result);
			if (result != null) {
				mContext.sendBroadcast(new Intent(BROADCAST_DATABASE_READY));
			}
		}
	}

}
