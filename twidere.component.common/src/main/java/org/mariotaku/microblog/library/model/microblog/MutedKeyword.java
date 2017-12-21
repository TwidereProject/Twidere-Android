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

import org.mariotaku.twidere.model.util.UnixEpochMillisDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 2017/3/26.
 */
@JsonObject
public class MutedKeyword {
    @JsonField(name = "id")
    String id;
    @JsonField(name = "keyword")
    String keyword;
    @JsonField(name = "created_at", typeConverter = UnixEpochMillisDateConverter.class)
    Date createdAt;

    public String getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "MutedKeyword{" +
                "id='" + id + '\'' +
                ", keyword='" + keyword + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
