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
public class UserEntities implements Parcelable {

    @JsonField(name = "url")
    Entities url;

    @JsonField(name = "description")
    Entities description;

    public UrlEntity[] getDescriptionEntities() {
        if (description == null) return null;
        return description.getUrls();
    }

    public UrlEntity[] getUrlEntities() {
        if (url == null) return null;
        return url.getUrls();
    }

    @Override
    public String toString() {
        return "UserEntities{" +
                "url=" + url +
                ", description=" + description +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserEntitiesParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserEntities> CREATOR = new Creator<UserEntities>() {
        @Override
        public UserEntities createFromParcel(Parcel source) {
            UserEntities target = new UserEntities();
            UserEntitiesParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UserEntities[] newArray(int size) {
            return new UserEntities[size];
        }
    };
}
