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

import static twitter4j.internal.util.InternalParseUtil.getInt;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.http.HttpRequest;
import twitter4j.http.HttpResponse;
import twitter4j.http.HttpResponseCode;
import twitter4j.internal.json.InternalJSONFactoryImpl;
import twitter4j.internal.util.InternalParseUtil;

import java.util.List;
import java.util.Locale;

/**
 * An exception class that will be thrown when TwitterAPI calls are failed.<br>
 * In case the Twitter server returned HTTP error code, you can get the HTTP
 * status code using getStatusCode() method.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class TwitterException extends Exception implements TwitterResponse, HttpResponseCode {
	private int statusCode = -1;
	private int errorCode = -1;
	private static final long serialVersionUID = -2623309261327598087L;
	private ExceptionDiagnosis exceptionDiagnosis = null;
	private HttpResponse response;
	private String errorMessage = null;
	private HttpRequest request;

	private final static String[] FILTER = new String[] { "twitter4j" };

	boolean nested = false;

	public TwitterException(final Exception cause) {
		this(cause.getMessage(), cause);
		if (cause instanceof TwitterException) {
			((TwitterException) cause).setNested();
		}
	}

	public TwitterException(final String message) {
		this(message, (Throwable) null);
	}

	public TwitterException(final String message, final Exception cause, final int statusCode) {
		this(message, cause);
		this.statusCode = statusCode;
	}

	public TwitterException(final String message, final HttpRequest req, final HttpResponse res) {
		this(message);
		response = res;
		request = req;
		statusCode = res != null ? res.getStatusCode() : -1;
	}

	public TwitterException(final String message, final HttpResponse res) {
		this(message, null, res);
	}

	public TwitterException(final String message, final Throwable cause) {
		super(message, cause);
		decode(message);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final TwitterException that = (TwitterException) o;

		if (errorCode != that.errorCode) return false;
		if (nested != that.nested) return false;
		if (statusCode != that.statusCode) return false;
		if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) return false;
		if (exceptionDiagnosis != null ? !exceptionDiagnosis.equals(that.exceptionDiagnosis)
				: that.exceptionDiagnosis != null) return false;
		if (response != null ? !response.equals(that.response) : that.response != null) return false;

		return true;
	}

	/**
	 * Tests if the exception is caused by rate limitation exceed
	 * 
	 * @return if the exception is caused by rate limitation exceed
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.2
	 */
	public boolean exceededRateLimitation() {
		return statusCode == 400 && getRateLimitStatus() != null // REST API
				|| statusCode == ENHANCE_YOUR_CLAIM // Streaming API
				|| statusCode == TOO_MANY_REQUESTS; // API 1.1
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccessLevel() {
		return InternalParseUtil.toAccessLevel(response);
	}

	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns error message from the API if available.
	 * 
	 * @return error message from the API
	 * @since Twitter4J 2.2.3
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns a hexadecimal representation of this exception stacktrace.<br>
	 * An exception code is a hexadecimal representation of the stacktrace which
	 * enables it easier to Google known issues.<br>
	 * Format : XXXXXXXX:YYYYYYYY[ XX:YY]<br>
	 * Where XX is a hash code of stacktrace without line number<br>
	 * YY is a hash code of stacktrace excluding line number<br>
	 * [-XX:YY] will appear when this instance a root cause
	 * 
	 * @return a hexadecimal representation of this exception stacktrace
	 */
	public String getExceptionCode() {
		return getExceptionDiagnosis().asHexString();
	}

	public HttpRequest getHttpRequest() {
		return request;
	}

	public HttpResponse getHttpResponse() {
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		if (errorMessage != null && errorCode != -1)
			return String.format(Locale.US, "Error %d: %s", errorCode, errorMessage);
		else if (statusCode != -1)
			return String.format(Locale.US, "Error %d", statusCode);
		else
			return super.getMessage();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since Twitter4J 2.1.2
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		if (null == response) return null;
		return InternalJSONFactoryImpl.createRateLimitStatusFromResponseHeader(response);
	}

	public String getResponseHeader(final String name) {
		String value = null;
		if (response != null) {
			final List<String> header = response.getResponseHeaderFields().get(name);
			if (header.size() > 0) {
				value = header.get(0);
			}
		}
		return value;
	}

	/**
	 * Returns int value of "Retry-After" response header (Search API) or
	 * seconds_until_reset (REST API). An application that exceeds the rate
	 * limitations of the Search API will receive HTTP 420 response codes to
	 * requests. It is a best practice to watch for this error condition and
	 * honor the Retry-After header that instructs the application when it is
	 * safe to continue. The Retry-After header's value is the number of seconds
	 * your application should wait before submitting another query (for
	 * example: Retry-After: 67).<br>
	 * Check if getStatusCode() == 503 before calling this method to ensure that
	 * you are actually exceeding rate limitation with query apis.<br>
	 * 
	 * @return instructs the application when it is safe to continue in seconds
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.0
	 */
	public int getRetryAfter() {
		int retryAfter = -1;
		if (statusCode == 400) {
			final RateLimitStatus rateLimitStatus = getRateLimitStatus();
			if (rateLimitStatus != null) {
				retryAfter = rateLimitStatus.getSecondsUntilReset();
			}
		} else if (statusCode == ENHANCE_YOUR_CLAIM) {
			try {
				final String retryAfterStr = response.getResponseHeader("Retry-After");
				if (retryAfterStr != null) {
					retryAfter = Integer.valueOf(retryAfterStr);
				}
			} catch (final NumberFormatException ignore) {
			}
		}
		return retryAfter;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public int hashCode() {
		int result = statusCode;
		result = 31 * result + errorCode;
		result = 31 * result + (exceptionDiagnosis != null ? exceptionDiagnosis.hashCode() : 0);
		result = 31 * result + (response != null ? response.hashCode() : 0);
		result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
		result = 31 * result + (nested ? 1 : 0);
		return result;
	}

	/**
	 * Tests if the exception is caused by network issue
	 * 
	 * @return if the exception is caused by network issue
	 * @since Twitter4J 2.1.2
	 */
	public boolean isCausedByNetworkIssue() {
		return getCause() instanceof java.io.IOException;
	}

	/**
	 * Tests if error message from the API is available
	 * 
	 * @return true if error message from the API is available
	 * @since Twitter4J 2.2.3
	 */
	public boolean isErrorMessageAvailable() {
		return errorMessage != null;
	}

	/**
	 * Tests if the exception is caused by non-existing resource
	 * 
	 * @return if the exception is caused by non-existing resource
	 * @since Twitter4J 2.1.2
	 */
	public boolean resourceNotFound() {
		return statusCode == NOT_FOUND;
	}

	@Override
	public String toString() {
		return getMessage();
	}

	private void decode(final String str) {
		if (str != null && str.startsWith("{")) {
			try {
				final JSONObject json = new JSONObject(str);
				if (!json.isNull("errors")) {
					final JSONObject error = json.getJSONArray("errors").getJSONObject(0);
					errorMessage = error.getString("message");
					errorCode = getInt("code", error);
				}
			} catch (final JSONException ignore) {
			}
		}
	}

	private ExceptionDiagnosis getExceptionDiagnosis() {
		if (null == exceptionDiagnosis) {
			exceptionDiagnosis = new ExceptionDiagnosis(this, FILTER);
		}
		return exceptionDiagnosis;
	}

	void setNested() {
		nested = true;
	}
}
