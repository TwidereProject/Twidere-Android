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

import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 15/2/6.
 */
public class Paging extends SimpleValueMap {

    public void setSinceId(String sinceId) {
        put("since_id", sinceId);
    }

    public void setMaxId(String maxId) {
        put("max_id", maxId);
    }

    public void setMinPosition(long minPosition) {
        put("min_position", minPosition);
    }

    public void setMaxPosition(long maxPosition) {
        put("max_position", maxPosition);
    }

    public void setCount(int count) {
        put("count", count);
    }

    public void setPage(int page) {
        put("page", page);
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

    public void setRpp(int rpp) {
        put("rpp", rpp);
    }

    public Paging sinceId(String sinceId) {
        setSinceId(sinceId);
        return this;
    }

    public Paging latestResults(boolean latestResults) {
        setLatestResults(latestResults);
        return this;
    }

    public Paging maxId(String maxId) {
        setMaxId(maxId);
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
        setPage(page);
        return this;
    }

    public Paging cursor(long cursor) {
        setCursor(cursor);
        return this;
    }

    public Paging rpp(int rpp) {
        setRpp(rpp);
        return this;
    }
}
