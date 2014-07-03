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

package twitter4j.internal.json;

import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import java.util.Arrays;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
final class PlaceJSONImpl extends TwitterResponseImpl implements Place {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2733342903542563480L;
	private String name;
	private String streetAddress;
	private String countryCode;
	private String id;
	private String country;
	private String placeType;
	private String url;
	private String fullName;
	private String boundingBoxType;
	private GeoLocation[][] boundingBoxCoordinates;
	private String geometryType;
	private GeoLocation[][] geometryCoordinates;
	private Place[] containedWithIn;

	/* For serialization purposes only. */
	PlaceJSONImpl() {

	}

	/* package */PlaceJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	PlaceJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	PlaceJSONImpl(final JSONObject json, final HttpResponse res) throws TwitterException {
		super(res);
		init(json);
	}

	@Override
	public int compareTo(final Place that) {
		return id.compareTo(that.getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof Place && ((Place) obj).getId().equals(id);
	}

	@Override
	public GeoLocation[][] getBoundingBoxCoordinates() {
		return boundingBoxCoordinates;
	}

	@Override
	public String getBoundingBoxType() {
		return boundingBoxType;
	}

	@Override
	public Place[] getContainedWithIn() {
		return containedWithIn;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public String getCountryCode() {
		return countryCode;
	}

	@Override
	public String getFullName() {
		return fullName;
	}

	@Override
	public GeoLocation[][] getGeometryCoordinates() {
		return geometryCoordinates;
	}

	@Override
	public String getGeometryType() {
		return geometryType;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPlaceType() {
		return placeType;
	}

	@Override
	public String getStreetAddress() {
		return streetAddress;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "PlaceJSONImpl{" + "name='" + name + '\'' + ", streetAddress='" + streetAddress + '\''
				+ ", countryCode='" + countryCode + '\'' + ", id='" + id + '\'' + ", country='" + country + '\''
				+ ", placeType='" + placeType + '\'' + ", url='" + url + '\'' + ", fullName='" + fullName + '\''
				+ ", boundingBoxType='" + boundingBoxType + '\'' + ", boundingBoxCoordinates="
				+ (boundingBoxCoordinates == null ? null : Arrays.asList(boundingBoxCoordinates)) + ", geometryType='"
				+ geometryType + '\'' + ", geometryCoordinates="
				+ (geometryCoordinates == null ? null : Arrays.asList(geometryCoordinates)) + ", containedWithIn="
				+ (containedWithIn == null ? null : Arrays.asList(containedWithIn)) + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		try {
			name = getHTMLUnescapedString("name", json);
			streetAddress = getHTMLUnescapedString("street_address", json);
			countryCode = getRawString("country_code", json);
			id = getRawString("id", json);
			country = getRawString("country", json);
			if (!json.isNull("place_type")) {
				placeType = getRawString("place_type", json);
			} else {
				placeType = getRawString("type", json);
			}
			url = getRawString("url", json);
			fullName = getRawString("full_name", json);
			if (!json.isNull("bounding_box")) {
				final JSONObject boundingBoxJSON = json.getJSONObject("bounding_box");
				boundingBoxType = getRawString("type", boundingBoxJSON);
				final JSONArray array = boundingBoxJSON.getJSONArray("coordinates");
				boundingBoxCoordinates = InternalJSONFactoryImpl.coordinatesAsGeoLocationArray(array);
			} else {
				boundingBoxType = null;
				boundingBoxCoordinates = null;
			}

			if (!json.isNull("geometry")) {
				final JSONObject geometryJSON = json.getJSONObject("geometry");
				geometryType = getRawString("type", geometryJSON);
				final JSONArray array = geometryJSON.getJSONArray("coordinates");
				if (geometryType.equals("Point")) {
					geometryCoordinates = new GeoLocation[1][1];
					geometryCoordinates[0][0] = new GeoLocation(array.getDouble(0), array.getDouble(1));
				} else if (geometryType.equals("Polygon")) {
					geometryCoordinates = InternalJSONFactoryImpl.coordinatesAsGeoLocationArray(array);
				} else {
					// MultiPolygon currently unsupported.
					geometryType = null;
					geometryCoordinates = null;
				}
			} else {
				geometryType = null;
				geometryCoordinates = null;
			}

			if (!json.isNull("contained_within")) {
				final JSONArray containedWithInJSON = json.getJSONArray("contained_within");
				containedWithIn = new Place[containedWithInJSON.length()];
				for (int i = 0; i < containedWithInJSON.length(); i++) {
					containedWithIn[i] = new PlaceJSONImpl(containedWithInJSON.getJSONObject(i));
				}
			} else {
				containedWithIn = null;
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */
	static ResponseList<Place> createPlaceList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		JSONObject json = null;
		try {
			json = res.asJSONObject();
			return createPlaceList(json.getJSONObject("result").getJSONArray("places"), res, conf);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */
	static ResponseList<Place> createPlaceList(final JSONArray list, final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final int size = list.length();
			final ResponseList<Place> places = new ResponseListImpl<Place>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Place place = new PlaceJSONImpl(json);
				places.add(place);
			}
			return places;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
