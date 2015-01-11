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

import org.json.JSONObject;

import twitter4j.AccountTotals;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getInt;

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
