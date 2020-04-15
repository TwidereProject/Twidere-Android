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

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#notification}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Notification {
    /**
     * The notification ID
     */
    @JsonField(name = "id")
    String id;
    /**
     * One of: {@link Type#MENTION}, {@link Type#REBLOG}, {@link Type#FAVOURITE},
     * {@link Type#FOLLOW}
     */
    @JsonField(name = "type")
    String type;
    /**
     * The time the notification was created
     */
    @JsonField(name = "created_at")
    Date createdAt;
    /**
     * The {@link Account} sending the notification to the user
     */
    @JsonField(name = "account")
    Account account;
    /**
     * The {@link Status} associated with the notification, if applicable
     */
    @JsonField(name = "status")
    @Nullable
    Status status;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Account getAccount() {
        return account;
    }

    @Nullable
    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", account=" + account +
                ", status=" + status +
                '}';
    }

    @StringDef({Type.MENTION, Type.REBLOG, Type.FAVOURITE, Type.FOLLOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        String MENTION = "mention", REBLOG = "reblog", FAVOURITE = "favourite", FOLLOW = "follow";
    }
}
