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

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.twidere.TwidereConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.SocketFactory;

import okio.BufferedSink;
import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpRequest;
import twitter4j.http.HttpResponse;
import twitter4j.http.RequestMethod;

/**
 * Created by mariotaku on 15/1/22.
 */
public class OkHttpClientImpl implements HttpClient, TwidereConstants {

    public static final MediaType APPLICATION_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
    private final Context context;
    private final HttpClientConfiguration conf;
    private final OkHttpClient client;
    private final HostAddressResolver resolver;

    public OkHttpClientImpl(Context context, HttpClientConfiguration conf) {
        this.context = context;
        this.conf = conf;
        this.resolver = conf.getHostAddressResolverFactory().getInstance(conf);
        this.client = createHttpClient(conf);
    }

    @Override
    public HttpResponse request(HttpRequest req) throws TwitterException {
        final Builder builder = new Builder();
        for (Entry<String, List<String>> headerEntry : req.getRequestHeaders().entrySet()) {
            final String name = headerEntry.getKey();
            for (String value : headerEntry.getValue()) {
                builder.addHeader(name, value);
            }
        }
        final Authorization authorization = req.getAuthorization();
        if (authorization != null) {
            final String authHeader = authorization.getAuthorizationHeader(req);
            if (authHeader != null) {
                builder.header("Authorization", authHeader);
            }
        }
        Response response = null;
        try {
            setupRequestBuilder(builder, req);
             response = client.newCall(builder.build()).execute();
            return new OkHttpResponse(conf, null, response);
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    @Override
    public void shutdown() {

    }

    private OkHttpClient createHttpClient(HttpClientConfiguration conf) {
        final OkHttpClient client = new OkHttpClient();
        final boolean ignoreSSLError = conf.isSSLErrorIgnored();
        final SSLCertificateSocketFactory sslSocketFactory;
        if (ignoreSSLError) {
            sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getInsecure(0, null);
        } else {
            sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0, null);
        }
//        sslSocketFactory.setTrustManagers(new TrustManager[]{new TwidereTrustManager(context)});
//        client.setHostnameVerifier(new HostResolvedHostnameVerifier(context, ignoreSSLError));
        client.setSslSocketFactory(sslSocketFactory);
        client.setSocketFactory(SocketFactory.getDefault());
        client.setConnectTimeout(conf.getHttpConnectionTimeout(), TimeUnit.MILLISECONDS);

        if (conf.isProxyConfigured()) {
            client.setProxy(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(conf.getHttpProxyHost(),
                    conf.getHttpProxyPort())));
        }
        Internal.instance.setNetwork(client, new Network() {
            @Override
            public InetAddress[] resolveInetAddresses(String host) throws UnknownHostException {
                try {
                    return resolver.resolve(host);
                } catch (IOException e) {
                    if (e instanceof UnknownHostException) throw (UnknownHostException) e;
                    throw new UnknownHostException("Unable to resolve address " + e.getMessage());
                }
            }
        });
        return client;
    }

    private RequestBody getRequestBody(HttpParameter[] params) throws IOException {
        if (params == null) return null;
        if (!HttpParameter.containsFile(params)) {
            return RequestBody.create(APPLICATION_FORM_URLENCODED, HttpParameter.encodeParameters(params));
        }
        final MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        for (final HttpParameter param : params) {
            if (param.isFile()) {
                RequestBody requestBody;
                if (param.hasFileBody()) {
                    requestBody = new StreamRequestBody(MediaType.parse(param.getContentType()), param.getFileBody(), true);
                } else {
                    requestBody = RequestBody.create(MediaType.parse(param.getContentType()), param.getFile());
                }
                builder.addFormDataPart(param.getName(), param.getFileName(), requestBody);
            } else {
                builder.addFormDataPart(param.getName(), param.getValue());
            }
        }
        return builder.build();
    }

    static class StreamRequestBody extends RequestBody {

        private final MediaType contentType;
        private final InputStream stream;
        private final boolean closeAfterWrite;

        StreamRequestBody(MediaType contentType, InputStream stream, boolean closeAfterWrite) {
            this.contentType = contentType;
            this.stream = stream;
            this.closeAfterWrite = closeAfterWrite;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            int len;
            byte[] buf = new byte[8192];
            while ((len = stream.read(buf)) != -1) {
                sink.write(buf, 0, len);
            }
            if (closeAfterWrite) {
                IoUtils.closeSilently(stream);
            }
        }
    }

    private void setupRequestBuilder(Builder builder, HttpRequest req) throws IOException {
        final Uri.Builder uriBuilder = Uri.parse(req.getURL()).buildUpon();
        final RequestMethod method = req.getMethod();
        if (method != RequestMethod.POST && method != RequestMethod.PUT) {
            final HttpParameter[] parameters = req.getParameters();
            if (parameters != null) {
                for (HttpParameter param : parameters) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }
            }
        }
        final Uri uri = uriBuilder.build();
        switch (req.getMethod()) {
            case GET: {
                builder.get();
                break;
            }
            case POST: {
                builder.post(getRequestBody(req.getParameters()));
                break;
            }
            case DELETE: {
                builder.delete();
                break;
            }
            case HEAD: {
                builder.head();
                break;
            }
            case PUT: {
                builder.put(getRequestBody(req.getParameters()));
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        builder.url(uri.toString());
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

        @Override
        public List<String> getResponseHeaders(String name) {
            return response.headers(name);
        }
    }
}
