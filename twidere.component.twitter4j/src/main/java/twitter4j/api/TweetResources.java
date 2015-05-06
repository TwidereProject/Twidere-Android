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
import twitter4j.ReportAs;
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

    IDs getRetweetersIDs(long statusId) throws TwitterException;

    IDs getRetweetersIDs(long statusId, Paging paging) throws TwitterException;

    /**
     * Returns up to 100 of the first retweets of a given tweet. <br>
     * This method calls http://api.twitter.com/1.1/statuses/retweets
     *
     * @param statusId The numerical ID of the tweet you want the retweets of.
     * @return the retweets of a given tweet
     * @throws twitter4j.TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/statuses/retweets/:id">Tweets
     * Resources › statuses/retweets/:id</a>
     * @since Twitter4J 2.0.10
     */
    ResponseList<Status> getRetweets(long statusId) throws TwitterException;

    /**
     * Returns up to 100 of the first retweets of a given tweet. <br>
     * This method calls http://api.twitter.com/1.1/statuses/retweets
     *
     * @param statusId The numerical ID of the desired status.
     * @param count    Specifies the number of records to retrieve. Must be less
     *                 than or equal to 100.
     * @return the retweets of a given tweet
     * @throws twitter4j.TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/statuses/retweets/:id">Tweets
     * Resources › statuses/retweets/:id</a>
     * @since Twitter4J 2.0.10
     */
    ResponseList<Status> getRetweets(long statusId, int count) throws TwitterException;

    int reportSpam(long statusId, ReportAs reportAs, boolean blockUser) throws TwitterException;

    @POST("/statuses/retweet/{id}.json")
    Status retweetStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/show.json")
    Status showStatus(@Query("id") long id) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form({"status", "in_reply_to_status_id", "possibly_sensitive", "lat",
            "long", "place_id", "display_coordinates", "media_ids"}) StatusUpdate latestStatus) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form("status") String status) throws TwitterException;
}
