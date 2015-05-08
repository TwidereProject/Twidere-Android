/*
 * Twidere - Twitter client for Android
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

package twitter4j.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
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
