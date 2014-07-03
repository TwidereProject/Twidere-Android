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

import static twitter4j.internal.util.InternalParseUtil.getLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Friendship;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.9
 */
class FriendshipJSONImpl implements Friendship {

	private final long id;
	private final String name;
	private final String screenName;
	private boolean following = false;
	private boolean followedBy = false;

	/* package */FriendshipJSONImpl(final JSONObject json) throws TwitterException {
		super();
		try {
			id = getLong("id", json);
			name = json.getString("name");
			screenName = json.getString("screen_name");
			final JSONArray connections = json.getJSONArray("connections");
			for (int i = 0; i < connections.length(); i++) {
				final String connection = connections.getString(i);
				if ("following".equals(connection)) {
					following = true;
				} else if ("followed_by".equals(connection)) {
					followedBy = true;
				}
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final FriendshipJSONImpl that = (FriendshipJSONImpl) o;

		if (followedBy != that.followedBy) return false;
		if (following != that.following) return false;
		if (id != that.id) return false;
		if (!name.equals(that.name)) return false;
		if (!screenName.equals(that.screenName)) return false;

		return true;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getScreenName() {
		return screenName;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ id >>> 32);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
		result = 31 * result + (following ? 1 : 0);
		result = 31 * result + (followedBy ? 1 : 0);
		return result;
	}

	@Override
	public boolean isFollowedBy() {
		return followedBy;
	}

	@Override
	public boolean isFollowing() {
		return following;
	}

	@Override
	public String toString() {
		return "FriendshipJSONImpl{" + "id=" + id + ", name='" + name + '\'' + ", screenName='" + screenName + '\''
				+ ", following=" + following + ", followedBy=" + followedBy + '}';
	}

	/* package */
	static ResponseList<Friendship> createFriendshipList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<Friendship> friendshipList = new ResponseListImpl<Friendship>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final Friendship friendship = new FriendshipJSONImpl(json);
				friendshipList.add(friendship);
			}
			return friendshipList;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
