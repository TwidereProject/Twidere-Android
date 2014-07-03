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

import static twitter4j.internal.util.InternalParseUtil.getInt;

import org.json.JSONObject;

import twitter4j.AccountTotals;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
class AccountTotalsJSONImpl extends TwitterResponseImpl implements AccountTotals {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1766976816658998807L;
	private final int updates;
	private final int followers;
	private final int favorites;
	private final int friends;

	private AccountTotalsJSONImpl(final HttpResponse res, final JSONObject json) {
		super(res);
		updates = getInt("updates", json);
		followers = getInt("followers", json);
		favorites = getInt("favorites", json);
		friends = getInt("friends", json);
	}

	/* package */AccountTotalsJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		this(res, res.asJSONObject());
	}

	/* package */AccountTotalsJSONImpl(final JSONObject json) throws TwitterException {
		this(null, json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final AccountTotalsJSONImpl that = (AccountTotalsJSONImpl) o;

		if (favorites != that.favorites) return false;
		if (followers != that.followers) return false;
		if (friends != that.friends) return false;
		if (updates != that.updates) return false;

		return true;
	}

	@Override
	public int getFavorites() {
		return favorites;
	}

	@Override
	public int getFollowers() {
		return followers;
	}

	@Override
	public int getFriends() {
		return friends;
	}

	@Override
	public int getUpdates() {
		return updates;
	}

	@Override
	public int hashCode() {
		int result = updates;
		result = 31 * result + followers;
		result = 31 * result + favorites;
		result = 31 * result + friends;
		return result;
	}

	@Override
	public String toString() {
		return "AccountTotalsJSONImpl{" + "updates=" + updates + ", followers=" + followers + ", favorites="
				+ favorites + ", friends=" + friends + '}';
	}
}
