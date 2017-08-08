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

import java.util.List;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#context}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Context {
    /**
     * The ancestors of the status in the conversation, as a list of {@link Status}
     */
    @JsonField(name = "ancestors")
    List<Status> ancestors;
    /**
     * The descendants of the status in the conversation, as a list of {@link Status}
     */
    @JsonField(name = "descendants")
    List<Status> descendants;

    public List<Status> getAncestors() {
        return ancestors;
    }

    public List<Status> getDescendants() {
        return descendants;
    }

    @Override
    public String toString() {
        return "Context{" +
                "ancestors=" + ancestors +
                ", descendants=" + descendants +
                '}';
    }
}
