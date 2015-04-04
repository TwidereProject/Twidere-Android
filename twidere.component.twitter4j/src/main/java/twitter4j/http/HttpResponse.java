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

package twitter4j.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.logging.Logger;

/**
 * A data class representing HTTP Response
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public abstract class HttpResponse {
	private static final Logger logger = Logger.getLogger(HttpResponse.class);
	protected final HttpClientConfiguration CONF;

	protected int statusCode;

	protected String responseAsString = null;

	protected InputStream is;
	private boolean streamConsumed = false;
	private JSONObject json = null;
	private JSONArray jsonArray = null;

	public HttpResponse(final HttpClientConfiguration conf) {
		CONF = conf;
	}

	HttpResponse() {
		CONF = ConfigurationContext.getInstance();
	}

	/**
	 * Returns the response body as twitter4j.internal.org.json.JSONArray.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as twitter4j.internal.org.json.JSONArray
	 * @throws TwitterException
	 */
	public JSONArray asJSONArray() throws TwitterException {
		if (jsonArray == null) {
			try {
				if (responseAsString == null) {
					responseAsString = asString();
				}
				jsonArray = new JSONArray(responseAsString);
				if (CONF.isPrettyDebugEnabled()) {
					logger.debug(jsonArray.toString(1));
				} else {
					logger.debug(responseAsString != null ? responseAsString : jsonArray.toString());
				}
			} catch (final JSONException jsone) {
				if (logger.isDebugEnabled())
					throw new TwitterException(jsone.getMessage() + ":" + responseAsString, jsone);
				else
					throw new TwitterException(jsone.getMessage(), jsone);
			} finally {
				disconnectForcibly();
			}
		}
		return jsonArray;
	}

	/**
	 * Returns the response body as twitter4j.internal.org.json.JSONObject.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as twitter4j.internal.org.json.JSONObject
	 * @throws TwitterException
	 */
	public JSONObject asJSONObject() throws TwitterException {
		if (json == null) {
			try {
				if (responseAsString == null) {
					responseAsString = asString();
				}
				json = new JSONObject(responseAsString);
				if (CONF.isPrettyDebugEnabled()) {
					logger.debug(json.toString(1));
				} else {
					logger.debug(responseAsString != null ? responseAsString : json.toString());
				}
			} catch (final JSONException jsone) {
				if (responseAsString == null)
					throw new TwitterException(jsone.getMessage(), jsone);
				else
					throw new TwitterException(jsone.getMessage() + ":" + responseAsString, jsone);
			} finally {
				disconnectForcibly();
			}
		}
		return json;
	}

	public Reader asReader() {
		try {
			return new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (final java.io.UnsupportedEncodingException uee) {
			return new InputStreamReader(is);
		}
	}

	/**
	 * Returns the response stream.<br>
	 * This method cannot be called after calling asString() or asDcoument()<br>
	 * It is suggested to call disconnect() after consuming the stream.
	 * <p/>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body stream
	 * @throws TwitterException
	 * @see #disconnect()
	 */
	public InputStream asStream() {
		if (streamConsumed) throw new IllegalStateException("Stream has already been consumed.");
		return is;
	}

	/**
	 * Returns the response body as string.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body
	 * @throws TwitterException
	 */
	public String asString() throws TwitterException {
		if (null == responseAsString) {
			BufferedReader br = null;
			InputStream stream = null;
			try {
				stream = asStream();
				if (null == stream) return null;
				br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				final StringBuilder buf = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					buf.append(line).append("\n");
				}
				responseAsString = buf.toString();
				logger.debug(responseAsString);
				stream.close();
				streamConsumed = true;
			} catch (final OutOfMemoryError oome) {
				throw new TwitterException(oome.getMessage(), oome);
			} catch (final IOException ioe) {
				throw new TwitterException(ioe.getMessage(), ioe);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (final IOException ignore) {
					}
				}
				if (br != null) {
					try {
						br.close();
					} catch (final IOException ignore) {
					}
				}
				disconnectForcibly();
			}
		}
		return responseAsString;
	}

	public abstract void disconnect() throws IOException;

	public long getContentLength() {
		try {
			return Long.parseLong(getResponseHeader("Content-Length"));
		} catch (final Exception e) {
			return -1;
		}
	}

	public abstract String getResponseHeader(String name);

	public abstract List<String> getResponseHeaders(String name);

	public abstract Map<String, List<String>> getResponseHeaderFields();

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		return "HttpResponse{" + "statusCode=" + statusCode + ", responseAsString='" + responseAsString + '\''
				+ ", is=" + is + ", streamConsumed=" + streamConsumed + '}';
	}

	private void disconnectForcibly() {
		try {
			disconnect();
		} catch (final Exception ignore) {
		}
	}
}
