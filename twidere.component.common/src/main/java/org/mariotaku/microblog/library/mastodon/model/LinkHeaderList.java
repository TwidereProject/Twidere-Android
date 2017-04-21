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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.restfu.http.HttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 2017/4/21.
 */
public class LinkHeaderList<E> extends ArrayList<E> {

    @NonNull
    private Map<String, String> linkParts = new HashMap<>();

    public LinkHeaderList(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkHeaderList() {
    }

    public LinkHeaderList(@NonNull Collection<? extends E> c) {
        super(c);
    }

    public final void processResponseHeader(HttpResponse resp) {
        linkParts.clear();
        String linkHeader = resp.getHeader("Link");
        if (linkHeader == null) return;
        for (String link : TextUtils.split(linkHeader, ",")) {
            String[] segments = TextUtils.split(link, ";");
            if (segments.length < 2) continue;
            String linkPart = segments[0].trim();
            if (!linkPart.startsWith("<") || !linkPart.endsWith(">"))
                continue;
            linkPart = linkPart.substring(1, linkPart.length() - 1);
            for (int i = 1; i < segments.length; i++) {
                String[] rel = TextUtils.split(segments[i].trim(), "=");
                if (rel.length < 2 || !"rel".equals(rel[0]))
                    continue;

                String relValue = rel[1];
                if (relValue.startsWith("\"") && relValue.endsWith("\""))
                    relValue = relValue.substring(1, relValue.length() - 1);

                linkParts.put(relValue, linkPart);
            }
        }
    }

    @Nullable
    public String getLinkPart(String key) {
        return linkParts.get(key);
    }
}
