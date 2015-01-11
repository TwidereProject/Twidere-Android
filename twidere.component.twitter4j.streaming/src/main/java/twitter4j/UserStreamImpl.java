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

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
class UserStreamImpl extends StatusStreamImpl implements UserStream {
	/* package */UserStreamImpl(final Dispatcher dispatcher, final HttpResponse response, final StreamConfiguration conf)
			throws IOException {
		super(dispatcher, response, conf);
	}

	/* package */UserStreamImpl(final Dispatcher dispatcher, final InputStream stream, final StreamConfiguration conf)
			throws IOException {
		super(dispatcher, stream, conf);
	}

	@Override
	public void next(final UserStreamListener listener) throws TwitterException {
		handleNextElement(new StreamListener[] { listener }, EMPTY);
	}

	@Override
	protected void onBlock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onBlock(asUser(source), asUser(target));
		}
	}

	@Override
	protected void onDirectMessage(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		final DirectMessage directMessage = asDirectMessage(json);
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onDirectMessage(directMessage);
		}
	}

	@Override
	protected void onFavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onFavorite(asUser(source), asUser(target), asStatus(targetObject));
		}
	}

	@Override
	protected void onFollow(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onFollow(asUser(source), asUser(target));
		}
	}

	@Override
	protected void onFriends(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		final long[] friendIds = asFriendList(json);
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onFriendList(friendIds);
		}
	}

	@Override
	protected void onScrubGeo(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		// Not implemented yet
		logger.info("Geo-tagging deletion notice (not implemented yet): " + line);
	}

	@Override
	protected void onSender(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onDirectMessage(factory.createDirectMessage(json));
		}
	}

	@Override
	protected void onUnblock(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUnblock(asUser(source), asUser(target));
		}
	}

	@Override
	protected void onUnfavorite(final JSONObject source, final JSONObject target, final JSONObject targetObject,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUnfavorite(asUser(source), asUser(target), asStatus(targetObject));
		}
	}

	@Override
	protected void onUserListCreation(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListCreation(asUser(source), asUserList(target));
		}
	}

	@Override
	protected void onUserListDestroyed(final JSONObject source, final JSONObject target,
			final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListDeletion(asUser(source), asUserList(target));
		}
	}

	@Override
	protected void onUserListMemberAddition(final JSONObject addedMember, final JSONObject owner,
			final JSONObject target, final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListMemberAddition(asUser(addedMember), asUser(owner),
					asUserList(target));
		}
	}

	@Override
	protected void onUserListMemberDeletion(final JSONObject deletedMember, final JSONObject owner,
			final JSONObject target, final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListMemberDeletion(asUser(deletedMember), asUser(owner),
					asUserList(target));
		}
	}

	@Override
	protected void onUserListSubscription(final JSONObject source, final JSONObject owner, final JSONObject target,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListSubscription(asUser(source), asUser(owner), asUserList(target));
		}
	}

	@Override
	protected void onUserListUnsubscription(final JSONObject source, final JSONObject owner, final JSONObject target,
			final StreamListener[] listeners) throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListUnsubscription(asUser(source), asUser(owner), asUserList(target));
		}
	}

	@Override
	protected void onUserListUpdated(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException, JSONException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserListUpdate(asUser(source), asUserList(target));
		}
	}

	@Override
	protected void onUserUpdate(final JSONObject source, final JSONObject target, final StreamListener[] listeners)
			throws TwitterException {
		for (final StreamListener listener : listeners) {
			((UserStreamListener) listener).onUserProfileUpdate(asUser(source));
		}
	}

}