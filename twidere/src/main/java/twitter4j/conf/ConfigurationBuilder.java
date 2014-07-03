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

import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientFactory;

/**
 * A builder that can be used to construct a twitter4j configuration with
 * desired settings. This builder has sensible defaults such that
 * {@code new ConfigurationBuilder().build()} would create a usable
 * configuration. This configuration builder is useful for clients that wish to
 * configure twitter4j in unit tests or from command line flags for example.
 * 
 * @author John Sirois - john.sirois at gmail.com
 */
public final class ConfigurationBuilder {

	private ConfigurationBase configuration = new ConfigurationBase();

	public ConfigurationBuilder() {

	}

	public ConfigurationBuilder(final Configuration conf) {
		configuration.setClientName(conf.getClientName());
		configuration.setClientURL(conf.getClientURL());
		configuration.setClientVersion(conf.getClientVersion());
		configuration.setDebug(conf.isDebugEnabled());
		configuration.setGZIPEnabled(conf.isGZIPEnabled());
		configuration.setHostAddressResolverFactory(conf.getHostAddressResolverFactory());
		configuration.setHttpClientFactory(conf.getHttpClientFactory());
		configuration.setHttpConnectionTimeout(conf.getHttpConnectionTimeout());
		configuration.setHttpDefaultMaxPerRoute(conf.getHttpDefaultMaxPerRoute());
		configuration.setHttpMaxTotalConnections(conf.getHttpMaxTotalConnections());
		configuration.setHttpProxyHost(conf.getHttpProxyHost());
		configuration.setHttpProxyPassword(conf.getHttpProxyPassword());
		configuration.setHttpProxyPort(conf.getHttpProxyPort());
		configuration.setHttpProxyUser(conf.getHttpProxyUser());
		configuration.setHttpReadTimeout(conf.getHttpReadTimeout());
		configuration.setHttpRetryCount(conf.getHttpRetryCount());
		configuration.setHttpRetryIntervalSeconds(conf.getHttpRetryIntervalSeconds());
		configuration.setHttpUserAgent(conf.getHttpUserAgent());
		configuration.setIgnoreSSLError(conf.isSSLErrorIgnored());
		configuration.setIncludeEntitiesEnbled(conf.isIncludeEntitiesEnabled());
		configuration.setIncludeRTsEnbled(conf.isIncludeRTsEnabled());
		configuration.setIncludeTwitterClientHeader(conf.isTwitterClientHeaderIncluded());
		configuration.setOAuthAccessToken(conf.getOAuthAccessToken());
		configuration.setOAuthAccessTokenSecret(conf.getOAuthAccessTokenSecret());
		configuration.setOAuthBaseURL(conf.getOAuthBaseURL());
		configuration.setOAuthConsumerKey(conf.getOAuthConsumerKey());
		configuration.setOAuthConsumerSecret(conf.getOAuthConsumerSecret());
		configuration.setPassword(conf.getPassword());
		configuration.setPrettyDebugEnabled(conf.isPrettyDebugEnabled());
		configuration.setRestBaseURL(conf.getRestBaseURL());
		configuration.setSigningOAuthBaseURL(conf.getSigningOAuthBaseURL());
		configuration.setSigningRestBaseURL(conf.getSigningRestBaseURL());
		configuration.setUploadBaseURL(conf.getUploadBaseURL());
		configuration.setSigningUploadBaseURL(conf.getSigningUploadBaseURL());
		configuration.setUser(conf.getUser());
		configuration.setUseSSL(conf.isSSLEnabled());

	}

	public Configuration build() {
		checkNotBuilt();
		configuration.cacheInstance();
		try {
			return configuration;
		} finally {
			configuration = null;
		}
	}

	public ConfigurationBuilder setClientName(final String clientName) {
		checkNotBuilt();
		configuration.setClientName(clientName);
		return this;
	}

	public ConfigurationBuilder setClientURL(final String clientURL) {
		checkNotBuilt();
		configuration.setClientURL(clientURL);
		return this;
	}

	public ConfigurationBuilder setClientVersion(final String clientVersion) {
		checkNotBuilt();
		configuration.setClientVersion(clientVersion);
		return this;
	}

	public ConfigurationBuilder setDebugEnabled(final boolean debugEnabled) {
		checkNotBuilt();
		configuration.setDebug(debugEnabled);
		return this;
	}

	public ConfigurationBuilder setGZIPEnabled(final boolean gzipEnabled) {
		checkNotBuilt();
		configuration.setGZIPEnabled(gzipEnabled);
		return this;
	}

	public ConfigurationBuilder setHostAddressResolverFactory(final HostAddressResolverFactory factory) {
		checkNotBuilt();
		configuration.setHostAddressResolverFactory(factory);
		return this;
	}

	public ConfigurationBuilder setHttpClientFactory(final HttpClientFactory factory) {
		checkNotBuilt();
		configuration.setHttpClientFactory(factory);
		return this;
	}

	public ConfigurationBuilder setHttpConnectionTimeout(final int httpConnectionTimeout) {
		checkNotBuilt();
		configuration.setHttpConnectionTimeout(httpConnectionTimeout);
		return this;
	}

	public ConfigurationBuilder setHttpDefaultMaxPerRoute(final int httpDefaultMaxPerRoute) {
		checkNotBuilt();
		configuration.setHttpDefaultMaxPerRoute(httpDefaultMaxPerRoute);
		return this;
	}

