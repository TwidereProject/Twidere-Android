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
import org.mariotaku.twidere.api.twitter.model.Friendship;
import org.mariotaku.twidere.api.twitter.model.FriendshipUpdate;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;

@SuppressWarnings("RedundantThrows")
@Queries({@KeyValue(key = "include_entities", valueKey = "include_entities")})
public interface FriendsFollowersResources {

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    User createFriendship(@Param("user_id") long userId) throws TwitterException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    User createFriendship(@Param("user_id") long userId, @Param("follow") boolean follow) throws TwitterException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    User createFriendship(@Param("screen_name") String screenName) throws TwitterException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    User createFriendship(@Param("screen_name") String screenName, @Param("follow") boolean follow) throws TwitterException;

    @POST("/friendships/destroy.json")
    @BodyType(BodyType.FORM)
    User destroyFriendship(@Param("user_id") long userId) throws TwitterException;

    @POST("/friendships/destroy.json")
    @BodyType(BodyType.FORM)
    User destroyFriendship(@Param("screen_name") String screenName) throws TwitterException;

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

    @POST("/friendships/lookup.json")
    ResponseList<Friendship> lookupFriendships(@Param(value = "id",arrayDelimiter = ',')long[] ids) throws TwitterException;
    @POST("/friendships/lookup.json")
    ResponseList<Friendship> lookupFriendships(@Param(value = "id",arrayDelimiter = ',')String[] screenNames) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_id") long sourceId, @Query("target_id") long targetId) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("target_id") long targetId) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_screen_name") String sourceScreenName,
                                @Query("target_screen_name") String targetScreenName) throws TwitterException;

    @POST("/friendships/update.json")
    @BodyType(BodyType.FORM)
    Relationship updateFriendship(@Param("user_id") long userId, @Param FriendshipUpdate update) throws TwitterException;

    @POST("/friendships/update.json")
    @BodyType(BodyType.FORM)
    Relationship updateFriendship(@Param("screen_name") String screenName, @Param FriendshipUpdate update) throws TwitterException;
}
