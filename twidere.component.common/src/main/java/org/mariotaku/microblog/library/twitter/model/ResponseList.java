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

import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.microblog.library.annotation.NoObfuscate;
import org.mariotaku.microblog.library.twitter.util.InternalParseUtil;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Response list
 * Created by mariotaku on 15/5/7.
 */
@NoObfuscate
public class ResponseList<T> extends AbstractList<T> implements TwitterResponse {

    private List<T> list;
    private int accessLevel;
    private RateLimitStatus rateLimitStatus;

    public ResponseList(List<T> list) {
        this.list = list;
    }

    @Override
    public void add(int location, T object) {
        list.add(location, object);
    }

    @Override
    public T set(int location, T object) {
        return list.set(location, object);
    }

    @Override
    public T get(int location) {
        return list.get(location);
    }

    @Override
    public T remove(int location) {
        return list.remove(location);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public int size() {
        return list.size();
    }

    public ResponseList() {
        this(new ArrayList<T>());
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
