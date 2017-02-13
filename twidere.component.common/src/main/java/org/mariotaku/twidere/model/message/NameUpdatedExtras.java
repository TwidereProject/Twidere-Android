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
public class NameUpdatedExtras extends MessageExtras implements Parcelable {
    @JsonField(name = "user")
    ParcelableUser user;
    @JsonField(name = "name")
    String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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
        NameUpdatedExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<NameUpdatedExtras> CREATOR = new Creator<NameUpdatedExtras>() {
        public NameUpdatedExtras createFromParcel(Parcel source) {
            NameUpdatedExtras target = new NameUpdatedExtras();
            NameUpdatedExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public NameUpdatedExtras[] newArray(int size) {
            return new NameUpdatedExtras[size];
        }
    };
}
