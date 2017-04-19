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

import org.mariotaku.microblog.library.mastodon.annotation.StatusVisibility;
import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 2017/4/19.
 */

public class StatusUpdate extends SimpleValueMap {
    public StatusUpdate(String status) {
        put("status", status);
    }

    public StatusUpdate inReplyToId(String id) {
        if (id != null) {
            put("in_reply_to_id", id);
        } else {
            remove("in_rpely_to_id");
        }
        return this;
    }

    public StatusUpdate mediaIds(String[] ids) {
        if (ids != null) {
            put("media_ids[]", ids);
        } else {
            remove("media_ids[]");
        }
        return this;
    }

    public StatusUpdate sensitive(boolean sensitive) {
        put("sensitive", sensitive);
        return this;
    }


    public StatusUpdate spoilerText(String text) {
        if (text != null) {
            put("spoiler_text", text);
        } else {
            remove("spoiler_text");
        }
        return this;
    }

    public StatusUpdate visibility(@StatusVisibility String visibility) {
        if (visibility != null) {
            put("visibility", visibility);
        } else {
            remove("visibility");
        }
        return this;
    }


}
