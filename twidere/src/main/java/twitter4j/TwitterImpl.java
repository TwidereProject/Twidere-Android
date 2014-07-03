/*
 * Copyright (C) 2007 Yusuke Yamamoto
 * Copyright (C) 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import static twitter4j.http.HttpParameter.getParameterArray;

import org.json.JSONException;

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalStringUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A java representation of the <a
 * href="https://dev.twitter.com/docs/api">Twitter REST API</a><br>
 * This class is thread safe and can be cached/re-used and used concurrently.<br>
 * Currently this class is not carefully designed to be extended. It is
 * suggested to extend this class only for mock testing purpose.<br>
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
final class TwitterImpl extends TwitterBaseImpl implements Twitter {

	private final HttpParameter INCLUDE_ENTITIES;

	private final HttpParameter INCLUDE_RTS;

	private final HttpParameter INCLUDE_MY_RETWEET;

	/* package */
	TwitterImpl(final Configuration conf, final Authorization auth) {
		super(conf, auth);
		INCLUDE_ENTITIES = new HttpParameter("include_entities", conf.isIncludeEntitiesEnabled());
		INCLUDE_RTS = new HttpParameter("include_rts", conf.isIncludeRTsEnabled());
		INCLUDE_MY_RETWEET = new HttpParameter("include_my_retweet", 1);
	}

	@Override
	public User acceptFriendship(final long userId) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_ACCEPT;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_ACCEPT;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId)));
	}

	@Override
	public User acceptFriendship(final String screenName) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_ACCEPT;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_ACCEPT;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName)));
	}

	@Override
	public UserList addUserListMember(final long listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE, new HttpParameter("list_id", listId),
				new HttpParameter("list_id", listId)));
	}

	@Override
	public UserList addUserListMember(final long listId, final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE, new HttpParameter("list_id", listId),
				new HttpParameter("screen_name", screenName)));
	}

	@Override
	public UserList addUserListMembers(final long listId, final long[] userIds) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE_ALL,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE_ALL, new HttpParameter("list_id", listId),
				new HttpParameter("user_id", InternalStringUtil.join(userIds))));
	}

	@Override
	public UserList addUserListMembers(final long listId, final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE_ALL,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_CREATE_ALL, new HttpParameter("list_id", listId),
				new HttpParameter("screen_name", InternalStringUtil.join(screenNames))));
	}

	@Override
	public User createBlock(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public User createBlock(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public Status createFavorite(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_FAVORITES_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_CREATE;
		return factory.createStatus(post(url, signUrl, new HttpParameter("id", id), INCLUDE_ENTITIES));
	}

	@Override
	public User createFriendship(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId)));
	}

	@Override
	public User createFriendship(final long userId, final boolean follow) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId), new HttpParameter("follow",
				follow)));
	}

	@Override
	public User createFriendship(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName)));
	}

	@Override
	public User createFriendship(final String screenName, final boolean follow) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName), new HttpParameter(
				"follow", follow)));
	}

	@Override
	public User createMute(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public User createMute(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_CREATE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_CREATE;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public Place createPlace(final String name, final String containedWithin, final String token,
			final GeoLocation location, final String streetAddress) throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> params = new ArrayList<HttpParameter>(6);
		addParameterToList(params, "name", name);
		addParameterToList(params, "contained_within", containedWithin);
		addParameterToList(params, "token", token);
		addParameterToList(params, "lat", location.getLatitude());
		addParameterToList(params, "long", location.getLongitude());
		addParameterToList(params, "attribute:street_address", streetAddress);
		return factory.createPlace(post(conf.getRestBaseURL() + ENDPOINT_GEO_PLACE, conf.getSigningRestBaseURL()
				+ ENDPOINT_GEO_PLACE, params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public SavedSearch createSavedSearch(final String query) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(post(conf.getRestBaseURL() + ENDPOINT_SAVED_SEARCHES_CREATE,
				conf.getSigningRestBaseURL() + ENDPOINT_SAVED_SEARCHES_CREATE, new HttpParameter("query", query)));
	}

	@Override
	public UserList createUserList(final String listName, final boolean isPublicList, final String description)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> params = new ArrayList<HttpParameter>();
		addParameterToList(params, "name", listName);
		addParameterToList(params, "mode", isPublicList ? "public" : "private");
		addParameterToList(params, "description", description);
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_CREATE, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_CREATE, params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public UserList createUserListSubscription(final long listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_CREATE,
						conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_CREATE, new HttpParameter("list_id",
								listId)));
	}

	@Override
	public UserList deleteUserListMember(final long listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY, new HttpParameter("list_id", listId),
				new HttpParameter("user_id", userId)));
	}

	@Override
	public UserList deleteUserListMember(final long listId, final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY, new HttpParameter("list_id", listId),
				new HttpParameter("screen_name", screenName)));
	}

	@Override
	public UserList deleteUserListMembers(final long listId, final long[] userIds) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY_ALL,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY_ALL,
				new HttpParameter("list_id", listId), new HttpParameter("user_id", InternalStringUtil.join(userIds))));
	}

	@Override
	public UserList deleteUserListMembers(final long listId, final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY_ALL,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS_DESTROY_ALL,
				new HttpParameter("list_id", listId),
				new HttpParameter("screen_name", InternalStringUtil.join(screenNames))));
	}

	@Override
	public User denyFriendship(final long userId) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_DENY;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_DENY;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId)));
	}

	@Override
	public User denyFriendship(final String screenName) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_DENY;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_DENY;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName)));
	}

	@Override
	public User destroyBlock(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_BLOCKS_DESTROY, conf.getSigningRestBaseURL()
				+ ENDPOINT_BLOCKS_DESTROY, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public User destroyBlock(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_BLOCKS_DESTROY, conf.getSigningRestBaseURL()
				+ ENDPOINT_BLOCKS_DESTROY, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public DirectMessage destroyDirectMessage(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_DESTROY, new HttpParameter("id", id),
				INCLUDE_ENTITIES));
	}

	@Override
	public Status destroyFavorite(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + ENDPOINT_FAVORITES_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_DESTROY, new HttpParameter("id", id),
				INCLUDE_ENTITIES));
	}

	@Override
	public User destroyFriendship(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_DESTROY, new HttpParameter("user_id", userId)));
	}

	@Override
	public User destroyFriendship(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_DESTROY, new HttpParameter("screen_name",
						screenName)));
	}

	@Override
	public User destroyMute(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_DESTROY;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_DESTROY;
		return factory.createUser(post(url, signUrl, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public User destroyMute(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_DESTROY;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_DESTROY;
		return factory.createUser(post(url, signUrl, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public SavedSearch destroySavedSearch(final int id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(post(conf.getRestBaseURL() + "saved_searches/destroy/" + id + ".json",
				conf.getSigningRestBaseURL() + "saved_searches/destroy/" + id + ".json"));
	}

	@Override
	public Status destroyStatus(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "statuses/destroy/" + statusId + ".json",
				conf.getSigningRestBaseURL() + "statuses/destroy/" + statusId + ".json", INCLUDE_ENTITIES));
	}

	@Override
	public UserList destroyUserList(final long listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_DESTROY,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_DESTROY, new HttpParameter("list_id", listId)));
	}

	@Override
	public UserList destroyUserListSubscription(final long listId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_DESTROY,
						conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_DESTROY, new HttpParameter("list_id",
								listId)));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		final TwitterImpl twitter = (TwitterImpl) o;

		if (!INCLUDE_ENTITIES.equals(twitter.INCLUDE_ENTITIES)) return false;
		if (!INCLUDE_RTS.equals(twitter.INCLUDE_RTS)) return false;

		return true;
	}

	@Override
	public AccountSettings getAccountSettings() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createAccountSettings(get(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_SETTINGS,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_SETTINGS));
	}

	@Override
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException {
		return getActivitiesAboutMe(null);
	}

	@Override
	public ResponseList<Activity> getActivitiesAboutMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createActivityList(get(conf.getRestBaseURL() + ENDPOINT_ACTIVITY_ABOUT_ME,
				conf.getSigningRestBaseURL() + ENDPOINT_ACTIVITY_ABOUT_ME,
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	@Override
	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException {
		return getActivitiesByFriends(null);
	}

	@Override
	public ResponseList<Activity> getActivitiesByFriends(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createActivityList(get(conf.getRestBaseURL() + ENDPOINT_ACTIVITY_BY_FRIENDS,
				conf.getSigningRestBaseURL() + ENDPOINT_ACTIVITY_BY_FRIENDS,
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	@Override
	public TwitterAPIConfiguration getAPIConfiguration() throws TwitterException {
		return factory.createTwitterAPIConfiguration(get(conf.getRestBaseURL() + ENDPOINT_HELP_CONFIGURATION,
				conf.getSigningRestBaseURL() + ENDPOINT_HELP_CONFIGURATION));
	}

	@Override
	public ResponseList<Location> getAvailableTrends() throws TwitterException {
		return factory.createLocationList(get(conf.getRestBaseURL() + ENDPOINT_TRENDS_AVAILABLE,
				conf.getSigningRestBaseURL() + ENDPOINT_TRENDS_AVAILABLE));
	}

	@Override
	public ResponseList<Location> getAvailableTrends(final GeoLocation location) throws TwitterException {
		return factory.createLocationList(get(conf.getRestBaseURL() + ENDPOINT_TRENDS_AVAILABLE,
				conf.getSigningRestBaseURL() + ENDPOINT_TRENDS_AVAILABLE,
				new HttpParameter("lat", location.getLatitude()), new HttpParameter("long", location.getLongitude())));
	}

	@Override
	public IDs getBlocksIDs() throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_IDS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_IDS;
		return factory.createIDs(get(url, signUrl));
	}

	@Override
	public IDs getBlocksIDs(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_IDS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_IDS;
		return factory.createIDs(get(url, signUrl, paging.asPostParameterArray()));
	}

	@Override
	public PagableResponseList<User> getBlocksList() throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_LIST;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_LIST;
		return factory.createPagableUserList(get(url, signUrl, INCLUDE_ENTITIES));
	}

	@Override
	public PagableResponseList<User> getBlocksList(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_BLOCKS_LIST;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_BLOCKS_LIST;
		final HttpParameter[] params = mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES);
		return factory.createPagableUserList(get(url, signUrl, params));
	}

	@Override
	public ResponseList<Location> getClosestTrends(final GeoLocation location) throws TwitterException {
		return factory.createLocationList(get(conf.getRestBaseURL() + ENDPOINT_TRENDS_CLOSEST,
				conf.getSigningRestBaseURL() + ENDPOINT_TRENDS_CLOSEST,
				new HttpParameter("lat", location.getLatitude()), new HttpParameter("long", location.getLongitude())));
	}

	@Override
	public ResponseList<DirectMessage> getDirectMessages() throws TwitterException {
		return getDirectMessages(null);
	}

	@Override
	public ResponseList<DirectMessage> getDirectMessages(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessageList(get(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES,
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	@Override
	public ResponseList<Status> getFavorites() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST, INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Status> getFavorites(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST, new HttpParameter("user_id", userId),
				INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Status> getFavorites(final long userId, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory
				.createStatusList(get(
						conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
						conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST,
						mergeParameters(paging.asPostParameterArray(), new HttpParameter("user_id", userId),
								INCLUDE_ENTITIES)));
	}

	@Override
	public ResponseList<Status> getFavorites(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				mergeParameters(paging != null ? paging.asPostParameterArray() : null, INCLUDE_ENTITIES)));
	}

	@Override
	public ResponseList<Status> getFavorites(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST, new HttpParameter("screen_name", screenName),
				INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Status> getFavorites(final String screenName, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(
				conf.getRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FAVORITES_LIST,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("screen_name", screenName),
						INCLUDE_ENTITIES)));
	}

	@Override
	public IDs getFollowersIDs(final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FOLLOWERS_IDS, paging.asPostParameterArray()));
	}

	@Override
	public IDs getFollowersIDs(final long userId, final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FOLLOWERS_IDS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("user_id", userId))));
	}

	@Override
	public IDs getFollowersIDs(final String screenName, final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FOLLOWERS_IDS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("screen_name", screenName))));
	}

	@Override
	public PagableResponseList<User> getFollowersList(final CursorPaging paging) throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FOLLOWERS_LIST, paging.asPostParameterArray()));
	}

	@Override
	public PagableResponseList<User> getFollowersList(final long userId, final CursorPaging paging)
			throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FOLLOWERS_LIST,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("user_id", userId))));
	}

	@Override
	public PagableResponseList<User> getFollowersList(final String screenName, final CursorPaging paging)
			throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FOLLOWERS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FOLLOWERS_LIST,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("screen_name", screenName))));
	}

	@Override
	public IDs getFriendsIDs(final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FRIENDS_IDS, paging.asPostParameterArray()));
	}

	@Override
	public IDs getFriendsIDs(final long userId, final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FRIENDS_IDS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("user_id", userId))));
	}

	@Override
	public IDs getFriendsIDs(final String screenName, final CursorPaging paging) throws TwitterException {
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_IDS, conf.getSigningRestBaseURL()
				+ ENDPOINT_FRIENDS_IDS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("screen_name", screenName))));
	}

	@Override
	public PagableResponseList<User> getFriendsList(final CursorPaging paging) throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDS_LIST, paging.asPostParameterArray()));
	}

	@Override
	public PagableResponseList<User> getFriendsList(final long userId, final CursorPaging paging)
			throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDS_LIST,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("user_id", userId))));
	}

	@Override
	public PagableResponseList<User> getFriendsList(final String screenName, final CursorPaging paging)
			throws TwitterException {
		return factory.createPagableUserList(get(conf.getRestBaseURL() + ENDPOINT_FRIENDS_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDS_LIST,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("screen_name", screenName))));
	}

	@Override
	public Place getGeoDetails(final String id) throws TwitterException {
		return factory.createPlace(get(conf.getRestBaseURL() + "geo/id/" + id + ".json", conf.getSigningRestBaseURL()
				+ "geo/id/" + id + ".json"));
	}

	@Override
	public ResponseList<Status> getHomeTimeline() throws TwitterException {
		return getHomeTimeline(null);
	}

	@Override
	public ResponseList<Status> getHomeTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_HOME_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_HOME_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public IDs getIncomingFriendships(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_INCOMING,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_INCOMING, paging.asPostParameterArray()));
	}

	@Override
	public ResponseList<Language> getLanguages() throws TwitterException {
		return factory.createLanguageList(get(conf.getRestBaseURL() + ENDPOINT_HELP_LANGUAGES,
				conf.getSigningRestBaseURL() + ENDPOINT_HELP_LANGUAGES));
	}

	@Override
	public Trends getLocationTrends(final int woeid) throws TwitterException {
		return getPlaceTrends(woeid);
	}

	@Override
	public ResponseList<Status> getMediaTimeline() throws TwitterException {
		return getMediaTimeline(new Paging());
	}

	@Override
	public ResponseList<Status> getMediaTimeline(final long userId) throws TwitterException {
		return getMediaTimeline(userId, null);
	}

	@Override
	public ResponseList<Status> getMediaTimeline(final long userId, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		paramsList.add(new HttpParameter("user_id", userId));
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public ResponseList<Status> getMediaTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public ResponseList<Status> getMediaTimeline(final String screenName) throws TwitterException {
		return getMediaTimeline(screenName, null);
	}

	@Override
	public ResponseList<Status> getMediaTimeline(final String screenName, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_MEDIA_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		paramsList.add(new HttpParameter("screen_name", screenName));
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public ResponseList<User> getMemberSuggestions(final String categorySlug) throws TwitterException {
		final HttpResponse res = get(conf.getRestBaseURL() + "users/suggestions/" + categorySlug + "/members.json",
				conf.getSigningRestBaseURL() + "users/suggestions/" + categorySlug + "/members.json");
		return factory.createUserListFromJSONArray(res);
	}

	@Override
	public ResponseList<Status> getMentionsTimeline() throws TwitterException {
		ensureAuthorizationEnabled();
		return getMentionsTimeline(null);
	}

	@Override
	public ResponseList<Status> getMentionsTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_MENTIONS_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_MENTIONS_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public IDs getMutesUsersIDs() throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_IDS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_IDS;
		return factory.createIDs(get(url, signUrl));
	}

	@Override
	public IDs getMutesUsersIDs(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_IDS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_IDS;
		return factory.createIDs(get(url, signUrl, paging.asPostParameterArray()));
	}

	@Override
	public PagableResponseList<User> getMutesUsersList() throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_LIST;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_LIST;
		return factory.createPagableUserList(get(url, signUrl, INCLUDE_ENTITIES));
	}

	@Override
	public PagableResponseList<User> getMutesUsersList(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_MUTES_USERS_LIST;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_MUTES_USERS_LIST;
		final HttpParameter[] params = mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES);
		return factory.createPagableUserList(get(url, signUrl, params));
	}

	@Override
	public OEmbed getOEmbed(final OEmbedRequest req) throws TwitterException {
		return factory.createOEmbed(get(conf.getRestBaseURL() + ENDPOINT_STATUSES_OEMBED, conf.getRestBaseURL()
				+ ENDPOINT_STATUSES_OEMBED, req.asHttpParameterArray()));
	}

	@Override
	public IDs getOutgoingFriendships(final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_OUTGOING,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_OUTGOING, paging.asPostParameterArray()));
	}

	@Override
	public Trends getPlaceTrends(final int woeid) throws TwitterException {
		return factory.createTrends(get(conf.getRestBaseURL() + ENDPOINT_TRENDS_PLACE, conf.getSigningRestBaseURL()
				+ ENDPOINT_TRENDS_PLACE, new HttpParameter("id", woeid)));
	}

	@Override
	public String getPrivacyPolicy() throws TwitterException {
		try {
			return get(conf.getRestBaseURL() + ENDPOINT_LEGAL_PRIVACY,
					conf.getSigningRestBaseURL() + ENDPOINT_LEGAL_PRIVACY).asJSONObject().getString("privacy");
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	@Override
	public Map<String, RateLimitStatus> getRateLimitStatus() throws TwitterException {
		return factory.createRateLimitStatus(get(conf.getRestBaseURL() + ENDPOINT_RATE_LIMIT_STATUS,
				conf.getSigningRestBaseURL() + ENDPOINT_RATE_LIMIT_STATUS));
	}

	@Override
	public Map<String, RateLimitStatus> getRateLimitStatus(final String... resources) throws TwitterException {
		return factory.createRateLimitStatus(get(conf.getRestBaseURL() + ENDPOINT_RATE_LIMIT_STATUS,
				conf.getSigningRestBaseURL() + ENDPOINT_RATE_LIMIT_STATUS, new HttpParameter("resources",
						InternalStringUtil.join(resources))));
	}

	@Override
	public IDs getRetweetersIDs(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_STATUSES_RETWEETERS_IDS,
				conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_RETWEETERS_IDS, new HttpParameter("id", statusId)));
	}

	@Override
	public IDs getRetweetersIDs(final long statusId, final CursorPaging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createIDs(get(conf.getRestBaseURL() + ENDPOINT_STATUSES_RETWEETERS_IDS,
				conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_RETWEETERS_IDS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("id", statusId))));
	}

	@Override
	public ResponseList<Status> getRetweets(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets/" + statusId + ".json",
				conf.getSigningRestBaseURL() + "statuses/retweets/" + statusId + ".json", INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Status> getRetweets(final long statusId, final int count) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + "statuses/retweets/" + statusId + ".json",
				conf.getSigningRestBaseURL() + "statuses/retweets/" + statusId + ".json", new HttpParameter("count",
						count), INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Status> getRetweetsOfMe() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_STATUSES_RETWEETS_OF_ME,
				conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_RETWEETS_OF_ME, INCLUDE_ENTITIES, INCLUDE_RTS));
	}

	@Override
	public ResponseList<Status> getRetweetsOfMe(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatusList(get(conf.getRestBaseURL() + ENDPOINT_STATUSES_RETWEETS_OF_ME,
				conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_RETWEETS_OF_ME,
				mergeParameters(paging.asPostParameterArray(), INCLUDE_RTS, INCLUDE_ENTITIES)));
	}

	@Override
	public ResponseList<SavedSearch> getSavedSearches() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearchList(get(conf.getRestBaseURL() + ENDPOINT_SAVED_SEARCHES_LIST,
				conf.getSigningRestBaseURL() + ENDPOINT_SAVED_SEARCHES_LIST));
	}

	@Override
	public ResponseList<DirectMessage> getSentDirectMessages() throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessageList(get(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SENT,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SENT, INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<DirectMessage> getSentDirectMessages(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessageList(get(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SENT,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SENT,
				mergeParameters(paging.asPostParameterArray(), INCLUDE_ENTITIES)));
	}

	@Override
	public SimilarPlaces getSimilarPlaces(final GeoLocation location, final String name, final String containedWithin,
			final String streetAddress) throws TwitterException {
		final List<HttpParameter> params = new ArrayList<HttpParameter>(3);
		params.add(new HttpParameter("lat", location.getLatitude()));
		params.add(new HttpParameter("long", location.getLongitude()));
		params.add(new HttpParameter("name", name));
		if (containedWithin != null) {
			params.add(new HttpParameter("contained_within", containedWithin));
		}
		if (streetAddress != null) {
			params.add(new HttpParameter("attribute:street_address", streetAddress));
		}
		return factory.createSimilarPlaces(get(conf.getRestBaseURL() + ENDPOINT_GEO_SIMILAR_PLACES,
				conf.getSigningRestBaseURL() + ENDPOINT_GEO_SIMILAR_PLACES,
				params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public StatusActivitySummary getStatusActivitySummary(final long statusId) throws TwitterException {
		return getStatusActivitySummary(statusId, false);
	}

	@Override
	public StatusActivitySummary getStatusActivitySummary(final long statusId, final boolean includeDescendentReplyCount)
			throws TwitterException {
		final String endpoint = String.format(Locale.ROOT, "statuses/%d/activity/summary.json", statusId);
		final String url = conf.getRestBaseURL() + endpoint;
		final String signUrl = conf.getSigningRestBaseURL() + endpoint;
		final HttpParameter paramIncludeDescendentReplyCount = new HttpParameter("include_descendent_reply_count",
				includeDescendentReplyCount);
		return factory.createStatusActivitySummary(get(url, signUrl, paramIncludeDescendentReplyCount));
	}

	@Override
	public ResponseList<Category> getSuggestedUserCategories() throws TwitterException {
		return factory.createCategoryList(get(conf.getRestBaseURL() + ENDPOINT_USERS_SUGGESTIONS,
				conf.getSigningRestBaseURL() + ENDPOINT_USERS_SUGGESTIONS));
	}

	@Override
	public String getTermsOfService() throws TwitterException {
		try {
			return get(conf.getRestBaseURL() + ENDPOINT_LEGAL_TOS, conf.getSigningRestBaseURL() + ENDPOINT_LEGAL_TOS)
					.asJSONObject().getString("tos");
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	@Override
	public PagableResponseList<User> getUserListMembers(final long listId, final CursorPaging paging)
			throws TwitterException {
		return factory
				.createPagableUserList(get(
						conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
						conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
						mergeParameters(paging.asPostParameterArray(), new HttpParameter("list_id", listId),
								INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<User> getUserListMembers(final String slug, final long ownerId, final CursorPaging paging)
			throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_id", ownerId), INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<User> getUserListMembers(final String slug, final String ownerScreenName,
			final CursorPaging paging) throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_screen_name", ownerScreenName), INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long cursor) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS, new HttpParameter("cursor", cursor)));

	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor)
			throws TwitterException {
		return getUserListMemberships(listMemberId, cursor, false);
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(final long listMemberId, final long cursor,
			final boolean filterToOwnedLists) throws TwitterException {
		if (filterToOwnedLists) {
			ensureAuthorizationEnabled();
		}
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS, new HttpParameter("user_id", listMemberId),
				new HttpParameter("cursor", cursor), new HttpParameter("filter_to_owned_lists", filterToOwnedLists)));
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor)
			throws TwitterException {
		return getUserListMemberships(listMemberScreenName, cursor, false);
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(final String listMemberScreenName, final long cursor,
			final boolean filterToOwnedLists) throws TwitterException {
		if (filterToOwnedLists) {
			ensureAuthorizationEnabled();
		}
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_MEMBERSHIPS, new HttpParameter("screen_name",
						listMemberScreenName), new HttpParameter("cursor", cursor), new HttpParameter(
						"filter_to_owned_lists", filterToOwnedLists)));
	}

	@Override
	public PagableResponseList<UserList> getUserListOwnerships(final long cursor) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		return factory.createPagableUserListList(get(url, signUrl, new HttpParameter("cursor", cursor)));

	}

	@Override
	public PagableResponseList<UserList> getUserListOwnerships(final long listMemberId, final long cursor)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		return factory.createPagableUserListList(get(url, signUrl, new HttpParameter("user_id", listMemberId),
				new HttpParameter("cursor", cursor)));
	}

	@Override
	public PagableResponseList<UserList> getUserListOwnerships(final String listMemberScreenName, final long cursor)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_LISTS_OWNERSHIPS;
		return factory.createPagableUserListList(get(url, signUrl, new HttpParameter("screen_name",
				listMemberScreenName), new HttpParameter("cursor", cursor)));
	}

	@Override
	public ResponseList<UserList> getUserLists(final long listOwnerUserId) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_LIST, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_LIST, new HttpParameter("user_id", listOwnerUserId)));
	}

	@Override
	public ResponseList<UserList> getUserLists(final String listOwnerScreenName) throws TwitterException {
		return factory.createUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_LIST, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_LIST, new HttpParameter("screen_name", listOwnerScreenName)));
	}

	@Override
	public ResponseList<Status> getUserListStatuses(final long listId, final Paging paging) throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("list_id", listId), INCLUDE_ENTITIES,
						INCLUDE_RTS)));
	}

	@Override
	public ResponseList<Status> getUserListStatuses(final String slug, final long ownerId, final Paging paging)
			throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_id", ownerId), INCLUDE_ENTITIES, INCLUDE_RTS)));

	}

	@Override
	public ResponseList<Status> getUserListStatuses(final String slug, final String ownerScreenName, final Paging paging)
			throws TwitterException {
		return factory.createStatusList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_STATUSES,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_screen_name", ownerScreenName), INCLUDE_ENTITIES, INCLUDE_RTS)));
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(final long listId, final CursorPaging paging)
			throws TwitterException {
		return factory
				.createPagableUserList(get(
						conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
						conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
						mergeParameters(paging.asPostParameterArray(), new HttpParameter("list_id", listId),
								INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(final String slug, final long ownerId,
			final CursorPaging paging) throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_id", ownerId), INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(final String slug, final String ownerScreenName,
			final CursorPaging paging) throws TwitterException {
		return factory.createPagableUserList(get(
				conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS,
				mergeParameters(paging.asPostParameterArray(), new HttpParameter("slug", slug), new HttpParameter(
						"owner_screen_name", ownerScreenName), INCLUDE_ENTITIES)));
	}

	@Override
	public PagableResponseList<UserList> getUserListSubscriptions(final String listOwnerScreenName, final long cursor)
			throws TwitterException {
		return factory.createPagableUserListList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIPTIONS,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIPTIONS, new HttpParameter("screen_name",
						listOwnerScreenName), new HttpParameter("cursor", cursor)));
	}

	@Override
	public ResponseList<User> getUserSuggestions(final String categorySlug) throws TwitterException {
		final HttpResponse res = get(conf.getRestBaseURL() + "users/suggestions/" + categorySlug + ".json",
				conf.getSigningRestBaseURL() + "users/suggestions/" + categorySlug + ".json");
		return factory.createUserListFromJSONArray_Users(res);
	}

	@Override
	public ResponseList<Status> getUserTimeline() throws TwitterException {
		return getUserTimeline(new Paging());
	}

	@Override
	public ResponseList<Status> getUserTimeline(final long userId) throws TwitterException {
		return getUserTimeline(userId, null);
	}

	@Override
	public ResponseList<Status> getUserTimeline(final long userId, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		paramsList.add(new HttpParameter("user_id", userId));
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public ResponseList<Status> getUserTimeline(final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public ResponseList<Status> getUserTimeline(final String screenName) throws TwitterException {
		return getUserTimeline(screenName, null);
	}

	@Override
	public ResponseList<Status> getUserTimeline(final String screenName, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_USER_TIMELINE;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		paramsList.add(new HttpParameter("screen_name", screenName));
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, signUrl, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + INCLUDE_ENTITIES.hashCode();
		result = 31 * result + INCLUDE_RTS.hashCode();
		return result;
	}

	@Override
	public ResponseList<Friendship> lookupFriendships(final long[] ids) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createFriendshipList(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_LOOKUP,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_LOOKUP, new HttpParameter("user_id",
						InternalStringUtil.join(ids))));
	}

	@Override
	public ResponseList<Friendship> lookupFriendships(final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createFriendshipList(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_LOOKUP,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_LOOKUP, new HttpParameter("screen_name",
						InternalStringUtil.join(screenNames))));
	}

	@Override
	public ResponseList<User> lookupUsers(final long[] ids) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(conf.getRestBaseURL() + ENDPOINT_USERS_LOOKUP, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_LOOKUP, new HttpParameter("user_id", InternalStringUtil.join(ids)), INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<User> lookupUsers(final String[] screenNames) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(conf.getRestBaseURL() + ENDPOINT_USERS_LOOKUP, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_LOOKUP, new HttpParameter("screen_name", InternalStringUtil.join(screenNames)),
				INCLUDE_ENTITIES));
	}

	@Override
	public void removeProfileBannerImage() throws TwitterException {
		ensureAuthorizationEnabled();
		post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_REMOVE_PROFILE_BANNER, conf.getSigningRestBaseURL()
				+ ENDPOINT_ACCOUNT_REMOVE_PROFILE_BANNER);

	}

	@Override
	public User reportSpam(final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_USERS_REPORT_SPAM, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_REPORT_SPAM, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public int reportSpam(final long statusId, final ReportAs reportAs, final boolean blockUser)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final HttpParameter[] params = { new HttpParameter("status_id", statusId),
				new HttpParameter("report_as", reportAs.value()), new HttpParameter("block_user", blockUser) };
		return post(conf.getRestBaseURL() + ENDPOINT_STATUSES_REPORT_SPAM,
				conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_REPORT_SPAM, params).getStatusCode();
	}

	@Override
	public User reportSpam(final String screenName) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_USERS_REPORT_SPAM, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_REPORT_SPAM, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public Status retweetStatus(final long statusId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + "statuses/retweet/" + statusId + ".json",
				conf.getSigningRestBaseURL() + "statuses/retweet/" + statusId + ".json", INCLUDE_ENTITIES));
	}

	@Override
	public ResponseList<Place> reverseGeoCode(final GeoQuery query) throws TwitterException {
		try {
			return factory.createPlaceList(get(conf.getRestBaseURL() + ENDPOINT_GEO_REVERSE_GEOCODE,
					conf.getSigningRestBaseURL() + ENDPOINT_GEO_REVERSE_GEOCODE, query.asHttpParameterArray()));
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404)
				return factory.createEmptyResponseList();
			else
				throw te;
		}
	}

	@Override
	public QueryResult search(final Query query) throws TwitterException {
		return factory.createQueryResult(
				get(conf.getRestBaseURL() + ENDPOINT_SEARCH_TWEETS, conf.getSigningRestBaseURL()
						+ ENDPOINT_SEARCH_TWEETS, query.asHttpParameterArray(INCLUDE_ENTITIES, INCLUDE_RTS)), query);
	}

	@Override
	public ResponseList<Place> searchPlaces(final GeoQuery query) throws TwitterException {
		return factory.createPlaceList(get(conf.getRestBaseURL() + ENDPOINT_GEO_SEARCH, conf.getSigningRestBaseURL()
				+ ENDPOINT_GEO_SEARCH, query.asHttpParameterArray()));
	}

	@Override
	public ResponseList<User> searchUsers(final String query, final int page) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUserList(get(conf.getRestBaseURL() + ENDPOINT_USERS_SEARCH, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_SEARCH, new HttpParameter("q", query), new HttpParameter("per_page", 20),
				new HttpParameter("page", page), INCLUDE_ENTITIES));
	}

	@Override
	public DirectMessage sendDirectMessage(final long userId, final String text) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW, new HttpParameter("user_id", userId),
				new HttpParameter("text", text), INCLUDE_ENTITIES));
	}

	@Override
	public DirectMessage sendDirectMessage(final long userId, final String text, final long mediaId)
			throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW, new HttpParameter("user_id", userId),
				new HttpParameter("text", text), new HttpParameter("media_id", mediaId), INCLUDE_ENTITIES));
	}

	@Override
	public DirectMessage sendDirectMessage(final String screenName, final String text) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW, new HttpParameter("screen_name",
						screenName), new HttpParameter("text", text), INCLUDE_ENTITIES));
	}

	@Override
	public DirectMessage sendDirectMessage(final String screenName, final String text, final long mediaId)
			throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createDirectMessage(post(conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW,
				conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_NEW, new HttpParameter("screen_name",
						screenName), new HttpParameter("text", text), new HttpParameter("media_id", mediaId),
				INCLUDE_ENTITIES));

	}

	@Override
	public ResponseList<Status> showConversation(final long statusId) throws TwitterException {
		return showConversation(statusId, null);
	}

	@Override
	public ResponseList<Status> showConversation(final long statusId, final Paging paging) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_CONVERSATION_SHOW;
		final String sign_url = conf.getSigningRestBaseURL() + ENDPOINT_CONVERSATION_SHOW;
		final List<HttpParameter> paramsList = new ArrayList<HttpParameter>();
		paramsList.add(INCLUDE_ENTITIES);
		paramsList.add(INCLUDE_MY_RETWEET);
		paramsList.add(new HttpParameter("id", statusId));
		if (paging != null) {
			paramsList.addAll(paging.asPostParameterList());
		}
		return factory.createStatusList(get(url, sign_url, paramsList.toArray(new HttpParameter[paramsList.size()])));
	}

	@Override
	public DirectMessage showDirectMessage(final long id) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SHOW;
		final String sign_url = conf.getSigningRestBaseURL() + ENDPOINT_DIRECT_MESSAGES_SHOW;
		return factory.createDirectMessage(get(url, sign_url, new HttpParameter("id", id), INCLUDE_ENTITIES));
	}

	@Override
	public Relationship showFriendship(final long sourceId, final long targetId) throws TwitterException {
		return factory.createRelationship(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_SHOW,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_SHOW, new HttpParameter("source_id", sourceId),
				new HttpParameter("target_id", targetId)));
	}

	@Override
	public Relationship showFriendship(final String sourceScreenName, final String targetScreenName)
			throws TwitterException {
		return factory.createRelationship(get(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_SHOW,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_SHOW,
				getParameterArray("source_screen_name", sourceScreenName, "target_screen_name", targetScreenName)));
	}

	@Override
	public SavedSearch showSavedSearch(final int id) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createSavedSearch(get(conf.getRestBaseURL() + "saved_searches/show/" + id + ".json",
				conf.getSigningRestBaseURL() + "saved_searches/show/" + id + ".json"));
	}

	@Override
	public Status showStatus(final long statusId) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_STATUSES_SHOW;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_STATUSES_SHOW;
		final HttpParameter paramStatus = new HttpParameter("id", statusId);
		return factory.createStatus(get(url, signUrl, paramStatus, INCLUDE_ENTITIES, INCLUDE_MY_RETWEET));
	}

	@Override
	public TranslationResult showTranslation(final long statusId, final String dest) throws TwitterException {
		final String url = conf.getRestBaseURL() + ENDPOINT_TRANSLATIONS_SHOW;
		final String signUrl = conf.getSigningRestBaseURL() + ENDPOINT_TRANSLATIONS_SHOW;
		final HttpParameter paramStatus = new HttpParameter("id", statusId);
		final HttpParameter paramDest = new HttpParameter("dest", dest);
		return factory.createTranslationResult(get(url, signUrl, paramStatus, paramDest));
	}

	@Override
	public User showUser(final long userId) throws TwitterException {
		return factory.createUser(get(conf.getRestBaseURL() + ENDPOINT_USERS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_SHOW, new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public User showUser(final String screenName) throws TwitterException {
		return factory.createUser(get(conf.getRestBaseURL() + ENDPOINT_USERS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_USERS_SHOW, new HttpParameter("screen_name", screenName), INCLUDE_ENTITIES));
	}

	@Override
	public UserList showUserList(final long listId) throws TwitterException {
		return factory.createAUserList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_SHOW, new HttpParameter("list_id", listId)));
	}

	@Override
	public UserList showUserList(final String slug, final long ownerId) throws TwitterException {
		return factory.createAUserList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_SHOW, new HttpParameter("slug", slug), new HttpParameter("owner_id", ownerId)));
	}

	@Override
	public UserList showUserList(final String slug, final String ownerScreenName) throws TwitterException {
		return factory.createAUserList(get(conf.getRestBaseURL() + ENDPOINT_LISTS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_SHOW, new HttpParameter("slug", slug), new HttpParameter("owner_screen_name",
				ownerScreenName)));
	}

	@Override
	public User showUserListMembership(final long listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(get(conf.getRestBaseURL() + ENDPOINT_LISTS_MEMBERS_SHOW, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_MEMBERS_SHOW, new HttpParameter("list_id", listId), new HttpParameter("user_id",
				userId), INCLUDE_ENTITIES));
	}

	@Override
	public User showUserListSubscription(final long listId, final long userId) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(get(conf.getRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_SHOW,
				conf.getSigningRestBaseURL() + ENDPOINT_LISTS_SUBSCRIBERS_SHOW, new HttpParameter("list_id", listId),
				new HttpParameter("user_id", userId), INCLUDE_ENTITIES));
	}

	@Override
	public String toString() {
		return "TwitterImpl{" + "INCLUDE_ENTITIES=" + INCLUDE_ENTITIES + ", INCLUDE_RTS=" + INCLUDE_RTS + '}';
	}

	@Override
	public AccountSettings updateAccountSettings(final Integer trend_locationWoeid, final Boolean sleep_timeEnabled,
			final String start_sleepTime, final String end_sleepTime, final String time_zone, final String lang)
			throws TwitterException {

		ensureAuthorizationEnabled();

		final List<HttpParameter> params = new ArrayList<HttpParameter>(6);
		addParameterToList(params, "trend_location_woeid", trend_locationWoeid);
		addParameterToList(params, "sleep_time_enabled", sleep_timeEnabled);
		addParameterToList(params, "start_sleep_time", start_sleepTime);
		addParameterToList(params, "end_sleep_time", end_sleepTime);
		addParameterToList(params, "time_zone", time_zone);
		addParameterToList(params, "lang", lang);
		params.add(INCLUDE_ENTITIES);
		return factory.createAccountSettings(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_SETTINGS,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_SETTINGS,
				params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public Relationship updateFriendship(final long userId, final boolean enableDeviceNotification,
			final boolean retweets) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createRelationship(post(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_UPDATE,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_UPDATE, new HttpParameter("user_id", userId),
				new HttpParameter("device", enableDeviceNotification), new HttpParameter("retweets",
						enableDeviceNotification)));
	}

	@Override
	public Relationship updateFriendship(final String screenName, final boolean enableDeviceNotification,
			final boolean retweets) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createRelationship(post(conf.getRestBaseURL() + ENDPOINT_FRIENDSHIPS_UPDATE,
				conf.getSigningRestBaseURL() + ENDPOINT_FRIENDSHIPS_UPDATE,
				new HttpParameter("screen_name", screenName), new HttpParameter("device", enableDeviceNotification),
				new HttpParameter("retweets", enableDeviceNotification)));
	}

	@Override
	public User updateProfile(final String name, final String url, final String location, final String description)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final ArrayList<HttpParameter> params = new ArrayList<HttpParameter>();
		addParameterToList(params, "name", name);
		addParameterToList(params, "url", url);
		addParameterToList(params, "location", location);
		addParameterToList(params, "description", description);
		params.add(INCLUDE_ENTITIES);
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE,
				params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public User updateProfileBackgroundImage(final File image, final boolean tile) throws TwitterException {
		ensureAuthorizationEnabled();
		checkFileValidity(image);
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BACKGROUND_IMAGE,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BACKGROUND_IMAGE, new HttpParameter(
						"image", image), new HttpParameter("tile", tile), INCLUDE_ENTITIES));
	}

	@Override
	public User updateProfileBackgroundImage(final InputStream image, final boolean tile) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BACKGROUND_IMAGE,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BACKGROUND_IMAGE, new HttpParameter(
						"image", "image", image), new HttpParameter("tile", tile), INCLUDE_ENTITIES));
	}

	@Override
	public void updateProfileBannerImage(final File banner) throws TwitterException {
		ensureAuthorizationEnabled();
		checkFileValidity(banner);
		post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, conf.getSigningRestBaseURL()
				+ ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, new HttpParameter("banner", banner));
	}

	@Override
	public void updateProfileBannerImage(final File banner, final int width, final int height, final int offsetLeft,
			final int offsetTop) throws TwitterException {
		ensureAuthorizationEnabled();
		checkFileValidity(banner);
		final List<HttpParameter> params = new ArrayList<HttpParameter>(5);
		addParameterToList(params, "width", width);
		addParameterToList(params, "height", height);
		addParameterToList(params, "offset_left", offsetLeft);
		addParameterToList(params, "offset_top", offsetTop);
		params.add(new HttpParameter("banner", banner));
		post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, conf.getSigningRestBaseURL()
				+ ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, params.toArray(new HttpParameter[params.size()]));

	}

	@Override
	public void updateProfileBannerImage(final InputStream banner) throws TwitterException {
		ensureAuthorizationEnabled();
		post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, conf.getSigningRestBaseURL()
				+ ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, new HttpParameter("banner", "banner", banner));
	}

	@Override
	public void updateProfileBannerImage(final InputStream banner, final int width, final int height,
			final int offsetLeft, final int offsetTop) throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> params = new ArrayList<HttpParameter>(5);
		addParameterToList(params, "width", width);
		addParameterToList(params, "height", height);
		addParameterToList(params, "offset_left", offsetLeft);
		addParameterToList(params, "offset_top", offsetTop);
		params.add(new HttpParameter("banner", "banner", banner));
		post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, conf.getSigningRestBaseURL()
				+ ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER, params.toArray(new HttpParameter[params.size()]));
	}

	@Override
	public User updateProfileColors(final String profileBackgroundColor, final String profileTextColor,
			final String profileLinkColor, final String profileSidebarFillColor, final String profileSidebarBorderColor)
			throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> params = new ArrayList<HttpParameter>(6);
		addParameterToList(params, "profile_background_color", profileBackgroundColor);
		addParameterToList(params, "profile_text_color", profileTextColor);
		addParameterToList(params, "profile_link_color", profileLinkColor);
		addParameterToList(params, "profile_sidebar_fill_color", profileSidebarFillColor);
		addParameterToList(params, "profile_sidebar_border_color", profileSidebarBorderColor);
		params.add(INCLUDE_ENTITIES);
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_COLORS,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_COLORS,
				params.toArray(new HttpParameter[params.size()])));
	}

	@Override
	public User updateProfileImage(final File image) throws TwitterException {
		checkFileValidity(image);
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_IMAGE,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_IMAGE,
				new HttpParameter("image", image), INCLUDE_ENTITIES));
	}

	@Override
	public User updateProfileImage(final InputStream image) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createUser(post(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_IMAGE,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_UPDATE_PROFILE_IMAGE, new HttpParameter("image",
						"image", image), INCLUDE_ENTITIES));
	}

	@Override
	public Status updateStatus(final StatusUpdate status) throws TwitterException {
		ensureAuthorizationEnabled();
		final String url = conf.getRestBaseURL()
				+ (status.isWithMedia() ? ENDPOINT_STATUSES_UPDATE_WITH_MEDIA : ENDPOINT_STATUSES_UPDATE);
		final String sign_url = conf.getSigningRestBaseURL()
				+ (status.isWithMedia() ? ENDPOINT_STATUSES_UPDATE_WITH_MEDIA : ENDPOINT_STATUSES_UPDATE);
		return factory.createStatus(post(url, sign_url, status.asHttpParameterArray(INCLUDE_ENTITIES)));
	}

	@Override
	public Status updateStatus(final String status) throws TwitterException {
		ensureAuthorizationEnabled();
		return factory.createStatus(post(conf.getRestBaseURL() + ENDPOINT_STATUSES_UPDATE, conf.getSigningRestBaseURL()
				+ ENDPOINT_STATUSES_UPDATE, new HttpParameter("status", status), INCLUDE_ENTITIES));
	}

	@Override
	public UserList updateUserList(final long listId, final String newListName, final boolean isPublicList,
			final String newDescription) throws TwitterException {
		ensureAuthorizationEnabled();
		final List<HttpParameter> httpParams = new ArrayList<HttpParameter>();
		httpParams.add(new HttpParameter("list_id", listId));
		if (newListName != null) {
			httpParams.add(new HttpParameter("name", newListName));
		}
		httpParams.add(new HttpParameter("mode", isPublicList ? "public" : "private"));
		if (newDescription != null) {
			httpParams.add(new HttpParameter("description", newDescription));
		}
		return factory.createAUserList(post(conf.getRestBaseURL() + ENDPOINT_LISTS_UPDATE, conf.getSigningRestBaseURL()
				+ ENDPOINT_LISTS_UPDATE, httpParams.toArray(new HttpParameter[httpParams.size()])));
	}

	@Override
	public MediaUploadResponse uploadMedia(final File file) throws TwitterException {
		final String url = conf.getUploadBaseURL() + ENDPOINT_MEDIA_UPLOAD;
		final String signUrl = conf.getSigningUploadBaseURL() + ENDPOINT_MEDIA_UPLOAD;
		return factory.createMediaUploadResponse(post(url, signUrl, new HttpParameter("media", file)));
	}

	@Override
	public MediaUploadResponse uploadMedia(final String fileName, final InputStream fileBody, final String fileType)
			throws TwitterException {
		final String url = conf.getUploadBaseURL() + ENDPOINT_MEDIA_UPLOAD;
		final String signUrl = conf.getSigningUploadBaseURL() + ENDPOINT_MEDIA_UPLOAD;
		return factory.createMediaUploadResponse(post(url, signUrl, new HttpParameter("media", fileName, fileBody,
				fileType)));
	}

	@Override
	public User verifyCredentials() throws TwitterException {
		return super.fillInIDAndScreenName();
	}

}
