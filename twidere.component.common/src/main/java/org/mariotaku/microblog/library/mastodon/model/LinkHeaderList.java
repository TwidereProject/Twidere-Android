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

import org.mariotaku.restfu.http.HttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by mariotaku on 2017/4/21.
 */
public class LinkHeaderList<E> extends ArrayList<E> implements LinkHeaderResponse {

    @Nullable
    private Map<String, String> linkParts;

    public LinkHeaderList(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkHeaderList() {
    }

    public LinkHeaderList(@NonNull Collection<? extends E> c) {
        super(c);
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
}
