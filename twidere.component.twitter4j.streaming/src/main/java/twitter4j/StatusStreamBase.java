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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.conf.StreamConfiguration;
import twitter4j.http.HttpResponse;
import twitter4j.internal.async.Dispatcher;
import twitter4j.internal.json.DataObjectFactoryUtil;
import twitter4j.internal.json.InternalJSONFactory;
import twitter4j.internal.json.InternalJSONFactoryImpl;
import twitter4j.internal.logging.Logger;
import twitter4j.json.JSONObjectType;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
abstract class StatusStreamBase implements StatusStream {
	protected static final Logger logger = Logger.getLogger(StatusStreamImpl.class);

	private boolean streamAlive = true;
	private final BufferedReader br;
	private final InputStream is;
	private HttpResponse response;
	protected final Dispatcher dispatcher;
	protected final StreamConfiguration CONF;
	protected InternalJSONFactory factory;

	/* package */

	StatusStreamBase(final Dispatcher dispatcher, final HttpResponse response, final StreamConfiguration conf)
			throws IOException {
		this(dispatcher, response.asStream(), conf);
		this.response = response;
	}

	StatusStreamBase(final Dispatcher dispatcher, final InputStream stream, final StreamConfiguration conf)
			throws IOException {
		is = stream;
		br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		this.dispatcher = dispatcher;
		CONF = conf;
		factory = new InternalJSONFactoryImpl(conf);
	}

	/* package */

	@Override
	public void close() throws IOException {
		streamAlive = false;
		is.close();
		br.close();
		if (response != null) {
			response.disconnect();
		}
	}

	@Override
	public abstract void next(StatusListener listener) throws TwitterException;

	public abstract void next(StreamListener[] listeners, RawStreamListener[] rawStreamListeners)
			throws TwitterException;

	public void onException(final Exception e, final StreamListener[] listeners,
			final RawStreamListener[] rawStreamListeners) {
		for (final StreamListener listener : listeners) {
			listener.onException(e);
		}
		for (final RawStreamListener listener : rawStreamListeners) {
			listener.onException(e);
		}
	}

