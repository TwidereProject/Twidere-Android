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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getInt;

/**
 * A data class representing Twitter REST API's rate limit status
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
 *      Twitter Developers</a>
 */
/* package */final class RateLimitStatusJSONImpl implements RateLimitStatus, java.io.Serializable {

	private static final long serialVersionUID = 1625565652687304084L;
	private int remaining;
	private int limit;
	private int resetTimeInSeconds;
	private int secondsUntilReset;

	private RateLimitStatusJSONImpl(final int limit, final int remaining, final int resetTimeInSeconds) {
		this.limit = limit;
		this.remaining = remaining;
		this.resetTimeInSeconds = resetTimeInSeconds;
		secondsUntilReset = (int) ((resetTimeInSeconds * 1000L - System.currentTimeMillis()) / 1000);
	}

	RateLimitStatusJSONImpl(final JSONObject json) throws TwitterException {
		init(json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final RateLimitStatusJSONImpl that = (RateLimitStatusJSONImpl) o;

		if (limit != that.limit) return false;
		if (remaining != that.remaining) return false;
		if (resetTimeInSeconds != that.resetTimeInSeconds) return false;
		if (secondsUntilReset != that.secondsUntilReset) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLimit() {
		return limit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRemaining() {
		return remaining;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRemainingHits() {
		return getRemaining();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResetTimeInSeconds() {
		return resetTimeInSeconds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSecondsUntilReset() {
		return secondsUntilReset;
	}

	@Override
	public int hashCode() {
		int result = remaining;
		result = 31 * result + limit;
		result = 31 * result + resetTimeInSeconds;
		result = 31 * result + secondsUntilReset;
		return result;
	}

	@Override
	public String toString() {
		return "RateLimitStatusJSONImpl{" + "remaining=" + remaining + ", limit=" + limit + ", resetTimeInSeconds="
				+ resetTimeInSeconds + ", secondsUntilReset=" + secondsUntilReset + '}';
	}

	void init(final JSONObject json) throws TwitterException {
		limit = getInt("limit", json);
		remaining = getInt("remaining", json);
		resetTimeInSeconds = getInt("reset", json);
		secondsUntilReset = (int) ((resetTimeInSeconds * 1000L - System.currentTimeMillis()) / 1000);
	}

	static RateLimitStatus createFromResponseHeader(final HttpResponse res) {
		if (null == res) return null;
		int remainingHits;// "X-Rate-Limit-Remaining"
		int limit;// "X-Rate-Limit-Limit"
		int resetTimeInSeconds;// not included in the response header. Need to
								// be calculated.

		final String strLimit = res.getResponseHeader("X-Rate-Limit-Limit");
		if (strLimit != null) {
			limit = Integer.parseInt(strLimit);
		} else
			return null;
		final String remaining = res.getResponseHeader("X-Rate-Limit-Remaining");
		if (remaining != null) {
			remainingHits = Integer.parseInt(remaining);
		} else
			return null;
		final String reset = res.getResponseHeader("X-Rate-Limit-Reset");
		if (reset != null) {
			final long longReset = Long.parseLong(reset);
			resetTimeInSeconds = (int) longReset;
		} else
			return null;
		return new RateLimitStatusJSONImpl(limit, remainingHits, resetTimeInSeconds);
	}

	static Map<String, RateLimitStatus> createRateLimitStatuses(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		final JSONObject json = res.asJSONObject();
		final Map<String, RateLimitStatus> map = createRateLimitStatuses(json);
		return map;
	}

	static Map<String, RateLimitStatus> createRateLimitStatuses(final JSONObject json) throws TwitterException {
		final Map<String, RateLimitStatus> map = new HashMap<String, RateLimitStatus>();
		try {
			final JSONObject resources = json.getJSONObject("resources");
			final Iterator<?> resourceKeys = resources.keys();
			while (resourceKeys.hasNext()) {
				final JSONObject resource = resources.getJSONObject((String) resourceKeys.next());
				final Iterator<?> endpointKeys = resource.keys();
				while (endpointKeys.hasNext()) {
					final String endpoint = (String) endpointKeys.next();
					final JSONObject rateLimitStatusJSON = resource.getJSONObject(endpoint);
					final RateLimitStatus rateLimitStatus = new RateLimitStatusJSONImpl(rateLimitStatusJSON);
					map.put(endpoint, rateLimitStatus);
				}
			}
			return Collections.unmodifiableMap(map);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

}
