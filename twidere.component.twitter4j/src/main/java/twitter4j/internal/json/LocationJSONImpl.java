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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

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
