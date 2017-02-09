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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

@ParcelablePlease
@JsonObject
public class ParcelableStatusUpdate implements Parcelable {

    @SuppressWarnings("NullableProblems")
    @JsonField(name = "accounts")
    @NonNull
    @ParcelableThisPlease
    public AccountDetails[] accounts;
    @JsonField(name = "media")
    @ParcelableThisPlease
    public ParcelableMediaUpdate[] media;
    @JsonField(name = "text")
    @ParcelableThisPlease
    public String text;
    @JsonField(name = "location")
    @ParcelableThisPlease
    public ParcelableLocation location;
    @JsonField(name = "display_coordinates")
    @ParcelableThisPlease
    public boolean display_coordinates = true;
    @JsonField(name = "in_reply_to_status")
    @ParcelableThisPlease
    public ParcelableStatus in_reply_to_status;
    @JsonField(name = "is_possibly_sensitive")
    @ParcelableThisPlease
    public boolean is_possibly_sensitive;
    @JsonField(name = "repost_status_id")
    @ParcelableThisPlease
    public String repost_status_id;
    @JsonField(name = "attachment_url")
    @ParcelableThisPlease
    public String attachment_url;
    @JsonField(name = "draft_unique_id")
    @ParcelableThisPlease
    public String draft_unique_id;

    public ParcelableStatusUpdate() {
    }

    @Override
    public String toString() {
        return "ParcelableStatusUpdate{" +
                "accounts=" + Arrays.toString(accounts) +
                ", media=" + Arrays.toString(media) +
                ", text='" + text + '\'' +
                ", location=" + location +
                ", display_coordinates=" + display_coordinates +
                ", in_reply_to_status=" + in_reply_to_status +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                ", repost_status_id='" + repost_status_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusUpdateParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableStatusUpdate> CREATOR = new Creator<ParcelableStatusUpdate>() {
        @Override
        public ParcelableStatusUpdate createFromParcel(Parcel source) {
            ParcelableStatusUpdate target = new ParcelableStatusUpdate();
            ParcelableStatusUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableStatusUpdate[] newArray(int size) {
            return new ParcelableStatusUpdate[size];
        }
    };
}
