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
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Created by mariotaku on 15/4/7.
 */
@ParcelablePlease
@JsonObject
public class UserList extends TwitterResponseObject implements Comparable<UserList>, TwitterResponse,
        Parcelable {
    @JsonField(name = "id")
    String id;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "uri")
    String uri;

    @JsonField(name = "subscriber_count")
    long subscriberCount;

    @JsonField(name = "member_count")
    long memberCount;

    @Mode
    @JsonField(name = "mode")
    String mode;

    @JsonField(name = "description")
    String description;

    @JsonField(name = "slug")
    String slug;

    @JsonField(name = "full_name")
    String fullName;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "following")
    boolean following;

    @JsonField(name = "user")
    User user;

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public long getMemberCount() {
        return memberCount;
    }

    @Mode
    public String getMode() {
        return mode;
    }

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isFollowing() {
        return following;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int compareTo(@NonNull UserList another) {
        return id.compareTo(another.id);
    }

    @Override
    public String toString() {
        return "UserList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", subscriberCount=" + subscriberCount +
                ", memberCount=" + memberCount +
                ", mode=" + mode +
                ", description='" + description + '\'' +
                ", slug='" + slug + '\'' +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                ", following=" + following +
                ", user=" + user +
                "} " + super.toString();
    }

    @StringDef({Mode.PRIVATE, Mode.PUBLIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        String PUBLIC = "public";
        String PRIVATE = "private";

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserListParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserList> CREATOR = new Creator<UserList>() {
        @Override
        public UserList createFromParcel(Parcel source) {
            UserList target = new UserList();
            UserListParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UserList[] newArray(int size) {
            return new UserList[size];
        }
    };
}
