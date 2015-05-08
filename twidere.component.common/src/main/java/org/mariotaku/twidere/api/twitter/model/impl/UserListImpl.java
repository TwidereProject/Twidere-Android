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

package org.mariotaku.twidere.api.twitter.model.impl;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.Date;

import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;

/**
 * Created by mariotaku on 15/4/7.
 */
@JsonObject
public class UserListImpl extends TwitterResponseImpl implements UserList {
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

    @Override
    public long getId() {
        return id;
    }


    @Override
    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public long getSubscriberCount() {
        return subscriberCount;
    }

    @Override
    public long getMemberCount() {
        return memberCount;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean isFollowing() {
        return following;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public int compareTo(@NonNull UserList another) {
        return (int) (id - another.getId());
    }

    @Override
    public String toString() {
        return "UserListImpl{" +
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

}
