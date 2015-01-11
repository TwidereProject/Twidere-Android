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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;
import twitter4j.internal.util.InternalParseUtil;

/**
 * A data interface representing one single user mention entity.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
/* package */class UserMentionEntityJSONImpl implements UserMentionEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 205671795665637553L;
	private int start = -1;
	private int end = -1;
	private String name;
	private String screenName;
	private long id;

	/* For serialization purposes only. */
	/* package */UserMentionEntityJSONImpl() {

	}

	/* package */UserMentionEntityJSONImpl(final int start, final int end, final String name, final String screenName,
			final long id) {
		super();
		this.start = start;
		this.end = end;
		this.name = name;
		this.screenName = screenName;
		this.id = id;
	}

	/* package */UserMentionEntityJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final UserMentionEntityJSONImpl that = (UserMentionEntityJSONImpl) o;

		if (end != that.end) return false;
		if (id != that.id) return false;
		if (start != that.start) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (screenName != null ? !screenName.equals(that.screenName) : that.screenName != null) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEnd() {
		return end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
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
	public String getScreenName() {
		return screenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int hashCode() {
		int result = start;
		result = 31 * result + end;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
		result = 31 * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "UserMentionEntityJSONImpl{" + "start=" + start + ", end=" + end + ", name='" + name + '\''
				+ ", screenName='" + screenName + '\'' + ", id=" + id + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		try {
			final JSONArray indicesArray = json.getJSONArray("indices");
			start = indicesArray.getInt(0);
			end = indicesArray.getInt(1);

			if (!json.isNull("name")) {
				name = json.getString("name");
			}
			if (!json.isNull("screen_name")) {
				screenName = json.getString("screen_name");
			}
			id = InternalParseUtil.getLong("id", json);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}
}
