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

package org.mariotaku.microblog.library.mastodon.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

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
     * One of: {@code mention}, {@code reblog}, {@code favourite}, {@code follow}
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
}
