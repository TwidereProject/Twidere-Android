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
