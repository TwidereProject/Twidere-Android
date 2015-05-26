/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.RestHttpResponse;

import org.mariotaku.twidere.api.twitter.model.RateLimitStatus;

/**
 * A data class representing Twitter REST API's rate limit status
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
 * Twitter Developers</a>
 */
@JsonObject
public final class RateLimitStatusJSONImpl implements RateLimitStatus {

    private final long creationTimeInMillis;

    @JsonField(name = "remaining")
    int remaining;
    @JsonField(name = "limit")
    int limit;
    @JsonField(name = "reset")
    int resetTimeInSeconds;

    private RateLimitStatusJSONImpl(final int limit, final int remaining, final int resetTimeInSeconds) {
        this();
        this.limit = limit;
        this.remaining = remaining;
        this.resetTimeInSeconds = resetTimeInSeconds;
    }

    public RateLimitStatusJSONImpl() {
        creationTimeInMillis = System.currentTimeMillis();
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
        return (int) ((resetTimeInSeconds * 1000L - creationTimeInMillis) / 1000);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RateLimitStatusJSONImpl that = (RateLimitStatusJSONImpl) o;

        if (creationTimeInMillis != that.creationTimeInMillis) return false;
        if (remaining != that.remaining) return false;
        if (limit != that.limit) return false;
        return resetTimeInSeconds == that.resetTimeInSeconds;

    }

    @Override
    public int hashCode() {
        int result = (int) (creationTimeInMillis ^ (creationTimeInMillis >>> 32));
        result = 31 * result + remaining;
        result = 31 * result + limit;
        result = 31 * result + resetTimeInSeconds;
        return result;
    }

    @Override
    public String toString() {
        return "RateLimitStatusJSONImpl{" +
                "creationTimeInMillis=" + creationTimeInMillis +
                ", remaining=" + remaining +
                ", limit=" + limit +
                ", resetTimeInSeconds=" + resetTimeInSeconds +
                '}';
    }

    public static RateLimitStatus createFromResponseHeader(final RestHttpResponse res) {
        if (null == res) return null;
        int remainingHits;// "X-Rate-Limit-Remaining"
        int limit;// "X-Rate-Limit-Limit"
        int resetTimeInSeconds;// not included in the response header. Need to
        // be calculated.

        final String strLimit = res.getHeader("X-Rate-Limit-Limit");
        if (strLimit != null) {
            limit = Integer.parseInt(strLimit);
        } else
            return null;
        final String remaining = res.getHeader("X-Rate-Limit-Remaining");
        if (remaining != null) {
            remainingHits = Integer.parseInt(remaining);
        } else
            return null;
        final String reset = res.getHeader("X-Rate-Limit-Reset");
        if (reset != null) {
            final long longReset = Long.parseLong(reset);
            resetTimeInSeconds = (int) longReset;
        } else
            return null;
        return new RateLimitStatusJSONImpl(limit, remainingHits, resetTimeInSeconds);
    }

//	static Map<String, RateLimitStatus> createRateLimitStatuses(final HttpResponse res, final Configuration conf)
//			throws TwitterException {
//		final JSONObject json = res.asJSONObject();
//		final Map<String, RateLimitStatus> map = createRateLimitStatuses(json);
//		return map;
//	}
//
//	static Map<String, RateLimitStatus> createRateLimitStatuses(final InputStream stream) throws TwitterException {
//		final Map<String, RateLimitStatus> map = new HashMap<String, RateLimitStatus>();
//		try {
//			final JSONObject resources = json.getJSONObject("resources");
//			final Iterator<?> resourceKeys = resources.keys();
//			while (resourceKeys.hasNext()) {
//				final JSONObject resource = resources.getJSONObject((String) resourceKeys.next());
//				final Iterator<?> endpointKeys = resource.keys();
//				while (endpointKeys.hasNext()) {
//					final String endpoint = (String) endpointKeys.next();
//					final JSONObject rateLimitStatusJSON = resource.getJSONObject(endpoint);
//					final RateLimitStatus rateLimitStatus = new RateLimitStatusJSONImpl(rateLimitStatusJSON);
//					map.put(endpoint, rateLimitStatus);
//				}
//			}
//			return Collections.unmodifiableMap(map);
//		} catch (final JSONException jsone) {
//			throw new TwitterException(jsone);
//		}
//	}

}