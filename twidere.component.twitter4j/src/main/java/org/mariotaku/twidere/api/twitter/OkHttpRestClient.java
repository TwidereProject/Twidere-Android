/*
 * Twidere - Twitter client for Android
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

import android.support.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.http.KeyValuePair;
import org.mariotaku.simplerestapi.http.RestHttpClient;
import org.mariotaku.simplerestapi.http.RestRequest;
import org.mariotaku.simplerestapi.http.RestResponse;
import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;

/**
 * Created by mariotaku on 15/5/5.
 */
public class OkHttpRestClient implements RestHttpClient {

    private final OkHttpClient client;

    public OkHttpRestClient() {
        client = new OkHttpClient();
    }

    @Override
    public RestResponse execute(RestRequest restRequest) throws IOException {
        final Authorization authorization = restRequest.getAuthorization();
        final Request.Builder builder = new Request.Builder();
        builder.url(restRequest.getUrl());
        builder.method(restRequest.getMethod(), RestToOkBody.wrap(restRequest.getBody()));
        final List<KeyValuePair> headers = restRequest.getHeaders();
        if (headers != null) {
            for (KeyValuePair header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
        }
        if (authorization != null && authorization.hasAuthorization()) {
            builder.header("Authorization", authorization.getHeader(restRequest.getEndpoint(),
                    restRequest.getRestMethodInfo()));
        }
        builder.header("Accept-Encoding", "gzip; q=1.0, *; q=0.5");
        final Call call = client.newCall(builder.build());
        return new OkRestResponse(call.execute());
    }

    private static class RestToOkBody extends RequestBody {
        private final TypedData body;

        public RestToOkBody(TypedData body) {
            this.body = body;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(body.contentType().toHeader());
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            body.writeTo(sink.outputStream());
        }

        @Nullable
        public static RequestBody wrap(@Nullable TypedData body) {
            if (body == null) return null;
            return new RestToOkBody(body);
        }
    }

    private static class OkRestResponse extends RestResponse {
        private final Response response;
        private TypedData body;

        public OkRestResponse(Response response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.code();
        }

        @Override
        public List<KeyValuePair> getHeaders() {
            final Headers headers = response.headers();
            final ArrayList<KeyValuePair> headersList = new ArrayList<>();
            for (int i = 0, j = headers.size(); i < j; i++) {
                headersList.add(new KeyValuePair(headers.name(i), headers.value(i)));
            }
            return headersList;
        }

        @Override
        public String getHeader(String name) {
            return response.header(name);
        }

        @Override
        public KeyValuePair[] getHeaders(String name) {
            final List<String> values = response.headers(name);
            final KeyValuePair[] headers = new KeyValuePair[values.size()];
            for (int i = 0, j = headers.length; i < j; i++) {
                headers[i] = new KeyValuePair(name, values.get(i));
            }
            return headers;
        }

        @Override
        public TypedData getBody() {
            if (body != null) return body;
            return body = new OkToRestBody(response.body());
        }

        @Override
        public void close() throws IOException {
            if (body != null) {
                body.close();
                body = null;
            }
        }
    }

    private static class OkToRestBody implements TypedData {

        private final ResponseBody body;

        public OkToRestBody(ResponseBody body) {
            this.body = body;
        }

        @Override
        public ContentType contentType() {
            return ContentType.parse(body.contentType().toString());
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
        public void writeTo(OutputStream os) throws IOException {
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
}
