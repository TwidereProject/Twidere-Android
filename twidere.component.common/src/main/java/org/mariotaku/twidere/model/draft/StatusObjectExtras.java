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
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 2017/2/7.
 */

@ParcelablePlease
@JsonObject
public class StatusObjectExtras implements ActionExtras, Parcelable {
    @JsonField(name = "status")
    public ParcelableStatus status;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusObjectExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusObjectExtras> CREATOR = new Creator<StatusObjectExtras>() {
        public StatusObjectExtras createFromParcel(Parcel source) {
            StatusObjectExtras target = new StatusObjectExtras();
            StatusObjectExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StatusObjectExtras[] newArray(int size) {
            return new StatusObjectExtras[size];
        }
    };
}
