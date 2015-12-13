/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *   
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/4/7.
 */
@JsonObject
public class UserList extends TwitterResponseObject implements Comparable<UserList>, TwitterResponse {
    @JsonField(name = "id")
    long id;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "uri")
    String uri;

    @JsonField(name = "subscriber_count")
    long subscriberCount;

    @JsonField(name = "member_count")
    long memberCount;

    @JsonField(name = "mode")
    Mode mode;

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

    public long getId() {
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

    public Mode getMode() {
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
        return (int) (id - another.id);
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

    public enum Mode {
        PUBLIC("public"), PRIVATE("private");

        private final String mode;

        Mode(String mode) {
            this.mode = mode;
        }

        public static Mode parse(String str) {
            switch (str) {
                case "public":
                    return PUBLIC;
                case "private":
                    return PRIVATE;
            }
            throw new UnsupportedOperationException();
        }

        public String getMode() {
            return mode;
        }
    }
}
