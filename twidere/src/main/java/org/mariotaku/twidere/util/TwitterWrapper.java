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
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TwidereDataStore.Notifications;
import org.mariotaku.twidere.provider.TwidereDataStore.UnreadCounts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

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

    public static int removeUnreadCounts(final Context context, final int position, final LongSparseArray<Set<Long>> counts) {
        if (context == null || position < 0 || counts == null) return 0;
        int result = 0;
        for (int i = 0, j = counts.size(); i < j; i++) {
            final long key = counts.keyAt(i);
            final Set<Long> value = counts.valueAt(i);
            final Uri.Builder builder = UnreadCounts.CONTENT_URI.buildUpon();
            builder.appendPath(String.valueOf(position));
            builder.appendPath(String.valueOf(key));
            builder.appendPath(CollectionUtils.toString(value, ',', false));
            result += context.getContentResolver().delete(builder.build(), null, null);
        }
        return result;
    }

    @NonNull
    public static User showUser(final Twitter twitter, final long id, final String screenName) throws TwitterException {
        if (id != -1)
            return twitter.showUser(id);
        else if (screenName != null) return twitter.showUser(screenName);
        throw new IllegalArgumentException();
    }

    @NonNull
    public static User showUserAlternative(final Twitter twitter, final long id, final String screenName)
            throws TwitterException {
        final String searchScreenName;
        if (screenName != null) {
            searchScreenName = screenName;
        } else if (id != -1) {
            searchScreenName = twitter.showFriendship(twitter.getId(), id).getTargetUserScreenName();
        } else
            throw new IllegalArgumentException();
        final Paging paging = new Paging();
        paging.count(1);
        if (id != -1) {
            final ResponseList<Status> timeline = twitter.getUserTimeline(id, paging);
            for (final Status status : timeline) {
                final User user = status.getUser();
                if (user.getId() == id) return user;
            }
        } else {
            final ResponseList<Status> timeline = twitter.getUserTimeline(screenName, paging);
            for (final Status status : timeline) {
                final User user = status.getUser();
                if (searchScreenName.equalsIgnoreCase(user.getScreenName()))
                    return user;
            }
        }
        for (final User user : twitter.searchUsers(searchScreenName, 1)) {
            if (user.getId() == id || searchScreenName.equalsIgnoreCase(user.getScreenName())) return user;
        }
        throw new TwitterException("can't find user");
    }

    @NonNull
    public static User tryShowUser(final Twitter twitter, final long id, final String screenName)
            throws TwitterException {
        try {
            return showUser(twitter, id, screenName);
        } catch (final TwitterException e) {
            if (e.getCause() instanceof IOException)
                throw e;
        }
        return showUserAlternative(twitter, id, screenName);
    }

    public static SingleResponse<ParcelableUser> updateProfile(final Context context, final long account_id,
                                                               final String name, final String url, final String location, final String description) {
        final Twitter twitter = getTwitterInstance(context, account_id, false);
        if (twitter != null) {
            try {
                final User user = twitter.updateProfile(name, url, location, description);
                return new SingleResponse<>(new ParcelableUser(user, account_id), null);
            } catch (final TwitterException e) {
                return new SingleResponse<>(null, e);
            }
        }
        return SingleResponse.getInstance();
    }

    public static void updateProfileBannerImage(final Context context, final long accountId,
                                                final Uri imageUri, final boolean deleteImage)
            throws FileNotFoundException, TwitterException {
        final Twitter twitter = getTwitterInstance(context, accountId, false);
        updateProfileBannerImage(context, twitter, imageUri, deleteImage);
    }

    public static void updateProfileBannerImage(final Context context, final Twitter twitter,
                                                final Uri imageUri, final boolean deleteImage)
            throws FileNotFoundException, TwitterException {
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            twitter.updateProfileBannerImage(is);
        } finally {
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
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            return twitter.updateProfileImage(is);
        } finally {
            if (deleteImage && "file".equals(imageUri.getScheme())) {
                final File file = new File(imageUri.getPath());
                if (!file.delete()) {
                    Log.w(LOGTAG, String.format("Unable to delete %s", file));
                }
            }
        }
    }

    public static User updateProfileImage(final Context context, final long accountId,
                                          final Uri imageUri, final boolean deleteImage)
            throws FileNotFoundException, TwitterException {
        final Twitter twitter = getTwitterInstance(context, accountId, true);
        return updateProfileImage(context, twitter, imageUri, deleteImage);
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
