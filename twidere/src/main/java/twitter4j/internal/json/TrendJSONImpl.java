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

import static twitter4j.internal.util.InternalParseUtil.getRawString;

import org.json.JSONObject;

import twitter4j.Trend;

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
