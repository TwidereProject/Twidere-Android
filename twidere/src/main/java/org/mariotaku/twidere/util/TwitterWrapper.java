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

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.Log;

import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Notifications;
import org.mariotaku.twidere.provider.TwidereDataStore.UnreadCounts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class TwitterWrapper implements Constants {

    public static int clearNotification(final Context context, final int notificationType, final UserKey accountId) {
        final Uri.Builder builder = Notifications.CONTENT_URI.buildUpon();
        builder.appendPath(String.valueOf(notificationType));
        if (accountId != null) {
            builder.appendPath(String.valueOf(accountId));
        }
        return context.getContentResolver().delete(builder.build(), null, null);
    }

    public static int clearUnreadCount(final Context context, final int position) {
        if (context == null || position < 0) return 0;
        final Uri uri = UnreadCounts.CONTENT_URI.buildUpon().appendPath(String.valueOf(position)).build();
        return context.getContentResolver().delete(uri, null, null);
    }

    public static SingleResponse<Boolean> deleteProfileBannerImage(final Context context,
                                                                   final UserKey accountKey) {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, false);
        if (twitter == null) return new SingleResponse<>(false, null);
        try {
            twitter.removeProfileBannerImage();
            return new SingleResponse<>(true, null);
        } catch (final TwitterException e) {
            return new SingleResponse<>(false, e);
        }
    }

    public static int removeUnreadCounts(final Context context, final int position, final long account_id,
                                         final long... status_ids) {
        if (context == null || position < 0 || status_ids == null || status_ids.length == 0)
            return 0;
        int result = 0;
        final Uri.Builder builder = UnreadCounts.CONTENT_URI.buildUpon();
        builder.appendPath(String.valueOf(position));
        builder.appendPath(String.valueOf(account_id));
        builder.appendPath(TwidereArrayUtils.toString(status_ids, ',', false));
        result += context.getContentResolver().delete(builder.build(), null, null);
        return result;
    }

    public static int removeUnreadCounts(final Context context, final int position,
                                         final SimpleArrayMap<UserKey, Set<String>> counts) {
        if (context == null || position < 0 || counts == null) return 0;
        int result = 0;
        for (int i = 0, j = counts.size(); i < j; i++) {
            final UserKey key = counts.keyAt(i);
            final Set<String> value = counts.valueAt(i);
            final Uri.Builder builder = UnreadCounts.CONTENT_URI.buildUpon();
            builder.appendPath(String.valueOf(position));
            builder.appendPath(String.valueOf(key));
            builder.appendPath(CollectionUtils.toString(value, ',', false));
            result += context.getContentResolver().delete(builder.build(), null, null);
        }
        return result;
    }

    @NonNull
    public static User showUser(final Twitter twitter, final String id, final String screenName) throws TwitterException {
        if (id != null) {
            return twitter.showUser(id);
        } else if (screenName != null) {
            return twitter.showUserByScreenName(screenName);
        }
        throw new TwitterException("Invalid user id or screen name");
    }

    @NonNull
    public static User showUserAlternative(final Twitter twitter, final String id, final String screenName)
            throws TwitterException {
        final String searchScreenName;
        if (screenName != null) {
            searchScreenName = screenName;
        } else if (id != null) {
            searchScreenName = twitter.showFriendship(id).getTargetUserScreenName();
        } else
            throw new IllegalArgumentException();
        final Paging paging = new Paging();
        paging.count(1);
        for (final User user : twitter.searchUsers(searchScreenName, paging)) {
            if (TextUtils.equals(user.getId(), id) || searchScreenName.equalsIgnoreCase(user.getScreenName()))
                return user;
        }
        if (id != null) {
            final ResponseList<Status> timeline = twitter.getUserTimeline(id, paging);
            for (final Status status : timeline) {
                final User user = status.getUser();
                if (TextUtils.equals(user.getId(), id)) return user;
            }
        } else {
            final ResponseList<Status> timeline = twitter.getUserTimelineByScreenName(screenName, paging);
            for (final Status status : timeline) {
                final User user = status.getUser();
                if (searchScreenName.equalsIgnoreCase(user.getScreenName()))
                    return user;
            }
        }
        throw new TwitterException("can't find user");
    }

    @NonNull
    public static User tryShowUser(final Twitter twitter, final String id, final String screenName)
            throws TwitterException {
        try {
            return showUser(twitter, id, screenName);
        } catch (final TwitterException e) {
            // Twitter specific error for private API calling through proxy
            if (e.getStatusCode() == 200) {
                return showUserAlternative(twitter, id, screenName);
            }
            throw e;
        }
    }

    public static void updateProfileBannerImage(final Context context, final Twitter twitter,
                                                final Uri imageUri, final boolean deleteImage)
            throws FileNotFoundException, TwitterException {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            twitter.updateProfileBannerImage(new FileBody(is, "image", -1, null));
        } finally {
            Utils.closeSilently(is);
            if (deleteImage && "file".equals(imageUri.getScheme())) {
                final File file = new File(imageUri.getPath());
                if (!file.delete()) {
                    Log.w(LOGTAG, String.format("Unable to delete %s", file));
                }
            }
        }
    }

    public static User updateProfileImage(final Context context, final Twitter twitter,
                                          final Uri imageUri, final boolean deleteImage)
            throws FileNotFoundException, TwitterException {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            return twitter.updateProfileImage(new FileBody(is, "image", -1, null));
        } finally {
            Utils.closeSilently(is);
            if (deleteImage && "file".equals(imageUri.getScheme())) {
                final File file = new File(imageUri.getPath());
                if (!file.delete()) {
                    Log.w(LOGTAG, String.format("Unable to delete %s", file));
                }
            }
        }
    }

    public static final class MessageListResponse extends TwitterListResponse<DirectMessage> {

        public MessageListResponse(final UserKey accountKey, final Exception exception) {
            this(accountKey, null, null, null, exception);
        }

        public MessageListResponse(final UserKey accountKey, final List<DirectMessage> list) {
            this(accountKey, null, null, list, null);
        }

        public MessageListResponse(final UserKey accountKey, final String maxId, final String sinceId,
                                   final List<DirectMessage> list) {
            this(accountKey, maxId, sinceId, list, null);
        }

        MessageListResponse(final UserKey accountKey, final String maxId, final String sinceId,
                            final List<DirectMessage> list, final Exception exception) {
            super(accountKey, maxId, sinceId, list, exception);
        }

    }

    public static final class StatusListResponse extends TwitterListResponse<Status> {

        public final boolean truncated;

        public StatusListResponse(final UserKey accountKey, final Exception exception) {
            this(accountKey, null, null, null, false, exception);
        }

        public StatusListResponse(final UserKey accountKey, final List<Status> list) {
            this(accountKey, null, null, list, false, null);
        }

        public StatusListResponse(final UserKey accountKey, final String maxId, final String sinceId,
                                  final List<Status> list, final boolean truncated) {
            this(accountKey, maxId, sinceId, list, truncated, null);
        }

        StatusListResponse(final UserKey accountKey, final String maxId, final String sinceId, final List<Status> list,
                           final boolean truncated, final Exception exception) {
            super(accountKey, maxId, sinceId, list, exception);
            this.truncated = truncated;
        }

    }

    public static class TwitterListResponse<Data> extends ListResponse<Data> {

        public final UserKey accountKey;
        public final String maxId;
        public final String sinceId;

        public TwitterListResponse(final UserKey accountKey,
                                   final Exception exception) {
            this(accountKey, null, null, null, exception);
        }

        public TwitterListResponse(final UserKey accountKey, final String maxId,
                                   final String sinceId, final List<Data> list) {
            this(accountKey, maxId, sinceId, list, null);
        }

        TwitterListResponse(final UserKey accountKey, final String maxId, final String sinceId,
                            final List<Data> list, final Exception exception) {
            super(list, exception);
            this.accountKey = accountKey;
            this.maxId = maxId;
            this.sinceId = sinceId;
        }

    }
}
