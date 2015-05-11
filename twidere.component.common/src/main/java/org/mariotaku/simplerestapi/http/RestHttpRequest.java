package org.mariotaku.simplerestapi.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.mariotaku.simplerestapi.RequestInfo;
import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public final class RestHttpRequest {

    private final String method;
    private final String url;
    private final List<Pair<String, String>> headers;
    private final TypedData body;
    private final Object extra;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<Pair<String, String>> getHeaders() {
        return headers;
    }

    public TypedData getBody() {
        return body;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return "RestRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", body=" + body +
                '}';
    }

    public RestHttpRequest(String method, String url, List<Pair<String, String>> headers, TypedData body, Object extra) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.extra = extra;
    }

    public static final class Builder {
        private String method;
        private String url;
        private List<Pair<String, String>> headers;
        private TypedData body;
        private Object extra;

        public Builder() {
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder headers(List<Pair<String, String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(TypedData body) {
            this.body = body;
            return this;
        }

        public Builder extra(Object extra) {
            this.extra = extra;
            return this;
        }

        public RestHttpRequest build() {
            return new RestHttpRequest(method, url, headers, body, extra);
        }
    }

    public interface Factory {
        RestHttpRequest create(@NonNull Endpoint endpoint, @NonNull RequestInfo info, @Nullable Authorization authorization);
    }


    public static final class DefaultFactory implements Factory {

        @Override
        public RestHttpRequest create(@NonNull Endpoint endpoint, @NonNull RequestInfo requestInfo, @Nullable Authorization authorization) {
            final String url = Endpoint.constructUrl(endpoint.getUrl(), requestInfo);
            final ArrayList<Pair<String, String>> headers = new ArrayList<>(requestInfo.getHeaders());

            if (authorization != null && authorization.hasAuthorization()) {
                headers.add(Pair.create("Authorization", authorization.getHeader(endpoint, requestInfo)));
            }
            return new RestHttpRequest(requestInfo.getMethod(), url, headers, requestInfo.getBody(), null);
        }
    }
}
