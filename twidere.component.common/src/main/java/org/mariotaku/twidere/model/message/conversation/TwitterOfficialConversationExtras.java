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

package org.mariotaku.twidere.model.message.conversation;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.model.microblog.DMResponse;

/**
 * Created by mariotaku on 2017/2/13.
 */

@ParcelablePlease
@JsonObject
public class TwitterOfficialConversationExtras extends ConversationExtras implements Parcelable {

    @JsonField(name = "max_entry_id")
    public String maxEntryId;
    @JsonField(name = "min_entry_id")
    public String minEntryId;
    @JsonField(name = "status")
    @DMResponse.Status
    public String status;
    @JsonField(name = "max_entry_timestamp")
    public long maxEntryTimestamp;
    @JsonField(name = "read_only")
    public boolean readOnly;
    @JsonField(name = "notifications_disabled")
    public boolean notificationsDisabled;

    @Override
    public String toString() {
        return "TwitterOfficialConversationExtras{" +
                "maxEntryId='" + maxEntryId + '\'' +
                ", minEntryId='" + minEntryId + '\'' +
                ", status='" + status + '\'' +
                ", maxEntryTimestamp=" + maxEntryTimestamp +
                ", readOnly=" + readOnly +
                ", notificationsDisabled=" + notificationsDisabled +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TwitterOfficialConversationExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TwitterOfficialConversationExtras> CREATOR = new Creator<TwitterOfficialConversationExtras>() {
        public TwitterOfficialConversationExtras createFromParcel(Parcel source) {
            TwitterOfficialConversationExtras target = new TwitterOfficialConversationExtras();
            TwitterOfficialConversationExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TwitterOfficialConversationExtras[] newArray(int size) {
            return new TwitterOfficialConversationExtras[size];
        }
    };
}
