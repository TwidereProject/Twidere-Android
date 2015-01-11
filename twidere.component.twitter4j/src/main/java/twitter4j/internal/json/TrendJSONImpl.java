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

import twitter4j.Trend;

import static twitter4j.internal.util.InternalParseUtil.getRawString;

/**
 * A data class representing Trend.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.0.2
 */
/* package */final class TrendJSONImpl implements Trend {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2205410210175259078L;
	private final String name;
	private String url = null;
	private String query = null;

	/* package */TrendJSONImpl(final JSONObject json) {
		name = getRawString("name", json);
		url = getRawString("url", json);
		query = getRawString("query", json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Trend)) return false;

		final Trend trend = (Trend) o;

		if (!name.equals(trend.getName())) return false;
		if (query != null ? !query.equals(trend.getQuery()) : trend.getQuery() != null) return false;
		if (url != null ? !url.equals(trend.getUrl()) : trend.getUrl() != null) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (query != null ? query.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TrendJSONImpl{" + "name='" + name + '\'' + ", url='" + url + '\'' + ", query='" + query + '\'' + '}';
	}
}
