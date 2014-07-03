/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.util.net;

import static android.text.TextUtils.isEmpty;

import android.content.Context;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.methods.HttpPutHC4;
import org.apache.http.client.methods.HttpRequestBaseHC4;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContextHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.TextUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.net.ssl.HostResolvedSSLConnectionSocketFactory;
import org.mariotaku.twidere.util.net.ssl.TwidereSSLSocketFactory;

import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.http.FactoryUtils;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponseCode;
import twitter4j.http.RequestMethod;
import twitter4j.internal.logging.Logger;
import twitter4j.internal.util.InternalStringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

/**
 * HttpClient implementation for Apache HttpClient 4.0.x
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class TwidereHttpClientImpl implements twitter4j.http.HttpClient, HttpResponseCode {
	private static final Logger logger = Logger.getLogger(TwidereHttpClientImpl.class);
	private final HttpClientConfiguration conf;
	private final CloseableHttpClient client;

	public TwidereHttpClientImpl(final Context context, final HttpClientConfiguration conf) {
		this.conf = conf;
		final HttpClientBuilder clientBuilder = HttpClients.custom();
		final LayeredConnectionSocketFactory factory = TwidereSSLSocketFactory.getSocketFactory(context,
				conf.isSSLErrorIgnored());
		clientBuilder.setSSLSocketFactory(factory);
		final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setConnectionRequestTimeout(conf.getHttpConnectionTimeout());
		requestConfigBuilder.setConnectTimeout(conf.getHttpConnectionTimeout());
		requestConfigBuilder.setSocketTimeout(conf.getHttpReadTimeout());
		requestConfigBuilder.setRedirectsEnabled(false);
		clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
		if (conf.isProxyConfigured()) {
			final HttpHost proxy = new HttpHost(conf.getHttpProxyHost(), conf.getHttpProxyPort());
			clientBuilder.setProxy(proxy);
			if (!TextUtils.isEmpty(conf.getHttpProxyUser())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy AuthUser: " + conf.getHttpProxyUser());
					logger.debug("Proxy AuthPassword: " + InternalStringUtil.maskString(conf.getHttpProxyPassword()));
				}
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(conf.getHttpProxyHost(), conf.getHttpProxyPort()),
						new UsernamePasswordCredentials(conf.getHttpProxyUser(), conf.getHttpProxyPassword()));
				clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
		}
		client = clientBuilder.build();
	}

	@Override
	public twitter4j.http.HttpResponse request(final twitter4j.http.HttpRequest req) throws TwitterException {
		final HostAddressResolver resolver = FactoryUtils.getHostAddressResolver(conf);
		final String urlString = req.getURL();
		final URI urlOrig = ParseUtils.parseURI(urlString);
		final String host = urlOrig.getHost(), authority = urlOrig.getAuthority();
		try {
			HttpRequestBaseHC4 commonsRequest;
			final String resolvedHost = resolver != null ? resolver.resolve(host) : null;
			final String resolvedUrl = !isEmpty(resolvedHost) ? urlString.replace("://" + host, "://" + resolvedHost)
					: urlString;
			final RequestMethod method = req.getMethod();
			if (method == RequestMethod.GET) {
				commonsRequest = new HttpGetHC4(resolvedUrl);
			} else if (method == RequestMethod.POST) {
				final HttpPostHC4 post = new HttpPostHC4(resolvedUrl);
				post.setEntity(getAsEntity(req.getParameters()));
				post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
				commonsRequest = post;
			} else if (method == RequestMethod.DELETE) {
				commonsRequest = new HttpDeleteHC4(resolvedUrl);
			} else if (method == RequestMethod.HEAD) {
				commonsRequest = new HttpHeadHC4(resolvedUrl);
			} else if (method == RequestMethod.PUT) {
				final HttpPutHC4 put = new HttpPutHC4(resolvedUrl);
				put.setEntity(getAsEntity(req.getParameters()));
				commonsRequest = put;
			} else
				throw new TwitterException("Unsupported request method " + method);
			final HttpParams httpParams = commonsRequest.getParams();
			HttpClientParams.setRedirecting(httpParams, false);
			final Map<String, String> headers = req.getRequestHeaders();
			for (final String headerName : headers.keySet()) {
				commonsRequest.addHeader(headerName, headers.get(headerName));
			}
			final Authorization authorization = req.getAuthorization();
			final String authorizationHeader = authorization != null ? authorization.getAuthorizationHeader(req) : null;
			if (authorizationHeader != null) {
				commonsRequest.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
			}
			if (resolvedHost != null && !resolvedHost.isEmpty() && !resolvedHost.equals(host)) {
				commonsRequest.addHeader(HttpHeaders.HOST, authority);
			}

			final ApacheHttpClientHttpResponseImpl res;
			try {
				final HttpContext httpContext = new BasicHttpContextHC4();
				httpContext.setAttribute(HostResolvedSSLConnectionSocketFactory.HTTP_CONTEXT_KEY_ORIGINAL_HOST, host);
				res = new ApacheHttpClientHttpResponseImpl(client.execute(commonsRequest, httpContext), conf);
			} catch (final IllegalStateException e) {
				throw new TwitterException("Please check your API settings.", e);
			} catch (final NullPointerException e) {
				// Bug http://code.google.com/p/android/issues/detail?id=5255
				throw new TwitterException("Please check your APN settings, make sure not to use WAP APNs.", e);
			} catch (final OutOfMemoryError e) {
				// I don't know why OOM thown, but it should be catched.
				System.gc();
				throw new TwitterException("Unknown error", e);
			}
			final int statusCode = res.getStatusCode();
			if (statusCode < OK || statusCode > ACCEPTED) throw new TwitterException(res.asString(), req, res);
			return res;
		} catch (final IOException e) {
			// TODO
			if (resolver instanceof TwidereHostAddressResolver) {
				final TwidereHostAddressResolver twidereResolver = (TwidereHostAddressResolver) resolver;
				twidereResolver.removeCachedHost(host);
			}
			throw new TwitterException(e);
		}
	}

	@Override
	public void shutdown() {
		Utils.closeSilently(client);
	}

	private static HttpEntity getAsEntity(final HttpParameter[] params) throws UnsupportedEncodingException {
		if (params == null) return null;
		if (!HttpParameter.containsFile(params)) return new HttpParameterFormEntity(params);
		final MultipartEntityBuilder me = MultipartEntityBuilder.create();
		for (final HttpParameter param : params) {
			if (param.isFile()) {
				final ContentType contentType = ContentType.create(param.getContentType());
				final ContentBody body;
				if (param.getFile() != null) {
					body = new FileBody(param.getFile(), ContentType.create(param.getContentType()));
				} else {
					body = new InputStreamBody(param.getFileBody(), contentType, param.getFileName());
				}
				me.addPart(param.getName(), body);
			} else {
				final ContentType contentType = ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8);
				final ContentBody body = new StringBody(param.getValue(), contentType);
				me.addPart(param.getName(), body);
			}
		}
		return me.build();
	}
}
