/*
 * Copyright 2007 Yusuke Yamamoto
 * Copyright (C) 2012 Twitter, Inc.
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
package twitter4j;

import static twitter4j.internal.util.InternalParseUtil.getBoolean;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

import java.io.Serializable;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;
import twitter4j.internal.util.InternalStringUtil;

/**
 * @author Yusuke Yamamoto - yusuke at twitter.com
 * @since Twitter4J 2.2.6
 */
public class StreamController {
	private String controlURI = null;
	private final HttpClientWrapper HTTP;
	private final Authorization AUTH;

	Object lock = new Object();

	/* package */StreamController(final Configuration conf) {
		HTTP = new HttpClientWrapper(conf);
		AUTH = AuthorizationFactory.getInstance(conf);
	}

	/* package */StreamController(final HttpClientWrapper http, final Authorization auth) {
		HTTP = http;
		AUTH = auth;
	}

	public String addUsers(final long[] userIds) throws TwitterException {
		ensureControlURISet();
		final HttpParameter param = new HttpParameter("user_id", InternalStringUtil.join(userIds));
		final HttpResponse res = HTTP.post(controlURI + "/add_user.json", controlURI + "/add_user.json",
				new HttpParameter[] { param }, AUTH);
		return res.asString();
	}

	public FriendsIDs getFriendsIDs(final long userId, final long cursor) throws TwitterException {
		ensureControlURISet();
		final HttpResponse res = HTTP
				.post(controlURI + "/friends/ids.json", controlURI + "/friends/ids.json", new HttpParameter[] {
						new HttpParameter("user_id", userId), new HttpParameter("cursor", cursor) }, AUTH);
		return new FriendsIDs(res);
	}

	public ControlStreamInfo getInfo() throws TwitterException {
		ensureControlURISet();
		final HttpResponse res = HTTP.get(controlURI + "/info.json", controlURI + "/info.json", AUTH);
		return new ControlStreamInfo(this, res.asJSONObject());
	}

	public String removeUsers(final long[] userIds) throws TwitterException {
		ensureControlURISet();
		final HttpParameter param = new HttpParameter("user_id", InternalStringUtil.join(userIds));
		final HttpResponse res = HTTP.post(controlURI + "/remove_user.json", controlURI + "/remove_user.json",
				new HttpParameter[] { param }, AUTH);
		return res.asString();
	}

	/* package */User createUser(final JSONObject json) {
		return new User(json);
	}

	void ensureControlURISet() throws TwitterException {
		synchronized (lock) {
			try {
				while (controlURI == null) {
					lock.wait(30000);
					throw new TwitterException("timed out for control uri to be ready");
				}
			} catch (final InterruptedException e) {
			}
		}
	}

	String getControlURI() {
		return controlURI;
	}

	void setControlURI(final String controlURI) {
		this.controlURI = controlURI.replace("/1.1//1.1/", "/1.1/");
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public final class FriendsIDs implements CursorSupport, Serializable {
		private static final long serialVersionUID = -6282978710522199102L;
		private long[] ids;
		private long previousCursor = -1;
		private long nextCursor = -1;
		private User user;

		/* package */FriendsIDs(final HttpResponse res) throws TwitterException {
			init(res.asJSONObject());
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final FriendsIDs that = (FriendsIDs) o;

			if (nextCursor != that.nextCursor) return false;
			if (previousCursor != that.previousCursor) return false;
			if (!Arrays.equals(ids, that.ids)) return false;
			if (user != null ? !user.equals(that.user) : that.user != null) return false;

			return true;
		}

		public long[] getIds() {
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

		public User getUser() {
			return user;
		}

		@Override
		public int hashCode() {
			int result = ids != null ? Arrays.hashCode(ids) : 0;
			result = 31 * result + (int) (previousCursor ^ previousCursor >>> 32);
			result = 31 * result + (int) (nextCursor ^ nextCursor >>> 32);
			result = 31 * result + (user != null ? user.hashCode() : 0);
			return result;
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
			return "FriendsIDs{" + "ids=" + ids + ", previousCursor=" + previousCursor + ", nextCursor=" + nextCursor
					+ ", user=" + user + '}';
		}

		private void init(final JSONObject json) throws TwitterException {
			try {
				final JSONObject follow = json.getJSONObject("follow");
				final JSONArray idList = follow.getJSONArray("friends");
				ids = new long[idList.length()];
				for (int i = 0; i < idList.length(); i++) {
					try {
						ids[i] = Long.parseLong(idList.getString(i));
					} catch (final NumberFormatException nfe) {
						throw new TwitterException("Twitter API returned malformed response: " + json, nfe);
					}
				}
				user = new User(follow.getJSONObject("user"));
				previousCursor = InternalParseUtil.getLong("previous_cursor", json);
				nextCursor = InternalParseUtil.getLong("next_cursor", json);
			} catch (final JSONException jsone) {
				throw new TwitterException(jsone);
			}
		}
	}

	public final class User implements Serializable {
		private static final long serialVersionUID = -2925833063500478073L;
		private final long id;
		private final String name;
		private final boolean dm;

		/* package */User(final JSONObject json) {
			id = getLong("id", json);
			name = getRawString("name", json);
			dm = getBoolean("dm", json);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final User user = (User) o;

			if (dm != user.dm) return false;
			if (id != user.id) return false;
			if (name != null ? !name.equals(user.name) : user.name != null) return false;

			return true;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			int result = (int) (id ^ id >>> 32);
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + (dm ? 1 : 0);
			return result;
		}

		public boolean isDMAccessible() {
			return dm;
		}

		@Override
		public String toString() {
			return "User{" + "id=" + id + ", name='" + name + '\'' + ", dm=" + dm + '}';
		}
	}
}
