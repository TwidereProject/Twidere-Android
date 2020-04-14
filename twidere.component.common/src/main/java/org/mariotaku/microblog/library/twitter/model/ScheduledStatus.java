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

import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Created by mariotaku on 15/7/6.
 */
@JsonObject
public class ScheduledStatus {

    @JsonField(name = "updated_at", typeConverter = TwitterDateConverter.class)
    Date updatedAt;
    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;
    @JsonField(name = "execute_at", typeConverter = TwitterDateConverter.class)
    Date executeAt;
    @JsonField(name = "text")
    String text;
    @JsonField(name = "media_ids")
    long[] mediaIds;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "possiblySensitive")
    boolean possiblySensitive;
    @JsonField(name = "user_id")
    long userId;
    @JsonField(name = "state")
    @State
    String state;

    public long getUserId() {
        return userId;
    }

    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    public long getId() {
        return id;
    }

    public long[] getMediaIds() {
        return mediaIds;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getExecuteAt() {
        return executeAt;
    }

    public String getText() {
        return text;
    }

    public
    @State
    String getState() {
        return state;
    }

    @StringDef({State.SCHEDULED, State.FAILED, State.CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        String SCHEDULED = "scheduled", FAILED = "failed", CANCELED = "canceled";

    }
}
