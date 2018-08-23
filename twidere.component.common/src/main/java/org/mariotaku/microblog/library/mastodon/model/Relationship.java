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

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#relationship}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Relationship {
    /**
     * Target account id
     */
    @JsonField(name = "id")
    String id;
    /**
     * Whether the user is currently following the account
     */
    @JsonField(name = "following")
    boolean following;
    /**
     * Whether the user is currently being followed by the account
     */
    @JsonField(name = "followed_by")
    boolean followedBy;
    /**
     * Whether the user is currently blocking the account
     */
    @JsonField(name = "blocking")
    boolean blocking;
    /**
     * Whether the user is currently muting the account
     */
    @JsonField(name = "muting")
    boolean muting;
    /**
     * Whether the user is also muting notifications
     */
    @JsonField(name = "muting_notifications")
    boolean muting_notifications;
    /**
     * Whether the user has requested to follow the account
     */
    @JsonField(name = "requested")
    boolean requested;
    /**
     * Whether the user is currently blocking the accounts's domain
     */
    @JsonField(name = "domain_blocking")
    boolean domain_blocking;
    /**
     * Whether the user's reblogs will show up in the home timeline
     */
    @JsonField(name = "showing_reblogs")
    boolean showing_reblogs;

    public String getId() {
        return id;
    }

    public boolean isFollowing() {
        return following;
    }

    public boolean isFollowedBy() {
        return followedBy;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public boolean isMuting() {
        return muting;
    }

    public boolean isMutingNotifications() {
        return muting_notifications;
    }

    public boolean isRequested() {
        return requested;
    }

    public boolean isDomainBlocking() {
        return domain_blocking;
    }

    public boolean isShowingReblogs() {
        return showing_reblogs;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "id='" + id + '\'' +
                ", following=" + following +
                ", followedBy=" + followedBy +
                ", blocking=" + blocking +
                ", muting=" + muting +
                ", muting_notifications=" + muting_notifications +
                ", requested=" + requested +
                ", domain_blocking=" + domain_blocking +
                ", showing_reblogs=" + showing_reblogs +
                '}';
    }
}
