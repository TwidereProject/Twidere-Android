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

package org.mariotaku.microblog.library.model.mastodon;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#report}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Report {
    /**
     * The ID of the report
     */
    @JsonField(name = "id")
    String id;
    /**
     * The action taken in response to the report
     */
    @JsonField(name = "action_taken")
    boolean actionTaken;

    public String getId() {
        return id;
    }

    public boolean isActionTaken() {
        return actionTaken;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", actionTaken=" + actionTaken +
                '}';
    }
}
