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

package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.CursorTimestampResponse;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;

@SuppressWarnings("RedundantThrows")
@Queries({@KeyValue(key = "include_my_retweet", valueKey = "include_my_retweet"),
        @KeyValue(key = "include_rts", valueKey = "include_entities"),
        @KeyValue(key = "include_entities", valueKey = "include_entities"),
        @KeyValue(key = "include_cards", valueKey = "include_cards"),
        @KeyValue(key = "cards_platform", valueKey = "cards_platform"),
        @KeyValue(key = "include_reply_count", valueKey = "include_reply_count"),
        @KeyValue(key = "include_descendent_reply_count", valueKey = "include_descendent_reply_count"),
        @KeyValue(key = "model_version", valueKey = "model_version"),
        @KeyValue(key = "skip_aggregation", valueKey = "skip_aggregation")})
public interface PrivateActivityResources extends PrivateResources {

    @GET("/activity/about_me.json")
    ResponseList<Activity> getActivitiesAboutMe(@Query Paging paging) throws TwitterException;

    @GET("/activity/by_friends.json")
    ResponseList<Activity> getActivitiesByFriends(@Query Paging paging) throws TwitterException;

    @Queries({})
    @GET("/activity/about_me/unread.json")
    CursorTimestampResponse getActivitiesAboutMeUnread(@Query("cursor") boolean cursor) throws TwitterException;

    @Queries({})
    @POST("/activity/about_me/unread.json")
    @BodyType(BodyType.FORM)
    CursorTimestampResponse setActivitiesAboutMeUnread(@Param("cursor") long cursor) throws TwitterException;


}
