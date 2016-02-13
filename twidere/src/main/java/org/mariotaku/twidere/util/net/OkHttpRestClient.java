/*
 * Copyright (c) 2015 mariotaku
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

package org.mariotaku.twidere.util.net;

import org.mariotaku.restfu.Pair;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpCall;
import org.mariotaku.restfu.http.HttpCallback;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.Body;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by mariotaku on 16/2/4.
 */
public class OkHttpRestClient implements RestHttpClient {

    private OkHttpClient client;

    public OkHttpRestClient(OkHttpClient client) {
        setClient(client);
    }

    @Override
    public HttpCall newCall(final HttpRequest httpRequest) {
        final Request.Builder builder = new Request.Builder();
        final OkRequestBody requestBody = OkRequestBody.wrap(httpRequest.getBody());
        builder.method(httpRequest.getMethod(), requestBody);
        builder.url(httpRequest.getUrl());
        final MultiValueMap<String> headers = httpRequest.getHeaders();
        if (headers != null) {
            for (Pair<String, String> header : headers.toList()) {
                builder.addHeader(header.first, header.second);
            }
        }
        final OkCall call = new OkCall(this, client.newCall(builder.build()));
        if (requestBody != null) {
            requestBody.setCall(call);
        }
        return call;
    }

    @Override
    public void enqueue(final HttpCall call, final HttpCallback callback) {
        call.enqueue(callback);
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        if (client == null) throw new NullPointerException();
        this.client = client;
    }

    static class OkRequestBody extends RequestBody {
        private final Body body;
        private OkCall call;

        public OkRequestBody(Body body) {
            this.body = body;
        }

        public static OkRequestBody wrap(Body body) {
            if (body == null) return null;
            return new OkRequestBody(body);
        }

        @Override
        public MediaType contentType() {
            final ContentType contentType = body.contentType();
            if (contentType == null) return null;
            return MediaType.parse(contentType.toHeader());
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            call.setEstablished(true);
            body.writeTo(sink.outputStream());
        }

        @Override
        public long contentLength() throws IOException {
            return body.length();
        }

        public void setCall(OkCall call) {
            this.call = call;
        }
    }

    private static class OkResponse extends HttpResponse {
        private final Response response;
        private Body body;

        public OkResponse(Response response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.code();
        }

        @Override
        public MultiValueMap<String> getHeaders() {
            final Headers headers = response.headers();
            return new MultiValueMap<>(headers.toMultimap(), true);
        }

        @Override
        public String getHeader(String name) {
            return response.header(name);
        }

        @Override
        public List<String> getHeaders(String name) {
            return response.headers(name);
        }

        @Override
        public Body getBody() {
            if (body != null) return body;
            return body = new OkResponseBody(response.body());
        }

        @Override
        public void close() throws IOException {
            if (body != null) {
                body.close();
                body = null;
            }
        }
    }

    private static class OkResponseBody implements Body {

        private final ResponseBody body;

        public OkResponseBody(ResponseBody body) {
            this.body = body;
        }

        @Override
        public ContentType contentType() {
            final MediaType mediaType = body.contentType();
            if (mediaType == null) return null;
            return ContentType.parse(mediaType.toString());
        }

        @Override
        public String contentEncoding() {
            return null;
        }

        @Override
        public long length() throws IOException {
            return body.contentLength();
        }

        @Override
        public long writeTo(OutputStream os) throws IOException {
            final BufferedSink sink = Okio.buffer(Okio.sink(os));
            final long result = sink.writeAll(body.source());
            sink.flush();
            return result;
        }

        @Override
        public InputStream stream() throws IOException {
            return body.byteStream();
        }

        @Override
        public void close() throws IOException {
            body.close();
        }
    }

    static class OkCall implements HttpCall {
        private final OkHttpRestClient client;
        private final Call call;
        private boolean established;

        public OkCall(OkHttpRestClient client, Call call) {
            this.client = client;
            this.call = call;
        }

        @Override
        public HttpResponse execute() throws IOException {
            final RequestRunnable runnable = new RequestRunnable(this, client.client.connectTimeoutMillis());
            final Thread thread = new Thread(runnable);
            thread.setPriority(Thread.currentThread().getPriority());
            thread.start();
            while (runnable.shouldWait()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            return new OkResponse(runnable.getResponse());
        }


        @Override
        public void enqueue(HttpCallback callback) {
            call.enqueue(new OkCallback(callback));
        }

        public void cancel() {
            call.cancel();
        }

        @Override
        public boolean isCanceled() {
            return call.isCanceled();
        }

        @Override
        public void close() throws IOException {
        }

        boolean isEstablished() {
            return established;
        }

        void setEstablished(boolean established) {
            this.established = established;
        }

        private static class RequestRunnable implements Runnable {
            private final OkCall call;
            private final long timeout;
            private long start = -1;
            private Response response;
            private IOException exception;
            private boolean finished;

            public RequestRunnable(OkCall call, long timeout) {
                this.call = call;
                this.timeout = timeout;
            }

            @Override
            public void run() {
                finished = false;
                call.setEstablished(false);
                start = System.currentTimeMillis();
                try {
                    response = call.call.execute();
                } catch (IOException e) {
                    exception = e;
                }
                finished = true;
            }

            public boolean shouldWait() {
                return !finished && (start < 0 || call.isEstablished() || !reachedTimeout());
            }

            private boolean reachedTimeout() {
                return System.currentTimeMillis() - start > timeout;
            }

            public Response getResponse() throws IOException {
                if (exception != null) throw exception;
                if (response == null) {
                    if (!call.isCanceled()) {
                        call.cancel();
                    }
                    if (reachedTimeout()) {
                        ConnectionPool pool = call.client.client.connectionPool();
                        pool.evictAll();
                        throw new SocketTimeoutException("Request timed out after " + timeout + " ms");
                    } else {
                        throw new IOException("Request cancelled");
                    }
                }
                return response;
            }
        }
    }

    private static class OkCallback implements Callback {
        private final HttpCallback callback;

        public OkCallback(HttpCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            this.callback.failure(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            this.callback.response(new OkResponse(response));
        }
    }
}
