/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                case 26:
                    // “read-write-privatemessages” (Read, Write, & Direct
                    // “read-write-directmessages” (Read, Write, & Direct
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
