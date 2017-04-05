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

package org.mariotaku.microblog.library.fanfou.model;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.commons.logansquare.JsonStringConverter;
import org.mariotaku.microblog.library.fanfou.model.util.StreamDateConverter;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 2017/3/11.
 */
@JsonObject
public class FanfouStreamObject {
    @JsonField(name = "event")
    String event;
    @JsonField(name = "created_at", typeConverter = StreamDateConverter.class)
    Date createdAt;

    @JsonField(name = "source")
    User source;
    @JsonField(name = "target")
    User target;
    @JsonField(name = "object", typeConverter = JsonStringConverter.class)
    String rawObject;

    @NonNull
    public String getEvent() {
        if (event == null) return "";
        return event;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getSource() {
        return source;
    }

    public User getTarget() {
        return target;
    }

    public <T> T getObject(Class<T> cls) throws IOException {
        if (rawObject == null) return null;
        return JsonSerializer.parse(rawObject, cls);
    }

}
