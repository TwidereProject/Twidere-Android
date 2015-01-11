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

import java.io.Serializable;

/**
 * Super interface of Twitter Response data interfaces which indicates that rate
 * limit status is avaialble.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see DirectMessage
 * @see twitter4j.Status
 * @see User
 */
public interface TwitterResponse extends Serializable {
	int NONE = 0;

	int READ = 1;

	int READ_WRITE = 2;
	int READ_WRITE_DIRECTMESSAGES = 3;

	/**
	 * @return application permission model
	 * @see <a
	 *      href="https://dev.twitter.com/pages/application-permission-model-faq#how-do-we-know-what-the-access-level-of-a-user-token-is">Application
	 *      Permission Model FAQ - How do we know what the access level of a
	 *      user token is?</a>
	 * @since Twitter4J 2.2.3
	 */
	int getAccessLevel();

	/**
	 * Returns the current rate limit status if available.
	 * 
	 * @return current rate limit status
	 * @since Twitter4J 2.1.0
	 */
	RateLimitStatus getRateLimitStatus();

}
