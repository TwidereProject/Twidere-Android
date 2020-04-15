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

import androidx.annotation.Keep;

import org.mariotaku.microblog.library.twitter.util.InternalParseUtil;
import org.mariotaku.restfu.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Response list
 * Created by mariotaku on 15/5/7.
 */
@Keep
public class ResponseList<T> extends ArrayList<T> implements TwitterResponse {

    private int accessLevel;
    private RateLimitStatus rateLimitStatus;

    public ResponseList(List<T> list) {
        super(list);
    }

    public ResponseList() {
    }

    @Override
    public final void processResponseHeader(HttpResponse resp) {
        rateLimitStatus = RateLimitStatus.createFromResponseHeader(resp);
        accessLevel = InternalParseUtil.toAccessLevel(resp);
    }

    @AccessLevel
    @Override
    public final int getAccessLevel() {
        return accessLevel;
    }

    @Override
    public final RateLimitStatus getRateLimitStatus() {
        return rateLimitStatus;
    }
}
