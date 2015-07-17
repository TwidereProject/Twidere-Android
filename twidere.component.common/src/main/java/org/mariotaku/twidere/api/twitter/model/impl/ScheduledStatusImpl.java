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

import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/7/6.
 */
@JsonObject
public class ScheduledStatusImpl implements ScheduledStatus {

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

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long[] getMediaIds() {
        return mediaIds;
    }

    @Override
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public Date getExecuteAt() {
        return executeAt;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public State getState() {
        return state;
    }
}
