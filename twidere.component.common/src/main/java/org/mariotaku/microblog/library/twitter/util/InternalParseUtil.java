/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.microblog.library.twitter.util;

import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.microblog.library.twitter.model.TwitterResponse;

/**
 * A tiny parse utility class.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class InternalParseUtil {

    private InternalParseUtil() {
        // should never be instantiated
        throw new AssertionError("This class should never be instantiated");
    }

    @TwitterResponse.AccessLevel
    public static int toAccessLevel(final HttpResponse res) {
        if (res == null) return TwitterResponse.AccessLevel.NONE;
        final String xAccessLevel = res.getHeader("X-Access-Level");
        int accessLevel;
        if (null == xAccessLevel) {
            accessLevel = TwitterResponse.AccessLevel.NONE;
        } else {
            // https://dev.twitter.com/pages/application-permission-model-faq#how-do-we-know-what-the-access-level-of-a-user-token-is
            switch (xAccessLevel.length()) {
                // “read” (Read-only)
                case 4:
                    accessLevel = TwitterResponse.AccessLevel.READ;
                    break;
                case 10:
                    // “read-write” (Read & Write)
                    accessLevel = TwitterResponse.AccessLevel.READ_WRITE;
                    break;
                case 25:
                    // “read-write-directmessages” (Read, Write, & Direct
                    // Message)
                    accessLevel = TwitterResponse.AccessLevel.READ_WRITE_DIRECTMESSAGES;
                    break;
                case 26:
                    // “read-write-privatemessages” (Read, Write, & Direct
                    // Message)
                    accessLevel = TwitterResponse.AccessLevel.READ_WRITE_DIRECTMESSAGES;
                    break;
                default:
                    accessLevel = TwitterResponse.AccessLevel.NONE;
                    // unknown access level;
            }
        }
        return accessLevel;
    }
}
