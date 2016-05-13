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

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/7.
 */
@ParcelablePlease
@JsonObject
public class DirectMessage extends TwitterResponseObject implements TwitterResponse, EntitySupport,
        Parcelable {

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "sender")
    User sender;

    @JsonField(name = "recipient")
    User recipient;

    @JsonField(name = "entities")
    Entities entities;

    @JsonField(name = "text")
    String text;

    @JsonField(name = "id")
    String id;

    public String getId() {
        return id;
    }

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getSender() {
        return sender;
    }

    public String getSenderId() {
        return sender.id;
    }

    public String getSenderScreenName() {
        return sender.screenName;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getRecipientId() {
        return recipient.id;
    }

    public String getRecipientScreenName() {
        return recipient.screenName;
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id == null || recipient == null || sender == null)
            throw new IOException("Malformed DirectMessage object");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DirectMessageParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<DirectMessage> CREATOR = new Creator<DirectMessage>() {
        @Override
        public DirectMessage createFromParcel(Parcel source) {
            DirectMessage target = new DirectMessage();
            DirectMessageParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public DirectMessage[] newArray(int size) {
            return new DirectMessage[size];
        }
    };
}
