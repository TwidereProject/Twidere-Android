/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

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
