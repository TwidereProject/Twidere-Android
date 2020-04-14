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

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.StringDef;

import org.mariotaku.microblog.library.twitter.util.InternalArrayUtil;
import org.mariotaku.restfu.http.SimpleValueMap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 15/10/21.
 */
public class UniversalSearchQuery extends SimpleValueMap {

    public UniversalSearchQuery(String query) {
        setQuery(query);
    }

    public void setCursor(String cursor) {
        put("cursor", cursor);
    }

    public void setQuery(String query) {
        put("q", query);
    }

    public void setCount(int count) {
        put("count", count);
    }

    public void setModules(String... modules) {
        put("modules", InternalArrayUtil.join(modules, ","));
    }

    public void setFilter(@Filter String filter) {
        put("filter", filter);
    }

    public void setResultType(@ResultType String resultType) {
        put("result_type", resultType);
    }

    public void setNear(GeoLocation location) {
        put("near", location.getLatitude() + "," + location.getLongitude());
    }

    public void setPaging(Paging paging) {
        if (paging == null) return;
        copyValue(paging, "count");
    }

    @StringDef({Filter.IMAGES, Filter.VIDEOS, Filter.PERISCOPE, Filter.NEWS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Filter {
        String IMAGES = "images";
        String VIDEOS = "videos";
        String PERISCOPE = "periscope";
        String NEWS = "news";
    }

    @StringDef({Module.TWEET, Module.USER_GALLERY, Module.NEWS, Module.MEDIA_GALLERY,
            Module.SUGGESTION, Module.EVENT, Module.TWEET_GALLERY, Module.FOLLOWS_TWEET_GALLERY,
            Module.NEARBY_TWEET_GALLERY, Module.SUMMARY, Module.EVENT_SUMMARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Module {
        String TWEET = "tweet";
        String USER_GALLERY = "user_gallery";
        String NEWS = "news";
        String MEDIA_GALLERY = "media_gallery";
        String SUGGESTION = "suggestion";
        String EVENT = "event";
        String TWEET_GALLERY = "tweet_gallery";
        String FOLLOWS_TWEET_GALLERY = "follows_tweet_gallery";
        String NEARBY_TWEET_GALLERY = "nearby_tweet_gallery";
        String SUMMARY = "summary";
        String EVENT_SUMMARY = "event_summary";
    }

    @StringDef({ResultType.RECENT, ResultType.FOLLOWS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResultType {
        String RECENT = "recent";
        String FOLLOWS = "follows";
    }
}
