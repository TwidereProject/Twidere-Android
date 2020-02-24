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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by mariotaku on 15/3/23.
 */
public class UriUtils {

    private UriUtils() {
    }

    public static Uri appendQueryParameters(final Uri uri, final String key, String value) {
        final Uri.Builder builder = uri.buildUpon();
        builder.appendQueryParameter(key, value);
        return builder.build();
    }

    public static Uri appendQueryParameters(Uri uri, String key, boolean value) {
        return appendQueryParameters(uri, key, String.valueOf(value));
    }

    @Nullable
    public static String getAuthority(@NonNull String link) {
        int start = link.indexOf("://");
        if (start < 0) return null;
        int end = link.indexOf('/', start + 3);
        if (end < 0) {
            end = link.length();
        }
        return link.substring(start + 3, end);
    }


    @Nullable
    public static int[] getAuthorityRange(@NonNull String link) {
        int start = link.indexOf("://");
        if (start < 0) return null;
        int end = link.indexOf('/', start + 3);
        if (end < 0) {
            end = link.length();
        }
        return new int[]{start + 3, end};
    }

    @Nullable
    public static String getPath(@NonNull String link) {
        int start = link.indexOf("://");
        if (start < 0) return null;
        start = link.indexOf('/', start + 3);
        if (start < 0) {
            return "";
        }
        int end = link.indexOf('?', start);
        if (end < 0) {
            end = link.indexOf('#', start);
            if (end < 0) {
                end = link.length();
            }
        }
        return link.substring(start, end);
    }
}