	public ConfigurationBuilder setHttpMaxTotalConnections(final int httpMaxConnections) {
		checkNotBuilt();
		configuration.setHttpMaxTotalConnections(httpMaxConnections);
		return this;
	}

	public ConfigurationBuilder setHttpProxyHost(final String httpProxyHost) {
		checkNotBuilt();
		configuration.setHttpProxyHost(httpProxyHost);
		return this;
	}

	public ConfigurationBuilder setHttpProxyPassword(final String httpProxyPassword) {
		checkNotBuilt();
		configuration.setHttpProxyPassword(httpProxyPassword);
		return this;
	}

	public ConfigurationBuilder setHttpProxyPort(final int httpProxyPort) {
		checkNotBuilt();
		configuration.setHttpProxyPort(httpProxyPort);
		return this;
	}

	public ConfigurationBuilder setHttpProxyUser(final String httpProxyUser) {
		checkNotBuilt();
		configuration.setHttpProxyUser(httpProxyUser);
		return this;
	}

	public ConfigurationBuilder setHttpReadTimeout(final int httpReadTimeout) {
		checkNotBuilt();
		configuration.setHttpReadTimeout(httpReadTimeout);
		return this;
	}

	public ConfigurationBuilder setHttpRetryCount(final int httpRetryCount) {
		checkNotBuilt();
		configuration.setHttpRetryCount(httpRetryCount);
		return this;
	}

	public ConfigurationBuilder setHttpRetryIntervalSeconds(final int httpRetryIntervalSeconds) {
		checkNotBuilt();
		configuration.setHttpRetryIntervalSeconds(httpRetryIntervalSeconds);
		return this;
	}

	public ConfigurationBuilder setHttpUserAgent(final String userAgent) {
		checkNotBuilt();
		configuration.setHttpUserAgent(userAgent);
		return this;
	}

	public ConfigurationBuilder setIgnoreSSLError(final boolean ignoreSSLError) {
		checkNotBuilt();
		configuration.setIgnoreSSLError(ignoreSSLError);
		return this;
	}

	public ConfigurationBuilder setIncludeEntitiesEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeEntitiesEnbled(enabled);
		return this;
	}

	public ConfigurationBuilder setIncludeRTsEnabled(final boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeRTsEnbled(enabled);
		return this;
	}

	public ConfigurationBuilder setIncludeTwitterClientHeader(final boolean includeTwitterClientHeader) {
		checkNotBuilt();
		configuration.setIncludeTwitterClientHeader(includeTwitterClientHeader);
		return this;
	}

	public ConfigurationBuilder setOAuthAccessToken(final String oAuthAccessToken) {
		checkNotBuilt();
		configuration.setOAuthAccessToken(oAuthAccessToken);
		return this;
	}

	public ConfigurationBuilder setOAuthAccessTokenSecret(final String oAuthAccessTokenSecret) {
		checkNotBuilt();
		configuration.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		return this;
	}

	public ConfigurationBuilder setOAuthBaseURL(final String oAuthBaseURL) {
		checkNotBuilt();
		configuration.setOAuthBaseURL(oAuthBaseURL);
		return this;
	}

	public ConfigurationBuilder setOAuthConsumerKey(final String oAuthConsumerKey) {
		checkNotBuilt();
		configuration.setOAuthConsumerKey(oAuthConsumerKey);
		return this;
	}

	public ConfigurationBuilder setOAuthConsumerSecret(final String oAuthConsumerSecret) {
		checkNotBuilt();
		configuration.setOAuthConsumerSecret(oAuthConsumerSecret);
		return this;
	}

	public ConfigurationBuilder setPassword(final String password) {
		checkNotBuilt();
		configuration.setPassword(password);
		return this;
	}

	public ConfigurationBuilder setPrettyDebugEnabled(final boolean prettyDebugEnabled) {
		checkNotBuilt();
		configuration.setPrettyDebugEnabled(prettyDebugEnabled);
		return this;
	}

	public ConfigurationBuilder setRestBaseURL(final String restBaseURL) {
		checkNotBuilt();
		configuration.setRestBaseURL(restBaseURL);
		return this;
	}

	public ConfigurationBuilder setSigningOAuthBaseURL(final String signingOAuthBaseURL) {
		checkNotBuilt();
		configuration.setSigningOAuthBaseURL(signingOAuthBaseURL);
		return this;
	}

	public ConfigurationBuilder setSigningRestBaseURL(final String signingRestBaseURL) {
		checkNotBuilt();
		configuration.setSigningRestBaseURL(signingRestBaseURL);
		return this;
	}

	public ConfigurationBuilder setSigningUploadBaseURL(final String signingUploadBaseURL) {
		checkNotBuilt();
		configuration.setSigningUploadBaseURL(signingUploadBaseURL);
		return this;
	}

	public ConfigurationBuilder setUploadBaseURL(final String uploadBaseURL) {
		checkNotBuilt();
		configuration.setUploadBaseURL(uploadBaseURL);
		return this;
	}

	public ConfigurationBuilder setUser(final String user) {
		checkNotBuilt();
		configuration.setUser(user);
		return this;
	}

	public ConfigurationBuilder setUseSSL(final boolean useSSL) {
		checkNotBuilt();
		configuration.setUseSSL(useSSL);
		return this;
	}

	private void checkNotBuilt() {
		if (configuration == null)
			throw new IllegalStateException("Cannot use this builder any longer, build() has already been called");
	}
}
