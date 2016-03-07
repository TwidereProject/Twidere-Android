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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/3/14.
 */
public class LinkCreator implements Constants {

    private static final String AUTHORITY_TWITTER = "twitter.com";

    public static Uri getTwitterStatusLink(String screenName, long statusId) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTPS);
        builder.authority(AUTHORITY_TWITTER);
        builder.appendPath(screenName);
        builder.appendPath("status");
        builder.appendPath(String.valueOf(statusId));
        return builder.build();
    }

    public static Uri getTwidereStatusLink(UserKey accountKey, long statusId) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_TWIDERE);
        builder.authority(AUTHORITY_STATUS);
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString());
        }
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(statusId));
        return builder.build();
    }

    public static Uri getTwidereUserLink(@Nullable UserKey accountKey, long userId, String screenName) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_TWIDERE);
        builder.authority(AUTHORITY_USER);
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString());
        }
        if (userId > 0) {
            builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(userId));
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

    public static Uri getTwitterUserLink(String screenName) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME_HTTPS);
        builder.authority(AUTHORITY_TWITTER);
        builder.appendPath(screenName);
        return builder.build();
    }

    public static Uri getTwitterStatusLink(ParcelableStatus status) {
        if (status.extras != null && !TextUtils.isEmpty(status.extras.external_url)) {
            return Uri.parse(status.extras.external_url);
        }
        return getTwitterStatusLink(status.user_screen_name, status.id);
    }
}
