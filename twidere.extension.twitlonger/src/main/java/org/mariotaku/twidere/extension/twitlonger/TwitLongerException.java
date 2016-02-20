package org.mariotaku.twidere.extension.twitlonger;

import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;

/**
 * Created by mariotaku on 16/2/20.
 */
public class TwitLongerException extends Exception {

    private HttpRequest request;
    private HttpResponse response;

    public TwitLongerException() {
        super();
    }

    public TwitLongerException(String detailMessage) {
        super(detailMessage);
    }

    public TwitLongerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TwitLongerException(Throwable throwable) {
        super(throwable);
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
