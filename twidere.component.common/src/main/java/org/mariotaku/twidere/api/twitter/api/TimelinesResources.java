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
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;

@SuppressWarnings("RedundantThrows")
@MethodExtra(name = "extra_params", values = {"include_my_retweet", "include_rts", "include_entities",
        "include_cards", "cards_platform", "include_reply_count", "include_descendent_reply_count"})
public interface TimelinesResources {

    @GET("/statuses/home_timeline.json")
    ResponseList<Status> getHomeTimeline(@Query Paging paging) throws TwitterException;

    @GET("/statuses/mentions_timeline.json")
    ResponseList<Status> getMentionsTimeline(@Query Paging paging) throws TwitterException;

    @GET("/statuses/retweets_of_me.json")
    ResponseList<Status> getRetweetsOfMe(@Query Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;
}
