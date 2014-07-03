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

import static twitter4j.internal.util.InternalParseUtil.getDate;
import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getInt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import java.util.Date;

/**
 * A data class representing a Saved Search
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.0.8
 */
/* package */final class SavedSearchJSONImpl extends TwitterResponseImpl implements SavedSearch {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3252481061731777994L;
	private Date createdAt;
	private String query;
	private int position;
	private String name;
	private int id;

	/* package */SavedSearchJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */SavedSearchJSONImpl(final JSONObject savedSearch) throws TwitterException {
		init(savedSearch);
	}

	@Override
	public int compareTo(final SavedSearch that) {
		return id - that.getId();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof SavedSearch)) return false;

		final SavedSearch that = (SavedSearch) o;

		if (id != that.getId()) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getId() {
		return id;
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
	public int getPosition() {
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public int hashCode() {
		int result = createdAt.hashCode();
		result = 31 * result + query.hashCode();
		result = 31 * result + position;
		result = 31 * result + name.hashCode();
		result = 31 * result + id;
		return result;
	}

	@Override
	public String toString() {
		return "SavedSearchJSONImpl{" + "createdAt=" + createdAt + ", query='" + query + '\'' + ", position="
				+ position + ", name='" + name + '\'' + ", id=" + id + '}';
	}

	private void init(final JSONObject savedSearch) throws TwitterException {
		createdAt = getDate("created_at", savedSearch, "EEE MMM dd HH:mm:ss z yyyy");
		query = getHTMLUnescapedString("query", savedSearch);
		position = getInt("position", savedSearch);
		name = getHTMLUnescapedString("name", savedSearch);
		id = getInt("id", savedSearch);
	}

	/* package */
	static ResponseList<SavedSearch> createSavedSearchList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		final JSONArray json = res.asJSONArray();
		ResponseList<SavedSearch> savedSearches;
		try {
			savedSearches = new ResponseListImpl<SavedSearch>(json.length(), res);
			for (int i = 0; i < json.length(); i++) {
				final JSONObject savedSearchesJSON = json.getJSONObject(i);
				final SavedSearch savedSearch = new SavedSearchJSONImpl(savedSearchesJSON);
				savedSearches.add(savedSearch);
			}
			return savedSearches;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + res.asString(), jsone);
		}
	}
}
