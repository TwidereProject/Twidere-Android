/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.internal.json;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterResponse;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

/**
 * Super interface of Twitter Response data interfaces which indicates that rate
 * limit status is available.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see twitter4j.DirectMessage
 * @see twitter4j.Status
 * @see twitter4j.User
 */
/* package */abstract class TwitterResponseImpl implements TwitterResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8016245009711538262L;

	private transient RateLimitStatus rateLimitStatus = null;

	private transient int accessLevel;

	public TwitterResponseImpl() {
		accessLevel = NONE;
	}

	public TwitterResponseImpl(final HttpResponse res) {
		rateLimitStatus = RateLimitStatusJSONImpl.createFromResponseHeader(res);
		accessLevel = InternalParseUtil.toAccessLevel(res);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return rateLimitStatus;
	}

}
