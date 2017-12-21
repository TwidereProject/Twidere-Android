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

package org.mariotaku.microblog.library.model.microblog;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.util.twitter.TwitterDateConverter;

import java.io.IOException;
import java.util.Date;

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
