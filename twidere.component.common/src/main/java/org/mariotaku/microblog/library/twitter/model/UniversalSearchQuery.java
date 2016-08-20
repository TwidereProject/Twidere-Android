/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.microblog.library.twitter.model;

import android.support.annotation.StringDef;

import org.mariotaku.microblog.library.twitter.util.InternalArrayUtil;
import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 15/10/21.
 */
public class UniversalSearchQuery extends SimpleValueMap {

    public void setCursor(String cursor) {
        put("cursor", cursor);
    }

    public void setQuery(String query) {
        put("q", query);
    }

    public void setCount(int count) {
        put("count", count);
    }

    public void setModules(String[] modules) {
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

    @StringDef({Filter.IMAGES, Filter.VIDEOS, Filter.PERISCOPE, Filter.NEWS})
    public @interface Filter {
        String IMAGES = "images";
        String VIDEOS = "videos";
        String PERISCOPE = "periscope";
        String NEWS = "news";
    }

    @StringDef({Module.TWEET, Module.USER_GALLERY, Module.NEWS, Module.MEDIA_GALLERY,
            Module.SUGGESTION, Module.EVENT, Module.TWEET_GALLERY, Module.FOLLOWS_TWEET_GALLERY,
            Module.NEARBY_TWEET_GALLERY, Module.SUMMARY, Module.EVENT_SUMMARY})
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
    public @interface ResultType {
        String RECENT = "recent";
        String FOLLOWS = "follows";
    }
}
