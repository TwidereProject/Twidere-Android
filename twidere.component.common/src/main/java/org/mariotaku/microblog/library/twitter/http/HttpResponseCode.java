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

package org.mariotaku.microblog.library.twitter.http;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public interface HttpResponseCode {
	/** OK: Success! **/
	int OK = 200;
	int CREATED = 201;
	int ACCEPTED = 202;
	int MULTIPLE_CHOICES = 300;//
	int FOUND = 302;//
	/** Not Modified: There was no new data to return. **/
	int NOT_MODIFIED = 304;
	/**
	 * Bad Request: The request was invalid. An accompanying error message will
	 * explain why. This is the status code will be returned during rate
	 * limiting.
	 **/
	int BAD_REQUEST = 400;
	/** Not Authorized: Authentication credentials were missing or incorrect. **/
	int UNAUTHORIZED = 401;
	/**
	 * Forbidden: The request is understood, but it has been refused. An
	 * accompanying error message will explain why.
	 **/
	int FORBIDDEN = 403;
	/**
	 * Not Found: The URI requested is invalid or the resource requested, such
	 * as a user, does not exists.
	 **/
	int NOT_FOUND = 404;
	/**
	 * Not Acceptable: Returned by the Search API when an invalid format is
	 * specified in the request.
	 **/
	int NOT_ACCEPTABLE = 406;
	/**
	 * Enhance Your Calm: Returned by the Search and Trends API when you are
	 * being rate limited. Not registered in RFC.
	 **/
	int ENHANCE_YOUR_CLAIM = 420;
	/**
	 * Returned when an image uploaded to POST account/update_profile_banner is
	 * unable to be processed.
	 **/
	int UNPROCESSABLE_ENTITY = 422;
	/**
	 * Returned in API v1.1 when a request cannot be served due to the
	 * application's rate limit having been exhausted for the resource. See Rate
	 * Limiting in API v1.1.
	 **/
	int TOO_MANY_REQUESTS = 429;
	/**
	 * Internal Server Error: Something is broken. Please post to the group so
	 * the Twitter team can investigate.
	 **/
	int INTERNAL_SERVER_ERROR = 500;
	/** Bad Gateway: Twitter is down or being upgraded. **/
	int BAD_GATEWAY = 502;
	/**
	 * Service Unavailable: The Twitter servers are up, but overloaded with
	 * requests. Try again later. The search and trend methods use this to
	 * indicate when you are being rate limited.
	 **/
	int SERVICE_UNAVAILABLE = 503;
	/**
	 * The Twitter servers are up, but the request couldn't be serviced due to
	 * some failure within our stack. Try again later.
	 **/
	int GATEWAY_TIMEOUT = 504;
}
