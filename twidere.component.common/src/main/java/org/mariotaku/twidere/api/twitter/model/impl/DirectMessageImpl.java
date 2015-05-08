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

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.Date;

import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.HashtagEntity;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class DirectMessageImpl extends TwitterResponseImpl implements DirectMessage {

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "sender")
    UserImpl sender;

    @JsonField(name = "recipient")
    UserImpl recipient;

    @JsonField(name = "entities")
    EntitiesImpl entities;

    @JsonField(name = "text")
    String text;

    @JsonField(name = "id")
    long id;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        if (entities == null) return null;
        return entities.getHashtags();
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        if (entities == null) return null;
        return entities.getMedia();
    }

    @Override
    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrls();
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        if (entities == null) return null;
        return entities.getUserMentions();
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public UserImpl getSender() {
        return sender;
    }

    @Override
    public long getSenderId() {
        return sender.id;
    }

    @Override
    public String getSenderScreenName() {
        return sender.screenName;
    }

    @Override
    public UserImpl getRecipient() {
        return recipient;
    }

    @Override
    public long getRecipientId() {
        return recipient.id;
    }

    @Override
    public String getRecipientScreenName() {
        return recipient.screenName;
    }
}
