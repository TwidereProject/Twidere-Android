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

import java.util.ArrayList;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
class ResponseListImpl<T> extends ArrayList<T> implements ResponseList<T> {
	private static final long serialVersionUID = -7789068763212377625L;
	private transient RateLimitStatus rateLimitStatus = null;
	private transient int accessLevel;

	ResponseListImpl(final HttpResponse res) {
		super();
		init(res);
	}

	ResponseListImpl(final int size, final HttpResponse res) {
		super(size);
		init(res);
	}

	@Override
	public int getAccessLevel() {
		return accessLevel;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		return rateLimitStatus;
	}

	private void init(final HttpResponse res) {
		this.rateLimitStatus = RateLimitStatusJSONImpl.createFromResponseHeader(res);
		accessLevel = InternalParseUtil.toAccessLevel(res);
	}
}
