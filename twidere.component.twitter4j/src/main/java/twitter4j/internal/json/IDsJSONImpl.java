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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import twitter4j.IDs;
import twitter4j.TwitterException;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

/**
 * A data class representing array of numeric IDs.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class IDsJSONImpl extends TwitterResponseImpl implements IDs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 443834529674409001L;
	private long[] ids;
	private long previousCursor = -1;
	private long nextCursor = -1;

	/* package */IDsJSONImpl(final HttpResponse res) throws TwitterException {
		super(res);
		final String json = res.asString();
		init(json);
	}

	/* package */IDsJSONImpl(final String json) throws TwitterException {
		init(json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof IDs)) return false;

		final IDs iDs = (IDs) o;

		if (!Arrays.equals(ids, iDs.getIDs())) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] getIDs() {
		return ids;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPreviousCursor() {
		return previousCursor;
	}

	@Override
	public int hashCode() {
		return ids != null ? Arrays.hashCode(ids) : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return 0 != nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasPrevious() {
		return 0 != previousCursor;
	}

	@Override
	public String toString() {
		return "IDsJSONImpl{" + "ids=" + Arrays.toString(ids) + ", previousCursor=" + previousCursor + ", nextCursor="
				+ nextCursor + '}';
	}

	private void init(final String jsonStr) throws TwitterException {
		JSONArray idList;
		try {
			if (jsonStr.startsWith("{")) {
				final JSONObject json = new JSONObject(jsonStr);
				idList = json.getJSONArray("ids");
				ids = new long[idList.length()];
				for (int i = 0; i < idList.length(); i++) {
					try {
						ids[i] = Long.parseLong(idList.getString(i));
					} catch (final NumberFormatException nfe) {
						throw new TwitterException("Twitter API returned malformed response: " + json, nfe);
					}
				}
				previousCursor = InternalParseUtil.getLong("previous_cursor", json);
				nextCursor = InternalParseUtil.getLong("next_cursor", json);
			} else {
				idList = new JSONArray(jsonStr);
				ids = new long[idList.length()];
				for (int i = 0; i < idList.length(); i++) {
					try {
						ids[i] = Long.parseLong(idList.getString(i));
					} catch (final NumberFormatException nfe) {
						throw new TwitterException("Twitter API returned malformed response: " + idList, nfe);
					}
				}
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}
}
