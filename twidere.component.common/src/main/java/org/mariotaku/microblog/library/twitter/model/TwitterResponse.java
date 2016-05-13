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

package org.mariotaku.microblog.library.twitter.model;

import android.support.annotation.IntDef;

import org.mariotaku.restfu.http.HttpResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface TwitterResponse {

    void processResponseHeader(HttpResponse resp);

    @AccessLevel
    int getAccessLevel();

    RateLimitStatus getRateLimitStatus();

    @IntDef({AccessLevel.NONE, AccessLevel.READ, AccessLevel.READ_WRITE, AccessLevel.READ_WRITE_DIRECTMESSAGES})
    @Retention(RetentionPolicy.SOURCE)
    @interface AccessLevel {

        int NONE = 0;
        int READ = 1;
        int READ_WRITE = 2;
        int READ_WRITE_DIRECTMESSAGES = 3;
    }
}
