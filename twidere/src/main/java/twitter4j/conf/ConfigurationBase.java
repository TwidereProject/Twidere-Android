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

package twitter4j.conf;

import twitter4j.TwitterConstants;
import twitter4j.Version;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientFactory;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration base class with default settings.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class ConfigurationBase implements TwitterConstants, Configuration {

	static final boolean DEFAULT_USE_SSL = true;

	private boolean debug;
	private String userAgent;
	private String user;
	private String password;
	private boolean useSSL;
	private boolean ignoreSSLError;
	private boolean prettyDebug;
	private boolean gzipEnabled;
	private String httpProxyHost;
	private String httpProxyUser;
	private String httpProxyPassword;
	private int httpProxyPort;
	private int httpConnectionTimeout;
	private int httpReadTimeout;

	private int httpRetryCount;
	private int httpRetryIntervalSeconds;
	private int maxTotalConnections;
	private int defaultMaxPerRoute;
	private String oAuthConsumerKey;
	private String oAuthConsumerSecret;
	private String oAuthAccessToken;
	private String oAuthAccessTokenSecret;

	private String oAuthRequestTokenURL;
	private String oAuthAuthorizationURL;
	private String oAuthAccessTokenURL;
	private String oAuthAuthenticationURL;

	private String signingOAuthRequestTokenURL;
	private String signingOAuthAuthorizationURL;
	private String signingOAuthAccessTokenURL;
	private String signingOAuthAuthenticationURL;

	private String oAuthBaseURL = DEFAULT_OAUTH_BASE_URL;

	private String signingOAuthBaseURL;
	private String signingRestBaseURL;
	private String signingUploadBaseURL;

	private String restBaseURL;
	private String uploadBaseURL;

	private boolean includeRTsEnabled;

	private boolean includeEntitiesEnabled;

	private boolean includeTwitterClientHeader;

	// hidden portion
	private String clientVersion;
	private String clientURL;
	private String clientName;

	private HttpClientFactory httpClientFactory;
	private HostAddressResolverFactory hostAddressResolverFactory;

	// method for HttpRequestFactoryConfiguration
	Map<String, String> requestHeaders;

	private static final List<ConfigurationBase> instances = new ArrayList<ConfigurationBase>();

	protected ConfigurationBase() {
		setDebug(false);
		setUser(null);
		setPassword(null);
		setUseSSL(false);
		setPrettyDebugEnabled(false);
		setGZIPEnabled(true);
		setHttpProxyHost(null);
		setHttpProxyUser(null);
		setHttpProxyPassword(null);
		setHttpProxyPort(-1);
		setHttpConnectionTimeout(20000);
		setHttpReadTimeout(120000);
		setHttpRetryCount(0);
		setHttpRetryIntervalSeconds(5);
		setHttpMaxTotalConnections(20);
		setHttpDefaultMaxPerRoute(2);
		setHttpClientFactory(null);
		setOAuthConsumerKey(null);
		setOAuthConsumerSecret(null);
		setOAuthAccessToken(null);
		setOAuthAccessTokenSecret(null);
		setClientName("Twitter4J");
		setClientVersion(Version.getVersion());
		setClientURL("http://twitter4j.org/en/twitter4j-" + Version.getVersion() + ".xml");
		setHttpUserAgent("twitter4j http://twitter4j.org/ /" + Version.getVersion());

		setIncludeRTsEnbled(true);

		setIncludeEntitiesEnbled(true);

		setOAuthBaseURL(DEFAULT_OAUTH_BASE_URL);
		setSigningOAuthBaseURL(DEFAULT_SIGNING_OAUTH_BASE_URL);

		setRestBaseURL(DEFAULT_REST_BASE_URL);
		setSigningRestBaseURL(DEFAULT_SIGNING_REST_BASE_URL);

		setUploadBaseURL(DEFAULT_UPLOAD_BASE_URL);
		setSigningUploadBaseURL(DEFAULT_SIGNING_UPLOAD_BASE_URL);
		setIncludeRTsEnbled(true);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ConfigurationBase)) return false;
		final ConfigurationBase other = (ConfigurationBase) obj;
		if (clientName == null) {
			if (other.clientName != null) return false;
		} else if (!clientName.equals(other.clientName)) return false;
		if (clientURL == null) {
			if (other.clientURL != null) return false;
		} else if (!clientURL.equals(other.clientURL)) return false;
		if (clientVersion == null) {
			if (other.clientVersion != null) return false;
		} else if (!clientVersion.equals(other.clientVersion)) return false;
		if (debug != other.debug) return false;
		if (defaultMaxPerRoute != other.defaultMaxPerRoute) return false;
		if (gzipEnabled != other.gzipEnabled) return false;
		if (hostAddressResolverFactory == null) {
			if (other.hostAddressResolverFactory != null) return false;
		} else if (!hostAddressResolverFactory.equals(other.hostAddressResolverFactory)) return false;
		if (httpClientFactory == null) {
			if (other.httpClientFactory != null) return false;
		} else if (!httpClientFactory.equals(other.httpClientFactory)) return false;
		if (httpConnectionTimeout != other.httpConnectionTimeout) return false;
		if (httpProxyHost == null) {
			if (other.httpProxyHost != null) return false;
		} else if (!httpProxyHost.equals(other.httpProxyHost)) return false;
		if (httpProxyPassword == null) {
			if (other.httpProxyPassword != null) return false;
		} else if (!httpProxyPassword.equals(other.httpProxyPassword)) return false;
		if (httpProxyPort != other.httpProxyPort) return false;
		if (httpProxyUser == null) {
			if (other.httpProxyUser != null) return false;
		} else if (!httpProxyUser.equals(other.httpProxyUser)) return false;
		if (httpReadTimeout != other.httpReadTimeout) return false;
		if (httpRetryCount != other.httpRetryCount) return false;
		if (httpRetryIntervalSeconds != other.httpRetryIntervalSeconds) return false;
		if (ignoreSSLError != other.ignoreSSLError) return false;
		if (includeEntitiesEnabled != other.includeEntitiesEnabled) return false;
		if (includeRTsEnabled != other.includeRTsEnabled) return false;
		if (includeTwitterClientHeader != other.includeTwitterClientHeader) return false;
		if (maxTotalConnections != other.maxTotalConnections) return false;
		if (oAuthAccessToken == null) {
			if (other.oAuthAccessToken != null) return false;
		} else if (!oAuthAccessToken.equals(other.oAuthAccessToken)) return false;
		if (oAuthAccessTokenSecret == null) {
			if (other.oAuthAccessTokenSecret != null) return false;
		} else if (!oAuthAccessTokenSecret.equals(other.oAuthAccessTokenSecret)) return false;
		if (oAuthAccessTokenURL == null) {
			if (other.oAuthAccessTokenURL != null) return false;
		} else if (!oAuthAccessTokenURL.equals(other.oAuthAccessTokenURL)) return false;
		if (oAuthAuthenticationURL == null) {
			if (other.oAuthAuthenticationURL != null) return false;
		} else if (!oAuthAuthenticationURL.equals(other.oAuthAuthenticationURL)) return false;
		if (oAuthAuthorizationURL == null) {
			if (other.oAuthAuthorizationURL != null) return false;
		} else if (!oAuthAuthorizationURL.equals(other.oAuthAuthorizationURL)) return false;
		if (oAuthBaseURL == null) {
			if (other.oAuthBaseURL != null) return false;
		} else if (!oAuthBaseURL.equals(other.oAuthBaseURL)) return false;
		if (oAuthConsumerKey == null) {
			if (other.oAuthConsumerKey != null) return false;
		} else if (!oAuthConsumerKey.equals(other.oAuthConsumerKey)) return false;
		if (oAuthConsumerSecret == null) {
			if (other.oAuthConsumerSecret != null) return false;
		} else if (!oAuthConsumerSecret.equals(other.oAuthConsumerSecret)) return false;
		if (oAuthRequestTokenURL == null) {
			if (other.oAuthRequestTokenURL != null) return false;
		} else if (!oAuthRequestTokenURL.equals(other.oAuthRequestTokenURL)) return false;
		if (password == null) {
			if (other.password != null) return false;
		} else if (!password.equals(other.password)) return false;
		if (prettyDebug != other.prettyDebug) return false;
		if (requestHeaders == null) {
			if (other.requestHeaders != null) return false;
		} else if (!requestHeaders.equals(other.requestHeaders)) return false;
		if (restBaseURL == null) {
			if (other.restBaseURL != null) return false;
		} else if (!restBaseURL.equals(other.restBaseURL)) return false;
		if (signingOAuthAccessTokenURL == null) {
			if (other.signingOAuthAccessTokenURL != null) return false;
		} else if (!signingOAuthAccessTokenURL.equals(other.signingOAuthAccessTokenURL)) return false;
		if (signingOAuthAuthenticationURL == null) {
			if (other.signingOAuthAuthenticationURL != null) return false;
		} else if (!signingOAuthAuthenticationURL.equals(other.signingOAuthAuthenticationURL)) return false;
		if (signingOAuthAuthorizationURL == null) {
			if (other.signingOAuthAuthorizationURL != null) return false;
		} else if (!signingOAuthAuthorizationURL.equals(other.signingOAuthAuthorizationURL)) return false;
		if (signingOAuthBaseURL == null) {
			if (other.signingOAuthBaseURL != null) return false;
		} else if (!signingOAuthBaseURL.equals(other.signingOAuthBaseURL)) return false;
		if (signingOAuthRequestTokenURL == null) {
			if (other.signingOAuthRequestTokenURL != null) return false;
		} else if (!signingOAuthRequestTokenURL.equals(other.signingOAuthRequestTokenURL)) return false;
		if (signingRestBaseURL == null) {
			if (other.signingRestBaseURL != null) return false;
		} else if (!signingRestBaseURL.equals(other.signingRestBaseURL)) return false;
		if (useSSL != other.useSSL) return false;
		if (user == null) {
			if (other.user != null) return false;
		} else if (!user.equals(other.user)) return false;
		if (userAgent == null) {
			if (other.userAgent != null) return false;
		} else if (!userAgent.equals(other.userAgent)) return false;
		return true;
	}

	@Override
	public final String getClientName() {
		return clientName;
	}

	@Override
	public final String getClientURL() {
		return clientURL;
	}

	@Override
	public final String getClientVersion() {
		return clientVersion;
	}

	@Override
	public HostAddressResolverFactory getHostAddressResolverFactory() {
		return hostAddressResolverFactory;
	}

	@Override
	public HttpClientFactory getHttpClientFactory() {
		return httpClientFactory;
	}

	@Override
	public final int getHttpConnectionTimeout() {
		return httpConnectionTimeout;
	}

	@Override
	public final int getHttpDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}

	@Override
	public final int getHttpMaxTotalConnections() {
		return maxTotalConnections;
	}

	@Override
	public final String getHttpProxyHost() {
		return httpProxyHost;
	}

	@Override
	public final String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	@Override
	public final int getHttpProxyPort() {
		return httpProxyPort;
	}

	@Override
	public final String getHttpProxyUser() {
		return httpProxyUser;
	}

	@Override
	public final int getHttpReadTimeout() {
		return httpReadTimeout;
	}

	@Override
	public final int getHttpRetryCount() {
		return httpRetryCount;
	}

	@Override
	public final int getHttpRetryIntervalSeconds() {
		return httpRetryIntervalSeconds;
	}

	@Override
	public final String getHttpUserAgent() {
		return userAgent;
	}

	@Override
	public String getOAuthAccessToken() {
		return oAuthAccessToken;
	}

	@Override
	public String getOAuthAccessTokenSecret() {
		return oAuthAccessTokenSecret;
	}

	@Override
	public String getOAuthAccessTokenURL() {
		return oAuthAccessTokenURL;
	}

	@Override
	public String getOAuthAuthenticationURL() {
		return oAuthAuthenticationURL;
	}

	@Override
	public String getOAuthAuthorizationURL() {
		return oAuthAuthorizationURL;
	}

	@Override
	public String getOAuthBaseURL() {
		return oAuthBaseURL;
	}

	@Override
	public final String getOAuthConsumerKey() {
		return oAuthConsumerKey;
	}

	@Override
	public final String getOAuthConsumerSecret() {
		return oAuthConsumerSecret;
	}

	@Override
	public String getOAuthRequestTokenURL() {
		return oAuthRequestTokenURL;
	}

	@Override
	public final String getPassword() {
		return password;
	}

	@Override
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public String getRestBaseURL() {
		return restBaseURL;
	}

	@Override
	public String getSigningOAuthAccessTokenURL() {
		return signingOAuthAccessTokenURL != null ? signingOAuthAccessTokenURL : oAuthAccessTokenURL;
	}

	@Override
	public String getSigningOAuthAuthenticationURL() {
		return signingOAuthAuthenticationURL != null ? signingOAuthAuthenticationURL : oAuthAuthenticationURL;
	}

	@Override
	public String getSigningOAuthAuthorizationURL() {
		return signingOAuthAuthorizationURL != null ? signingOAuthAuthorizationURL : oAuthAuthorizationURL;
	}

	@Override
	public String getSigningOAuthBaseURL() {
		return signingOAuthBaseURL != null ? signingOAuthBaseURL : oAuthBaseURL;
	}

	@Override
	public String getSigningOAuthRequestTokenURL() {
		return signingOAuthRequestTokenURL != null ? signingOAuthRequestTokenURL : oAuthRequestTokenURL;
	}

	@Override
	public String getSigningRestBaseURL() {
		return signingRestBaseURL != null ? signingRestBaseURL : restBaseURL;
	}

	@Override
	public String getSigningUploadBaseURL() {
		return signingUploadBaseURL != null ? signingUploadBaseURL : uploadBaseURL;
	}

	@Override
	public String getUploadBaseURL() {
		return uploadBaseURL;
	}

	@Override
	public final String getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clientName == null ? 0 : clientName.hashCode());
		result = prime * result + (clientURL == null ? 0 : clientURL.hashCode());
		result = prime * result + (clientVersion == null ? 0 : clientVersion.hashCode());
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + defaultMaxPerRoute;
		result = prime * result + (gzipEnabled ? 1231 : 1237);
		result = prime * result + (hostAddressResolverFactory == null ? 0 : hostAddressResolverFactory.hashCode());
		result = prime * result + (httpClientFactory == null ? 0 : httpClientFactory.hashCode());
		result = prime * result + httpConnectionTimeout;
		result = prime * result + (httpProxyHost == null ? 0 : httpProxyHost.hashCode());
		result = prime * result + (httpProxyPassword == null ? 0 : httpProxyPassword.hashCode());
		result = prime * result + httpProxyPort;
		result = prime * result + (httpProxyUser == null ? 0 : httpProxyUser.hashCode());
		result = prime * result + httpReadTimeout;
		result = prime * result + httpRetryCount;
		result = prime * result + httpRetryIntervalSeconds;
		result = prime * result + (ignoreSSLError ? 1231 : 1237);
		result = prime * result + (includeEntitiesEnabled ? 1231 : 1237);
		result = prime * result + (includeRTsEnabled ? 1231 : 1237);
		result = prime * result + (includeTwitterClientHeader ? 1231 : 1237);
		result = prime * result + maxTotalConnections;
		result = prime * result + (oAuthAccessToken == null ? 0 : oAuthAccessToken.hashCode());
		result = prime * result + (oAuthAccessTokenSecret == null ? 0 : oAuthAccessTokenSecret.hashCode());
		result = prime * result + (oAuthAccessTokenURL == null ? 0 : oAuthAccessTokenURL.hashCode());
		result = prime * result + (oAuthAuthenticationURL == null ? 0 : oAuthAuthenticationURL.hashCode());
		result = prime * result + (oAuthAuthorizationURL == null ? 0 : oAuthAuthorizationURL.hashCode());
		result = prime * result + (oAuthBaseURL == null ? 0 : oAuthBaseURL.hashCode());
		result = prime * result + (oAuthConsumerKey == null ? 0 : oAuthConsumerKey.hashCode());
		result = prime * result + (oAuthConsumerSecret == null ? 0 : oAuthConsumerSecret.hashCode());
		result = prime * result + (oAuthRequestTokenURL == null ? 0 : oAuthRequestTokenURL.hashCode());
		result = prime * result + (password == null ? 0 : password.hashCode());
		result = prime * result + (prettyDebug ? 1231 : 1237);
		result = prime * result + (requestHeaders == null ? 0 : requestHeaders.hashCode());
		result = prime * result + (restBaseURL == null ? 0 : restBaseURL.hashCode());
		result = prime * result + (signingOAuthAccessTokenURL == null ? 0 : signingOAuthAccessTokenURL.hashCode());
		result = prime * result
				+ (signingOAuthAuthenticationURL == null ? 0 : signingOAuthAuthenticationURL.hashCode());
		result = prime * result + (signingOAuthAuthorizationURL == null ? 0 : signingOAuthAuthorizationURL.hashCode());
		result = prime * result + (signingOAuthBaseURL == null ? 0 : signingOAuthBaseURL.hashCode());
		result = prime * result + (signingOAuthRequestTokenURL == null ? 0 : signingOAuthRequestTokenURL.hashCode());
		result = prime * result + (signingRestBaseURL == null ? 0 : signingRestBaseURL.hashCode());
		result = prime * result + (useSSL ? 1231 : 1237);
		result = prime * result + (user == null ? 0 : user.hashCode());
		result = prime * result + (userAgent == null ? 0 : userAgent.hashCode());
		return result;
	}

	@Override
	public final boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public boolean isGZIPEnabled() {
		return gzipEnabled;
	}

	@Override
	public boolean isIncludeEntitiesEnabled() {
		return includeEntitiesEnabled;
	}

	@Override
	public boolean isIncludeRTsEnabled() {
		return includeRTsEnabled;
	}

	@Override
	public boolean isPrettyDebugEnabled() {
		return prettyDebug;
	}

	@Override
	public boolean isProxyConfigured() {
		return (getHttpProxyHost() != null || "".equals(getHttpProxyHost())) && getHttpProxyPort() > 0;
	}

	@Override
	public boolean isSSLEnabled() {
		return getRestBaseURL() != null && getRestBaseURL().startsWith("https://");
	}

	@Override
	public final boolean isSSLErrorIgnored() {
		return ignoreSSLError;
	}

	@Override
	public boolean isTwitterClientHeaderIncluded() {
		return includeTwitterClientHeader;
	}

	@Override
	public String toString() {
		return "ConfigurationBase{debug=" + debug + ", userAgent=" + userAgent + ", user=" + user + ", password="
				+ password + ", useSSL=" + useSSL + ", ignoreSSLError=" + ignoreSSLError + ", prettyDebug="
				+ prettyDebug + ", gzipEnabled=" + gzipEnabled + ", httpProxyHost=" + httpProxyHost
				+ ", httpProxyUser=" + httpProxyUser + ", httpProxyPassword=" + httpProxyPassword + ", httpProxyPort="
				+ httpProxyPort + ", httpConnectionTimeout=" + httpConnectionTimeout + ", httpReadTimeout="
				+ httpReadTimeout + ", httpRetryCount=" + httpRetryCount + ", httpRetryIntervalSeconds="
				+ httpRetryIntervalSeconds + ", maxTotalConnections=" + maxTotalConnections + ", defaultMaxPerRoute="
				+ defaultMaxPerRoute + ", oAuthConsumerKey=" + oAuthConsumerKey + ", oAuthConsumerSecret="
				+ oAuthConsumerSecret + ", oAuthAccessToken=" + oAuthAccessToken + ", oAuthAccessTokenSecret="
				+ oAuthAccessTokenSecret + ", oAuthRequestTokenURL=" + oAuthRequestTokenURL
				+ ", oAuthAuthorizationURL=" + oAuthAuthorizationURL + ", oAuthAccessTokenURL=" + oAuthAccessTokenURL
				+ ", oAuthAuthenticationURL=" + oAuthAuthenticationURL + ", signingOAuthRequestTokenURL="
				+ signingOAuthRequestTokenURL + ", signingOAuthAuthorizationURL=" + signingOAuthAuthorizationURL
				+ ", signingOAuthAccessTokenURL=" + signingOAuthAccessTokenURL + ", signingOAuthAuthenticationURL="
				+ signingOAuthAuthenticationURL + ", oAuthBaseURL=" + oAuthBaseURL + ", signingOAuthBaseURL="
				+ signingOAuthBaseURL + ", signingRestBaseURL=" + signingRestBaseURL + ", restBaseURL=" + restBaseURL
				+ ", includeRTsEnabled=" + includeRTsEnabled + ", includeEntitiesEnabled=" + includeEntitiesEnabled
				+ ", includeTwitterClientHeader=" + includeTwitterClientHeader + ", clientVersion=" + clientVersion
				+ ", clientURL=" + clientURL + ", clientName=" + clientName + ", httpClientFactory="
				+ httpClientFactory + ", hostAddressResolverFactory=" + hostAddressResolverFactory
				+ ", requestHeaders=" + requestHeaders + "}";
	}

	protected void cacheInstance() {
		cacheInstance(this);
	}

	// assures equality after deserializedation
	protected Object readResolve() throws ObjectStreamException {
		return getInstance(this);
	}

	protected final void setClientName(final String clientName) {
		this.clientName = clientName;
		initRequestHeaders();
	}

	protected final void setClientURL(final String clientURL) {
		this.clientURL = clientURL;
		initRequestHeaders();
	}

	protected final void setClientVersion(final String clientVersion) {
		this.clientVersion = clientVersion;
		initRequestHeaders();
	}

	protected final void setDebug(final boolean debug) {
		this.debug = debug;
	}

	protected final void setGZIPEnabled(final boolean gzipEnabled) {
		this.gzipEnabled = gzipEnabled;
		initRequestHeaders();
	}

	protected void setHostAddressResolverFactory(final HostAddressResolverFactory factory) {
		hostAddressResolverFactory = factory;
	}

	protected void setHttpClientFactory(final HttpClientFactory factory) {
		httpClientFactory = factory;
	}

	protected final void setHttpConnectionTimeout(final int connectionTimeout) {
		httpConnectionTimeout = connectionTimeout;
	}

	protected final void setHttpDefaultMaxPerRoute(final int defaultMaxPerRoute) {
		this.defaultMaxPerRoute = defaultMaxPerRoute;
	}

	protected final void setHttpMaxTotalConnections(final int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	protected final void setHttpProxyHost(final String proxyHost) {
		httpProxyHost = proxyHost;
		initRequestHeaders();
	}

	protected final void setHttpProxyPassword(final String proxyPassword) {
		httpProxyPassword = proxyPassword;
	}

	protected final void setHttpProxyPort(final int proxyPort) {
		httpProxyPort = proxyPort;
		initRequestHeaders();
	}

	protected final void setHttpProxyUser(final String proxyUser) {
		httpProxyUser = proxyUser;
	}

	protected final void setHttpReadTimeout(final int readTimeout) {
		httpReadTimeout = readTimeout;
	}

	protected final void setHttpRetryCount(final int retryCount) {
		httpRetryCount = retryCount;
	}

	protected final void setHttpRetryIntervalSeconds(final int retryIntervalSeconds) {
		httpRetryIntervalSeconds = retryIntervalSeconds;
	}

	protected final void setHttpUserAgent(final String userAgent) {
		this.userAgent = userAgent;
		initRequestHeaders();
	}

	protected final void setIgnoreSSLError(final boolean ignore) {
		ignoreSSLError = ignore;
		initRequestHeaders();
	}

	protected final void setIncludeEntitiesEnbled(final boolean enabled) {
		includeEntitiesEnabled = enabled;
	}

	protected final void setIncludeRTsEnbled(final boolean enabled) {
		includeRTsEnabled = enabled;
	}

	protected final void setIncludeTwitterClientHeader(final boolean includeHeader) {
		includeTwitterClientHeader = includeHeader;
		initRequestHeaders();
	}

	protected final void setOAuthAccessToken(final String accessToken) {
		oAuthAccessToken = accessToken;
	}

	protected final void setOAuthAccessTokenSecret(final String accessTokenSecret) {
		oAuthAccessTokenSecret = accessTokenSecret;
	}

	protected final void setOAuthBaseURL(String oAuthBaseURL) {
		if (isNullOrEmpty(oAuthBaseURL)) {
			oAuthBaseURL = DEFAULT_OAUTH_BASE_URL;
		}
		this.oAuthBaseURL = fixURLSlash(oAuthBaseURL);

		oAuthAccessTokenURL = oAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN;
		oAuthAuthenticationURL = oAuthBaseURL + PATH_SEGMENT_AUTHENTICATION;
		oAuthAuthorizationURL = oAuthBaseURL + PATH_SEGMENT_AUTHORIZATION;
		oAuthRequestTokenURL = oAuthBaseURL + PATH_SEGMENT_REQUEST_TOKEN;

		setSigningOAuthBaseURL(oAuthBaseURL);
		fixOAuthBaseURL();
	}

	protected final void setOAuthConsumerKey(final String oAuthConsumerKey) {
		this.oAuthConsumerKey = oAuthConsumerKey;
		fixRestBaseURL();
	}

	protected final void setOAuthConsumerSecret(final String oAuthConsumerSecret) {
		this.oAuthConsumerSecret = oAuthConsumerSecret;
		fixRestBaseURL();
	}

	protected final void setPassword(final String password) {
		this.password = password;
	}

	protected final void setPrettyDebugEnabled(final boolean prettyDebug) {
		this.prettyDebug = prettyDebug;
	}

	protected final void setRestBaseURL(String restBaseURL) {
		if (isNullOrEmpty(restBaseURL)) {
			restBaseURL = DEFAULT_REST_BASE_URL;
		}
		this.restBaseURL = fixURLSlash(restBaseURL);
		fixRestBaseURL();
	}

	protected final void setSigningOAuthBaseURL(String signingOAuthBaseURL) {
		if (isNullOrEmpty(signingOAuthBaseURL)) {
			signingOAuthBaseURL = DEFAULT_SIGNING_OAUTH_BASE_URL;
		}
		this.signingOAuthBaseURL = fixURLSlash(signingOAuthBaseURL);

		signingOAuthAccessTokenURL = signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN;
		signingOAuthAuthenticationURL = signingOAuthBaseURL + PATH_SEGMENT_AUTHENTICATION;
		signingOAuthAuthorizationURL = signingOAuthBaseURL + PATH_SEGMENT_AUTHORIZATION;
		signingOAuthRequestTokenURL = signingOAuthBaseURL + PATH_SEGMENT_REQUEST_TOKEN;

		fixOAuthBaseURL();
	}

	protected final void setSigningRestBaseURL(String signingRestBaseURL) {
		if (isNullOrEmpty(signingRestBaseURL)) {
			signingRestBaseURL = DEFAULT_SIGNING_REST_BASE_URL;
		}
		this.signingRestBaseURL = fixURLSlash(signingRestBaseURL);
		fixRestBaseURL();
	}

	protected void setSigningUploadBaseURL(String signingUploadBaseURL) {
		if (isNullOrEmpty(signingUploadBaseURL)) {
			signingUploadBaseURL = DEFAULT_SIGNING_UPLOAD_BASE_URL;
		}
		this.signingUploadBaseURL = fixURLSlash(signingUploadBaseURL);
		fixUploadBaseURL();
	}

	protected void setUploadBaseURL(String uploadBaseURL) {
		if (isNullOrEmpty(uploadBaseURL)) {
			uploadBaseURL = DEFAULT_UPLOAD_BASE_URL;
		}
		this.uploadBaseURL = fixURLSlash(uploadBaseURL);
		fixUploadBaseURL();
	}

	protected final void setUser(final String user) {
		this.user = user;
	}

	protected final void setUseSSL(final boolean useSSL) {
		this.useSSL = useSSL;
		fixRestBaseURL();
	}

	private void fixOAuthBaseURL() {
		if (DEFAULT_OAUTH_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, oAuthBaseURL))) {
			oAuthBaseURL = fixURL(useSSL, oAuthBaseURL);
		}
		if (oAuthBaseURL != null && oAuthBaseURL.equals(fixURL(DEFAULT_USE_SSL, signingOAuthBaseURL))) {
			signingOAuthBaseURL = fixURL(useSSL, signingOAuthBaseURL);
		}
		if (oAuthBaseURL != null
				&& (oAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, oAuthAccessTokenURL))) {
			oAuthAccessTokenURL = fixURL(useSSL, oAuthAccessTokenURL);
		}
		if (oAuthBaseURL != null
				&& (oAuthBaseURL + PATH_SEGMENT_AUTHENTICATION).equals(fixURL(DEFAULT_USE_SSL, oAuthAuthenticationURL))) {
			oAuthAuthenticationURL = fixURL(useSSL, oAuthAuthenticationURL);
		}
		if (oAuthBaseURL != null
				&& (oAuthBaseURL + PATH_SEGMENT_AUTHORIZATION).equals(fixURL(DEFAULT_USE_SSL, oAuthAuthorizationURL))) {
			oAuthAuthorizationURL = fixURL(useSSL, oAuthAuthorizationURL);
		}
		if (oAuthBaseURL != null
				&& (oAuthBaseURL + PATH_SEGMENT_REQUEST_TOKEN).equals(fixURL(DEFAULT_USE_SSL, oAuthRequestTokenURL))) {
			oAuthRequestTokenURL = fixURL(useSSL, oAuthRequestTokenURL);
		}
		if (signingOAuthBaseURL != null
				&& (signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL,
						signingOAuthAccessTokenURL))) {
			signingOAuthAccessTokenURL = fixURL(useSSL, signingOAuthAccessTokenURL);
		}
		if (signingOAuthBaseURL != null
				&& (signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL,
						signingOAuthAuthenticationURL))) {
			signingOAuthAuthenticationURL = fixURL(useSSL, signingOAuthAuthenticationURL);
		}
		if (signingOAuthBaseURL != null
				&& (signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL,
						signingOAuthAuthorizationURL))) {
			signingOAuthAuthorizationURL = fixURL(useSSL, signingOAuthAuthorizationURL);
		}
		if (signingOAuthBaseURL != null
				&& (signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL,
						signingOAuthRequestTokenURL))) {
			signingOAuthRequestTokenURL = fixURL(useSSL, signingOAuthRequestTokenURL);
		}
		initRequestHeaders();
	}

	private void fixRestBaseURL() {
		if (DEFAULT_REST_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, restBaseURL))) {
			restBaseURL = fixURL(useSSL, restBaseURL);
		}
		if (restBaseURL != null && restBaseURL.equals(fixURL(DEFAULT_USE_SSL, signingRestBaseURL))) {
			signingRestBaseURL = fixURL(useSSL, signingRestBaseURL);
		}
		initRequestHeaders();
	}

	private void fixUploadBaseURL() {
		if (DEFAULT_UPLOAD_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, uploadBaseURL))) {
			uploadBaseURL = fixURL(useSSL, uploadBaseURL);
		}
		if (uploadBaseURL != null && uploadBaseURL.equals(fixURL(DEFAULT_USE_SSL, uploadBaseURL))) {
			uploadBaseURL = fixURL(useSSL, uploadBaseURL);
		}
		initRequestHeaders();
	}

	final void initRequestHeaders() {
		requestHeaders = new HashMap<String, String>();
		if (includeTwitterClientHeader) {
			requestHeaders.put("X-Twitter-Client-Version", getClientVersion());
			requestHeaders.put("X-Twitter-Client-URL", getClientURL());
			requestHeaders.put("X-Twitter-Client", getClientName());
		}

		requestHeaders.put("User-Agent", getHttpUserAgent());
		if (gzipEnabled) {
			requestHeaders.put("Accept-Encoding", "gzip");
		}
		// I found this may cause "Socket is closed" error in Android, so I
		// changed it to "keep-alive".
		if (!isNullOrEmpty(httpProxyHost) && httpProxyPort > 0) {
			requestHeaders.put("Connection", "keep-alive");
		} else {
			requestHeaders.put("Connection", "close");
		}
	}

	private static void cacheInstance(final ConfigurationBase conf) {
		if (!instances.contains(conf)) {
			instances.add(conf);
		}
	}

	private static ConfigurationBase getInstance(final ConfigurationBase configurationBase) {
		int index;
		if ((index = instances.indexOf(configurationBase)) == -1) {
			instances.add(configurationBase);
			return configurationBase;
		} else
			return instances.get(index);
	}

	static String fixURL(final boolean useSSL, String url) {
		if (null == url) return null;
		if (!url.startsWith("http://") || !url.startsWith("https://")) {
			url = "https://" + url;
		}
		final int index = url.indexOf("://");
		if (-1 == index) throw new IllegalArgumentException("url should contain '://'");
		final String hostAndLater = url.substring(index + 3);
		if (useSSL)
			return "https://" + hostAndLater;
		else
			return "http://" + hostAndLater;
	}

	static String fixURLSlash(final String urlOrig) {
		if (urlOrig == null) return null;
		if (!urlOrig.endsWith("/")) return urlOrig + "/";
		return urlOrig;
	}

	static boolean isNullOrEmpty(final String string) {
		if (string == null) return true;
		if (string.length() == 0) return true;
		return false;
	}

}
