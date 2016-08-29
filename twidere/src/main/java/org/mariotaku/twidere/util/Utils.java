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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.os.AsyncTask;
import android.os.BadParcelableException;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.support.v4.app.Fragment;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.GeoLocation;
import org.mariotaku.microblog.library.twitter.model.RateLimitStatus;
import org.mariotaku.microblog.library.twitter.model.Relationship;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.sqliteqb.library.AllColumns;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.sqliteqb.library.Selectable;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.CopyLinkActivity;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.fragment.AccountsManagerFragment;
import org.mariotaku.twidere.fragment.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.DraftsFragment;
import org.mariotaku.twidere.fragment.FiltersFragment;
import org.mariotaku.twidere.fragment.GroupFragment;
import org.mariotaku.twidere.fragment.IncomingFriendshipsFragment;
import org.mariotaku.twidere.fragment.InteractionsTimelineFragment;
import org.mariotaku.twidere.fragment.ItemsListFragment;
import org.mariotaku.twidere.fragment.ListsFragment;
import org.mariotaku.twidere.fragment.MessagesConversationFragment;
import org.mariotaku.twidere.fragment.MutesUsersListFragment;
import org.mariotaku.twidere.fragment.PublicTimelineFragment;
import org.mariotaku.twidere.fragment.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.SearchFragment;
import org.mariotaku.twidere.fragment.StatusFavoritersListFragment;
import org.mariotaku.twidere.fragment.StatusFragment;
import org.mariotaku.twidere.fragment.StatusRetweetersListFragment;
import org.mariotaku.twidere.fragment.UserBlocksListFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserGroupsFragment;
import org.mariotaku.twidere.fragment.UserListFragment;
import org.mariotaku.twidere.fragment.UserListMembersFragment;
import org.mariotaku.twidere.fragment.UserListMembershipsFragment;
import org.mariotaku.twidere.fragment.UserListSubscribersFragment;
import org.mariotaku.twidere.fragment.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.UserMediaTimelineFragment;
import org.mariotaku.twidere.fragment.UserMentionsFragment;
import org.mariotaku.twidere.fragment.UserProfileEditorFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.graphic.PaddingDrawable;
import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableDirectMessageCursorIndices;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusCursorIndices;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.PebbleMessage;
import org.mariotaku.twidere.model.TwitterAccountExtra;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.TwidereLinkify.HighlightStyle;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.NotificationEvent;

import static org.mariotaku.twidere.provider.TwidereDataStore.DIRECT_MESSAGES_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.STATUSES_URIS;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;

public final class Utils implements Constants {

    public static final Pattern PATTERN_XML_RESOURCE_IDENTIFIER = Pattern.compile("res/xml/([\\w_]+)\\.xml");
    public static final Pattern PATTERN_RESOURCE_IDENTIFIER = Pattern.compile("@([\\w_]+)/([\\w_]+)");

