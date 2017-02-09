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

package org.mariotaku.twidere.model.draft;

import android.os.Parcel;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/2/21.
 */
@ParcelablePlease
@JsonObject
public class SendDirectMessageActionExtras implements ActionExtras {
    @ParcelableThisPlease
    @JsonField(name = "recipient_id")
    String recipientId;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SendDirectMessageActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendDirectMessageActionExtras that = (SendDirectMessageActionExtras) o;

        return recipientId != null ? recipientId.equals(that.recipientId) : that.recipientId == null;

    }

    @Override
    public int hashCode() {
        return recipientId != null ? recipientId.hashCode() : 0;
    }

    public static final Creator<SendDirectMessageActionExtras> CREATOR = new Creator<SendDirectMessageActionExtras>() {
        @Override
        public SendDirectMessageActionExtras createFromParcel(Parcel source) {
            SendDirectMessageActionExtras target = new SendDirectMessageActionExtras();
            SendDirectMessageActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public SendDirectMessageActionExtras[] newArray(int size) {
            return new SendDirectMessageActionExtras[size];
        }
    };
}
