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

package org.mariotaku.microblog.library.model;

import org.mariotaku.restfu.http.SimpleValueMap;

public class Paging extends SimpleValueMap {

    public void setMinPosition(long minPosition) {
        put("min_position", minPosition);
    }

    public void setMaxPosition(long maxPosition) {
        put("max_position", maxPosition);
    }

    public void setCount(int count) {
        put("count", count);
    }

    public void setCursor(long cursor) {
        put("cursor", cursor);
    }

    public void setCursor(String cursor) {
        put("cursor", cursor);
    }

    public void setLatestResults(boolean latestResults) {
        put("latest_results", latestResults);
    }

    public Paging sinceId(String sinceId) {
        put("since_id", sinceId);
        return this;
    }

    public Paging latestResults(boolean latestResults) {
        setLatestResults(latestResults);
        return this;
    }

    public Paging maxId(String maxId) {
        put("max_id", maxId);
        return this;
    }

    public Paging maxPosition(long maxPosition) {
        setMaxPosition(maxPosition);
        return this;
    }

    public Paging minPosition(long minPosition) {
        setMinPosition(minPosition);
        return this;
    }

    public Paging count(int count) {
        setCount(count);
        return this;
    }

    public Paging page(int page) {
        put("page", page);
        return this;
    }

    public Paging cursor(long cursor) {
        setCursor(cursor);
        return this;
    }

    public Paging cursor(String cursor) {
        setCursor(cursor);
        return this;
    }

    public Paging limit(int limit) {
        put("limit", limit);
        return this;
    }

    public Paging rpp(int rpp) {
        put("rpp", rpp);
        return this;
    }
}
