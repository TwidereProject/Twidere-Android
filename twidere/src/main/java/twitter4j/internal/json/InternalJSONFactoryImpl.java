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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.AccountSettings;
import twitter4j.AccountTotals;
import twitter4j.Activity;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.Friendship;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
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
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.api.HelpResources;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalStringUtil;

import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.4
 */
public class InternalJSONFactoryImpl implements InternalJSONFactory {

	private final Configuration conf;

	public InternalJSONFactoryImpl(final Configuration conf) {
		this.conf = conf;
	}

	@Override
	public AccountSettings createAccountSettings(final HttpResponse res) throws TwitterException {
		return new AccountSettingsJSONImpl(res, conf);
	}

	@Override
	public AccountTotals createAccountTotals(final HttpResponse res) throws TwitterException {
		return new AccountTotalsJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Activity> createActivityList(final HttpResponse res) throws TwitterException {
		return ActivityJSONImpl.createActivityList(res, conf);
	}

	@Override
	public UserList createAUserList(final HttpResponse res) throws TwitterException {
		return new UserListJSONImpl(res, conf);
	}

	@Override
	public UserList createAUserList(final JSONObject json) throws TwitterException {
		return new UserListJSONImpl(json);
	}

	@Override
	public ResponseList<Category> createCategoryList(final HttpResponse res) throws TwitterException {
		return CategoryJSONImpl.createCategoriesList(res, conf);
	}

	@Override
	public DirectMessage createDirectMessage(final HttpResponse res) throws TwitterException {
		return new DirectMessageJSONImpl(res, conf);
	}

	@Override
	public DirectMessage createDirectMessage(final JSONObject json) throws TwitterException {
		return new DirectMessageJSONImpl(json);
	}

	@Override
	public ResponseList<DirectMessage> createDirectMessageList(final HttpResponse res) throws TwitterException {
		return DirectMessageJSONImpl.createDirectMessageList(res, conf);
	}

	@Override
	public <T> ResponseList<T> createEmptyResponseList() {
		return new ResponseListImpl<T>(0, null);
	}

	@Override
	public ResponseList<Friendship> createFriendshipList(final HttpResponse res) throws TwitterException {
		return FriendshipJSONImpl.createFriendshipList(res, conf);
	}

	@Override
	public IDs createIDs(final HttpResponse res) throws TwitterException {
		return new IDsJSONImpl(res);
	}

	@Override
	public ResponseList<HelpResources.Language> createLanguageList(final HttpResponse res) throws TwitterException {
		return LanguageJSONImpl.createLanguageList(res, conf);
	}

	@Override
	public ResponseList<Location> createLocationList(final HttpResponse res) throws TwitterException {
		return LocationJSONImpl.createLocationList(res, conf);
	}

	@Override
	public MediaUploadResponse createMediaUploadResponse(final HttpResponse res) throws TwitterException {
		return new MediaUploadResponseJSONImpl(res);
	}

	@Override
	public OEmbed createOEmbed(final HttpResponse res) throws TwitterException {
		return new OEmbedJSONImpl(res, conf);
	}

	@Override
	public PagableResponseList<User> createPagableUserList(final HttpResponse res) throws TwitterException {
		return UserJSONImpl.createPagableUserList(res, conf);
	}

	@Override
	public PagableResponseList<UserList> createPagableUserListList(final HttpResponse res) throws TwitterException {
		return UserListJSONImpl.createPagableUserListList(res, conf);
	}

	@Override
	public Place createPlace(final HttpResponse res) throws TwitterException {
		return new PlaceJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Place> createPlaceList(final HttpResponse res) throws TwitterException {
		try {
			return PlaceJSONImpl.createPlaceList(res, conf);
		} catch (final TwitterException te) {
			if (te.getStatusCode() == 404)
				return new ResponseListImpl<Place>(0, null);
			else
				throw te;
		}
	}

	@Override
	public QueryResult createQueryResult(final HttpResponse res, final Query query) throws TwitterException {
		try {
			return new QueryResultJSONImpl(res);
		} catch (final TwitterException te) {
			if (404 == te.getStatusCode())
				return new QueryResultJSONImpl(query);
			else
				throw te;
		}
	}

	@Override
	public Map<String, RateLimitStatus> createRateLimitStatus(final HttpResponse res) throws TwitterException {
		return RateLimitStatusJSONImpl.createRateLimitStatuses(res, conf);
	}

	@Override
	public Relationship createRelationship(final HttpResponse res) throws TwitterException {
		return new RelationshipJSONImpl(res, conf);
	}

	@Override
	public SavedSearch createSavedSearch(final HttpResponse res) throws TwitterException {
		return new SavedSearchJSONImpl(res, conf);
	}

	@Override
	public ResponseList<SavedSearch> createSavedSearchList(final HttpResponse res) throws TwitterException {
		return SavedSearchJSONImpl.createSavedSearchList(res, conf);
	}

	@Override
	public SimilarPlaces createSimilarPlaces(final HttpResponse res) throws TwitterException {
		return SimilarPlacesImpl.createSimilarPlaces(res, conf);
	}

	@Override
	public Status createStatus(final HttpResponse res) throws TwitterException {
		return new StatusJSONImpl(res, conf);
	}

	@Override
	public Status createStatus(final JSONObject json) throws TwitterException {
		return new StatusJSONImpl(json);
	}

	@Override
	public StatusActivitySummary createStatusActivitySummary(final HttpResponse res) throws TwitterException {
		return new StatusActivitySummaryJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Status> createStatusList(final HttpResponse res) throws TwitterException {
		return StatusJSONImpl.createStatusList(res, conf);
	}

	@Override
	public TranslationResult createTranslationResult(final HttpResponse res) throws TwitterException {
		return new TranslationResultJSONImpl(res, conf);
	}

	@Override
	public Trends createTrends(final HttpResponse res) throws TwitterException {
		return new TrendsJSONImpl(res, conf);
	}

	@Override
	public ResponseList<Trends> createTrendsList(final HttpResponse res) throws TwitterException {
		return TrendsJSONImpl.createTrendsList(res, false);
	}

	@Override
	public TwitterAPIConfiguration createTwitterAPIConfiguration(final HttpResponse res) throws TwitterException {
		return new TwitterAPIConfigurationJSONImpl(res, conf);
	}

	@Override
	public User createUser(final HttpResponse res) throws TwitterException {
		return new UserJSONImpl(res, conf);
	}

	@Override
	public User createUser(final JSONObject json) throws TwitterException {
		return new UserJSONImpl(json);
	}

	@Override
	public ResponseList<User> createUserList(final HttpResponse res) throws TwitterException {
		return UserJSONImpl.createUserList(res, conf);
	}

	@Override
	public ResponseList<User> createUserListFromJSONArray(final HttpResponse res) throws TwitterException {
		return UserJSONImpl.createUserList(res.asJSONArray(), res, conf);
	}

	@Override
	public ResponseList<User> createUserListFromJSONArray_Users(final HttpResponse res) throws TwitterException {
		try {
			return UserJSONImpl.createUserList(res.asJSONObject().getJSONArray("users"), res, conf);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	@Override
	public ResponseList<UserList> createUserListList(final HttpResponse res) throws TwitterException {
		return UserListJSONImpl.createUserListList(res, conf);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof InternalJSONFactoryImpl)) return false;

		final InternalJSONFactoryImpl that = (InternalJSONFactoryImpl) o;

		if (conf != null ? !conf.equals(that.conf) : that.conf != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return conf != null ? conf.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "JSONImplFactory{" + "conf=" + conf + '}';
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return hashtag entity
	 * @since Twitter4J 2.2.6
	 */
	public static HashtagEntity createHashtagEntity(final int start, final int end, final String text) {
		return new HashtagEntityJSONImpl(start, end, text);
	}

	public static RateLimitStatus createRateLimitStatusFromResponseHeader(final HttpResponse res) {
		return RateLimitStatusJSONImpl.createFromResponseHeader(res);
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return url entity
	 * @since Twitter4J 2.2.6
	 */
	public static URLEntity createUrlEntity(final int start, final int end, final String url, final String expandedURL,
			final String displayURL) {
		return new URLEntityJSONImpl(start, end, url, expandedURL, displayURL);
	}

	/**
	 * static factory method for twitter-text-java
	 * 
	 * @return user mention entity
	 * @since Twitter4J 2.2.6
	 */
	public static UserMentionEntity createUserMentionEntity(final int start, final int end, final String name,
			final String screenName, final long id) {
		return new UserMentionEntityJSONImpl(start, end, name, screenName, id);
	}

	/* package */
	static GeoLocation[][] coordinatesAsGeoLocationArray(final JSONArray coordinates) throws TwitterException {
		try {
			final GeoLocation[][] boundingBox = new GeoLocation[coordinates.length()][];
			for (int i = 0; i < coordinates.length(); i++) {
				final JSONArray array = coordinates.getJSONArray(i);
				boundingBox[i] = new GeoLocation[array.length()];
				for (int j = 0; j < array.length(); j++) {
					final JSONArray coordinate = array.getJSONArray(j);
					boundingBox[i][j] = new GeoLocation(coordinate.getDouble(1), coordinate.getDouble(0));
				}
			}
			return boundingBox;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	/**
	 * returns a GeoLocation instance if a "geo" element is found.
	 * 
	 * @param json JSONObject to be parsed
	 * @return GeoLocation instance
	 * @throws TwitterException when coordinates is not included in geo element
	 *             (should be an API side issue)
	 */
	/* package */
	static GeoLocation createGeoLocation(final JSONObject json) throws TwitterException {
		try {
			if (!json.isNull("geo")) {
				String coordinates = json.getJSONObject("geo").getString("coordinates");
				coordinates = coordinates.substring(1, coordinates.length() - 1);
				final String[] point = InternalStringUtil.split(coordinates, ",");
				return new GeoLocation(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
		return null;
	}
}
