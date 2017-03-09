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

package org.mariotaku.twidere.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2016/12/2.
 */

@ParcelablePlease
@JsonObject
public class EmptyCredentials extends Credentials implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EmptyCredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "EmptyCredentials{} " + super.toString();
    }

    public static final Creator<EmptyCredentials> CREATOR = new Creator<EmptyCredentials>() {
        public EmptyCredentials createFromParcel(Parcel source) {
            EmptyCredentials target = new EmptyCredentials();
            EmptyCredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public EmptyCredentials[] newArray(int size) {
            return new EmptyCredentials[size];
        }
    };
}
