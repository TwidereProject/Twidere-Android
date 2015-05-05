package org.mariotaku.simplerestapi.http;

import org.mariotaku.simplerestapi.RestMethodInfo;
import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public class RestRequest {

    private final Authorization authorization;
    private final String method;
    private final String url;
    private final List<KeyValuePair> headers;
    private final Endpoint endpoint;
    private final RestMethodInfo restMethodInfo;

    public Authorization getAuthorization() {
        return authorization;
    }

    private final TypedData body;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<KeyValuePair> getHeaders() {
        return headers;
    }

    public TypedData getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{" +
                "authorization=" + authorization +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", endpoint=" + endpoint +
                ", restMethodInfo=" + restMethodInfo +
                ", body=" + body +
                '}';
    }

    public RestRequest(String method, String url, List<KeyValuePair> headers, TypedData body, Endpoint endpoint,
                       RestMethodInfo restMethodInfo, Authorization authorization) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.endpoint = endpoint;
        this.restMethodInfo = restMethodInfo;
        this.authorization = authorization;
    }

    public RestMethodInfo getRestMethodInfo() {
        return restMethodInfo;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public static class RequestBuilder {
        private Authorization authorization;
        private String method;
        private String url;
        private List<KeyValuePair> headers;
        private Endpoint endpoint;
        private RestMethodInfo restMethodInfo;
        private TypedData body;

        public RequestBuilder() {
        }


        public RequestBuilder authorization(Authorization authorization) {
            this.authorization = authorization;
            return this;
        }

        public RequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public RequestBuilder headers(List<KeyValuePair> headers) {
            this.headers = headers;
            return this;
        }

        public RequestBuilder endpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public RequestBuilder restMethodInfo(RestMethodInfo restMethodInfo) {
            this.restMethodInfo = restMethodInfo;
            return this;
        }

        public RequestBuilder body(TypedData body) {
            this.body = body;
            return this;
        }

        public RestRequest build() {
            return new RestRequest(method, url, headers, body, endpoint, restMethodInfo, authorization);
        }
    }
}
