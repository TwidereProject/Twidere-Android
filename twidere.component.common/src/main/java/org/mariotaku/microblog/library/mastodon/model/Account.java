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

package org.mariotaku.microblog.library.mastodon.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Date;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#account}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Account {
    /**
     * The ID of the account
     */
    @JsonField(name = "id")
    String id;
    /**
     * The username of the account
     */
    @JsonField(name = "username")
    String username;
    /**
     * Equals {@code username} for local users, includes {@code @domain} for remote ones
     */
    @JsonField(name = "acct")
    String acct;
    /**
     * The account's display name
     */
    @JsonField(name = "display_name")
    String displayName;
    /**
     * Boolean for when the account cannot be followed without waiting for approval first
     */
    @JsonField(name = "locked")
    boolean locked;
    /**
     * The time the account was created
     */
    @JsonField(name = "created_at")
    Date createdAt;
    /**
     * The number of followers for the account
     */
    @JsonField(name = "followers_count")
    long followersCount;
    /**
     * The number of accounts the given account is following
     */
    @JsonField(name = "following_count")
    long followingCount;
    /**
     * The number of statuses the account has made
     */
    @JsonField(name = "statuses_count")
    long statusesCount;
    /**
     * Biography of user
     */
    @JsonField(name = "note")
    String note;
    /**
     * URL of the user's profile page (can be remote)
     */
    @JsonField(name = "url")
    String url;
    /**
     * URL to the avatar image
     */
    @JsonField(name = "avatar")
    String avatar;
    /**
     * URL to the avatar static image (gif)
     */
    @JsonField(name = "avatar_static")
    String avatarStatic;
    /**
     * URL to the header image
     */
    @JsonField(name = "header")
    String header;
    /**
     * URL to the header static image (gif)
     */
    @JsonField(name = "header_static")
    String headerStatic;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAcct() {
        return acct;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isLocked() {
        return locked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public long getStatusesCount() {
        return statusesCount;
    }

    public String getNote() {
        return note;
    }

    public String getUrl() {
        return url;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarStatic() {
        return avatarStatic;
    }

    public String getHeader() {
        return header;
    }

    public String getHeaderStatic() {
        return headerStatic;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", acct='" + acct + '\'' +
                ", displayName='" + displayName + '\'' +
                ", locked=" + locked +
                ", createdAt=" + createdAt +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                ", statusesCount=" + statusesCount +
                ", note='" + note + '\'' +
                ", url='" + url + '\'' +
                ", avatar='" + avatar + '\'' +
                ", avatarStatic='" + avatarStatic + '\'' +
                ", header='" + header + '\'' +
                ", headerStatic='" + headerStatic + '\'' +
                '}';
    }
}
