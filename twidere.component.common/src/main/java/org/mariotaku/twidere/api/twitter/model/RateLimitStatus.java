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

package org.mariotaku.twidere.api.twitter.model;

/**
 * A data interface representing Twitter REST API's rate limit status
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
 *      Twitter Developers</a>
 */
public interface RateLimitStatus {

	int getLimit();

	int getRemaining();

	/**
	 * Returns the remaining number of API requests available.<br>
	 * This value is identical to the &quot;X-RateLimit-Remaining&quot; response
	 * header.
	 * 
	 * @return the remaining number of API requests available
	 */
	int getRemainingHits();

	/**
	 * Returns the seconds the current rate limiting period ends.<br>
	 * This should be a same as getResetTime().getTime()/1000.
	 * 
	 * @return the seconds the current rate limiting period ends
	 * @since Twitter4J 2.0.9
	 */
	int getResetTimeInSeconds();

	/**
	 * Returns the amount of seconds until the current rate limiting period
	 * ends.<br>
	 * This is a value provided/calculated only by Twitter4J for handiness and
	 * not a part of the twitter API spec.
	 * 
	 * @return the amount of seconds until next rate limiting period
	 * @since Twitter4J 2.1.0
	 */
	int getSecondsUntilReset();

}
