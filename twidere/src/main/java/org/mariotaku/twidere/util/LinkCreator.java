/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 15/3/14.
 */
public class LinkCreator implements Constants {

    private static final String AUTHORITY_TWITTER = "twitter.com";
    private static final String AUTHORITY_FANFOU = "fanfou.com";

    private LinkCreator() {
    }

    public static Uri getTwidereStatusLink(UserKey accountKey, @NonNull String statusId) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_TWIDERE);
        builder.authority(AUTHORITY_STATUS);
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString());
        }
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, statusId);
        return builder.build();
    }

    public static Uri getTwidereUserLink(@Nullable UserKey accountKey, @Nullable UserKey userKey, String screenName) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_TWIDERE);
        builder.authority(AUTHORITY_USER);
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString());
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString());
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
        }
        return builder.build();
    }

    public static Uri getTwitterUserListLink(String userScreenName, String listName) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTPS);
        builder.authority(AUTHORITY_TWITTER);
        builder.appendPath(userScreenName);
        builder.appendPath(listName);
        return builder.build();
    }

    @NonNull
    public static Uri getStatusWebLink(ParcelableStatus status) {
        if (status.extras != null && !TextUtils.isEmpty(status.extras.external_url)) {
            return Uri.parse(status.extras.external_url);
        }
        if (USER_TYPE_FANFOU_COM.equals(status.account_key.getHost())) {
            return getFanfouStatusLink(status.id);
        }
        return getTwitterStatusLink(status.user_screen_name, status.id);
    }

    public static Uri getQuotedStatusWebLink(ParcelableStatus status) {
        if (status.extras != null) {
            if (!TextUtils.isEmpty(status.extras.quoted_external_url)) {
                return Uri.parse(status.extras.quoted_external_url);
            } else if (!TextUtils.isEmpty(status.extras.external_url)) {
                return Uri.parse(status.extras.external_url);
            }
        }
        if (USER_TYPE_FANFOU_COM.equals(status.account_key.getHost())) {
            return getFanfouStatusLink(status.quoted_id);
        }
        return getTwitterStatusLink(status.quoted_user_screen_name, status.quoted_id);
    }

    @NonNull
    public static Uri getUserWebLink(@NonNull ParcelableUser user) {
        if (user.extras != null && user.extras.statusnet_profile_url != null) {
            return Uri.parse(user.extras.statusnet_profile_url);
        }
        if (USER_TYPE_FANFOU_COM.equals(user.key.getHost())) {
            return getFanfouUserLink(user.key.getId());
        }
        return getTwitterUserLink(user.screen_name);
    }

    @NonNull
    static Uri getTwitterStatusLink(@NonNull String screenName, @NonNull String statusId) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTPS);
        builder.authority(AUTHORITY_TWITTER);
        builder.appendPath(screenName);
        builder.appendPath("status");
        builder.appendPath(statusId);
        return builder.build();
    }

    static Uri getTwitterUserLink(String screenName) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTPS);
        builder.authority(AUTHORITY_TWITTER);
        builder.appendPath(screenName);
        return builder.build();
    }

    static Uri getFanfouStatusLink(String id) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTP);
        builder.authority(AUTHORITY_FANFOU);
        builder.appendPath("statuses");
        builder.appendPath(id);
        return builder.build();
    }

    static Uri getFanfouUserLink(String id) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTP);
        builder.authority(AUTHORITY_FANFOU);
        builder.appendPath(id);
        return builder.build();
    }
}
