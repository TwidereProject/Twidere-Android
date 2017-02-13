/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.twidere.model.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableUser;

/**
 * Created by mariotaku on 2017/2/12.
 */

@ParcelablePlease
@JsonObject
public class ConversationInfoUpdatedExtras extends MessageExtras implements Parcelable {
    @JsonField(name = "user")
    ParcelableUser user;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "avatar")
    String avatar;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    public void setUser(final ParcelableUser user) {
        this.user = user;
    }

    public ParcelableUser getUser() {
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ConversationInfoUpdatedExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ConversationInfoUpdatedExtras> CREATOR = new Creator<ConversationInfoUpdatedExtras>() {
        public ConversationInfoUpdatedExtras createFromParcel(Parcel source) {
            ConversationInfoUpdatedExtras target = new ConversationInfoUpdatedExtras();
            ConversationInfoUpdatedExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ConversationInfoUpdatedExtras[] newArray(int size) {
            return new ConversationInfoUpdatedExtras[size];
        }
    };
}
