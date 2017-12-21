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

package org.mariotaku.microblog.library.api.twitter;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.model.microblog.Friendship;
import org.mariotaku.microblog.library.model.microblog.FriendshipUpdate;
import org.mariotaku.microblog.library.model.microblog.IDs;
import org.mariotaku.microblog.library.model.microblog.PageableResponseList;
import org.mariotaku.microblog.library.model.microblog.Paging;
import org.mariotaku.microblog.library.model.microblog.Relationship;
import org.mariotaku.microblog.library.model.microblog.ResponseList;
import org.mariotaku.microblog.library.model.microblog.User;
import org.mariotaku.microblog.library.twitter.template.UserAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;

@SuppressWarnings("RedundantThrows")
@Queries({@KeyValue(key = "include_entities", valueKey = "include_entities")})
public interface FriendsFollowersResources {

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User createFriendship(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User createFriendship(@Param("user_id") String userId, @Param("follow") boolean follow) throws MicroBlogException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User createFriendshipByScreenName(@Param("screen_name") String screenName) throws MicroBlogException;

    @POST("/friendships/create.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User createFriendshipByScreenName(@Param("screen_name") String screenName, @Param("follow") boolean follow) throws MicroBlogException;

    @POST("/friendships/destroy.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User destroyFriendship(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/friendships/destroy.json")
    @BodyType(BodyType.FORM)
    @Params(template = UserAnnotationTemplate.class)
    User destroyFriendshipByScreenName(@Param("screen_name") String screenName) throws MicroBlogException;

    @GET("/followers/ids.json")
    IDs getFollowersIDs(@Query Paging paging) throws MicroBlogException;

    @GET("/followers/ids.json")
    IDs getFollowersIDs(@Query("user_id") String userId, @Query Paging paging) throws MicroBlogException;

    @GET("/followers/ids.json")
    IDs getFollowersIDsByScreenName(@Query("screen_name") String screenName, @Query Paging paging) throws MicroBlogException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query Paging paging) throws MicroBlogException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query("user_id") String userId, @Query Paging paging) throws MicroBlogException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersListByScreenName(@Query("screen_name") String screenName, @Query Paging paging) throws MicroBlogException;

    @GET("/friends/ids.json")
    IDs getFriendsIDs(String userId, Paging paging) throws MicroBlogException;

    @GET("/friends/ids.json")
    IDs getFriendsIDsByScreenName(String screenName, Paging paging) throws MicroBlogException;

    @GET("/friends/list.json")
    PageableResponseList<User> getFriendsList(@Query("user_id") String userId, @Query Paging paging)
            throws MicroBlogException;

    @GET("/friends/list.json")
    PageableResponseList<User> getFriendsListByScreenName(@Query("screen_name") String screenName,
            @Query Paging paging) throws MicroBlogException;

    @GET("/friendships/incoming.json")
    IDs getIncomingFriendships(@Query Paging paging) throws MicroBlogException;

    @GET("/friendships/outgoing.json")
    IDs getOutgoingFriendships(@Query Paging paging) throws MicroBlogException;

    @POST("/friendships/lookup.json")
    ResponseList<Friendship> lookupFriendships(@Param(value = "id", arrayDelimiter = ',') String[] ids)
            throws MicroBlogException;

    @POST("/friendships/lookup.json")
    ResponseList<Friendship> lookupFriendshipsByScreenName(@Param(value = "id", arrayDelimiter = ',') String[] screenNames)
            throws MicroBlogException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_id") String sourceId,
            @Query("target_id") String targetId) throws MicroBlogException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("target_id") String targetId) throws MicroBlogException;

    @GET("/friendships/show.json")
    Relationship showFriendshipByScreenName(@Query("source_screen_name") String sourceScreenName,
            @Query("target_screen_name") String targetScreenName)
            throws MicroBlogException;

    @POST("/friendships/update.json")
    @BodyType(BodyType.FORM)
    Relationship updateFriendship(@Param("user_id") String userId, @Param FriendshipUpdate update)
            throws MicroBlogException;

    @POST("/friendships/update.json")
    @BodyType(BodyType.FORM)
    Relationship updateFriendshipByScreenName(@Param("screen_name") String screenName,
            @Param FriendshipUpdate update) throws MicroBlogException;
}
