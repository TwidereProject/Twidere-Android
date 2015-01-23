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

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.mariotaku.twidere.TwidereConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpRequest;
import twitter4j.http.HttpResponse;

/**
 * Created by mariotaku on 15/1/22.
 */
public class OkHttpClientImpl implements HttpClient, TwidereConstants {

    private final HttpClientConfiguration conf;
    private final OkHttpClient client;

    public OkHttpClientImpl(HttpClientConfiguration conf) {
        this.conf = conf;
        this.client = createHttpClient(conf);
    }

    private OkHttpClient createHttpClient(HttpClientConfiguration conf) {
        final OkHttpClient client = new OkHttpClient();
        if (conf.isSSLErrorIgnored()) {
        }
        return client;
    }

    @Override
    public HttpResponse request(HttpRequest req) throws TwitterException {
        final Builder builder = new Builder();
        for (Entry<String, String> headerEntry : req.getRequestHeaders().entrySet()) {
            builder.header(headerEntry.getKey(), headerEntry.getValue());
        }
        final Authorization authorization = req.getAuthorization();
        if (authorization != null) {
            final String authHeader = authorization.getAuthorizationHeader(req);
            if (authHeader != null) {
                builder.header("Authorization", authHeader);
            }
        }
        final String url;
        try {
            switch (req.getMethod()) {
                case GET: {
                    url = getUrl(req);
                    builder.get();
                    break;
                }
                case POST: {
                    url = req.getURL();
                    builder.post(getRequestBody(req.getParameters()));
                    break;
                }
                case DELETE: {
                    url = getUrl(req);
                    builder.delete();
                    break;
                }
                case HEAD: {
                    url = getUrl(req);
                    builder.head();
                    break;
                }
                case PUT: {
                    url = req.getURL();
                    builder.put(getRequestBody(req.getParameters()));
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }
            builder.url(url);
            final Response response = client.newCall(builder.build()).execute();
            Log.d(TwidereConstants.LOGTAG, String.format("OkHttpClient finished a request to %s with %s protocol", url, response.protocol().name()));
            return new OkHttpResponse(conf, null, response);
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    private String getUrl(HttpRequest req) {
        final Uri.Builder uri = Uri.parse(req.getURL()).buildUpon();
        for (HttpParameter param : req.getParameters()) {
            uri.appendQueryParameter(param.getName(), param.getValue());
        }
        return uri.build().toString();
    }

    public static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
    public static final MediaType MULTIPART_FORM_DATA = MediaType.parse("multipart/form-data; charset=UTF-8");

    private RequestBody getRequestBody(HttpParameter[] params) {
        if (params == null) return null;
        if (!HttpParameter.containsFile(params)) {
            return RequestBody.create(APPLICATION_FORM_URLENCODED, HttpParameter.encodeParameters(params));
        }
        if (params.length == 1) {
            final HttpParameter param = params[0];
            if (param.hasFileBody()) {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                return RequestBody.create(MediaType.parse(param.getContentType()), os.toByteArray());
            } else {
                return RequestBody.create(MediaType.parse(param.getContentType()), param.getFile());
            }
        }
        return null;
    }

    @Override
    public void shutdown() {

    }

    private static class OkHttpResponse extends HttpResponse {

        private final Response response;

        public OkHttpResponse(HttpClientConfiguration conf, HttpRequest request, Response response)
                throws TwitterException, IOException {
            super(conf);
            this.response = response;
            statusCode = response.code();
            if ("gzip".equals(response.header("Content-Encoding"))) {
                is = new GZIPInputStream(response.body().byteStream());
            } else {
                is = response.body().byteStream();
            }
            if (!response.isSuccessful()) {
                throw new TwitterException(response.message(), request, this);
            }
        }

        @Override
        public void disconnect() throws IOException {
            if (is != null) {
                is.close();
            }
        }

        @Override
        public String getResponseHeader(String name) {
            return response.header(name);
        }

        @Override
        public Map<String, List<String>> getResponseHeaderFields() {
            final Headers headers = response.headers();
            final Map<String, List<String>> maps = new HashMap<>();
            for (final String name : headers.names()) {
                final List<String> values = new ArrayList<>(1);
                for (final String value : headers.values(name)) {
                    values.add(value);
                }
                maps.put(name, values);
            }
            return maps;
        }
    }
}