	protected DirectMessage asDirectMessage(final JSONObject json) throws TwitterException {
		DirectMessage directMessage;
		try {
			directMessage = factory.createDirectMessage(json.getJSONObject("direct_message"));
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
		if (CONF.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(directMessage, json);
		}
		return directMessage;
	}

	protected long[] asFriendList(final JSONObject json) throws TwitterException {
		JSONArray friends;
		try {
			friends = json.getJSONArray("friends");
			final long[] friendIds = new long[friends.length()];
			for (int i = 0; i < friendIds.length; ++i) {
				friendIds[i] = Long.parseLong(friends.getString(i));
			}
			return friendIds;
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	protected Status asStatus(final JSONObject json) throws TwitterException {
		final Status status = factory.createStatus(json);
		if (CONF.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(status, json);
		}
		return status;
	}

	protected User asUser(final JSONObject json) throws TwitterException {
		final User user = factory.createUser(json);
		if (CONF.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(user, json);
		}
		return user;
	}

	protected UserList asUserList(final JSONObject json) throws TwitterException {
		final UserList userList = factory.createAUserList(json);
		if (CONF.isJSONStoreEnabled()) {
			DataObjectFactoryUtil.registerJSONObject(userList, json);
		}
		return userList;
	}

	protected void handleNextElement(final StreamListener[] listeners, final RawStreamListener[] rawStreamListeners)
			throws TwitterException {
		if (!streamAlive) throw new IllegalStateException("Stream already closed.");
		try {
			final String line = br.readLine();
			if (null == line) // invalidate this status stream
				throw new IOException("the end of the stream has been reached");
			dispatcher.invokeLater(new StreamEvent(line) {
				@Override
				public void run() {
					try {
						if (rawStreamListeners.length > 0) {
							onMessage(line, rawStreamListeners);
						}
						// SiteStreamsImpl will parse "forUser" attribute
						line = parseLine(line);
						if (line != null && line.length() > 0) {
							// parsing JSON is an expensive process and can be
							// avoided when all listeners are instanceof
							// RawStreamListener
							if (listeners.length > 0) {
								if (CONF.isJSONStoreEnabled()) {
									DataObjectFactoryUtil.clearThreadLocalMap();
								}
								final JSONObject json = new JSONObject(line);
								final JSONObjectType.Type event = JSONObjectType.determine(json);
								if (logger.isDebugEnabled()) {
									logger.debug("Received:",
											CONF.isPrettyDebugEnabled() ? json.toString(1) : json.toString());
								}
								switch (event) {
									case SENDER:
										onSender(json, listeners);
										break;
									case STATUS:
										onStatus(json, listeners);
										break;
									case DIRECT_MESSAGE:
										onDirectMessage(json, listeners);
										break;
									case DELETE:
										onDelete(json, listeners);
										break;
									case LIMIT:
										onLimit(json, listeners);
										break;
									case STALL_WARNING:
										onStallWarning(json, listeners);
										break;
									case SCRUB_GEO:
										onScrubGeo(json, listeners);
										break;
									case FRIENDS:
										onFriends(json, listeners);
										break;
									case FAVORITE:
										onFavorite(json.getJSONObject("source"), json.getJSONObject("target"),
												json.getJSONObject("target_object"), listeners);
										break;
									case UNFAVORITE:
										onUnfavorite(json.getJSONObject("source"), json.getJSONObject("target"),
												json.getJSONObject("target_object"), listeners);
										break;
									case FOLLOW:
										onFollow(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
										break;
									case UNFOLLOW:
										onUnfollow(json.getJSONObject("source"), json.getJSONObject("target"),
												listeners);
										break;
									case USER_LIST_MEMBER_ADDED:
										onUserListMemberAddition(json.getJSONObject("target"),
												json.getJSONObject("source"), json.getJSONObject("target_object"),
												listeners);
										break;
									case USER_LIST_MEMBER_DELETED:
										onUserListMemberDeletion(json.getJSONObject("target"),
												json.getJSONObject("source"), json.getJSONObject("target_object"),
												listeners);
										break;
									case USER_LIST_SUBSCRIBED:
										onUserListSubscription(json.getJSONObject("source"),
												json.getJSONObject("target"), json.getJSONObject("target_object"),
												listeners);
										break;
									case USER_LIST_UNSUBSCRIBED:
										onUserListUnsubscription(json.getJSONObject("source"),
												json.getJSONObject("target"), json.getJSONObject("target_object"),
												listeners);
										break;
									case USER_LIST_CREATED:
										onUserListCreation(json.getJSONObject("source"), json.getJSONObject("target"),
												listeners);
										break;
									case USER_LIST_UPDATED:
										onUserListUpdated(json.getJSONObject("source"), json.getJSONObject("target"),
												listeners);
										break;
									case USER_LIST_DESTROYED:
										onUserListDestroyed(json.getJSONObject("source"), json.getJSONObject("target"),
												listeners);
										break;
									case USER_UPDATE:
										onUserUpdate(json.getJSONObject("source"), json.getJSONObject("target"),
												listeners);
										break;
									case BLOCK:
										onBlock(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
										break;
									case UNBLOCK:
										onUnblock(json.getJSONObject("source"), json.getJSONObject("target"), listeners);
										break;
									case DISCONNECTION:
										onDisconnectionNotice(line, listeners);
										break;
									case UNKNOWN:
									default:
										logger.warn("Received unknown event:",
												CONF.isPrettyDebugEnabled() ? json.toString(1) : json.toString());
								}
							}
						}
					} catch (final Exception ex) {
						onException(ex, listeners);
					}
				}
			});

		} catch (final IOException ioe) {
			try {
				is.close();
			} catch (final IOException ignore) {
			}
			final boolean isUnexpectedException = streamAlive;
			streamAlive = false;
			if (isUnexpectedException) throw new TwitterException("Stream closed.", ioe);
		}
	}

	protected void onBlock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		logger.warn("Unhandled event: onBlock");
	}

	protected void onDelete(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onDelete");
	}

	protected void onDirectMessage(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onDirectMessage");
	}

	protected void onDisconnectionNotice(final String line, final StreamListener[] listeners) {
		logger.warn("Unhandled event: ", line);
	}

	protected void onException(final Exception e, final StreamListener[] listeners) {
		logger.warn("Unhandled event: ", e.getMessage());
	}

	protected void onFavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onFavorite");
	}

	protected void onFollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		logger.warn("Unhandled event: onFollow");
	}

	protected void onFriends(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onFriends");
	}

	protected void onLimit(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onLimit");
	}

	protected void onMessage(final String rawString, final RawStreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onMessage");
	}

	protected void onScrubGeo(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onScrubGeo");
	}

	protected void onSender(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onSender");
	}

	protected void onStallWarning(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		logger.warn("Unhandled event: onStallWarning");
	}

	protected void onStatus(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onStatus");
	}

	protected void onUnblock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		logger.warn("Unhandled event: onUnblock");
	}

	protected void onUnfavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onUnfavorite");
	}

	protected void onUnfollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		logger.warn("Unhandled event: onUnfollow");
	}

	protected void onUserListCreation(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListCreation");
	}

	protected void onUserListDestroyed(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException {
		logger.warn("Unhandled event: onUserListDestroyed");
	}

	protected void onUserListMemberAddition(final JSONObject addedMember, final JSONObject owner,
			final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListMemberAddition");
	}

	protected void onUserListMemberDeletion(final JSONObject deletedMember, final JSONObject owner,
			final JSONObject userList, final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListMemberDeletion");
	}

	protected void onUserListSubscription(final JSONObject source, final JSONObject owner, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListSubscription");
	}

	protected void onUserListUnsubscription(final JSONObject source, final JSONObject owner, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListUnsubscription");
	}

	protected void onUserListUpdated(final JSONObject source, final JSONObject userList,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		logger.warn("Unhandled event: onUserListUpdated");
	}

	protected void onUserUpdate(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		logger.warn("Unhandled event: onUserUpdate");
	}

	protected String parseLine(final String line) {
		return line;
	}

	abstract class StreamEvent implements Runnable {
		String line;

		StreamEvent(final String line) {
			this.line = line;
		}
	}
}
