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
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class LocationJSONImpl implements Location {
	private final int woeid;
	private final String countryName;
	private final String countryCode;
	private final String placeName;
	private final int placeCode;
	private final String name;
	private final String url;

	/* package */LocationJSONImpl(final JSONObject location) throws TwitterException {
		try {
			woeid = getInt("woeid", location);
			countryName = getHTMLUnescapedString("country", location);
			countryCode = getRawString("countryCode", location);
			if (!location.isNull("placeType")) {
				final JSONObject placeJSON = location.getJSONObject("placeType");
				placeName = getHTMLUnescapedString("name", placeJSON);
				placeCode = getInt("code", placeJSON);
			} else {
				placeName = null;
				placeCode = -1;
			}
			name = getHTMLUnescapedString("name", location);
			url = getHTMLUnescapedString("url", location);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof LocationJSONImpl)) return false;

		final LocationJSONImpl that = (LocationJSONImpl) o;

		if (woeid != that.woeid) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCountryName() {
		return countryName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPlaceCode() {
		return placeCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPlaceName() {
		return placeName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getURL() {
		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWoeid() {
		return woeid;
	}

	@Override
	public int hashCode() {
		return woeid;
	}

	@Override
	public String toString() {
		return "LocationJSONImpl{" + "woeid=" + woeid + ", countryName='" + countryName + '\'' + ", countryCode='"
				+ countryCode + '\'' + ", placeName='" + placeName + '\'' + ", placeCode='" + placeCode + '\''
				+ ", name='" + name + '\'' + ", url='" + url + '\'' + '}';
	}

	/* package */
	static ResponseList<Location> createLocationList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		return createLocationList(res.asJSONArray());
	}

	/* package */
	static ResponseList<Location> createLocationList(final JSONArray list) throws TwitterException {
		try {
			final int size = list.length();
			final ResponseList<Location> locations = new ResponseListImpl<Location>(size, null);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Location location = new LocationJSONImpl(json);
				locations.add(location);
			}
			return locations;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
