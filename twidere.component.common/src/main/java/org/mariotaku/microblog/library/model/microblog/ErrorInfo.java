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

package org.mariotaku.microblog.library.model.microblog;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class ErrorInfo {

    public static final int PAGE_NOT_FOUND = 34;
    public static final int RATE_LIMIT_EXCEEDED = 88;
    public static final int NO_DIRECT_MESSAGE_PERMISSION = 93;
    public static final int STATUS_NOT_FOUND = 144;
    public static final int NOT_AUTHORIZED = 179;
    public static final int STATUS_IS_DUPLICATE = 187;
    public static final int SEARCH_ALREADY_SAVED = 172;
    public static final int RESOURCE_NOT_ALLOWED = 220;

    @JsonField(name = "code")
    int code;
    /**
     * Field for https://dev.twitter.com/rest/reference/get/media/upload-status
     */
    @JsonField(name = "name")
    String name;
    @JsonField(name = "message")
    String message;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getRequest() {
        return null;
    }
}
