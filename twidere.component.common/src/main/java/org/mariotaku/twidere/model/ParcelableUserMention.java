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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.util.UserKeyConverter;

@JsonObject
@ParcelablePlease(allFields = false)
public class ParcelableUserMention implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "id", typeConverter = UserKeyConverter.class)
    public UserKey key;
    @ParcelableThisPlease
    @JsonField(name = "name")
    public String name;
    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    public String screen_name;

    public ParcelableUserMention() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableUserMention that = (ParcelableUserMention) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(screen_name != null ? !screen_name.equals(that.screen_name) : that.screen_name != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (screen_name != null ? screen_name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableUserMention{id=" + key + ", name=" + name + ", screen_name=" + screen_name + "}";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableUserMentionParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableUserMention> CREATOR = new Creator<ParcelableUserMention>() {
        @Override
        public ParcelableUserMention createFromParcel(Parcel source) {
            ParcelableUserMention target = new ParcelableUserMention();
            ParcelableUserMentionParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableUserMention[] newArray(int size) {
            return new ParcelableUserMention[size];
        }
    };
}