    private static final UriMatcher LINK_HANDLER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final UriMatcher HOME_TABS_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS, null, LINK_ID_STATUS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER, null, LINK_ID_USER);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, LINK_ID_USER_TIMELINE);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_MEDIA_TIMELINE, null, LINK_ID_USER_MEDIA_TIMELINE);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWERS, null, LINK_ID_USER_FOLLOWERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FRIENDS, null, LINK_ID_USER_FRIENDS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, LINK_ID_USER_FAVORITES);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_BLOCKS, null, LINK_ID_USER_BLOCKS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DIRECT_MESSAGES_CONVERSATION, null,
                LINK_ID_DIRECT_MESSAGES_CONVERSATION);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DIRECT_MESSAGES, null, LINK_ID_DIRECT_MESSAGES);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_INTERACTIONS, null, LINK_ID_INTERACTIONS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_PUBLIC_TIMELINE, null, LINK_ID_PUBLIC_TIMELINE);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST, null, LINK_ID_USER_LIST);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_GROUP, null, LINK_ID_GROUP);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_TIMELINE, null, LINK_ID_USER_LIST_TIMELINE);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_MEMBERS, null, LINK_ID_USER_LIST_MEMBERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_SUBSCRIBERS, null, LINK_ID_USER_LIST_SUBSCRIBERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_MEMBERSHIPS, null, LINK_ID_USER_LIST_MEMBERSHIPS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LISTS, null, LINK_ID_USER_LISTS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_GROUPS, null, LINK_ID_USER_GROUPS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SAVED_SEARCHES, null, LINK_ID_SAVED_SEARCHES);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_MENTIONS, null, LINK_ID_USER_MENTIONS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_INCOMING_FRIENDSHIPS, null, LINK_ID_INCOMING_FRIENDSHIPS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_ITEMS, null, LINK_ID_ITEMS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS_RETWEETERS, null, LINK_ID_STATUS_RETWEETERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS_FAVORITERS, null, LINK_ID_STATUS_FAVORITERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SEARCH, null, LINK_ID_SEARCH);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_MUTES_USERS, null, LINK_ID_MUTES_USERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_MAP, null, LINK_ID_MAP);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SCHEDULED_STATUSES, null, LINK_ID_SCHEDULED_STATUSES);

        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_ACCOUNTS, null, LINK_ID_ACCOUNTS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DRAFTS, null, LINK_ID_DRAFTS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_FILTERS, null, LINK_ID_FILTERS);
        LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_PROFILE_EDITOR, null, LINK_ID_PROFILE_EDITOR);

        HOME_TABS_URI_MATCHER.addURI(CustomTabType.HOME_TIMELINE, null, TAB_CODE_HOME_TIMELINE);
        HOME_TABS_URI_MATCHER.addURI(CustomTabType.NOTIFICATIONS_TIMELINE, null, TAB_CODE_NOTIFICATIONS_TIMELINE);
        HOME_TABS_URI_MATCHER.addURI(CustomTabType.DIRECT_MESSAGES, null, TAB_CODE_DIRECT_MESSAGES);
    }


    private Utils() {
        throw new AssertionError("You are trying to create an instance for this utility class!");
    }

    public static void addIntentToMenuForExtension(final Context context, final Menu menu,
                                                   final int groupId, final String action,
                                                   final String parcelableKey, final String parcelableJSONKey,
                                                   final Parcelable parcelable) {
        if (context == null || menu == null || action == null || parcelableKey == null || parcelable == null)
            return;
        final PackageManager pm = context.getPackageManager();
        final Resources res = context.getResources();
        final float density = res.getDisplayMetrics().density;
        final int padding = Math.round(density * 4);
        final Intent queryIntent = new Intent(action);
        queryIntent.setExtrasClassLoader(context.getClassLoader());
        final List<ResolveInfo> activities = pm.queryIntentActivities(queryIntent, PackageManager.GET_META_DATA);
        final String parcelableJson = JsonSerializer.serialize(parcelable);
        for (final ResolveInfo info : activities) {
            final Intent intent = new Intent(queryIntent);
            if (isExtensionUseJSON(info) && parcelableJson != null) {
                intent.putExtra(parcelableJSONKey, parcelableJson);
            } else {
                intent.putExtra(parcelableKey, parcelable);
            }
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            final MenuItem item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm));
            item.setIntent(intent);
            final Drawable metaDataDrawable = getMetadataDrawable(pm, info.activityInfo, METADATA_KEY_EXTENSION_ICON);
            final int actionIconColor = ThemeUtils.getThemeForegroundColor(context);
            if (metaDataDrawable != null) {
                metaDataDrawable.mutate();
                metaDataDrawable.setColorFilter(actionIconColor, Mode.SRC_ATOP);
                item.setIcon(metaDataDrawable);
            } else {
                final Drawable icon = info.loadIcon(pm);
                final int iw = icon.getIntrinsicWidth(), ih = icon.getIntrinsicHeight();
                if (iw > 0 && ih > 0) {
                    final Drawable iconWithPadding = new PaddingDrawable(icon, padding);
                    iconWithPadding.setBounds(0, 0, iw, ih);
                    item.setIcon(iconWithPadding);
                } else {
                    item.setIcon(icon);
                }
            }

        }
    }

    public static void announceForAccessibilityCompat(final Context context, final View view, final CharSequence text,
                                                      final Class<?> cls) {
        final AccessibilityManager accessibilityManager = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) return;
        // Prior to SDK 16, announcements could only be made through FOCUSED
        // events. Jelly Bean (SDK 16) added support for speaking text verbatim
        // using the ANNOUNCEMENT event type.
        final int eventType;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
        } else {
            eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
        }

        // Construct an accessibility event with the minimum recommended
        // attributes. An event without a class name or package may be dropped.
        final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.getText().add(text);
        event.setClassName(cls.getName());
        event.setPackageName(context.getPackageName());
        event.setSource(view);

        // Sends the event directly through the accessibility manager. If your
        // application only targets SDK 14+, you should just call
        // getParent().requestSendAccessibilityEvent(this, event);
        accessibilityManager.sendAccessibilityEvent(event);
    }

    public static Uri buildDirectMessageConversationUri(final UserKey accountKey, final String conversationId,
                                                        final String screenName) {
        if (conversationId == null && screenName == null) return TwidereDataStore.CONTENT_URI_NULL;
        final Uri.Builder builder;
        if (conversationId != null) {
            builder = DirectMessages.Conversation.CONTENT_URI.buildUpon();
        } else {
            builder = DirectMessages.Conversation.CONTENT_URI_SCREEN_NAME.buildUpon();
        }
        builder.appendPath(String.valueOf(accountKey));
        if (conversationId != null) {
            builder.appendPath(String.valueOf(conversationId));
        } else {
            builder.appendPath(screenName);
        }
        return builder.build();
    }

    public static int calculateInSampleSize(final int width, final int height, final int preferredWidth,
                                            final int preferredHeight) {
        if (preferredHeight > height && preferredWidth > width) return 1;
        final int result = Math.round(Math.max(width, height) / (float) Math.max(preferredWidth, preferredHeight));
        return Math.max(1, result);
    }

    public static boolean closeSilently(final Closeable c) {
        if (c == null) return false;
        try {
            c.close();
        } catch (final IOException e) {
            return false;
        }
        return true;
    }

    public static boolean closeSilently(final Cursor c) {
        if (c == null) return false;
        c.close();
        return true;
    }

    public static void configBaseAdapter(final Context context, final IBaseAdapter adapter) {
        if (context == null) return;
        final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        adapter.setProfileImageDisplayed(pref.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true));
        adapter.setDisplayNameFirst(pref.getBoolean(KEY_NAME_FIRST, true));
        adapter.setLinkHighlightOption(pref.getString(KEY_LINK_HIGHLIGHT_OPTION, VALUE_LINK_HIGHLIGHT_OPTION_NONE));
        adapter.setTextSize(pref.getInt(KEY_TEXT_SIZE, getDefaultTextSize(context)));
        adapter.notifyDataSetChanged();
    }

    public static int[] getAccountColors(@Nullable final ParcelableAccount[] accounts) {
        if (accounts == null) return null;
        final int[] colors = new int[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            colors[i] = accounts[i].color;
        }
        return colors;
    }


    public static class NoAccountException extends Exception {
        String accountHost;

        public String getAccountHost() {
            return accountHost;
        }

        public void setAccountHost(String accountHost) {
            this.accountHost = accountHost;
        }
    }

    public static String getUserKeyParam(Uri uri) {
        final String paramUserKey = uri.getQueryParameter(QUERY_PARAM_USER_KEY);
        if (paramUserKey == null) {
            return uri.getQueryParameter(QUERY_PARAM_USER_ID);
        }
        return paramUserKey;
    }

    public static Intent createStatusShareIntent(@NonNull final Context context, @NonNull final ParcelableStatus status) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, IntentUtils.getStatusShareSubject(context, status));
        intent.putExtra(Intent.EXTRA_TEXT, IntentUtils.getStatusShareText(context, status));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @Nullable
    public static UserKey[] getAccountKeys(@NonNull Context context, @Nullable Bundle args) {
        if (args == null) return null;
        if (args.containsKey(EXTRA_ACCOUNT_KEYS)) {
            return newParcelableArray(args.getParcelableArray(EXTRA_ACCOUNT_KEYS), UserKey.CREATOR);
        } else if (args.containsKey(EXTRA_ACCOUNT_KEY)) {
            final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
            if (accountKey == null) return new UserKey[0];
            return new UserKey[]{accountKey};
        } else if (args.containsKey(EXTRA_ACCOUNT_ID)) {
            final String accountId = String.valueOf(args.get(EXTRA_ACCOUNT_ID));
            try {
                if (Long.parseLong(accountId) <= 0) return null;
            } catch (NumberFormatException e) {
                // Ignore
            }
            final UserKey accountKey = UserKeyUtils.findById(context, accountId);
            args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey);
            if (accountKey == null) return new UserKey[]{new UserKey(accountId, null)};
            return new UserKey[]{accountKey};
        }
        return null;
    }

    @Nullable
    public static UserKey getAccountKey(@NonNull Context context, @Nullable Bundle args) {
        final UserKey[] accountKeys = getAccountKeys(context, args);
        if (ArrayUtils.isEmpty(accountKeys)) return null;
        return accountKeys[0];
    }

    @NonNull
    public static String getReadPositionTagWithAccount(@NonNull final String tag,
                                                       @Nullable final UserKey accountKey) {
        if (accountKey == null) return tag;
        return tag + "_" + accountKey;
    }

    public static ParcelableDirectMessage findDirectMessageInDatabases(final Context context,
                                                                       final UserKey accountKey,
                                                                       final long messageId) {
        if (context == null) return null;
        final ContentResolver resolver = context.getContentResolver();
        ParcelableDirectMessage message = null;
        final String where = Expression.and(Expression.equalsArgs(DirectMessages.ACCOUNT_KEY),
                Expression.equalsArgs(DirectMessages.MESSAGE_ID)).getSQL();
        final String[] whereArgs = {accountKey.toString(), String.valueOf(messageId)};
        for (final Uri uri : DIRECT_MESSAGES_URIS) {
            final Cursor cur = resolver.query(uri, DirectMessages.COLUMNS, where, whereArgs, null);
            if (cur == null) {
                continue;
            }
            if (cur.getCount() > 0 && cur.moveToFirst()) {
                message = ParcelableDirectMessageCursorIndices.fromCursor(cur);
            }
            cur.close();
        }
        return message;
    }

    @NonNull
    @WorkerThread
    public static ParcelableStatus findStatus(@NonNull final Context context,
                                              @NonNull final UserKey accountKey,
                                              @NonNull final String statusId)
            throws MicroBlogException {
        final ParcelableStatus cached = findStatusInDatabases(context, accountKey, statusId);
        if (cached != null) return cached;
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, accountKey, true);
        if (twitter == null) throw new MicroBlogException("Account does not exist");
        final Status status = twitter.showStatus(statusId);
        final String where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.equalsArgs(Statuses.STATUS_ID)).getSQL();
        final String[] whereArgs = {accountKey.toString(), statusId};
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(CachedStatuses.CONTENT_URI, where, whereArgs);
        resolver.insert(CachedStatuses.CONTENT_URI, ContentValuesCreator.createStatus(status, accountKey));
        return ParcelableStatusUtils.INSTANCE.fromStatus(status, accountKey, false);
    }

    @Nullable
    @WorkerThread
    public static ParcelableStatus findStatusInDatabases(@NonNull final Context context,
                                                         @NonNull final UserKey accountKey,
                                                         @NonNull final String statusId) {
        final ContentResolver resolver = context.getContentResolver();
        ParcelableStatus status = null;
        final String where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.equalsArgs(Statuses.STATUS_ID)).getSQL();
        final String[] whereArgs = {accountKey.toString(), statusId};
        for (final Uri uri : STATUSES_URIS) {
            final Cursor cur = resolver.query(uri, Statuses.COLUMNS, where, whereArgs, null);
            if (cur == null) {
                continue;
            }
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                status = ParcelableStatusCursorIndices.fromCursor(cur);
            }
            cur.close();
        }
        return status;
    }

    @SuppressWarnings("deprecation")
    public static String formatSameDayTime(final Context context, final long timestamp) {
        if (context == null) return null;
        if (DateUtils.isToday(timestamp))
            return DateUtils.formatDateTime(context, timestamp,
                    DateFormat.is24HourFormat(context) ? DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR
                            : DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
        return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE);
    }

    @SuppressWarnings("deprecation")
    public static String formatToLongTimeString(final Context context, final long timestamp) {
        if (context == null) return null;
        final Time then = new Time();
        then.set(timestamp);
        final Time now = new Time();
        now.setToNow();

        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

        format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, timestamp, format_flags);
    }

    public static boolean isComposeNowSupported(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || context == null) return false;
        return hasNavBar(context);
    }

    public static boolean isOfficialCredentials(@NonNull final Context context, final UserKey accountKey) {
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context, accountKey);
        if (credentials == null) return false;
        return isOfficialCredentials(context, credentials);
    }

    public static boolean isOfficialCredentials(@NonNull final Context context,
                                                @NonNull final ParcelableCredentials account) {
        if (ParcelableAccount.Type.TWITTER.equals(account.account_type)) {
            final TwitterAccountExtra extra = JsonSerializer.parse(account.account_extras,
                    TwitterAccountExtra.class);
            if (extra != null) {
                return extra.isOfficialCredentials();
            }
        }
        final boolean isOAuth = ParcelableCredentialsUtils.isOAuth(account.auth_type);
        final String consumerKey = account.consumer_key, consumerSecret = account.consumer_secret;
        return isOAuth && TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret);
    }

    public static boolean setLastSeen(Context context, ParcelableUserMention[] entities, long time) {
        if (entities == null) return false;
        boolean result = false;
        for (ParcelableUserMention entity : entities) {
            result |= setLastSeen(context, entity.key, time);
        }
        return result;
    }

    public static boolean setLastSeen(Context context, UserKey userId, long time) {
        final ContentResolver cr = context.getContentResolver();
        final ContentValues values = new ContentValues();
        if (time > 0) {
            values.put(CachedUsers.LAST_SEEN, time);
        } else {
            // Zero or negative value means remove last seen
            values.putNull(CachedUsers.LAST_SEEN);
        }
        final String where = Expression.equalsArgs(CachedUsers.USER_KEY).getSQL();
        final String[] selectionArgs = {userId.toString()};
        return cr.update(CachedUsers.CONTENT_URI, values, where, selectionArgs) > 0;
    }


    public static Selectable getColumnsFromProjection(final String... projection) {
        if (projection == null) return new AllColumns();
        final int length = projection.length;
        final Column[] columns = new Column[length];
        for (int i = 0; i < length; i++) {
            columns[i] = new Column(projection[i]);
        }
        return new Columns(columns);
    }

    @Nullable
    public static UserKey getDefaultAccountKey(final Context context) {
        if (context == null) return null;
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        UserKey accountKey = UserKey.valueOf(prefs.getString(KEY_DEFAULT_ACCOUNT_KEY, null));
        final UserKey[] accountKeys = DataStoreUtils.getAccountKeys(context);
        int idMatchIdx = -1;
        for (int i = 0, accountIdsLength = accountKeys.length; i < accountIdsLength; i++) {
            if (accountKeys[i].equals(accountKey)) {
                idMatchIdx = i;
            }
        }
        if (idMatchIdx != -1) {
            return accountKeys[idMatchIdx];
        }
        if (accountKeys.length > 0 && !ArrayUtils.contains(accountKeys, accountKey)) {
             /* TODO: this is just a quick fix */
            return accountKeys[0];
        }
        return null;
    }

    public static int getDefaultTextSize(final Context context) {
        if (context == null) return 15;
        return context.getResources().getInteger(R.integer.default_text_size);
    }

    public static String getErrorMessage(final Context context, final CharSequence message) {
        if (context == null) return ParseUtils.parseString(message);
        if (TextUtils.isEmpty(message)) return context.getString(R.string.error_unknown_error);
        return context.getString(R.string.error_message, message);
    }

    public static String getErrorMessage(final Context context, final CharSequence action, final CharSequence message) {
        if (context == null || TextUtils.isEmpty(action)) return ParseUtils.parseString(message);
        if (TextUtils.isEmpty(message)) return context.getString(R.string.error_unknown_error);
        return context.getString(R.string.error_message_with_action, action, message);
    }

    public static String getErrorMessage(final Context context, final CharSequence action, @Nullable final Throwable t) {
        if (context == null) return null;
        if (t instanceof MicroBlogException)
            return getTwitterErrorMessage(context, action, (MicroBlogException) t);
        else if (t != null) return getErrorMessage(context, trimLineBreak(t.getMessage()));
        return context.getString(R.string.error_unknown_error);
    }

    public static String getErrorMessage(final Context context, final Throwable t) {
        if (t == null) return null;
        if (context != null && t instanceof MicroBlogException)
            return getTwitterErrorMessage(context, (MicroBlogException) t);
        return t.getMessage();
    }


    public static String getImagePathFromUri(final Context context, final Uri uri) {
        if (context == null || uri == null) return null;

        final String mediaUriStart = ParseUtils.parseString(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (ParseUtils.parseString(uri).startsWith(mediaUriStart)) {

            final String[] proj = {MediaStore.Images.Media.DATA};
            final Cursor cur = context.getContentResolver().query(uri, proj, null, null, null);

            if (cur == null) return null;

            final int idxData = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cur.moveToFirst();
            try {
                return cur.getString(idxData);
            } finally {
                cur.close();
            }
        } else {
            final String path = uri.getPath();
            if (path != null && new File(path).exists()) return path;
        }
        return null;
    }

    public static String getMediaUploadStatus(@NonNull final Context context,
                                              @Nullable final CharSequence[] links,
                                              @Nullable final CharSequence text) {
        if (ArrayUtils.isEmpty(links) || text == null) return ParseUtils.parseString(text);
        return text + " " + TwidereArrayUtils.toString(links, ' ', false);
    }

    public static File getInternalCacheDir(final Context context, final String cacheDirName) {
        if (context == null) throw new NullPointerException();
        final File cacheDir = new File(context.getCacheDir(), cacheDirName);
        if (cacheDir.isDirectory() || cacheDir.mkdirs()) return cacheDir;
        return new File(context.getCacheDir(), cacheDirName);
    }

    @Nullable
    public static File getExternalCacheDir(final Context context, final String cacheDirName) {
        if (context == null) throw new NullPointerException();
        final File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) return null;
        final File cacheDir = new File(externalCacheDir, cacheDirName);
        if (cacheDir.isDirectory() || cacheDir.mkdirs()) return cacheDir;
        return new File(context.getCacheDir(), cacheDirName);
    }

    @HighlightStyle
    public static int getLinkHighlightingStyleInt(final String option) {
        if (option == null) return VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE;
        switch (option) {
            case VALUE_LINK_HIGHLIGHT_OPTION_BOTH:
                return VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH;
            case VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT:
                return VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT;
            case VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE:
                return VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE;
        }
        return VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE;
    }

    public static String getLocalizedNumber(final Locale locale, final Number number) {
        final NumberFormat nf = NumberFormat.getInstance(locale);
        return nf.format(number);
    }

    @NonNull
    public static String[] getMatchedNicknameKeys(final String str, UserColorNameManager manager) {
        if (TextUtils.isEmpty(str)) return new String[0];
        final List<String> list = new ArrayList<>();
        for (final Entry<String, ?> entry : manager.getNameEntries()) {
            final String value = ParseUtils.parseString(entry.getValue());
            final String key = entry.getKey();
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                continue;
            }
            if (TwidereStringUtils.startsWithIgnoreCase(value, str)) {
                list.add(key);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    @NonNull
    public static String getNonEmptyString(final SharedPreferences pref, final String key, final String def) {
        if (pref == null) return def;
        final String val = pref.getString(key, def);
        return TextUtils.isEmpty(val) ? def : val;
    }

    public static String getNormalTwitterProfileImage(final String url) {
        return getTwitterProfileImageOfSize(url, "normal");
    }

    public static Uri getNotificationUri(final int tableId, final Uri def) {
        switch (tableId) {
            case TABLE_ID_DIRECT_MESSAGES:
            case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
            case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
            case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
                return DirectMessages.CONTENT_URI;
        }
        return def;
    }

    public static String getOriginalTwitterProfileImage(final String url) {
        if (url == null) return null;
        final Matcher matcher = PATTERN_TWITTER_PROFILE_IMAGES.matcher(url);
        if (matcher.matches())
            return matcher.replaceFirst("$1$2/profile_images/$3/$4$6");
        return url;
    }

    @ShapeStyle
    public static int getProfileImageStyle(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String style = prefs.getString(KEY_PROFILE_IMAGE_STYLE, null);
        return getProfileImageStyle(style);
    }

    @ShapeStyle
    public static int getProfileImageStyle(@NonNull SharedPreferences prefs) {
        final String style = prefs.getString(KEY_PROFILE_IMAGE_STYLE, null);
        return getProfileImageStyle(style);
    }

    @ShapeStyle
    public static int getProfileImageStyle(String style) {
        if (VALUE_PROFILE_IMAGE_STYLE_SQUARE.equalsIgnoreCase(style)) {
            return ShapedImageView.SHAPE_RECTANGLE;
        }
        return ShapedImageView.SHAPE_CIRCLE;
    }

    @PreviewStyle
    public static int getMediaPreviewStyle(String style) {
        if (VALUE_MEDIA_PREVIEW_STYLE_SCALE.equalsIgnoreCase(style)) {
            return VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE;
        }
        return VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP;
    }

    public static String getQuoteStatus(final Context context, final ParcelableStatus status) {
        if (context == null) return null;
        String quoteFormat = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
                KEY_QUOTE_FORMAT, DEFAULT_QUOTE_FORMAT);
        if (TextUtils.isEmpty(quoteFormat)) {
            quoteFormat = DEFAULT_QUOTE_FORMAT;
        }
        String result = quoteFormat.replace(FORMAT_PATTERN_LINK, LinkCreator.getStatusWebLink(status).toString());
        result = result.replace(FORMAT_PATTERN_NAME, status.user_screen_name);
        result = result.replace(FORMAT_PATTERN_TEXT, status.text_plain);
        return result;
    }

    public static int getResId(final Context context, final String string) {
        if (context == null || string == null) return 0;
        Matcher m = PATTERN_RESOURCE_IDENTIFIER.matcher(string);
        final Resources res = context.getResources();
        if (m.matches()) return res.getIdentifier(m.group(2), m.group(1), context.getPackageName());
        m = PATTERN_XML_RESOURCE_IDENTIFIER.matcher(string);
        if (m.matches()) return res.getIdentifier(m.group(1), "xml", context.getPackageName());
        return 0;
    }


    public static String getShareStatus(final Context context, final CharSequence title, final CharSequence text) {
        if (context == null) return null;
        String share_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
                KEY_SHARE_FORMAT, DEFAULT_SHARE_FORMAT);
        if (TextUtils.isEmpty(share_format)) {
            share_format = DEFAULT_SHARE_FORMAT;
        }
        if (TextUtils.isEmpty(title)) return ParseUtils.parseString(text);
        return share_format.replace(FORMAT_PATTERN_TITLE, title).replace(FORMAT_PATTERN_TEXT, text != null ? text : "");
    }

    public static String getTabDisplayOption(final Context context) {
        if (context == null) return null;
        final String defaultOption = context.getString(R.string.default_tab_display_option);
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TAB_DISPLAY_OPTION, defaultOption);
    }

    public static int getTabDisplayOptionInt(final Context context) {
        return getTabDisplayOptionInt(getTabDisplayOption(context));
    }

    public static int getTabDisplayOptionInt(final String option) {
        if (VALUE_TAB_DISPLAY_OPTION_ICON.equals(option))
            return VALUE_TAB_DISPLAY_OPTION_CODE_ICON;
        else if (VALUE_TAB_DISPLAY_OPTION_LABEL.equals(option))
            return VALUE_TAB_DISPLAY_OPTION_CODE_LABEL;
        return VALUE_TAB_DISPLAY_OPTION_CODE_BOTH;
    }

    public static boolean hasNavBar(@NonNull Context context) {
        final Resources resources = context.getResources();
        if (resources == null) return false;
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return resources.getBoolean(id);
        } else {
            // Check for keys
            return !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
                    && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        }
    }

    public static String getTwitterErrorMessage(final Context context, final CharSequence action,
                                                final MicroBlogException te) {
        if (context == null) return null;
        if (te == null) return context.getString(R.string.error_unknown_error);
        if (te.exceededRateLimitation()) {
            final RateLimitStatus status = te.getRateLimitStatus();
            final long secUntilReset = status.getSecondsUntilReset() * 1000;
            final String nextResetTime = ParseUtils.parseString(DateUtils.getRelativeTimeSpanString(System.currentTimeMillis()
                    + secUntilReset));
            if (TextUtils.isEmpty(action))
                return context.getString(R.string.error_message_rate_limit, nextResetTime.trim());
            return context.getString(R.string.error_message_rate_limit_with_action, action, nextResetTime.trim());
        } else if (te.getErrorCode() > 0) {
            final String msg = StatusCodeMessageUtils.getTwitterErrorMessage(context, te.getErrorCode());
            return getErrorMessage(context, action, msg != null ? msg : trimLineBreak(te.getMessage()));
        } else if (te.getCause() instanceof SSLException) {
            final String msg = te.getCause().getMessage();
            if (msg != null && msg.contains("!="))
                return getErrorMessage(context, action, context.getString(R.string.ssl_error));
            else
                return getErrorMessage(context, action, context.getString(R.string.network_error));
        } else if (te.getCause() instanceof IOException)
            return getErrorMessage(context, action, context.getString(R.string.network_error));
        else if (te.getCause() instanceof JSONException)
            return getErrorMessage(context, action, context.getString(R.string.api_data_corrupted));
        else
            return getErrorMessage(context, action, trimLineBreak(te.getMessage()));
    }

    public static String getTwitterErrorMessage(final Context context, final MicroBlogException te) {
        if (te == null) return null;
        if (StatusCodeMessageUtils.containsTwitterError(te.getErrorCode())) {
            return StatusCodeMessageUtils.getTwitterErrorMessage(context, te.getErrorCode());
        } else if (StatusCodeMessageUtils.containsHttpStatus(te.getStatusCode())) {
            return StatusCodeMessageUtils.getHttpStatusMessage(context, te.getStatusCode());
        } else if (te.getErrorMessage() != null) {
            return te.getErrorMessage();
        }
        return te.getMessage();
    }


    public static String getTwitterProfileImageOfSize(final String url, final String size) {
        if (url == null) return null;
        final Matcher matcher = PATTERN_TWITTER_PROFILE_IMAGES.matcher(url);
        if (matcher.matches()) {
            return matcher.replaceFirst("$1$2/profile_images/$3/$4_" + size + "$6");
        }
        return url;
    }

    @DrawableRes
    public static int getUserTypeIconRes(final boolean isVerified, final boolean isProtected) {
        if (isVerified)
            return R.drawable.ic_user_type_verified;
        else if (isProtected) return R.drawable.ic_user_type_protected;
        return 0;
    }

    @StringRes
    public static int getUserTypeDescriptionRes(final boolean isVerified, final boolean isProtected) {
        if (isVerified)
            return R.string.user_type_verified;
        else if (isProtected) return R.string.user_type_protected;
        return 0;
    }

    public static boolean hasAutoRefreshAccounts(final Context context) {
        final UserKey[] accountKeys = DataStoreUtils.getAccountKeys(context);
        return !ArrayUtils.isEmpty(AccountPreferences.getAutoRefreshEnabledAccountIds(context, accountKeys));
    }

    public static boolean isBatteryOkay(final Context context) {
        if (context == null) return false;
        final Context app = context.getApplicationContext();
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent intent = app.registerReceiver(null, filter);
        if (intent == null) return false;
        final boolean plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        final float level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        final float scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return plugged || level / scale > 0.15f;
    }

    public static boolean isMyAccount(final Context context, @Nullable final UserKey accountKey) {
        if (context == null || accountKey == null) return false;
        final String[] projection = new String[]{SQLFunctions.COUNT()};
        final Cursor cur = DataStoreUtils.getAccountCursor(context, projection, accountKey);
        if (cur == null) return false;
        try {
            if (cur.moveToFirst()) return cur.getLong(0) > 0;
        } finally {
            cur.close();
        }
        return false;
    }

    public static boolean isMyAccount(final Context context, final String screen_name) {
        if (context == null) return false;
        final ContentResolver resolver = context.getContentResolver();
        final String where = Expression.equalsArgs(Accounts.SCREEN_NAME).getSQL();
        final String[] projection = new String[0];
        final Cursor cur = resolver.query(Accounts.CONTENT_URI, projection, where, new String[]{screen_name}, null);
        try {
            return cur != null && cur.getCount() > 0;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static boolean isMyRetweet(final ParcelableStatus status) {
        return status != null && isMyRetweet(status.account_key, status.retweeted_by_user_key,
                status.my_retweet_id);
    }

    public static boolean isMyRetweet(final UserKey accountId, final UserKey retweetedById, final String myRetweetId) {
        return accountId.equals(retweetedById) || myRetweetId != null;
    }

    public static boolean isNetworkAvailable(final Context context) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (SecurityException e) {
            return true;
        }
    }

    public static int matchLinkId(@Nullable final Uri uri) {
        if (uri == null) return UriMatcher.NO_MATCH;
        return LINK_HANDLER_URI_MATCHER.match(uri);
    }


    public static int matchTabCode(@Nullable final Uri uri) {
        if (uri == null) return UriMatcher.NO_MATCH;
        return HOME_TABS_URI_MATCHER.match(uri);
    }


    @CustomTabType
    public static String matchTabType(@Nullable final Uri uri) {
        return getTabType(matchTabCode(uri));
    }

    @CustomTabType
    public static String getTabType(final int code) {
        switch (code) {
            case TAB_CODE_HOME_TIMELINE: {
                return CustomTabType.HOME_TIMELINE;
            }
            case TAB_CODE_NOTIFICATIONS_TIMELINE: {
                return CustomTabType.NOTIFICATIONS_TIMELINE;
            }
            case TAB_CODE_DIRECT_MESSAGES: {
                return CustomTabType.DIRECT_MESSAGES;
            }
        }
        return null;
    }


    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T extends Parcelable> T[] newParcelableArray(Parcelable[] array, Parcelable.Creator<T> creator) {
        if (array == null) return null;
        final T[] result = creator.newArray(array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    public static boolean setNdefPushMessageCallback(Activity activity, CreateNdefMessageCallback callback) {
        try {
            final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
            if (adapter == null) return false;
            adapter.setNdefPushMessageCallback(callback, activity);
            return true;
        } catch (SecurityException e) {
            Log.w(LOGTAG, e);
        }
        return false;
    }

    public static int getInsetsTopWithoutActionBarHeight(Context context, int top) {
        final int actionBarHeight;
        if (context instanceof AppCompatActivity) {
            actionBarHeight = getActionBarHeight(((AppCompatActivity) context).getSupportActionBar());
        } else if (context instanceof Activity) {
            actionBarHeight = getActionBarHeight(((Activity) context).getActionBar());
        } else {
            return top;
        }
        if (actionBarHeight > top) {
            return top;
        }
        return top - actionBarHeight;
    }

    public static void restartActivity(final Activity activity) {
        if (activity == null) return;
        final int enterAnim = android.R.anim.fade_in;
        final int exitAnim = android.R.anim.fade_out;
        activity.finish();
        activity.overridePendingTransition(enterAnim, exitAnim);
        activity.startActivity(activity.getIntent());
        activity.overridePendingTransition(enterAnim, exitAnim);
    }

    public static void scrollListToPosition(final AbsListView list, final int position) {
        scrollListToPosition(list, position, 0);
    }

    public static void scrollListToPosition(final AbsListView absListView, final int position, final int offset) {
        if (absListView == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (absListView instanceof ListView) {
                final ListView listView = (ListView) absListView;
                listView.setSelectionFromTop(position, offset);
            } else {
                absListView.setSelection(position);
            }
            stopListView(absListView);
        } else {
            stopListView(absListView);
            if (absListView instanceof ListView) {
                final ListView listView = (ListView) absListView;
                listView.setSelectionFromTop(position, offset);
            } else {
                absListView.setSelection(position);
            }
        }
    }

    public static void scrollListToTop(final AbsListView list) {
        if (list == null) return;
        scrollListToPosition(list, 0);
    }

    public static void addCopyLinkIntent(Context context, Intent chooserIntent, Uri uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        final Intent copyLinkIntent = new Intent(context, CopyLinkActivity.class);
        copyLinkIntent.setData(uri);
        final Intent[] alternateIntents = {copyLinkIntent};
        chooserIntent.putExtra(Intent.EXTRA_ALTERNATE_INTENTS, alternateIntents);
    }

    static boolean isMyStatus(ParcelableStatus status) {
        if (isMyRetweet(status)) return true;
        return status.account_key.maybeEquals(status.user_key);
    }

    public static boolean shouldStopAutoRefreshOnBatteryLow(final Context context) {
        final SharedPreferences mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        return mPreferences.getBoolean(KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW, true);
    }

    public static void showErrorMessage(final Context context, final CharSequence message, final boolean longMessage) {
        if (context == null) return;
        final Toast toast = Toast.makeText(context, message, longMessage ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showErrorMessage(final Context context, final CharSequence action,
                                        final CharSequence message, final boolean longMessage) {
        if (context == null) return;
        showErrorMessage(context, getErrorMessage(context, action, message), longMessage);
    }

    public static void showErrorMessage(final Context context, final CharSequence action,
                                        @Nullable final Throwable t, final boolean longMessage) {
        if (context == null) return;
        if (t instanceof MicroBlogException) {
            showTwitterErrorMessage(context, action, (MicroBlogException) t, longMessage);
            return;
        }
        showErrorMessage(context, getErrorMessage(context, action, t), longMessage);
    }

    public static void showErrorMessage(final Context context, final int actionRes, final String desc,
                                        final boolean longMessage) {
        if (context == null) return;
        showErrorMessage(context, context.getString(actionRes), desc, longMessage);
    }

    public static void showErrorMessage(final Context context, final int action,
                                        @Nullable final Throwable t,
                                        final boolean long_message) {
        if (context == null) return;
        showErrorMessage(context, context.getString(action), t, long_message);
    }

    public static void showInfoMessage(final Context context, final CharSequence message, final boolean long_message) {
        if (context == null || TextUtils.isEmpty(message)) return;
        final Toast toast = Toast.makeText(context, message, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showInfoMessage(final Context context, final int resId, final boolean long_message) {
        if (context == null) return;
        showInfoMessage(context, context.getText(resId), long_message);
    }

    public static void showMenuItemToast(final View v, final CharSequence text, final boolean isBottomBar) {
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        v.getLocationOnScreen(screenPos);
        v.getWindowVisibleDisplayFrame(displayFrame);
        final int width = v.getWidth();
        final int height = v.getHeight();
        final int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
        final Toast cheatSheet = Toast.makeText(v.getContext().getApplicationContext(), text, Toast.LENGTH_SHORT);
        if (isBottomBar) {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
        } else {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, screenWidth - screenPos[0] - width / 2, height);
        }
        cheatSheet.show();
    }

    public static void showOkMessage(final Context context, final CharSequence message, final boolean longMessage) {
        if (context == null || TextUtils.isEmpty(message)) return;
        final Toast toast = Toast.makeText(context, message, longMessage ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showOkMessage(final Context context, final int resId, final boolean long_message) {
        if (context == null) return;
        showOkMessage(context, context.getText(resId), long_message);
    }

    public static void showTwitterErrorMessage(final Context context, final CharSequence action,
                                               final MicroBlogException te, final boolean long_message) {
        if (context == null) return;
        final String message;
        if (te != null) {
            if (action != null) {
                final RateLimitStatus status = te.getRateLimitStatus();
                if (te.exceededRateLimitation() && status != null) {
                    final long secUntilReset = status.getSecondsUntilReset() * 1000;
                    final String nextResetTime = ParseUtils.parseString(DateUtils.getRelativeTimeSpanString(System
                            .currentTimeMillis() + secUntilReset));
                    message = context.getString(R.string.error_message_rate_limit_with_action, action,
                            nextResetTime.trim());
                } else if (isErrorCodeMessageSupported(te)) {
                    final String msg = StatusCodeMessageUtils
                            .getMessage(context, te.getStatusCode(), te.getErrorCode());
                    message = context.getString(R.string.error_message_with_action, action, msg != null ? msg
                            : trimLineBreak(te.getMessage()));
                } else if (!TextUtils.isEmpty(te.getErrorMessage())) {
                    message = context.getString(R.string.error_message_with_action, action,
                            trimLineBreak(te.getErrorMessage()));
                } else if (te.getCause() instanceof SSLException) {
                    final String msg = te.getCause().getMessage();
                    if (msg != null && msg.contains("!=")) {
                        message = context.getString(R.string.error_message_with_action, action,
                                context.getString(R.string.ssl_error));
                    } else {
                        message = context.getString(R.string.error_message_with_action, action,
                                context.getString(R.string.network_error));
                    }
                } else if (te.getCause() instanceof IOException) {
                    message = context.getString(R.string.error_message_with_action, action,
                            context.getString(R.string.network_error));
                } else {
                    message = context.getString(R.string.error_message_with_action, action,
                            trimLineBreak(te.getMessage()));
                }
            } else {
                message = context.getString(R.string.error_message, trimLineBreak(te.getMessage()));
            }
        } else {
            message = context.getString(R.string.error_unknown_error);
        }
        showErrorMessage(context, message, long_message);
    }

    public static void startRefreshServiceIfNeeded(@NonNull final Context context) {
        final Context appContext = context.getApplicationContext();
        if (appContext == null) return;
        final Intent refreshServiceIntent = new Intent(appContext, RefreshService.class);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable(appContext) && hasAutoRefreshAccounts(appContext)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOGTAG, "Start background refresh service");
                    }
                    appContext.startService(refreshServiceIntent);
                } else {
                    appContext.stopService(refreshServiceIntent);
                }
            }
        });
    }

    public static void startStatusShareChooser(final Context context, final ParcelableStatus status) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        final String name = status.user_name, screenName = status.user_screen_name;
        final String timeString = formatToLongTimeString(context, status.timestamp);
        final String subject = context.getString(R.string.status_share_subject_format_with_time, name, screenName, timeString);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, status.text_plain);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public static void stopListView(final AbsListView list) {
        if (list == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_CANCEL, 0, 0, 0));
        } else {
            list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, 0, 0, 0));
            list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP, 0, 0, 0));
        }
    }

    public static String trim(final String str) {
        return str != null ? str.trim() : null;
    }

    public static String trimLineBreak(final String orig) {
        if (orig == null) return null;
        return orig.replaceAll("\\n+", "\n");
    }

    public static void updateRelationship(Context context, UserKey accountKey, UserKey userKey,
                                          Relationship relationship) {
        final ContentResolver resolver = context.getContentResolver();
        final ContentValues values = ContentValuesCreator.createCachedRelationship(relationship,
                accountKey, userKey);
        resolver.insert(CachedRelationships.CONTENT_URI, values);
    }

    private static Drawable getMetadataDrawable(final PackageManager pm, final ActivityInfo info, final String key) {
        if (pm == null || info == null || info.metaData == null || key == null || !info.metaData.containsKey(key))
            return null;
        return pm.getDrawable(info.packageName, info.metaData.getInt(key), info.applicationInfo);
    }

    private static boolean isErrorCodeMessageSupported(final MicroBlogException te) {
        if (te == null) return false;
        return StatusCodeMessageUtils.containsHttpStatus(te.getStatusCode())
                || StatusCodeMessageUtils.containsTwitterError(te.getErrorCode());
    }

    private static boolean isExtensionUseJSON(final ResolveInfo info) {
        if (info == null || info.activityInfo == null) return false;
        final ActivityInfo activityInfo = info.activityInfo;
        if (activityInfo.metaData != null && activityInfo.metaData.containsKey(METADATA_KEY_EXTENSION_USE_JSON))
            return activityInfo.metaData.getBoolean(METADATA_KEY_EXTENSION_USE_JSON);
        final ApplicationInfo appInfo = activityInfo.applicationInfo;
        if (appInfo == null) return false;
        return appInfo.metaData != null && appInfo.metaData.getBoolean(METADATA_KEY_EXTENSION_USE_JSON, false);
    }

    public static int getActionBarHeight(@Nullable ActionBar actionBar) {
        if (actionBar == null) return 0;
        final Context context = actionBar.getThemedContext();
        final TypedValue tv = new TypedValue();
        final int height = actionBar.getHeight();
        if (height > 0) return height;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int getActionBarHeight(@Nullable android.support.v7.app.ActionBar actionBar) {
        if (actionBar == null) return 0;
        final Context context = actionBar.getThemedContext();
        final TypedValue tv = new TypedValue();
        final int height = actionBar.getHeight();
        if (height > 0) return height;
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }


    @Nullable
    public static ParcelableUser getUserForConversation(@NonNull final Context context,
                                                        @NonNull final UserKey accountKey,
                                                        @NonNull final String conversationId) {
        final ContentResolver cr = context.getContentResolver();
        final Expression where = Expression.and(Expression.equalsArgs(ConversationEntries.ACCOUNT_KEY),
                Expression.equalsArgs(ConversationEntries.CONVERSATION_ID));
        final String[] whereArgs = {accountKey.toString(), conversationId};
        final Cursor c = cr.query(ConversationEntries.CONTENT_URI, null, where.getSQL(), whereArgs,
                null);
        if (c == null) return null;
        try {
            if (c.moveToFirst()) return ParcelableUserUtils.fromDirectMessageConversationEntry(c);
        } finally {
            c.close();
        }
        return null;
    }


    public static void setSharedElementTransition(Context context, Window window, int transitionRes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        UtilsL.setSharedElementTransition(context, window, transitionRes);
    }

    public static <T> Object findFieldOfTypes(T obj, Class<? extends T> cls, Class<?>... checkTypes) {
        labelField:
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            final Object fieldObj;
            try {
                fieldObj = field.get(obj);
            } catch (Exception ignore) {
                continue;
            }
            if (fieldObj != null) {
                final Class<?> type = fieldObj.getClass();
                for (Class<?> checkType : checkTypes) {
                    if (!checkType.isAssignableFrom(type)) continue labelField;
                }
                return fieldObj;
            }
        }
        return null;
    }

    public static boolean isCustomConsumerKeySecret(String consumerKey, String consumerSecret) {
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) return false;
        return !TWITTER_CONSUMER_KEY.equals(consumerKey) && !TWITTER_CONSUMER_SECRET.equals(consumerKey)
                && !TWITTER_CONSUMER_KEY_LEGACY.equals(consumerKey) && !TWITTER_CONSUMER_SECRET_LEGACY.equals(consumerSecret);
    }

    public static boolean isStreamingEnabled() {
        return Boolean.parseBoolean("false");
    }

    public static int getErrorNo(Throwable t) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        return UtilsL.getErrorNo(t);
    }

    public static void logOpenNotificationFromUri(Context context, Uri uri) {
        if (!uri.getBooleanQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, false)) return;
        final String type = uri.getQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE);
        final UserKey accountKey = UserKey.valueOf(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY));
        final long itemId = NumberUtils.toLong(UriExtraUtils.getExtra(uri, "item_id"), -1);
        final long itemUserId = NumberUtils.toLong(UriExtraUtils.getExtra(uri, "item_user_id"), -1);
        final boolean itemUserFollowing = Boolean.parseBoolean(UriExtraUtils.getExtra(uri, "item_user_following"));
        final long timestamp = NumberUtils.toLong(uri.getQueryParameter(QUERY_PARAM_TIMESTAMP), -1);
        if (!NotificationEvent.isSupported(type) || accountKey == null || itemId < 0 || timestamp < 0)
            return;
        final NotificationEvent event = NotificationEvent.open(context, timestamp, type,
                accountKey.getId(), itemId, itemUserId, itemUserFollowing);
        HotMobiLogger.getInstance(context).log(accountKey, event);
    }

    public static int getNotificationId(int baseId, @Nullable UserKey accountId) {
        int result = baseId;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @SuppressLint("InlinedApi")
    public static boolean isCharging(final Context context) {
        if (context == null) return false;
        final Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return false;
        final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC
                || plugged == BatteryManager.BATTERY_PLUGGED_USB
                || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public static boolean isMediaPreviewEnabled(Context context, SharedPreferencesWrapper preferences) {
        if (!preferences.getBoolean(KEY_MEDIA_PREVIEW)) return false;
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return !ConnectivityManagerCompat.isActiveNetworkMetered(cm) || !preferences.getBoolean(KEY_BANDWIDTH_SAVING_MODE);
    }

    static class UtilsL {

        private UtilsL() {
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        static void setSharedElementTransition(Context context, Window window, int transitionRes) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            final TransitionInflater inflater = TransitionInflater.from(context);
            final Transition transition = inflater.inflateTransition(transitionRes);
            window.setSharedElementEnterTransition(transition);
            window.setSharedElementExitTransition(transition);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public static int getErrorNo(Throwable t) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
            if (t instanceof ErrnoException) {
                return ((ErrnoException) t).errno;
            }
            return 0;
        }
    }

    /**
     * Send Notifications to Pebble smart watches
     *
     * @param context Context
     * @param title   String
     * @param message String
     */
    public static void sendPebbleNotification(@NonNull final Context context, @Nullable final String title, @NonNull final String message) {
        String appName;

        if (title == null) {
            appName = context.getString(R.string.app_name);
        } else {
            appName = context.getString(R.string.app_name) + " - " + title;
        }

        if (TextUtils.isEmpty(message)) return;
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (prefs.getBoolean(KEY_PEBBLE_NOTIFICATIONS, false)) {

            final List<PebbleMessage> messages = new ArrayList<>();
            messages.add(new PebbleMessage(appName, message));

            final Intent intent = new Intent(INTENT_ACTION_PEBBLE_NOTIFICATION);
            intent.putExtra("messageType", "PEBBLE_ALERT");
            intent.putExtra("sender", appName);
            intent.putExtra("notificationData", JsonSerializer.serialize(messages, PebbleMessage.class));

            context.getApplicationContext().sendBroadcast(intent);
        }

    }

    @Nullable
    public static GeoLocation getCachedGeoLocation(Context context) {
        final Location location = getCachedLocation(context);
        if (location == null) return null;
        return new GeoLocation(location.getLatitude(), location.getLongitude());
    }

    @Nullable
    public static Location getCachedLocation(Context context) {
        if (BuildConfig.DEBUG) {
            Log.v(LOGTAG, "Fetching cached location", new Exception());
        }
        Location location = null;
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return null;
        try {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException ignore) {

        }
        if (location != null) return location;
        try {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException ignore) {

        }
        return location;
    }

    public static boolean checkDeviceCompatible() {
        try {
            Menu.class.isAssignableFrom(MenuBuilder.class);
        } catch (Error e) {
            TwidereBugReporter.logException(e);
            return false;
        }
        return true;
    }
}
