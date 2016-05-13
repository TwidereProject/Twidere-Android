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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.microblog.library.twitter.util.InternalParseUtil;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class QueryResult extends AbstractList<Status> implements TwitterResponse, CursorSupport {

    @JsonField(name = "previous_cursor")
    long previousCursor;
    @JsonField(name = "next_cursor")
    long nextCursor;

    @JsonField(name = "search_metadata")
    SearchMetadata metadata;

    @JsonField(name = "statuses")
    ArrayList<Status> statuses;

    @AccessLevel
    private int accessLevel;
    private RateLimitStatus rateLimitStatus;

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

    @Override
    public Status get(int index) {
        return statuses.get(index);
    }

    @Override
    public int size() {
        return statuses.size();
    }

    public double getCompletedIn() {
        return metadata.completedIn;
    }

    public long getMaxId() {
        return metadata.maxId;
    }

    public String getQuery() {
        return metadata.query;
    }

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

    public long getSinceId() {
        return metadata.sinceId;
    }

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
