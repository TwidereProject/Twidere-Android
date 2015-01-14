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

import java.net.URI;
import java.net.URISyntaxException;

import twitter4j.PageableResponseList;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getBoolean;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

/**
 * A data class representing Basic list information element
 * 
 * @author Dan Checkoway - dcheckoway at gmail.com
 */
/* package */class UserListJSONImpl extends TwitterResponseImpl implements UserList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2682622238509440140L;
	private long id;
	private String name;
	private String fullName;
	private String slug;
	private String description;
	private int subscriberCount;
	private int memberCount;
	private String uri;
	private boolean mode;
	private User user;
	private boolean following;

	/* package */UserListJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		init(res.asJSONObject());
	}

	/* package */UserListJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public int compareTo(final UserList that) {
		return (int) (id - that.getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof UserListJSONImpl)) return false;
		final UserListJSONImpl other = (UserListJSONImpl) obj;
		if (id != other.id) return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFullName() {
		return fullName;
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
	public int getMemberCount() {
		return memberCount;
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
	public String getSlug() {
		return slug;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSubscriberCount() {
		return subscriberCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI getURI() {
		try {
			return new URI(uri);
		} catch (final URISyntaxException ex) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser() {
		return user;
	}

	/* package */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFollowing() {
		return following;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPublic() {
		return mode;
	}

	@Override
	public String toString() {
		return "UserListJSONImpl{" + "id=" + id + ", name='" + name + '\'' + ", fullName='" + fullName + '\''
				+ ", slug='" + slug + '\'' + ", description='" + description + '\'' + ", subscriberCount="
				+ subscriberCount + ", memberCount=" + memberCount + ", uri='" + uri + '\'' + ", mode=" + mode
				+ ", user=" + user + ", following=" + following + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		id = getLong("id", json);
		name = getRawString("name", json);
		fullName = getRawString("full_name", json);
		slug = getRawString("slug", json);
		description = getRawString("description", json);
		subscriberCount = getInt("subscriber_count", json);
		memberCount = getInt("member_count", json);
		uri = getRawString("uri", json);
		mode = "public".equals(getRawString("mode", json));
		following = getBoolean("following", json);

		try {
			if (!json.isNull("user")) {
				user = new UserJSONImpl(json.getJSONObject("user"));
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	static PageableResponseList<UserList> createPagableUserListList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONObject json = res.asJSONObject();
			final JSONArray list = json.getJSONArray("lists");
			final int size = list.length();
			@SuppressWarnings("unchecked")
			final PageableResponseList<UserList> users = new PageableResponseListImpl<UserList>(size, json, res);
			for (int i = 0; i < size; i++) {
				final JSONObject userListJson = list.getJSONObject(i);
				final UserList userList = new UserListJSONImpl(userListJson);
				users.add(userList);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}

	/* package */
	static ResponseList<UserList> createUserListList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<UserList> users = new ResponseListImpl<UserList>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject userListJson = list.getJSONObject(i);
				final UserList userList = new UserListJSONImpl(userListJson);
				users.add(userList);
			}
			return users;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
