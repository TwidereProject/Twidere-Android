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
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.PageableResponseList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserListUpdate;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface ListsResources {
    UserList addUserListMember(long listId, long userId) throws TwitterException;

    UserList addUserListMember(long listId, String userScreenName) throws TwitterException;

    UserList addUserListMembers(long listId, long[] userIds) throws TwitterException;

    UserList addUserListMembers(long listId, String[] screenNames) throws TwitterException;

    UserList createUserList(String listName, boolean isPublicList, String description) throws TwitterException;

    UserList createUserListSubscription(long listId) throws TwitterException;

    UserList deleteUserListMember(long listId, long userId) throws TwitterException;

    UserList deleteUserListMember(long listId, String screenName) throws TwitterException;

    UserList deleteUserListMembers(long listId, long[] userIds) throws TwitterException;

    UserList deleteUserListMembers(long listId, String[] screenNames) throws TwitterException;

    UserList destroyUserList(long listId) throws TwitterException;

    UserList destroyUserListSubscription(long listId) throws TwitterException;

    PageableResponseList<User> getUserListMembers(long listId, Paging paging) throws TwitterException;

    PageableResponseList<User> getUserListMembers(String slug, long ownerId, Paging paging)
            throws TwitterException;

    PageableResponseList<User> getUserListMembers(String slug, String ownerScreenName, Paging paging)
            throws TwitterException;

    PageableResponseList<UserList> getUserListMemberships(long cursor) throws TwitterException;

    PageableResponseList<UserList> getUserListMemberships(long listMemberId, long cursor) throws TwitterException;

    PageableResponseList<UserList> getUserListMemberships(long listMemberId, long cursor, boolean filterToOwnedLists)
            throws TwitterException;

    PageableResponseList<UserList> getUserListMemberships(String listMemberScreenName, long cursor)
            throws TwitterException;

    PageableResponseList<UserList> getUserListMemberships(String listMemberScreenName, long cursor,
                                                          boolean filterToOwnedLists) throws TwitterException;

    PageableResponseList<UserList> getUserListOwnerships(long cursor) throws TwitterException;

    PageableResponseList<UserList> getUserListOwnerships(long listMemberId, long cursor) throws TwitterException;

    PageableResponseList<UserList> getUserListOwnerships(String listMemberScreenName, long cursor)
            throws TwitterException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserLists(@Query("user_id") long userId, @Query("reverse") boolean reverse) throws TwitterException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserLists(@Query("screen_name") String screenName, @Query("reverse") boolean reverse) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("list_id") long listId, @Query Paging paging) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("list_id") String slug, @Query("owner_id") long ownerId, @Query Paging paging) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("list_id") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
            throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") long listId, @Query Paging paging) throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String slug, @Query("owner_id") long ownerId, @Query Paging paging)
            throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
            throws TwitterException;


    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptions(@Query("screen_name") String listOwnerScreenName, long cursor)
            throws TwitterException;

    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptions(@Query("user_id") long userId, long cursor)
            throws TwitterException;

    UserList showUserList(long listId) throws TwitterException;

    UserList showUserList(String slug, long ownerId) throws TwitterException;

    UserList showUserList(String slug, String ownerScreenName) throws TwitterException;

    User showUserListMembership(long listId, long userId) throws TwitterException;

    User showUserListSubscription(long listId, long userId) throws TwitterException;

    UserList updateUserList(@Query("list_id") long listId, @Form UserListUpdate update) throws TwitterException;
}
