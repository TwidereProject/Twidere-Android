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
