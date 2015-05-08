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

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Path;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface TweetResources {
    @POST("/statuses/destroy/{id}.json")
    Status destroyStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/retweeters/ids.json")
    IDs getRetweetersIDs(@Query("id") long statusId, @Query Paging paging) throws TwitterException;

    @GET("/statuses/retweets/{id}.json")
    ResponseList<Status> getRetweets(@Path("id") long statusId, @Query Paging paging) throws TwitterException;

    @POST("/statuses/retweet/{id}.json")
    Status retweetStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/show.json")
    Status showStatus(@Query("id") long id) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form StatusUpdate latestStatus) throws TwitterException;

}
