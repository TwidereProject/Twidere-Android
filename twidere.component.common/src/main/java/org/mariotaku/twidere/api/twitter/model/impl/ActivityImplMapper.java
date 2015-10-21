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

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ActivityImplMapper extends JsonMapper<ActivityImpl> {
    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    public ActivityImpl parse(JsonParser jsonParser) throws IOException {
        ActivityImpl instance = new ActivityImpl();
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.skipChildren();
            return null;
        }
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            jsonParser.nextToken();
            parseField(instance, fieldName, jsonParser);
            jsonParser.skipChildren();
        }
        return instance;
    }

    @Override
    public void serialize(ActivityImpl activity, JsonGenerator jsonGenerator, boolean writeStartAndEnd) {
        throw new UnsupportedOperationException();
    }

    public void parseField(ActivityImpl instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("action".equals(fieldName)) {
            instance.action = Activity.Action.parse(jsonParser.getValueAsString());
        } else if ("created_at".equals(fieldName)) {
            try {
                instance.createdAt = ActivityImpl.DATE_FORMAT.parse(jsonParser.getValueAsString());
            } catch (ParseException e) {
                throw new IOException(e);
            }
        } else if ("min_position".equals(fieldName)) {
            instance.minPosition = jsonParser.getValueAsLong();
        } else if ("max_position".equals(fieldName)) {
            instance.maxPosition = jsonParser.getValueAsLong();
        } else if ("sources_size".equals(fieldName)) {
            instance.sourcesSize = jsonParser.getValueAsInt();
        } else if ("targets_size".equals(fieldName)) {
            instance.targetsSize = jsonParser.getValueAsInt();
        } else if ("target_objects_size".equals(fieldName)) {
            instance.targetObjectsSize = jsonParser.getValueAsInt();
        } else if ("sources".equals(fieldName)) {
            instance.sources = LoganSquare.mapperFor(User.class).parseList(jsonParser).toArray(new User[instance.sourcesSize]);
        } else if ("targets".equals(fieldName)) {
            if (instance.action == null) throw new IOException();
            switch (instance.action) {
                case FAVORITE:
                case REPLY:
                case RETWEET:
                case QUOTE:
                case FAVORITED_RETWEET:
                case RETWEETED_RETWEET:
                case RETWEETED_MENTION:
                case FAVORITED_MENTION:
                case MEDIA_TAGGED:
                case FAVORITED_MEDIA_TAGGED:
                case RETWEETED_MEDIA_TAGGED: {
                    instance.targetStatuses = LoganSquare.mapperFor(Status.class).parseList(jsonParser).toArray(new Status[instance.targetsSize]);
                    break;
                }
                case FOLLOW:
                case MENTION:
                case LIST_MEMBER_ADDED: {
                    instance.targetUsers = LoganSquare.mapperFor(User.class).parseList(jsonParser).toArray(new User[instance.targetsSize]);
                    break;
                }
                case LIST_CREATED: {
                    instance.targetUserLists = LoganSquare.mapperFor(UserList.class).parseList(jsonParser).toArray(new UserList[instance.targetsSize]);
                    break;
                }
            }
        } else if ("target_objects".equals(fieldName)) {
            if (instance.action == null) throw new IOException();
            switch (instance.action) {
                case FAVORITE:
                case FOLLOW:
                case MENTION:
                case REPLY:
                case RETWEET:
                case LIST_CREATED:
                case QUOTE: {
                    instance.targetObjectStatuses = LoganSquare.mapperFor(Status.class).parseList(jsonParser).toArray(new Status[instance.targetObjectsSize]);
                    break;
                }
                case LIST_MEMBER_ADDED: {
                    instance.targetObjectUserLists = LoganSquare.mapperFor(UserList.class).parseList(jsonParser).toArray(new UserList[instance.targetObjectsSize]);
                    break;
                }
                case FAVORITED_RETWEET:
                case RETWEETED_RETWEET:
                case RETWEETED_MENTION:
                case FAVORITED_MENTION:
                case MEDIA_TAGGED:
                case FAVORITED_MEDIA_TAGGED:
                case RETWEETED_MEDIA_TAGGED: {
                    instance.targetObjectUsers = LoganSquare.mapperFor(User.class).parseList(jsonParser).toArray(new User[instance.targetObjectsSize]);
                    break;
                }
            }
        }
    }
}
