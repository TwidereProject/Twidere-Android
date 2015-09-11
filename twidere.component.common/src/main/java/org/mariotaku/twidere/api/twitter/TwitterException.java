/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.http.HttpResponseCode;
import org.mariotaku.twidere.api.twitter.model.ErrorInfo;
import org.mariotaku.twidere.api.twitter.model.RateLimitStatus;
import org.mariotaku.twidere.api.twitter.model.TwitterResponse;
import org.mariotaku.twidere.api.twitter.model.impl.RateLimitStatusJSONImpl;
import org.mariotaku.twidere.api.twitter.util.InternalParseUtil;

import java.util.Locale;

/**
 * An exception class that will be thrown when TwitterAPI calls are failed.<br>
 * In case the Twitter server returned HTTP error code, you can get the HTTP
 * status code using getStatusCode() method.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@JsonObject
public class TwitterException extends Exception implements TwitterResponse, HttpResponseCode {

    private static final long serialVersionUID = -2623309261327598087L;
    @JsonField(name = "errors")
    ErrorInfo[] errors;
    boolean nested = false;
    private int statusCode = -1;
    private RateLimitStatus rateLimitStatus;
    private RestHttpRequest request;
    private RestHttpResponse response;

    public TwitterException() {
    }

    public TwitterException(RestHttpResponse response) {
        setResponse(response);
    }

    public TwitterException(final Throwable cause) {
        this(cause.getMessage(), cause);
        if (cause instanceof TwitterException) {
            ((TwitterException) cause).setNested();
        }
    }

    public TwitterException(final String message) {
        this(message, (Throwable) null);
    }

    public TwitterException(final String message, final Throwable cause, final int statusCode) {
        this(message, cause);
        setStatusCode(statusCode);
    }

    public TwitterException(final String message, final RestHttpRequest req, final RestHttpResponse res) {
        this(message);
        setResponse(res);
        setRequest(req);
    }

    public TwitterException(final String message, final Throwable cause, final RestHttpRequest req, final RestHttpResponse res) {
        this(message, cause);
        setResponse(res);
        setRequest(req);
    }

    public TwitterException(final String message, final RestHttpResponse res) {
        this(message, null, null, res);
    }

    public TwitterException(final String message, final Throwable cause, final RestHttpResponse res) {
        this(message, cause, null, res);
    }

    public TwitterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    private void setRequest(RestHttpRequest request) {
        this.request = request;
    }

    public ErrorInfo[] getErrors() {
        return errors;
    }

    public void setResponse(RestHttpResponse res) {
        response = res;
        if (res != null) {
            rateLimitStatus = RateLimitStatusJSONImpl.createFromResponseHeader(res);
            statusCode = res.getStatus();
        } else {
            rateLimitStatus = null;
            statusCode = -1;
        }
    }

    /**
     * Tests if the exception is caused by rate limitation exceed
     *
     * @return if the exception is caused by rate limitation exceed
     * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
     * Twitter Developers</a>
     * @since Twitter4J 2.1.2
     */
    public boolean exceededRateLimitation() {
        return statusCode == 400 && getRateLimitStatus() != null // REST API
                || statusCode == ENHANCE_YOUR_CLAIM // Streaming API
                || statusCode == TOO_MANY_REQUESTS; // API 1.1
    }

    @Override
    public void processResponseHeader(RestHttpResponse resp) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAccessLevel() {
        return InternalParseUtil.toAccessLevel(response);
    }

    public int getErrorCode() {
        if (errors == null || errors.length == 0) return -1;
        return errors[0].getCode();
    }

    public RestHttpRequest getHttpRequest() {
        return request;
    }

    public RestHttpResponse getHttpResponse() {
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        if (errors != null && errors.length > 0)
            return String.format(Locale.US, "Error %d: %s", errors[0].getCode(), errors[0].getMessage());
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
        return rateLimitStatus;
    }

    public String getResponseHeader(final String name) {
        if (response != null) {
            return response.getHeader(name);
        }
        return null;
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
     * Twitter Developers</a>
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
                final String retryAfterStr = response.getHeader("Retry-After");
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

    private void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
        return errors != null && errors.length > 0;
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


    void setNested() {
        nested = true;
    }

}
