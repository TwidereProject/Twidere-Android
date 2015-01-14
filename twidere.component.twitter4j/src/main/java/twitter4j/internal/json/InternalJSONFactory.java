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

package twitter4j.internal.json;

import org.json.JSONObject;

import java.util.Map;

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
import twitter4j.PageableResponseList;
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

	PageableResponseList<User> createPagableUserList(HttpResponse res) throws TwitterException;

	PageableResponseList<UserList> createPagableUserListList(HttpResponse res) throws TwitterException;

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
