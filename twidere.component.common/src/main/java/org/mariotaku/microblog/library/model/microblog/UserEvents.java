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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
@JsonObject
public class UserEvents extends MicroBlogResponseObject implements Parcelable {

    @JsonField(name = "user_events")
    DMResponse userEvents;

    public DMResponse getUserEvents() {
        return userEvents;
    }

    @Override
    public String toString() {
        return "UserEvents{" +
                "userEvents=" + userEvents +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserEventsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserEvents> CREATOR = new Creator<UserEvents>() {
        public UserEvents createFromParcel(Parcel source) {
            UserEvents target = new UserEvents();
            UserEventsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserEvents[] newArray(int size) {
            return new UserEvents[size];
        }
    };
}
