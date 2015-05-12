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

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Friendship;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;

@SuppressWarnings("RedundantThrows")
public interface FriendsFollowersResources {

    @POST("/friendships/create.json")
    @Body(BodyType.FORM)
    User createFriendship(@Form("user_id") long userId) throws TwitterException;

    @POST("/friendships/create.json")
    @Body(BodyType.FORM)
    User createFriendship(@Form("user_id") long userId, @Form("follow") boolean follow) throws TwitterException;

    @POST("/friendships/create.json")
    @Body(BodyType.FORM)
    User createFriendship(@Form("screen_name") String screenName) throws TwitterException;

    @POST("/friendships/create.json")
    @Body(BodyType.FORM)
    User createFriendship(@Form("screen_name") String screenName, @Form("follow") boolean follow) throws TwitterException;

    @POST("/friendships/destroy.json")
    @Body(BodyType.FORM)
    User destroyFriendship(@Form("user_id") long userId) throws TwitterException;

    @POST("/friendships/destroy.json")
    @Body(BodyType.FORM)
    User destroyFriendship(@Form("screen_name") String screenName) throws TwitterException;

    IDs getFollowersIDs(@Query Paging paging) throws TwitterException;

    IDs getFollowersIDs(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    IDs getFollowersIDs(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

    IDs getFriendsIDs(Paging paging) throws TwitterException;

    IDs getFriendsIDs(long userId, Paging paging) throws TwitterException;

    IDs getFriendsIDs(String screenName, Paging paging) throws TwitterException;

    @GET("/friends/list.json")
    PageableResponseList<User> getFriendsList(@Query Paging paging) throws TwitterException;

    @GET("/friends/list.json")
    PageableResponseList<User> getFriendsList(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/friends/list.json")
    PageableResponseList<User> getFriendsList(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

    @GET("/friendships/incoming.json")
    IDs getIncomingFriendships(@Query Paging paging) throws TwitterException;

    @GET("/friendships/outgoing.json")
    IDs getOutgoingFriendships(@Query Paging paging) throws TwitterException;

    ResponseList<Friendship> lookupFriendships(long[] ids) throws TwitterException;

    ResponseList<Friendship> lookupFriendships(String[] screenNames) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_id") long sourceId, @Query("target_id") long targetId) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("target_id") long targetId) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_screen_name") String sourceScreenName,
                                @Query("target_screen_name") String targetScreenName) throws TwitterException;

    Relationship updateFriendship(long userId, boolean enableDeviceNotification, boolean retweets)
            throws TwitterException;

    Relationship updateFriendship(String screenName, boolean enableDeviceNotification, boolean retweets)
            throws TwitterException;
}
