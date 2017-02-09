/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.HttpResponse;

/**
 * A data class representing Twitter REST API's rate limit status
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
 * Twitter Developers</a>
 */
@JsonObject
public final class RateLimitStatus {

    private final long creationTimeInMillis;

    @JsonField(name = "remaining")
    int remaining;
    @JsonField(name = "limit")
    int limit;
    @JsonField(name = "reset")
    int resetTimeInSeconds;

    private RateLimitStatus(final int limit, final int remaining, final int resetTimeInSeconds) {
        this();
        this.limit = limit;
        this.remaining = remaining;
        this.resetTimeInSeconds = resetTimeInSeconds;
    }

    public RateLimitStatus() {
        creationTimeInMillis = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    public int getLimit() {
        return limit;
    }

    /**
     * {@inheritDoc}
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * {@inheritDoc}
     */
    public int getRemainingHits() {
        return getRemaining();
    }

    /**
     * {@inheritDoc}
     */
    public int getResetTimeInSeconds() {
        return resetTimeInSeconds;
    }

    /**
     * {@inheritDoc}
     */
    public int getSecondsUntilReset() {
        return (int) ((resetTimeInSeconds * 1000L - creationTimeInMillis) / 1000);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RateLimitStatus that = (RateLimitStatus) o;

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
        return "RateLimitStatus{" +
                "creationTimeInMillis=" + creationTimeInMillis +
                ", remaining=" + remaining +
                ", limit=" + limit +
                ", resetTimeInSeconds=" + resetTimeInSeconds +
                '}';
    }

    public static RateLimitStatus createFromResponseHeader(final HttpResponse res) {
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
        return new RateLimitStatus(limit, remainingHits, resetTimeInSeconds);
    }

}