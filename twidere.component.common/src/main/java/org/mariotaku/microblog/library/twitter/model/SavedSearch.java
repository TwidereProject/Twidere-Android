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

import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class SavedSearch extends TwitterResponseObject implements Comparable<SavedSearch>, TwitterResponse {

    public long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public String getQuery() {
        return query;
    }

    @JsonField(name = "id")
    long id;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "position")
    int position;

    @JsonField(name = "query")
    String query;

    @Override
    public int compareTo(@NonNull SavedSearch another) {
        final long diff = id - another.id;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

}
