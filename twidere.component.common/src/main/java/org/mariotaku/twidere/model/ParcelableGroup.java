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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/3/9.
 */
@ParcelablePlease
@JsonObject
public class ParcelableGroup implements Parcelable, Comparable<ParcelableGroup> {

    @ParcelableThisPlease
    @JsonField(name = "account_key")
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "id")
    public String id;

    @ParcelableThisPlease
    @JsonField(name = "nickname")
    public String nickname;
    @ParcelableThisPlease
    @JsonField(name = "homepage")
    public String homepage;
    @ParcelableThisPlease
    @JsonField(name = "fullname")
    public String fullname;
    @ParcelableThisPlease
    @JsonField(name = "url")
    public String url;
    @ParcelableThisPlease
    @JsonField(name = "description")
    public String description;
    @ParcelableThisPlease
    @JsonField(name = "location")
    public String location;

    @ParcelableThisPlease
    @JsonField(name = "position")
    public long position;

    @ParcelableThisPlease
    @JsonField(name = "created")
    public long created;
    @ParcelableThisPlease
    @JsonField(name = "modified")
    public long modified;
    @ParcelableThisPlease
    @JsonField(name = "admin_count")
    public long admin_count;
    @ParcelableThisPlease
    @JsonField(name = "member_count")
    public long member_count;

    @ParcelableThisPlease
    @JsonField(name = "original_logo")
    public String original_logo;
    @ParcelableThisPlease
    @JsonField(name = "homepage_logo")
    public String homepage_logo;
    @ParcelableThisPlease
    @JsonField(name = "stream_logo")
    public String stream_logo;
    @ParcelableThisPlease
    @JsonField(name = "mini_logo")
    public String mini_logo;

    @ParcelableThisPlease
    @JsonField(name = "blocked")
    public boolean blocked;
    @ParcelableThisPlease
    @JsonField(name = "member")
    public boolean member;

    @Override
    public String toString() {
        return "ParcelableGroup{" +
                "account_key=" + account_key +
                ", id=" + id +
                ", nickname='" + nickname + '\'' +
                ", homepage='" + homepage + '\'' +
                ", fullname='" + fullname + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", admin_count=" + admin_count +
                ", member_count=" + member_count +
                ", original_logo='" + original_logo + '\'' +
                ", homepage_logo='" + homepage_logo + '\'' +
                ", stream_logo='" + stream_logo + '\'' +
                ", mini_logo='" + mini_logo + '\'' +
                ", blocked=" + blocked +
                ", member=" + member +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableGroupParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableGroup that = (ParcelableGroup) o;

        if (!account_key.equals(that.account_key)) return false;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        int result = account_key.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    public static final Creator<ParcelableGroup> CREATOR = new Creator<ParcelableGroup>() {
        @Override
        public ParcelableGroup createFromParcel(Parcel source) {
            ParcelableGroup target = new ParcelableGroup();
            ParcelableGroupParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableGroup[] newArray(int size) {
            return new ParcelableGroup[size];
        }
    };

    @Override
    public int compareTo(@NonNull ParcelableGroup another) {
        return (int) (this.position - another.position);
    }
}
