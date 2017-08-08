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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/7/5.
 */
@ParcelablePlease
@JsonObject
public class UserInbox extends TwitterResponseObject implements Parcelable {

    @JsonField(name = "user_inbox")
    DMResponse userInbox;

    public DMResponse getUserInbox() {
        return userInbox;
    }

    @Override
    public String toString() {
        return "UserInbox{" +
                "userInbox=" + userInbox +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserInboxParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserInbox> CREATOR = new Creator<UserInbox>() {
        public UserInbox createFromParcel(Parcel source) {
            UserInbox target = new UserInbox();
            UserInboxParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserInbox[] newArray(int size) {
            return new UserInbox[size];
        }
    };
}
