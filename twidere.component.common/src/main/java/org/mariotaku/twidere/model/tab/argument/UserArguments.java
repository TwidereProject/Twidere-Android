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

package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/6.
 */
@ParcelablePlease
@JsonObject
public class UserArguments extends TabArguments implements Parcelable {
    @JsonField(name = "user_id")
    String userId;
    @JsonField(name = "user_key")
    UserKey userKey;

    public void setUserKey(UserKey userKey) {
        this.userKey = userKey;
    }

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        if (userKey == null) {
            bundle.putParcelable(EXTRA_USER_KEY, UserKey.valueOf(userId));
        } else {
            bundle.putParcelable(EXTRA_USER_KEY, userKey);
        }
    }

    @Override
    public String toString() {
        return "UserArguments{" +
                "userId='" + userId + '\'' +
                ", userKey=" + userKey +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserArgumentsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserArguments> CREATOR = new Creator<UserArguments>() {
        public UserArguments createFromParcel(Parcel source) {
            UserArguments target = new UserArguments();
            UserArgumentsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserArguments[] newArray(int size) {
            return new UserArguments[size];
        }
    };
}
