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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.HttpResponse;

import java.util.List;
import java.util.Map;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#results}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Results implements LinkHeaderResponse {
    /**
     * A list of matched {@link Account}
     */
    @JsonField(name = "accounts")
    List<Account> accounts;
    /**
     * A list of matched {@link Status}
     */
    @JsonField(name = "statuses")
    List<Status> statuses;
    /**
     * A list of matched hashtags, as strings
     */
    @JsonField(name = "hashtags")
    List<String> hashtags;

    @Nullable
    private Map<String, String> linkParts;

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    @Override
    public final void processResponseHeader(@NonNull HttpResponse resp) {
        linkParts = Parser.parse(resp);
    }

    @Nullable
    @Override
    public String getLinkPart(String key) {
        if (linkParts == null) return null;
        return linkParts.get(key);
    }

    @Override
    public String toString() {
        return "Results{" +
                "accounts=" + accounts +
                ", statuses=" + statuses +
                ", hashtags=" + hashtags +
                '}';
    }
}
