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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.model.QueryResult;
import org.mariotaku.twidere.api.twitter.model.RateLimitStatus;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.util.InternalParseUtil;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class QueryResultImpl extends AbstractList<Status> implements QueryResult {

    @JsonField(name = "previous_cursor")
    long previousCursor;
    @JsonField(name = "next_cursor")
    long nextCursor;

    @JsonField(name = "search_metadata")
    SearchMetadata metadata;

    @JsonField(name = "statuses")
    ArrayList<Status> statuses;

    private int accessLevel;
    private RateLimitStatus rateLimitStatus;

    @Override
    public final void processResponseHeader(RestHttpResponse resp) {
        rateLimitStatus = RateLimitStatusJSONImpl.createFromResponseHeader(resp);
        accessLevel = InternalParseUtil.toAccessLevel(resp);
    }

    @Override
    public final int getAccessLevel() {
        return accessLevel;
    }

    @Override
    public final RateLimitStatus getRateLimitStatus() {
        return rateLimitStatus;
    }

    @Override
    public Status get(int index) {
        return statuses.get(index);
    }

    @Override
    public int size() {
        return statuses.size();
    }

    @Override
    public double getCompletedIn() {
        return metadata.completedIn;
    }

    @Override
    public long getMaxId() {
        return metadata.maxId;
    }

    @Override
    public String getQuery() {
        return metadata.query;
    }

    @Override
    public int getResultsPerPage() {
        return metadata.count;
    }

    @Override
    public long getNextCursor() {
        return nextCursor;
    }

    @Override
    public boolean hasNext() {
        return nextCursor != 0;
    }

    @Override
    public boolean hasPrevious() {
        return previousCursor != 0;
    }

    @Override
    public long getPreviousCursor() {
        return previousCursor;
    }

    @Override
    public long getSinceId() {
        return metadata.sinceId;
    }

    @Override
    public String getWarning() {
        return metadata.warning;
    }

    @JsonObject
    public static class SearchMetadata {
        @JsonField(name = "max_id")
        long maxId;
        @JsonField(name = "since_id")
        long sinceId;
        @JsonField(name = "count")
        int count;
        @JsonField(name = "completed_in")
        double completedIn;
        @JsonField(name = "query")
        String query;
        @JsonField(name = "warning")
        String warning;
    }

}
