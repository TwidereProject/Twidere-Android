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

package org.mariotaku.microblog.library.model.twitter;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.model.microblog.User;
import org.mariotaku.microblog.library.util.twitter.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class StreamEvent {
    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;
    @JsonField(name = "source")
    User source;
    @JsonField(name = "target")
    User target;

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getSource() {
        return source;
    }

    public User getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "StreamEvent{" +
                "createdAt=" + createdAt +
                ", source=" + source +
                ", target=" + target +
                '}';
    }
}
