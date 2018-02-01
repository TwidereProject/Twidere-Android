/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.model.twitter.dm;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.model.microblog.MicroBlogResponseObject;

import java.util.List;
import java.util.Map;

@JsonObject
public class DirectMessageEventList extends MicroBlogResponseObject {
    @JsonField(name = "apps")
    Map<String, App> apps;
    @JsonField(name = "events")
    List<DirectMessageEvent> events;

    public Map<String, App> getApps() {
        return apps;
    }

    public List<DirectMessageEvent> getEvents() {
        return events;
    }
}
