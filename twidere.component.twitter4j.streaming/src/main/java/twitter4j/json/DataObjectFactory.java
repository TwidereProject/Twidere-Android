/*
 * Copyright 2007 Yusuke Yamamoto
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

package twitter4j.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.AccountTotals;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.OEmbed;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
@SuppressWarnings("unchecked")
public final class DataObjectFactory {
	private static final Constructor<Status> statusConstructor;

	private static final Constructor<User> userConstructor;
	private static final Constructor<Relationship> relationshipConstructor;
	private static final Constructor<Place> placeConstructor;
	private static final Constructor<SavedSearch> savedSearchConstructor;
	private static final Constructor<Trend> trendConstructor;
	private static final Constructor<Trends> trendsConstructor;
	private static final Constructor<IDs> IDsConstructor;
	private static final Method rateLimitStatusConstructor;
	private static final Constructor<Category> categoryConstructor;
	private static final Constructor<DirectMessage> directMessageConstructor;
	private static final Constructor<Location> locationConstructor;
	private static final Constructor<UserList> userListConstructor;
	private static final Constructor<StatusDeletionNotice> statusDeletionNoticeConstructor;
	private static final Constructor<AccountTotals> accountTotalsConstructor;
	private static final Constructor<OEmbed> oembedConstructor;
	static {
		try {
			statusConstructor = (Constructor<Status>) Class.forName("twitter4j.internal.json.StatusJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			statusConstructor.setAccessible(true);

			userConstructor = (Constructor<User>) Class.forName("twitter4j.internal.json.UserJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			userConstructor.setAccessible(true);

			relationshipConstructor = (Constructor<Relationship>) Class.forName(
					"twitter4j.internal.json.RelationshipJSONImpl").getDeclaredConstructor(JSONObject.class);
			relationshipConstructor.setAccessible(true);

			placeConstructor = (Constructor<Place>) Class.forName("twitter4j.internal.json.PlaceJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			placeConstructor.setAccessible(true);

			savedSearchConstructor = (Constructor<SavedSearch>) Class.forName(
					"twitter4j.internal.json.SavedSearchJSONImpl").getDeclaredConstructor(JSONObject.class);
			savedSearchConstructor.setAccessible(true);

			trendConstructor = (Constructor<Trend>) Class.forName("twitter4j.internal.json.TrendJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			trendConstructor.setAccessible(true);

			trendsConstructor = (Constructor<Trends>) Class.forName("twitter4j.internal.json.TrendsJSONImpl")
					.getDeclaredConstructor(String.class);
			trendsConstructor.setAccessible(true);

			IDsConstructor = (Constructor<IDs>) Class.forName("twitter4j.internal.json.IDsJSONImpl")
					.getDeclaredConstructor(String.class);
			IDsConstructor.setAccessible(true);

			rateLimitStatusConstructor = Class.forName("twitter4j.internal.json.RateLimitStatusJSONImpl")
					.getDeclaredMethod("createRateLimitStatuses", JSONObject.class);
			rateLimitStatusConstructor.setAccessible(true);

			categoryConstructor = (Constructor<Category>) Class.forName("twitter4j.internal.json.CategoryJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			categoryConstructor.setAccessible(true);

			directMessageConstructor = (Constructor<DirectMessage>) Class.forName(
					"twitter4j.internal.json.DirectMessageJSONImpl").getDeclaredConstructor(JSONObject.class);
			directMessageConstructor.setAccessible(true);

			locationConstructor = (Constructor<Location>) Class.forName("twitter4j.internal.json.LocationJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			locationConstructor.setAccessible(true);

			userListConstructor = (Constructor<UserList>) Class.forName("twitter4j.internal.json.UserListJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			userListConstructor.setAccessible(true);

			statusDeletionNoticeConstructor = (Constructor<StatusDeletionNotice>) Class.forName(
					"twitter4j.StatusDeletionNoticeImpl").getDeclaredConstructor(JSONObject.class);
			statusDeletionNoticeConstructor.setAccessible(true);

			accountTotalsConstructor = (Constructor<AccountTotals>) Class.forName(
					"twitter4j.internal.json.AccountTotalsJSONImpl").getDeclaredConstructor(JSONObject.class);
			accountTotalsConstructor.setAccessible(true);
			oembedConstructor = (Constructor<OEmbed>) Class.forName("twitter4j.internal.json.OEmbedJSONImpl")
					.getDeclaredConstructor(JSONObject.class);
			oembedConstructor.setAccessible(true);
		} catch (final NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		} catch (final ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static final ThreadLocal<Map> rawJsonMap = new ThreadLocal<Map>() {
		@Override
		protected Map initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	private DataObjectFactory() {
		throw new AssertionError("not intended to be instantiated.");
	}

	/**
	 * Constructs an AccountTotals object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return AccountTotals
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.9
	 */
	public static AccountTotals createAccountTotals(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return accountTotalsConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Category object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Category
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Category createCategory(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return categoryConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a DirectMessage object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return DirectMessage
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static DirectMessage createDirectMessage(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return directMessageConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a IDs object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return IDs
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static IDs createIDs(final String rawJSON) throws TwitterException {
		try {
			return IDsConstructor.newInstance(rawJSON);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Location object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Location
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Location createLocation(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return locationConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Construct an object from rawJSON string. This method may be called when
	 * you do not know what a given raw JSON string contains. It will do the
	 * work of determining what type of object the JSON represents, and
	 * constructing the respective object type. For example, if the JSON
	 * contents represents a Status, then a Status will be returned. If it
	 * represents a deletion notice, then a StatusDeletionNotice will be
	 * returned. The caller can simply use instanceof to handle the returned
	 * object as applicable. NOTE: the raw JSONObject will be returned in cases
	 * where there isn't a discrete respective object type that can be
	 * constructed. That way, the caller can at least have access to the JSON
	 * itself.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return the respective constructed object, or the JSONObject in the case
	 *         where we cannot determine the object type.
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.9
	 */
	public static Object createObject(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			final JSONObjectType.Type jsonObjectType = JSONObjectType.determine(json);
			switch (jsonObjectType) {
				case SENDER:
					return registerJSONObject(
							directMessageConstructor.newInstance(json.getJSONObject("direct_message")), json);
				case STATUS:
					return registerJSONObject(statusConstructor.newInstance(json), json);
				case DIRECT_MESSAGE:
					return registerJSONObject(
							directMessageConstructor.newInstance(json.getJSONObject("direct_message")), json);
				case DELETE:
					return registerJSONObject(
							statusDeletionNoticeConstructor.newInstance(json.getJSONObject("delete").getJSONObject(
									"status")), json);
				case LIMIT:
					// TODO: Perhaps there should be a TrackLimitationNotice
					// object?
					// The onTrackLimitationNotice method could take that as an
					// arg.
					return json;
				case SCRUB_GEO:
					return json;
				default:
					// The object type is unrecognized...just return the json
					return json;
			}
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs an OEmbed object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return OEmbed
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 3.0.2
	 */
	public static OEmbed createOEmbed(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return oembedConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Place object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Place
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Place createPlace(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return placeConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a RateLimitStatus object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return RateLimitStatus
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Map<String, RateLimitStatus> createRateLimitStatus(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return (Map<String, RateLimitStatus>) rateLimitStatusConstructor.invoke(
					Class.forName("twitter4j.internal.json.RateLimitStatusJSONImpl"), json);
		} catch (final ClassNotFoundException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Relationship object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Relationship
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Relationship createRelationship(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return relationshipConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a SavedSearch object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return SavedSearch
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static SavedSearch createSavedSearch(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return savedSearchConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Status object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Status
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Status createStatus(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return statusConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Trend object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Trend
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Trend createTrend(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return trendConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a Trends object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return Trends
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static Trends createTrends(final String rawJSON) throws TwitterException {
		try {
			return trendsConstructor.newInstance(rawJSON);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new TwitterException(e);
		} catch (final InvocationTargetException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Constructs a User object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return User
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static User createUser(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return userConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Constructs a UserList object from rawJSON string.
	 * 
	 * @param rawJSON raw JSON form as String
	 * @return UserList
	 * @throws TwitterException when provided string is not a valid JSON string.
	 * @since Twitter4J 2.1.7
	 */
	public static UserList createUserList(final String rawJSON) throws TwitterException {
		try {
			final JSONObject json = new JSONObject(rawJSON);
			return userListConstructor.newInstance(json);
		} catch (final InstantiationException e) {
			throw new TwitterException(e);
		} catch (final IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (final InvocationTargetException e) {
			throw new TwitterException(e);
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * Returns a raw JSON form of the provided object.<br>
	 * Note that raw JSON forms can be retrieved only from the same thread
	 * invoked the last method call and will become inaccessible once another
	 * method call
	 * 
	 * @param obj
	 * @return raw JSON
	 * @since Twitter4J 2.1.7
	 */
	public static String getRawJSON(final Object obj) {
		final Object json = rawJsonMap.get().get(obj);
		if (json instanceof String)
			return (String) json;
		else if (json != null) // object must be instance of JSONObject
			return json.toString();
		else
			return null;
	}

	/**
	 * clear raw JSON forms associated with the current thread.<br>
	 * Currently this method is called indirectly by
	 * twitter4j.internal.util.DataObjectFactoryUtil, and should be called
	 * directly once *JSONImpl classes are migrated to twitter4j.json.* package.
	 * 
	 * @since Twitter4J 2.1.7
	 */
	static void clearThreadLocalMap() {
		rawJsonMap.get().clear();
	}

	/**
	 * associate a raw JSON form to the current thread<br>
	 * Currently this method is called indirectly by
	 * twitter4j.internal.util.DataObjectFactoryUtil, and should be called
	 * directly once *JSONImpl classes are migrated to twitter4j.json.* package.
	 * 
	 * @since Twitter4J 2.1.7
	 */
	static <T> T registerJSONObject(final T key, final Object json) {
		rawJsonMap.get().put(key, json);
		return key;
	}
}