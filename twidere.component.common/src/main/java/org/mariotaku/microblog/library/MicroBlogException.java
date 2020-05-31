/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.twitter.http.HttpResponseCode;
import org.mariotaku.microblog.library.twitter.model.ErrorInfo;
import org.mariotaku.microblog.library.twitter.model.RateLimitStatus;
import org.mariotaku.microblog.library.twitter.model.TwitterResponse;
import org.mariotaku.microblog.library.twitter.util.InternalParseUtil;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;

import java.io.IOException;
import java.util.Locale;

/**
 * An exception class that will be thrown when TwitterAPI calls are failed.<br>
 * In case the Twitter server returned HTTP error code, you can get the HTTP
 * status code using getStatusCode() method.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@JsonObject
public class MicroBlogException extends Exception implements TwitterResponse, HttpResponseCode {

    private static final long serialVersionUID = -2623309261327598087L;
    @JsonField(name = "errors")
    ErrorInfo[] errors;
    @JsonField(name = "error")
    String errorMessage;
    @JsonField(name = "request")
    String requestPath;
    boolean nested = false;
    private int statusCode = -1;
    private RateLimitStatus rateLimitStatus;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private boolean causedByNetworkIssue;

    public MicroBlogException() {
        super();
    }

    public MicroBlogException(String detailMessage) {
        super(detailMessage);
    }

    public MicroBlogException(final Throwable cause) {
        this(cause.getMessage(), cause);
        if (cause instanceof MicroBlogException) {
            ((MicroBlogException) cause).setNested();
        }
    }

    public MicroBlogException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        if (cause instanceof MicroBlogException) {
            ((MicroBlogException) cause).setNested();
        }
        setCausedByNetworkIssue(cause instanceof IOException);
    }


    public ErrorInfo[] getErrors() {
        if (errors == null && errorMessage != null && requestPath != null) {
            return new ErrorInfo[]{new SingleErrorInfo(errorMessage, requestPath)};
        }
        return errors;
    }

    public void setErrors(ErrorInfo[] errors) {
        this.errors = errors;
    }

    /**
     * Tests if the exception is caused by rate limitation exceed
     *
     * @return if the exception is caused by rate limitation exceed
     * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
     * Twitter Developers</a>
     * @since Twitter4J 2.1.2
     */
    public boolean isRateLimitExceeded() {
        return statusCode == 400 && getRateLimitStatus() != null // REST API
                || statusCode == ENHANCE_YOUR_CLAIM // Streaming API
                || statusCode == TOO_MANY_REQUESTS; // API 1.1
    }

    @Override
    public void processResponseHeader(HttpResponse resp) {

    }

    /**
     * {@inheritDoc}
     */
    @AccessLevel
    @Override
    public int getAccessLevel() {
        return InternalParseUtil.toAccessLevel(httpResponse);
    }

    public int getErrorCode() {
        if (errors == null || errors.length == 0) return -1;
        return errors[0].getCode();
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse res) {
        httpResponse = res;
        if (res != null) {
            rateLimitStatus = RateLimitStatus.createFromResponseHeader(res);
            statusCode = res.getStatus();
        } else {
            rateLimitStatus = null;
            statusCode = -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        if (errors != null && errors.length > 0) {
            return String.format(Locale.US, "Error %d: %s", errors[0].getCode(), errors[0].getMessage());
        } else if (statusCode != -1) {
            return String.format(Locale.US, "Error %d", statusCode);
        } else {
            return super.getMessage();
        }
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
        if (httpResponse != null) {
            return httpResponse.getHeader(name);
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
                final String retryAfterStr = httpResponse.getHeader("Retry-After");
                if (retryAfterStr != null) {
                    retryAfter = Integer.parseInt(retryAfterStr);
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
        return causedByNetworkIssue;
    }

    public void setCausedByNetworkIssue(boolean causedByNetworkIssue) {
        this.causedByNetworkIssue = causedByNetworkIssue;
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
        final String message = getMessage();
        if (message == null) return getClass().getSimpleName();
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    void setNested() {
        nested = true;
    }

    static class SingleErrorInfo extends ErrorInfo {
        private final String message;
        private final String request;
        private final int code;

        public SingleErrorInfo(String message, String request) {
            this.message = message;
            this.request = request;
            this.code = -1;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getRequest() {
            return request;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
