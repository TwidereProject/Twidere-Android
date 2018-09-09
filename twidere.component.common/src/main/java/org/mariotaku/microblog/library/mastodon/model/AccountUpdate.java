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

import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.restfu.http.mime.Body;

/**
 * Created by mariotaku on 2017/4/17.
 */
public class AccountUpdate extends SimpleValueMap {

    public AccountUpdate displayName(String displayName) {
        if (displayName != null) {
            put("display_name", displayName);
        } else {
            put("display_name", null);
        }
        return this;
    }

    public AccountUpdate note(String note) {
        if (note != null) {
            put("note", note);
        } else {
            put("note", null);
        }
        return this;
    }

    public AccountUpdate avatar(Body avatar) {
        if (avatar != null) {
            put("avatar", avatar);
        } else {
            put("avatar", null);
        }
        return this;
    }

    public AccountUpdate header(Body header) {
        if (header != null) {
            put("header", header);
        } else {
            put("header", null);
        }
        return this;
    }

    public AccountUpdate locked(boolean locked) {
        put("locked", locked);
        return this;
    }
}
