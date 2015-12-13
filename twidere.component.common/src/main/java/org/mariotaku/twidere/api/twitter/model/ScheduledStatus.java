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

package org.mariotaku.twidere.api.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

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
    State state;

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

    public State getState() {
        return state;
    }

    public enum State {
        SCHEDULED("scheduled"), FAILED("failed"), CANCELED("canceled");

        private final String value;

        State(String value) {
            this.value = value;
        }

        public static State parse(String value) {
            if (SCHEDULED.value.equalsIgnoreCase(value)) {
                return SCHEDULED;
            } else if (FAILED.value.equalsIgnoreCase(value)) {
                return FAILED;
            } else if (CANCELED.value.equalsIgnoreCase(value)) {
                return CANCELED;
            }
            return null;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
