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
 * StatusStream implementation. This class is NOT intended to be extended but
 * left non-final for the ease of mock testing.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
class StatusStreamImpl extends StatusStreamBase {
	/* package */

	protected String line;

	protected static final RawStreamListener[] EMPTY = new RawStreamListener[0];

	StatusStreamImpl(final Dispatcher dispatcher, final HttpResponse response, final StreamConfiguration conf)
			throws IOException {
		super(dispatcher, response, conf);
	}

	StatusStreamImpl(final Dispatcher dispatcher, final InputStream stream, final StreamConfiguration conf)
			throws IOException {
		super(dispatcher, stream, conf);
	}

	/* package */
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
	public void onException(final Exception e, final StreamListener[] listeners) {
		for (final StreamListener listener : listeners) {
			listener.onException(e);
		}
	}

	@Override
	protected void onDelete(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		for (final StreamListener listener : listeners) {
			final JSONObject deletionNotice = json.getJSONObject("delete");
			if (deletionNotice.has("status")) {
				((StatusListener) listener).onDeletionNotice(new StatusDeletionNoticeImpl(deletionNotice
						.getJSONObject("status")));
			} else {
				final JSONObject directMessage = deletionNotice.getJSONObject("direct_message");
				((UserStreamListener) listener).onDeletionNotice(InternalParseUtil.getLong("id", directMessage),
						InternalParseUtil.getLong("user_id", directMessage));
			}
		}
	}

	@Override
	protected void onLimit(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		for (final StreamListener listener : listeners) {
			((StatusListener) listener).onTrackLimitationNotice(InternalParseUtil.getInt("track",
					json.getJSONObject("limit")));
		}
	}

	@Override
	protected void onMessage(final String rawString, final RawStreamListener[] listeners) throws TwitterException {
		for (final RawStreamListener listener : listeners) {
			listener.onMessage(rawString);
		}
	}

	@Override
	protected void onScrubGeo(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		final JSONObject scrubGeo = json.getJSONObject("scrub_geo");
		for (final StreamListener listener : listeners) {
			((StatusListener) listener).onScrubGeo(InternalParseUtil.getLong("user_id", scrubGeo),
					InternalParseUtil.getLong("up_to_status_id", scrubGeo));
		}

	}

	@Override
	protected void onStallWarning(final JSONObject json, final StreamListener[] listeners) throws TwitterException,
			JSONException {
		for (final StreamListener listener : listeners) {
			((StatusListener) listener).onStallWarning(new StallWarning(json));
		}
	}

	@Override
	protected void onStatus(final JSONObject json, final StreamListener[] listeners) throws TwitterException {
		for (final StreamListener listener : listeners) {
			((StatusListener) listener).onStatus(asStatus(json));
		}
	}

	@Override
	protected String parseLine(final String line) {
		this.line = line;
		return line;
	}
}
