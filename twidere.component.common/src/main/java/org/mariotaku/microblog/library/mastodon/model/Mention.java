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
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#mention}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Mention {
    /**
     * URL of user's profile (can be remote)
     */
    @JsonField(name = "url")
    String url;
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
     * Account ID
     */
    @JsonField(name = "id")
    String id;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getAcct() {
        return acct;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Mention{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", acct='" + acct + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
