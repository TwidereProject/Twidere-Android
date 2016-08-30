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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.microblog.library.twitter.model.UserListUpdate;
import org.mariotaku.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.microblog.library.twitter.template.UserAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;

public interface ListResources {
    @POST("/lists/members/create.json")
    UserList addUserListMember(@Query("list_id") String listId, @Query("user_id") String userId) throws MicroBlogException;

    @POST("/lists/members/create.json")
    UserList addUserListMemberByScreenName(@Query("list_id") String listId, @Query("screen_name") String userScreenName) throws MicroBlogException;

    @POST("/lists/members/create_all.json")
    UserList addUserListMembers(@Param("list_id") String listId, @Param(value = "user_id", arrayDelimiter = ',') String[] userIds) throws MicroBlogException;

    @POST("/lists/members/create_all.json")
    UserList addUserListMembersByScreenName(@Param("list_id") String listId, @Param(value = "screen_name", arrayDelimiter = ',') String[] screenNames) throws MicroBlogException;

    @POST("/lists/create.json")
    UserList createUserList(@Param UserListUpdate update) throws MicroBlogException;

    @POST("/lists/subscribers/create.json")
    UserList createUserListSubscription(@Param("list_id") String listId) throws MicroBlogException;

    @POST("/lists/members/destroy.json")
    UserList deleteUserListMember(@Query("list_id") String listId, @Query("user_id") String userId) throws MicroBlogException;

    @POST("/lists/members/destroy.json")
    UserList deleteUserListMemberByScreenName(@Query("list_id") String listId, @Param("screen_name") String screenName) throws MicroBlogException;

    @POST("/lists/members/destroy_all.json")
    UserList deleteUserListMembers(@Param("list_id") String listId, @Param(value = "user_id", arrayDelimiter = ',') String[] userIds) throws MicroBlogException;

    @POST("/lists/members/destroy_all.json")
    UserList deleteUserListMembersByScreenName(@Query("list_id") String listId, @Param(value = "screen_name", arrayDelimiter = ',') String[] screenNames) throws MicroBlogException;

    @POST("/lists/destroy.json")
    UserList destroyUserList(@Param("list_id") String listId) throws MicroBlogException;

    @POST("/lists/subscribers/destroy.json")
    UserList destroyUserListSubscription(@Param("list_id") String listId) throws MicroBlogException;

    @GET("/lists/members.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListMembers(@Query("list_id") String listId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/members.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListMembers(@Query("slug") String slug,
                                                  @Query("owner_id") String ownerId,
                                                  @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/members.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListMembersByScreenName(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("user_id") String listMemberId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("user_id") String listMemberId, @Query Paging paging,
                                                          @Query("filter_to_owned_lists") boolean filterToOwnedLists) throws MicroBlogException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMembershipsByScreenName(@Query("screen_name") String listMemberScreenName, @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListMembershipsByScreenName(@Query("screen_name") String listMemberScreenName, @Query Paging paging,
                                                                      boolean filterToOwnedLists) throws MicroBlogException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnerships(@Query Paging paging) throws MicroBlogException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnerships(@Query("user_id") String ownerId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnershipsByScreenName(@Query("screen_name") String ownerScreenName, @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserLists(@Query("user_id") String userId, @Query("reverse") boolean reverse) throws MicroBlogException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserListsByScreenName(@Query("screen_name") String screenName, @Query("reverse") boolean reverse) throws MicroBlogException;

    @GET("/lists/statuses.json")
    @Queries(template = StatusAnnotationTemplate.class)
    ResponseList<Status> getUserListStatuses(@Query("list_id") String listId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/statuses.json")
    @Queries(template = StatusAnnotationTemplate.class)
    ResponseList<Status> getUserListStatuses(@Query("slug") String slug, @Query("owner_id") long ownerId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/statuses.json")
    @Queries(template = StatusAnnotationTemplate.class)
    ResponseList<Status> getUserListStatuses(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/subscribers.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String listId, @Query Paging paging) throws MicroBlogException;

    @GET("/lists/subscribers.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String slug, @Query("owner_id") String ownerId, @Query Paging paging)
            throws MicroBlogException;

    @GET("/lists/subscribers.json")
    @Queries(template = UserAnnotationTemplate.class)
    PageableResponseList<User> getUserListSubscribersByScreenName(@Query("list_id") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
            throws MicroBlogException;


    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptionsByScreenName(@Query("screen_name") String listOwnerScreenName, long cursor)
            throws MicroBlogException;

    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptions(@Query("user_id") String userId, long cursor)
            throws MicroBlogException;

    @GET("/lists/show.json")
    UserList showUserList(@Query("list_id") String listId) throws MicroBlogException;

    @GET("/lists/show.json")
    UserList showUserList(@Query("slug") String slug, @Query("owner_id") String ownerId) throws MicroBlogException;

    @GET("/lists/show.json")
    UserList showUserListByScrenName(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName) throws MicroBlogException;

    @POST("/lists/update.json")
    UserList updateUserList(@Param("list_id") String listId, @Param UserListUpdate update) throws MicroBlogException;
}
