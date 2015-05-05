package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.RestResponse;

/**
 * Created by mariotaku on 15/2/7.
 */
public class RestException extends RuntimeException {
    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestException(String message) {
        super(message);
    }

    public RestException() {
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    private RestResponse response;

    @Override
    public String toString() {
        return "RestException{" +
                "response=" + response +
                "} " + super.toString();
    }

    public RestResponse getResponse() {
        return response;
    }

    public void setResponse(RestResponse response) {
        this.response = response;
    }
}
