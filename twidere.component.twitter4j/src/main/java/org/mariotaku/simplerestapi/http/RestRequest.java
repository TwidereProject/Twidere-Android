package org.mariotaku.simplerestapi.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.simplerestapi.RestMethod;
import org.mariotaku.simplerestapi.RestMethodInfo;
import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public class RestRequest {

    private final String method;
    private final String url;
    private final List<KeyValuePair> headers;
    private final TypedData body;
    private final Object extra;

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

    public RestRequest(String method, String url, List<KeyValuePair> headers, TypedData body, Object extra) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.extra = extra;
    }

    public static final class Builder {
        private String method;
        private String url;
        private List<KeyValuePair> headers;
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

        public Builder headers(List<KeyValuePair> headers) {
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

        public RestRequest build() {
            return new RestRequest(method, url, headers, body, extra);
        }
    }

    public interface Factory {
        RestRequest create(@NonNull Endpoint endpoint, @NonNull RestMethodInfo info, @Nullable Authorization authorization);
    }

    public static final class DefaultFactory implements Factory {

        @Override
        public RestRequest create(@NonNull Endpoint endpoint, @NonNull RestMethodInfo methodInfo, @Nullable Authorization authorization) {
            final RestMethod restMethod = methodInfo.getMethod();
            final String url = Endpoint.constructUrl(endpoint.getUrl(), methodInfo);
            final ArrayList<KeyValuePair> headers = new ArrayList<>(methodInfo.getHeaders());

            if (authorization != null && authorization.hasAuthorization()) {
                headers.add(new KeyValuePair("Authorization", authorization.getHeader(endpoint, methodInfo)));
            }
            return new RestRequest(restMethod.value(), url, headers, methodInfo.getBody(), null);
        }
    }
}
