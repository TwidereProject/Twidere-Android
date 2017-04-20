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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

/**
 * Created by mariotaku on 2017/2/14.
 */
@ParcelablePlease
@JsonObject
public class ParcelableNewMessage implements Parcelable {

    @JsonField(name = "account")
    @ParcelableThisPlease
    public AccountDetails account;
    @JsonField(name = "conversation_id")
    @ParcelableThisPlease
    public String conversation_id;
    @JsonField(name = "recipient_ids")
    @ParcelableThisPlease
    public String[] recipient_ids;
    @JsonField(name = "text")
    @ParcelableThisPlease
    public String text;
    @JsonField(name = "media")
    @ParcelableThisPlease
    public ParcelableMediaUpdate[] media;
    @JsonField(name = "draft_unique_id")
    @ParcelableThisPlease
    public String draft_unique_id;
    @JsonField(name = "draft_action")
    @ParcelableThisPlease
    @Draft.Action
    public String draft_action;
    @JsonField(name = "is_temp_conversation")
    @ParcelableThisPlease
    public boolean is_temp_conversation;

    @Override
    public String toString() {
        return "ParcelableNewMessage{" +
                "account=" + account +
                ", conversation_id='" + conversation_id + '\'' +
                ", recipient_ids=" + Arrays.toString(recipient_ids) +
                ", text='" + text + '\'' +
                ", media=" + Arrays.toString(media) +
                ", draft_unique_id='" + draft_unique_id + '\'' +
                ", draft_action='" + draft_action + '\'' +
                ", is_temp_conversation=" + is_temp_conversation +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableNewMessageParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableNewMessage> CREATOR = new Creator<ParcelableNewMessage>() {
        public ParcelableNewMessage createFromParcel(Parcel source) {
            ParcelableNewMessage target = new ParcelableNewMessage();
            ParcelableNewMessageParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableNewMessage[] newArray(int size) {
            return new ParcelableNewMessage[size];
        }
    };
}
