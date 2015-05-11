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

package org.mariotaku.twidere.util.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.http.RestHttpClient;
import org.mariotaku.simplerestapi.http.RestHttpRequest;
import org.mariotaku.simplerestapi.http.RestHttpResponse;
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
        this(new OkHttpClient());
    }

    public OkHttpRestClient(OkHttpClient client) {
        this.client = client;
    }

    @NonNull
    @Override
    public RestHttpResponse execute(RestHttpRequest restHttpRequest) throws IOException {
        final Request.Builder builder = new Request.Builder();
        builder.method(restHttpRequest.getMethod(), RestToOkBody.wrap(restHttpRequest.getBody()));
        builder.url(restHttpRequest.getUrl());
        final List<Pair<String, String>> headers = restHttpRequest.getHeaders();
        if (headers != null) {
            for (Pair<String, String> header : headers) {
                builder.addHeader(header.first, header.second);
            }
        }
        final Call call = client.newCall(builder.build());
        return new OkRestHttpResponse(call.execute());
    }

    private static class RestToOkBody extends RequestBody {
        private final TypedData body;

        public RestToOkBody(TypedData body) {
            this.body = body;
        }

        @Override
        public MediaType contentType() {
            final ContentType contentType = body.contentType();
            if (contentType == null) return null;
            return MediaType.parse(contentType.toHeader());
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

    private static class OkRestHttpResponse extends RestHttpResponse {
        private final Response response;
        private TypedData body;

        public OkRestHttpResponse(Response response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.code();
        }

        @Override
        public List<Pair<String, String>> getHeaders() {
            final Headers headers = response.headers();
            final ArrayList<Pair<String, String>> headersList = new ArrayList<>();
            for (int i = 0, j = headers.size(); i < j; i++) {
                headersList.add(Pair.create(headers.name(i), headers.value(i)));
            }
            return headersList;
        }

        @Override
        public String getHeader(String name) {
            return response.header(name);
        }

        @Override
        public String[] getHeaders(String name) {
            final List<String> values = response.headers(name);
            return values.toArray(new String[values.size()]);
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
        public void writeTo(@NonNull OutputStream os) throws IOException {
            Utils.copyStream(stream(), os);
        }

        @NonNull
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
