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

package org.mariotaku.twidere.api.twitter.util;

import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.model.TwitterResponse;

/**
 * A tiny parse utility class.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class InternalParseUtil {

    private InternalParseUtil() {
        // should never be instantiated
        throw new AssertionError();
    }

    public static int toAccessLevel(final RestHttpResponse res) {
        if (null == res) return -1;
        final String xAccessLevel = res.getHeader("X-Access-Level");
        int accessLevel;
        if (null == xAccessLevel) {
            accessLevel = TwitterResponse.NONE;
        } else {
            // https://dev.twitter.com/pages/application-permission-model-faq#how-do-we-know-what-the-access-level-of-a-user-token-is
            switch (xAccessLevel.length()) {
                // “read” (Read-only)
                case 4:
                    accessLevel = TwitterResponse.READ;
                    break;
                case 10:
                    // “read-write” (Read & Write)
                    accessLevel = TwitterResponse.READ_WRITE;
                    break;
                case 25:
                    // “read-write-directmessages” (Read, Write, & Direct
                    // Message)
                    accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
                    break;
                case 26:
                    // “read-write-privatemessages” (Read, Write, & Direct
                    // Message)
                    accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
                    break;
                default:
                    accessLevel = TwitterResponse.NONE;
                    // unknown access level;
            }
        }
        return accessLevel;
    }
}
