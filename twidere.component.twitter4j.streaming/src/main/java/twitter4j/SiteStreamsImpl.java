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

package twitter4j;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.conf.StreamConfiguration;
import twitter4j.http.HttpResponse;
import twitter4j.internal.async.Dispatcher;
import twitter4j.internal.util.InternalParseUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
class SiteStreamsImpl extends StatusStreamBase {

	private final StreamController cs;

	private static ThreadLocal<Integer> forUser = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};

	protected static final RawStreamListener[] EMPTY = new RawStreamListener[0];

	/* package */SiteStreamsImpl(final Dispatcher dispatcher, final HttpResponse response,
			final StreamConfiguration conf, final StreamController cs) throws IOException {
		super(dispatcher, response, conf);
		this.cs = cs;
	}

	/* package */SiteStreamsImpl(final Dispatcher dispatcher, final InputStream stream, final StreamConfiguration conf,
			final StreamController cs) throws IOException {
		super(dispatcher, stream, conf);
		this.cs = cs;
	}

	@Override
	public void next(final StatusListener listener) throws TwitterException {
		handleNextElement(new StatusListener[] { listener }, EMPTY);
	}

	@Override
	public void next(final StreamListener[] listeners, final RawStreamListener[] rawStreamListeners)
			throws TwitterException {
		handleNextElement(listeners, rawStreamListeners);
	}

	@Override
	public void onDisconnectionNotice(final String line, final StreamListener[] listeners) {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onDisconnectionNotice(line);
		}
	}

	@Override
	public void onException(final Exception ex, final StreamListener[] listeners) {
		for (final StreamListener listener : listeners) {
			listener.onException(ex);
		}
	}

	@Override
	protected void onBlock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onBlock(forUser.get(), asUser(source), asUser(target));
		}
	}

	@Override
	protected void onDelete(final JSONObject json, final StreamListener[] listeners) throws JSONException {
		final JSONObject deletionNotice = json.getJSONObject("delete");
		if (deletionNotice.has("status")) {
			for (final StreamListener listener : listeners) {
				((SiteStreamsListener) listener).onDeletionNotice(forUser.get(), new StatusDeletionNoticeImpl(
						deletionNotice.getJSONObject("status")));
			}
		} else {
			final JSONObject directMessage = deletionNotice.getJSONObject("direct_message");
			for (final StreamListener listener : listeners) {
				((SiteStreamsListener) listener).onDeletionNotice(forUser.get(),
						InternalParseUtil.getInt("id", directMessage),
						InternalParseUtil.getLong("user_id", directMessage));
			}
		}
	}

	@Override
	protected void onDirectMessage(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onDirectMessage(forUser.get(), asDirectMessage(json));
		}
	}

	@Override
	protected void onFavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onFavorite(forUser.get(), asUser(source), asUser(target),
					asStatus(targetObject));
		}
	}

	@Override
	protected void onFollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onFollow(forUser.get(), asUser(source), asUser(target));
		}
	}

	@Override
	protected void onFriends(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onFriendList(forUser.get(), asFriendList(json));
		}
	}

	@Override
	protected void onMessage(final String rawString, final RawStreamListener[] listeners) throws TwitterException {
		for (final RawStreamListener listener : listeners) {
			listener.onMessage(rawString);
		}
	}

	@Override
	protected void onStatus(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onStatus(forUser.get(), asStatus(json));
		}
	}

	@Override
	protected void onUnblock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUnblock(forUser.get(), asUser(source), asUser(target));
		}
	}

	@Override
	protected void onUnfavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUnfavorite(forUser.get(), asUser(source), asUser(target),
					asStatus(targetObject));
		}
	}

	@Override
	protected void onUnfollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUnfollow(forUser.get(), asUser(source), asUser(target));
		}
	}

	@Override
	protected void onUserListCreation(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListCreation(forUser.get(), asUser(source), asUserList(userList));
		}
	}

	@Override
	protected void onUserListDestroyed(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListDeletion(forUser.get(), asUser(source), asUserList(userList));
		}
	}

	@Override
	protected void onUserListMemberAddition(final JSONObject addedMember, final JSONObject owner,
			final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListMemberAddition(forUser.get(), asUser(addedMember),
					asUser(owner), asUserList(userList));
		}
	}

	@Override
	protected void onUserListMemberDeletion(final JSONObject deletedMember, final JSONObject owner,
			final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListMemberDeletion(forUser.get(), asUser(deletedMember),
					asUser(owner), asUserList(userList));
		}
	}

	@Override
	protected void onUserListSubscription(final JSONObject source, final JSONObject owner, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListSubscription(forUser.get(), asUser(source), asUser(owner),
					asUserList(userList));
		}
	}

	@Override
	protected void onUserListUnsubscription(final JSONObject source, final JSONObject owner, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListUnsubscription(forUser.get(), asUser(source), asUser(owner),
					asUserList(userList));
		}
	}

	@Override
	protected void onUserListUpdated(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserListUpdate(forUser.get(), asUser(source), asUserList(userList));
		}
	}

	@Override
	protected void onUserUpdate(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((SiteStreamsListener) listener).onUserProfileUpdate(forUser.get(), asUser(source));
		}
	}

	@Override
	protected String parseLine(final String line) {
		if ("".equals(line) || null == line) return line;
		final int userIdEnd = line.indexOf(',', 12);
		// in the documentation for_user is not quoted, but actually it is
		// quoted
		if (cs.getControlURI() == null && line.charAt(2) == 'c' && line.charAt(3) == 'o' && line.charAt(4) == 'n') {
			// control endpoint uri
			// https://dev.twitter.com/docs/streaming-api/control-streams
			JSONObject control = null;
			try {
				control = new JSONObject(line);
				cs.setControlURI(CONF.getSiteStreamBaseURL()
						+ control.getJSONObject("control").getString("control_uri"));
				logger.info("control_uri: " + cs.getControlURI());
			} catch (final JSONException e) {
				logger.warn("received unexpected event:" + line);
			}
			return null;
		}

		if (line.charAt(2) == 'd') // disconnection notice
			// {"disconnect":{"code":3,"stream_name":"yusuke-sitestream6139-yusuke","reason":"control request for yusuke-sitestream6139 106.171.17.29 /1.1/site.json sitestream"}}
			return line;
		if (line.charAt(12) == '"') {
			forUser.set(Integer.parseInt(line.substring(13, userIdEnd - 1)));
		} else {
			forUser.set(Integer.parseInt(line.substring(12, userIdEnd)));
		}
		return line.substring(userIdEnd + 11, line.length() - 1);
	}

}