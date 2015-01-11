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

package twitter4j;

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface TwitterBase {

	/**
	 * Registers a RateLimitStatusListener for account associated rate limits
	 * 
	 * @param listener the listener to be added
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.12
	 */
	void addRateLimitStatusListener(RateLimitStatusListener listener);

	/**
	 * Returns the authorization scheme for this instance.<br>
	 * The returned type will be either of BasicAuthorization,
	 * OAuthAuthorization, or NullAuthorization
	 * 
	 * @return the authorization scheme for this instance
	 */
	Authorization getAuthorization();

	/**
	 * Returns the configuration associated with this instance
	 * 
	 * @return configuration associated with this instance
	 * @since Twitter4J 2.1.8
	 */
	Configuration getConfiguration();

	/**
	 * Returns authenticating user's user id.<br>
	 * This method may internally call verifyCredentials() on the first
	 * invocation if<br>
	 * - this instance is authenticated by Basic and email address is supplied
	 * instead of screen name, or - this instance is authenticated by OAuth.<br>
	 * 
	 * @return the authenticating user's id
	 * @throws twitter4j.TwitterException when verifyCredentials threw an exception.
	 * @throws IllegalStateException if no credentials are supplied. i.e.) this
	 *             is an anonymous Twitter instance
	 * @since Twitter4J 2.1.1
	 */
	long getId() throws TwitterException, IllegalStateException;

	/**
	 * Returns authenticating user's screen name.<br>
	 * This method may internally call verifyCredentials() on the first
	 * invocation if<br>
	 * - this instance is authenticated by Basic and email address is supplied
	 * instead of screen name, or - this instance is authenticated by OAuth.<br>
	 * Note that this method returns a transiently cached (will be lost upon
	 * serialization) screen name while it is possible to change a user's screen
	 * name.<br>
	 *
	 * @return the authenticating screen name
	 * @throws twitter4j.TwitterException when verifyCredentials threw an exception.
	 * @throws IllegalStateException if no credentials are supplied. i.e.) this
	 *             is an anonymous Twitter instance
	 * @since Twitter4J 2.1.1
	 */
	String getScreenName() throws TwitterException, IllegalStateException;

	/**
	 * Shuts down this instance and releases allocated resources.
	 */
	void shutdown();
}
