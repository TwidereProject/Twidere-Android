/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.http;

import static twitter4j.http.RequestMethod.POST;
import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.logging.Logger;
import twitter4j.internal.util.InternalStringUtil;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class HttpClientImpl extends HttpClientBase implements HttpClient, HttpResponseCode {
	private static final Logger logger = Logger.getLogger(HttpClientImpl.class);

	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new TrustAllX509TrustManager() };

	private static final SSLSocketFactory IGNORE_ERROR_SSL_FACTORY;

	static {
		System.setProperty("http.keepAlive", "false");
		SSLSocketFactory factory = null;
		try {
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
			factory = sc.getSocketFactory();
		} catch (final KeyManagementException e) {
		} catch (final NoSuchAlgorithmException e) {
		}
		IGNORE_ERROR_SSL_FACTORY = factory;
	}

	private static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();

	private static final Map<HttpClientConfiguration, HttpClient> instanceMap = new HashMap<HttpClientConfiguration, HttpClient>(
			1);

	public HttpClientImpl() {
		super(ConfigurationContext.getInstance());
	};

	public HttpClientImpl(final HttpClientConfiguration conf) {
		super(conf);
	}

	public HttpResponse get(final String url, final String sign_url) throws TwitterException {
		return request(new HttpRequest(RequestMethod.GET, url, sign_url, null, null, null));
	}

	public HttpResponse post(final String url, final String sign_url, final HttpParameter[] params)
			throws TwitterException {
		return request(new HttpRequest(RequestMethod.POST, url, sign_url, params, null, null));
	}

	@Override
	public HttpResponse request(final HttpRequest req) throws TwitterException {
		int retriedCount;
		final int retry = CONF.getHttpRetryCount() + 1;
		HttpResponse res = null;
		for (retriedCount = 0; retriedCount < retry; retriedCount++) {
			int responseCode = -1;
			try {
				HttpURLConnection con;
				OutputStream os = null;
				try {
					con = getConnection(req.getURL());
					con.setDoInput(true);
					setHeaders(req, con);
					con.setRequestMethod(req.getMethod().name());
					final HttpParameter[] params = req.getParameters();
					if (req.getMethod() == POST) {
						if (HttpParameter.containsFile(params)) {
							String boundary = "----Twitter4J-upload" + System.currentTimeMillis();
							con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
							boundary = "--" + boundary;
							con.setDoOutput(true);
							os = con.getOutputStream();
							final DataOutputStream out = new DataOutputStream(os);
							for (final HttpParameter param : params) {
								if (param.isFile()) {
									write(out, boundary + "\r\n");
									write(out, "Content-Disposition: form-data; name=\"" + param.getName()
											+ "\"; filename=\"" + param.getFileName() + "\"\r\n");
									write(out, "Content-Type: " + param.getContentType() + "\r\n\r\n");
									final BufferedInputStream in = new BufferedInputStream(
											param.hasFileBody() ? param.getFileBody() : new FileInputStream(
													param.getFile()));
									int buff;
									while ((buff = in.read()) != -1) {
										out.write(buff);
									}
									write(out, "\r\n");
									in.close();
								} else {
									write(out, boundary + "\r\n");
									write(out, "Content-Disposition: form-data; name=\"" + param.getName() + "\"\r\n");
									write(out, "Content-Type: text/plain; charset=UTF-8\r\n\r\n");
									logger.debug(param.getValue());
									out.write(param.getValue().getBytes("UTF-8"));
									write(out, "\r\n");
								}
							}
							write(out, boundary + "--\r\n");
							write(out, "\r\n");

						} else {
							con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
							final String postParam = HttpParameter.encodeParameters(req.getParameters());
							logger.debug("Post Params: ", postParam);
							final byte[] bytes = postParam.getBytes("UTF-8");
							con.setRequestProperty("Content-Length", Integer.toString(bytes.length));
							con.setDoOutput(true);
							os = con.getOutputStream();
							os.write(bytes);
						}
						os.flush();
						os.close();
					}
					res = new HttpResponseImpl(con, CONF);
					responseCode = con.getResponseCode();
					if (logger.isDebugEnabled()) {
						logger.debug("Response: ");
						final Map<String, List<String>> responseHeaders = con.getHeaderFields();
						for (final String key : responseHeaders.keySet()) {
							final List<String> values = responseHeaders.get(key);
							for (final String value : values) {
								if (key != null) {
									logger.debug(key + ": " + value);
								} else {
									logger.debug(value);
								}
							}
						}
					}
					if (responseCode < OK || responseCode > ACCEPTED) {
						if (responseCode == ENHANCE_YOUR_CLAIM || responseCode == BAD_REQUEST
								|| responseCode < INTERNAL_SERVER_ERROR || retriedCount == CONF.getHttpRetryCount())
							throw new TwitterException(res.asString(), req, res);
					} else {
						break;
					}
				} finally {
					try {
						if (os != null) {
							os.close();
						}
					} catch (final IOException ignore) {
					}
				}
			} catch (final IOException ioe) {
				// connection timeout or read timeout
				if (retriedCount == CONF.getHttpRetryCount())
				// throw new TwitterException(ioe.getMessage(), ioe,
				// responseCode);
					throw new TwitterException(ioe.getMessage(), req, res);
			} catch (final NullPointerException e) {
				// This exception will be thown when URL is invalid.
				e.printStackTrace();
				throw new TwitterException("The URL requested is invalid.", e);
			} catch (final OutOfMemoryError e) {
				throw new TwitterException(e.getMessage(), e);
			}
			try {
				if (logger.isDebugEnabled() && res != null) {
					res.asString();
				}
				logger.debug("Sleeping " + CONF.getHttpRetryIntervalSeconds() + " seconds until the next retry.");
				Thread.sleep(CONF.getHttpRetryIntervalSeconds() * 1000);
			} catch (final InterruptedException ignore) {
				// nothing to do
			}
		}
		return res;
	}

	private HttpURLConnection getConnection(final String url_string) throws IOException {

		final HttpURLConnection con;
		final Proxy proxy;
		if (isProxyConfigured()) {
			if (CONF.getHttpProxyUser() != null && !CONF.getHttpProxyUser().equals("")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy AuthUser: " + CONF.getHttpProxyUser());
					logger.debug("Proxy AuthPassword: " + InternalStringUtil.maskString(CONF.getHttpProxyPassword()));
				}
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						// respond only to proxy auth requests
						if (getRequestorType().equals(RequestorType.PROXY))
							return new PasswordAuthentication(CONF.getHttpProxyUser(), CONF.getHttpProxyPassword()
									.toCharArray());
						else
							return null;
					}
				});
			}
			proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(CONF.getHttpProxyHost(),
					CONF.getHttpProxyPort()));
			if (logger.isDebugEnabled()) {
				logger.debug("Opening proxied connection(" + CONF.getHttpProxyHost() + ":" + CONF.getHttpProxyPort()
						+ ")");
			}
		} else {
			proxy = Proxy.NO_PROXY;
		}
		final HostAddressResolver resolver = FactoryUtils.getHostAddressResolver(CONF);
		final URI url_orig;
		try {
			url_orig = new URI(url_string);
		} catch (final URISyntaxException e) {
			throw new IOException("Invalid URI " + url_string);
		}
		final String host = url_orig.getHost(), authority = url_orig.getAuthority();
		final String resolved_host = resolver != null ? resolver.resolve(host) : null;
		con = (HttpURLConnection) new URL(resolved_host != null ? url_string.replace("://" + host, "://"
				+ resolved_host) : url_string).openConnection(proxy);
		if (resolved_host != null && !host.equals(resolved_host)) {
			con.setRequestProperty("Host", authority);
		}
		if (CONF.getHttpConnectionTimeout() > 0) {
			con.setConnectTimeout(CONF.getHttpConnectionTimeout());
		}
		if (CONF.getHttpReadTimeout() > 0) {
			con.setReadTimeout(CONF.getHttpReadTimeout());
		}
		con.setInstanceFollowRedirects(false);
		if (con instanceof HttpsURLConnection && CONF.isSSLErrorIgnored()) {
			((HttpsURLConnection) con).setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
			if (IGNORE_ERROR_SSL_FACTORY != null) {
				((HttpsURLConnection) con).setSSLSocketFactory(IGNORE_ERROR_SSL_FACTORY);
			}
		}
		return con;
	}

	/**
	 * sets HTTP headers
	 * 
	 * @param req The request
	 * @param connection HttpURLConnection
	 */
	private void setHeaders(final HttpRequest req, final HttpURLConnection connection) {
		if (logger.isDebugEnabled()) {
			logger.debug("Request: ");
			logger.debug(req.getMethod().name() + " ", req.getURL());
		}

		String authorizationHeader;
		if (req.getAuthorization() != null
				&& (authorizationHeader = req.getAuthorization().getAuthorizationHeader(req)) != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Authorization: ", InternalStringUtil.maskString(authorizationHeader));
			}
			connection.addRequestProperty("Authorization", authorizationHeader);
		}
		final Map<String, String> req_headers = req.getRequestHeaders();
		if (req_headers != null) {
			for (final String key : req_headers.keySet()) {
				connection.addRequestProperty(key, req.getRequestHeaders().get(key));
				logger.debug(key + ": " + req.getRequestHeaders().get(key));
			}
		}
	}

	public static String encode(final String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (final java.io.UnsupportedEncodingException neverHappen) {
			throw new AssertionError("will never happen");
		}
	}

	public static HttpClient getInstance(final HttpClientConfiguration conf) {
		HttpClient client = instanceMap.get(conf);
		if (null == client) {
			client = new HttpClientImpl(conf);
			instanceMap.put(conf, client);
		}
		return client;
	}

	static class AllowAllHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(final String hostname, final SSLSession session) {
			return true;
		}
	}

	final static class TrustAllX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}
}
