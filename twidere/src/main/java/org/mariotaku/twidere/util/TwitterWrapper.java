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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TwitterWrapper implements Constants {

    public static SingleResponse<Boolean> deleteProfileBannerImage(final Context context,
            final UserKey accountKey) {
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, accountKey);
        if (twitter == null) return SingleResponse.Companion.getInstance(false);
        try {
            twitter.removeProfileBannerImage();
            return SingleResponse.Companion.getInstance(true);
        } catch (final MicroBlogException e) {
            return SingleResponse.Companion.getInstance(false, e);
        }
    }

    @NonNull
    public static User showUser(@NonNull final MicroBlog twitter, final String id, final String screenName,
            final String accountType) throws MicroBlogException {
        if (id != null) {
            if (AccountType.FANFOU.equals(accountType)) {
                return twitter.showFanfouUser(id);
            }
            return twitter.showUser(id);
        } else if (screenName != null) {
            if (AccountType.FANFOU.equals(accountType)) {
                return twitter.showFanfouUser(screenName);
            }
            return twitter.showUserByScreenName(screenName);
        }
        throw new MicroBlogException("Invalid user id or screen name");
    }

    @NonNull
    public static User showUserAlternative(@NonNull final MicroBlog twitter, final String id,
            final String screenName)
            throws MicroBlogException {
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
        throw new MicroBlogException("can't find user");
    }

    @NonNull
    public static User tryShowUser(@NonNull final MicroBlog twitter, final String id, final String screenName,
            String accountType)
            throws MicroBlogException {
        try {
            return showUser(twitter, id, screenName, accountType);
        } catch (final MicroBlogException e) {
            // Twitter specific error for private API calling through proxy
            if (e.getStatusCode() == 200) {
                return showUserAlternative(twitter, id, screenName);
            }
            throw e;
        }
    }

    public static void updateProfileBannerImage(@NonNull final Context context,
            @NonNull final MicroBlog twitter,
            @NonNull final Uri imageUri,
            final boolean deleteImage)
            throws IOException, MicroBlogException {
        FileBody fileBody = null;
        try {
            fileBody = getFileBody(context, imageUri);
            twitter.updateProfileBannerImage(fileBody);
        } finally {
            Utils.closeSilently(fileBody);
            if (deleteImage) {
                Utils.deleteMedia(context, imageUri);
            }
        }
    }

    public static void updateProfileBackgroundImage(@NonNull final Context context,
            @NonNull final MicroBlog twitter,
            @NonNull final Uri imageUri,
            final boolean tile,
            final boolean deleteImage)
            throws IOException, MicroBlogException {
        FileBody fileBody = null;
        try {
            fileBody = getFileBody(context, imageUri);
            twitter.updateProfileBackgroundImage(fileBody, tile);
        } finally {
            Utils.closeSilently(fileBody);
            if (deleteImage) {
                Utils.deleteMedia(context, imageUri);
            }
        }
    }

    public static User updateProfileImage(@NonNull final Context context, @NonNull final MicroBlog twitter,
            @NonNull final Uri imageUri, final boolean deleteImage)
            throws IOException, MicroBlogException {
        FileBody fileBody = null;
        try {
            fileBody = getFileBody(context, imageUri);
            return twitter.updateProfileImage(fileBody);
        } finally {
            Utils.closeSilently(fileBody);
            if (deleteImage) {
                Utils.deleteMedia(context, imageUri);
            }
        }
    }

    private static FileBody getFileBody(Context context, Uri imageUri) throws IOException {
        final ContentResolver cr = context.getContentResolver();
        String type = cr.getType(imageUri);
        if (type == null) {
            type = BitmapUtils.getImageMimeType(cr, imageUri);
        }
        final ContentType contentType;
        final String extension;
        if (type != null) {
            contentType = ContentType.parse(type);
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        } else {
            contentType = null;
            extension = null;
        }
        final InputStream is = cr.openInputStream(imageUri);
        if (is == null) throw new FileNotFoundException(imageUri.toString());

        final String fileName;
        if (extension != null) {
            fileName = "image." + extension;
        } else {
            fileName = "image";
        }
        return new FileBody(is, fileName, is.available(), contentType);
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
