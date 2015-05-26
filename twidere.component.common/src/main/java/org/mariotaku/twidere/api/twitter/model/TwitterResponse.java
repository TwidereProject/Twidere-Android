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

import org.mariotaku.restfu.http.RestHttpResponse;

/**
 * Super interface of Twitter Response data interfaces which indicates that rate
 * limit status is avaialble.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see DirectMessage
 * @see Status
 * @see User
 */
public interface TwitterResponse  {
	int NONE = 0;

	int READ = 1;

	int READ_WRITE = 2;
	int READ_WRITE_DIRECTMESSAGES = 3;

	void processResponseHeader(RestHttpResponse resp);

	int getAccessLevel();

	RateLimitStatus getRateLimitStatus();

}
