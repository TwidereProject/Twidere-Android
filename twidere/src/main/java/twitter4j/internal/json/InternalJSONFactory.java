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

package twitter4j.internal.json;

import org.json.JSONObject;

import twitter4j.AccountSettings;
import twitter4j.AccountTotals;
import twitter4j.Activity;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.Friendship;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.MediaUploadResponse;
import twitter4j.OEmbed;
import twitter4j.PagableResponseList;
import twitter4j.Place;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.SimilarPlaces;
import twitter4j.Status;
import twitter4j.StatusActivitySummary;
import twitter4j.TranslationResult;
import twitter4j.Trends;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.api.HelpResources;
import twitter4j.http.HttpResponse;

import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.4
 */
public interface InternalJSONFactory {
	AccountSettings createAccountSettings(HttpResponse res) throws TwitterException;

	AccountTotals createAccountTotals(HttpResponse res) throws TwitterException;

	ResponseList<Activity> createActivityList(HttpResponse res) throws TwitterException;

	UserList createAUserList(HttpResponse res) throws TwitterException;

	UserList createAUserList(JSONObject json) throws TwitterException;

	ResponseList<Category> createCategoryList(HttpResponse res) throws TwitterException;

	DirectMessage createDirectMessage(HttpResponse res) throws TwitterException;

	DirectMessage createDirectMessage(JSONObject json) throws TwitterException;

	ResponseList<DirectMessage> createDirectMessageList(HttpResponse res) throws TwitterException;

	<T> ResponseList<T> createEmptyResponseList();

	ResponseList<Friendship> createFriendshipList(HttpResponse res) throws TwitterException;

	IDs createIDs(HttpResponse res) throws TwitterException;

	ResponseList<HelpResources.Language> createLanguageList(HttpResponse res) throws TwitterException;

	ResponseList<Location> createLocationList(HttpResponse res) throws TwitterException;

	MediaUploadResponse createMediaUploadResponse(HttpResponse res) throws TwitterException;

	OEmbed createOEmbed(HttpResponse httpResponse) throws TwitterException;

	PagableResponseList<User> createPagableUserList(HttpResponse res) throws TwitterException;

	PagableResponseList<UserList> createPagableUserListList(HttpResponse res) throws TwitterException;

	Place createPlace(HttpResponse res) throws TwitterException;

	ResponseList<Place> createPlaceList(HttpResponse res) throws TwitterException;

	QueryResult createQueryResult(HttpResponse res, Query query) throws TwitterException;

	Map<String, RateLimitStatus> createRateLimitStatus(HttpResponse res) throws TwitterException;

	Relationship createRelationship(HttpResponse res) throws TwitterException;

	SavedSearch createSavedSearch(HttpResponse res) throws TwitterException;

	ResponseList<SavedSearch> createSavedSearchList(HttpResponse res) throws TwitterException;

	SimilarPlaces createSimilarPlaces(HttpResponse res) throws TwitterException;

	Status createStatus(HttpResponse res) throws TwitterException;

	Status createStatus(JSONObject json) throws TwitterException;

	StatusActivitySummary createStatusActivitySummary(HttpResponse res) throws TwitterException;

	ResponseList<Status> createStatusList(HttpResponse res) throws TwitterException;

	TranslationResult createTranslationResult(HttpResponse res) throws TwitterException;

	Trends createTrends(HttpResponse res) throws TwitterException;

	ResponseList<Trends> createTrendsList(HttpResponse res) throws TwitterException;

	TwitterAPIConfiguration createTwitterAPIConfiguration(HttpResponse res) throws TwitterException;

	User createUser(HttpResponse res) throws TwitterException;

	User createUser(JSONObject json) throws TwitterException;

	ResponseList<User> createUserList(HttpResponse res) throws TwitterException;

	ResponseList<User> createUserListFromJSONArray(HttpResponse res) throws TwitterException;

	ResponseList<User> createUserListFromJSONArray_Users(HttpResponse res) throws TwitterException;

	ResponseList<UserList> createUserListList(HttpResponse res) throws TwitterException;
}
