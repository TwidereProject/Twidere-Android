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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

import static twitter4j.internal.util.InternalParseUtil.getDate;

/**
 * A data class representing Trends.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.0.2
 */
/* package */final class TrendsJSONImpl extends TwitterResponseImpl implements Trends {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9046543905431036999L;
	private Date asOf;
	private Date trendAt;
	private Trend[] trends;
	private Location location;

	/* package */TrendsJSONImpl(final Date asOf, final Location location, final Date trendAt, final Trend[] trends) {
		this.asOf = asOf;
		this.location = location;
		this.trendAt = trendAt;
		this.trends = trends;
	}

	TrendsJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		init(res.asString());
	}

	TrendsJSONImpl(final String jsonStr) throws TwitterException {
		init(jsonStr);
	}

	@Override
	public int compareTo(final Trends that) {
		return trendAt.compareTo(that.getTrendAt());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Trends)) return false;

		final Trends trends1 = (Trends) o;

		if (asOf != null ? !asOf.equals(trends1.getAsOf()) : trends1.getAsOf() != null) return false;
		if (trendAt != null ? !trendAt.equals(trends1.getTrendAt()) : trends1.getTrendAt() != null) return false;
		if (!Arrays.equals(trends, trends1.getTrends())) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getAsOf() {
		return asOf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getTrendAt() {
		return trendAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Trend[] getTrends() {
		return trends;
	}

	@Override
	public int hashCode() {
		int result = asOf != null ? asOf.hashCode() : 0;
		result = 31 * result + (trendAt != null ? trendAt.hashCode() : 0);
		result = 31 * result + (trends != null ? Arrays.hashCode(trends) : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TrendsJSONImpl{" + "asOf=" + asOf + ", trendAt=" + trendAt + ", trends="
				+ (trends == null ? null : Arrays.asList(trends)) + '}';
	}

	void init(final String jsonStr) throws TwitterException {
		try {
			JSONObject json;
			if (jsonStr.startsWith("[")) {
				final JSONArray array = new JSONArray(jsonStr);
				if (array.length() > 0) {
					json = array.getJSONObject(0);
				} else
					throw new TwitterException("No trends found on the specified woeid");
			} else {
				json = new JSONObject(jsonStr);
			}
			asOf = InternalParseUtil.parseTrendsDate(json.getString("as_of"));
			location = extractLocation(json);
			final JSONArray array = json.getJSONArray("trends");
			trendAt = asOf;
			trends = jsonArrayToTrendArray(array);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage(), jsone);
		}
	}

	private static Location extractLocation(final JSONObject json) throws TwitterException {
		if (json.isNull("locations")) return null;
		ResponseList<Location> locations;
		try {
			locations = LocationJSONImpl.createLocationList(json.getJSONArray("locations"));
		} catch (final JSONException e) {
			throw new AssertionError("locations can't be null");
		}
		Location location;
		if (0 != locations.size()) {
			location = locations.get(0);
		} else {
			location = null;
		}
		return location;
	}

	private static Trend[] jsonArrayToTrendArray(final JSONArray array) throws JSONException {
		final Trend[] trends = new Trend[array.length()];
		for (int i = 0; i < array.length(); i++) {
			final JSONObject trend = array.getJSONObject(i);
			trends[i] = new TrendJSONImpl(trend);
		}
		return trends;
	}

	/* package */
	static ResponseList<Trends> createTrendsList(final HttpResponse res, final boolean storeJSON)
			throws TwitterException {
		final JSONObject json = res.asJSONObject();
		ResponseList<Trends> trends;
		try {
			final Date asOf = InternalParseUtil.parseTrendsDate(json.getString("as_of"));
			final JSONObject trendsJson = json.getJSONObject("trends");
			final Location location = extractLocation(json);
			trends = new ResponseListImpl<Trends>(trendsJson.length(), res);
			@SuppressWarnings("unchecked")
			final Iterator<String> ite = trendsJson.keys();
			while (ite.hasNext()) {
				final String key = ite.next();
				final JSONArray array = trendsJson.getJSONArray(key);
				final Trend[] trendsArray = jsonArrayToTrendArray(array);
				if (key.length() == 19) {
					// current trends
					trends.add(new TrendsJSONImpl(asOf, location, getDate(key, "yyyy-MM-dd HH:mm:ss"), trendsArray));
				} else if (key.length() == 16) {
					// daily trends
					trends.add(new TrendsJSONImpl(asOf, location, getDate(key, "yyyy-MM-dd HH:mm"), trendsArray));
				} else if (key.length() == 10) {
					// weekly trends
					trends.add(new TrendsJSONImpl(asOf, location, getDate(key, "yyyy-MM-dd"), trendsArray));
				}
			}
			Collections.sort(trends);
			return trends;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + res.asString(), jsone);
		}
	}
}
