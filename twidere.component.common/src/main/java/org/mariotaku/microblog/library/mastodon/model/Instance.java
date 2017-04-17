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

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#instance}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Instance {
    /**
     * URI of the current instance
     */
    @JsonField(name = "uri")
    String uri;
    /**
     * The instance's title
     */
    @JsonField(name = "title")
    String title;
    /**
     * A description for the instance
     */
    @JsonField(name = "description")
    String description;
    /**
     * An email address which can be used to contact the instance administrator
     */
    @JsonField(name = "email")
    String email;

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "uri='" + uri + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
