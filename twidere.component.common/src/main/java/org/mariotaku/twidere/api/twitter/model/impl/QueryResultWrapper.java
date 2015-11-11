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

import java.util.ArrayList;

import org.mariotaku.twidere.api.twitter.model.QueryResult;
import org.mariotaku.twidere.api.twitter.model.Status;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class QueryResultWrapper extends TwitterResponseImpl implements TwitterModelWrapper<QueryResult> {

    @JsonField(name = "previous_cursor")
    long previousCursor;
    @JsonField(name = "next_cursor")
    long nextCursor;

    @JsonField(name = "search_metadata")
    SearchMetadata metadata;

    @JsonField(name = "statuses")
    ArrayList<Status> statuses;

    @Override
    public QueryResult getWrapped(Object extra) {
        return new QueryResultImpl(statuses, metadata);
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
