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

package org.mariotaku.microblog.library.twitter.model;

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
    public static final int NOT_AUTHORIZED = 179;
    public static final int STATUS_IS_DUPLICATE = 187;
    public static final int STATUS_NOT_FOUND = 144;

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
