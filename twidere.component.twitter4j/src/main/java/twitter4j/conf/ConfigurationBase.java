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

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterConstants;
import twitter4j.Version;
import twitter4j.http.HostAddressResolverFactory;

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

    private boolean includeReplyCountEnabled;

    private boolean includeDescendentReplyCountEnabled;

    private boolean includeEntitiesEnabled;

    private boolean includeTwitterClientHeader;

    // hidden portion
    private String clientVersion;
    private String clientURL;
    private String clientName;

    private HostAddressResolverFactory hostAddressResolverFactory;

    private static final List<ConfigurationBase> instances = new ArrayList<ConfigurationBase>();
    private boolean includeCards;
    private String cardsPlatform;

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
        setOAuthConsumerKey(null);
        setOAuthConsumerSecret(null);
        setOAuthAccessToken(null);
        setOAuthAccessTokenSecret(null);
        setClientName("Twitter4J");
        setClientVersion(Version.getVersion());
        setClientURL("http://twitter4j.org/en/twitter4j-" + Version.getVersion() + ".xml");
        setHttpUserAgent("twitter4j http://twitter4j.org/ /" + Version.getVersion());

        setIncludeRTsEnabled(true);

        setIncludeEntitiesEnabled(true);

        setOAuthBaseURL(DEFAULT_OAUTH_BASE_URL);
        setSigningOAuthBaseURL(DEFAULT_SIGNING_OAUTH_BASE_URL);

        setRestBaseURL(DEFAULT_REST_BASE_URL);
        setSigningRestBaseURL(DEFAULT_SIGNING_REST_BASE_URL);

        setUploadBaseURL(DEFAULT_UPLOAD_BASE_URL);
        setSigningUploadBaseURL(DEFAULT_SIGNING_UPLOAD_BASE_URL);
        setIncludeRTsEnabled(true);
    }

    public String getCardsPlatform() {
        return cardsPlatform;
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
    public final boolean isDebugEnabled() {
        return debug;
    }

    @Override
    public boolean isIncludeCardsEnabled() {
        return includeCards;
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
    public boolean isIncludeReplyCountEnabled() {
        return includeReplyCountEnabled;
    }

    @Override
    public boolean isIncludeDescendentReplyCountEnabled() {
        return includeDescendentReplyCountEnabled;
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

    public void setCardsPlatform(String cardsPlatform) {
        this.cardsPlatform = cardsPlatform;
    }

    public void setIncludeCards(boolean includeCards) {
        this.includeCards = includeCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigurationBase that = (ConfigurationBase) o;

        if (debug != that.debug) return false;
        if (useSSL != that.useSSL) return false;
        if (ignoreSSLError != that.ignoreSSLError) return false;
        if (prettyDebug != that.prettyDebug) return false;
        if (gzipEnabled != that.gzipEnabled) return false;
        if (httpProxyPort != that.httpProxyPort) return false;
        if (httpConnectionTimeout != that.httpConnectionTimeout) return false;
        if (httpReadTimeout != that.httpReadTimeout) return false;
        if (httpRetryCount != that.httpRetryCount) return false;
        if (httpRetryIntervalSeconds != that.httpRetryIntervalSeconds) return false;
        if (maxTotalConnections != that.maxTotalConnections) return false;
        if (defaultMaxPerRoute != that.defaultMaxPerRoute) return false;
        if (includeRTsEnabled != that.includeRTsEnabled) return false;
        if (includeReplyCountEnabled != that.includeReplyCountEnabled) return false;
        if (includeDescendentReplyCountEnabled != that.includeDescendentReplyCountEnabled)
            return false;
        if (includeEntitiesEnabled != that.includeEntitiesEnabled) return false;
        if (includeTwitterClientHeader != that.includeTwitterClientHeader) return false;
        if (includeCards != that.includeCards) return false;
        if (userAgent != null ? !userAgent.equals(that.userAgent) : that.userAgent != null)
            return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (httpProxyHost != null ? !httpProxyHost.equals(that.httpProxyHost) : that.httpProxyHost != null)
            return false;
        if (httpProxyUser != null ? !httpProxyUser.equals(that.httpProxyUser) : that.httpProxyUser != null)
            return false;
        if (httpProxyPassword != null ? !httpProxyPassword.equals(that.httpProxyPassword) : that.httpProxyPassword != null)
            return false;
        if (oAuthConsumerKey != null ? !oAuthConsumerKey.equals(that.oAuthConsumerKey) : that.oAuthConsumerKey != null)
            return false;
        if (oAuthConsumerSecret != null ? !oAuthConsumerSecret.equals(that.oAuthConsumerSecret) : that.oAuthConsumerSecret != null)
            return false;
        if (oAuthAccessToken != null ? !oAuthAccessToken.equals(that.oAuthAccessToken) : that.oAuthAccessToken != null)
            return false;
        if (oAuthAccessTokenSecret != null ? !oAuthAccessTokenSecret.equals(that.oAuthAccessTokenSecret) : that.oAuthAccessTokenSecret != null)
            return false;
        if (oAuthRequestTokenURL != null ? !oAuthRequestTokenURL.equals(that.oAuthRequestTokenURL) : that.oAuthRequestTokenURL != null)
            return false;
        if (oAuthAuthorizationURL != null ? !oAuthAuthorizationURL.equals(that.oAuthAuthorizationURL) : that.oAuthAuthorizationURL != null)
            return false;
        if (oAuthAccessTokenURL != null ? !oAuthAccessTokenURL.equals(that.oAuthAccessTokenURL) : that.oAuthAccessTokenURL != null)
            return false;
        if (oAuthAuthenticationURL != null ? !oAuthAuthenticationURL.equals(that.oAuthAuthenticationURL) : that.oAuthAuthenticationURL != null)
            return false;
        if (signingOAuthRequestTokenURL != null ? !signingOAuthRequestTokenURL.equals(that.signingOAuthRequestTokenURL) : that.signingOAuthRequestTokenURL != null)
            return false;
        if (signingOAuthAuthorizationURL != null ? !signingOAuthAuthorizationURL.equals(that.signingOAuthAuthorizationURL) : that.signingOAuthAuthorizationURL != null)
            return false;
        if (signingOAuthAccessTokenURL != null ? !signingOAuthAccessTokenURL.equals(that.signingOAuthAccessTokenURL) : that.signingOAuthAccessTokenURL != null)
            return false;
        if (signingOAuthAuthenticationURL != null ? !signingOAuthAuthenticationURL.equals(that.signingOAuthAuthenticationURL) : that.signingOAuthAuthenticationURL != null)
            return false;
        if (oAuthBaseURL != null ? !oAuthBaseURL.equals(that.oAuthBaseURL) : that.oAuthBaseURL != null)
            return false;
        if (signingOAuthBaseURL != null ? !signingOAuthBaseURL.equals(that.signingOAuthBaseURL) : that.signingOAuthBaseURL != null)
            return false;
        if (signingRestBaseURL != null ? !signingRestBaseURL.equals(that.signingRestBaseURL) : that.signingRestBaseURL != null)
            return false;
        if (signingUploadBaseURL != null ? !signingUploadBaseURL.equals(that.signingUploadBaseURL) : that.signingUploadBaseURL != null)
            return false;
        if (restBaseURL != null ? !restBaseURL.equals(that.restBaseURL) : that.restBaseURL != null)
            return false;
        if (uploadBaseURL != null ? !uploadBaseURL.equals(that.uploadBaseURL) : that.uploadBaseURL != null)
            return false;
        if (clientVersion != null ? !clientVersion.equals(that.clientVersion) : that.clientVersion != null)
            return false;
        if (clientURL != null ? !clientURL.equals(that.clientURL) : that.clientURL != null)
            return false;
        if (clientName != null ? !clientName.equals(that.clientName) : that.clientName != null)
            return false;
        if (hostAddressResolverFactory != null ? !hostAddressResolverFactory.equals(that.hostAddressResolverFactory) : that.hostAddressResolverFactory != null)
            return false;
        return !(cardsPlatform != null ? !cardsPlatform.equals(that.cardsPlatform) : that.cardsPlatform != null);

    }

    @Override
    public int hashCode() {
        int result = (debug ? 1 : 0);
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (useSSL ? 1 : 0);
        result = 31 * result + (ignoreSSLError ? 1 : 0);
        result = 31 * result + (prettyDebug ? 1 : 0);
        result = 31 * result + (gzipEnabled ? 1 : 0);
        result = 31 * result + (httpProxyHost != null ? httpProxyHost.hashCode() : 0);
        result = 31 * result + (httpProxyUser != null ? httpProxyUser.hashCode() : 0);
        result = 31 * result + (httpProxyPassword != null ? httpProxyPassword.hashCode() : 0);
        result = 31 * result + httpProxyPort;
        result = 31 * result + httpConnectionTimeout;
        result = 31 * result + httpReadTimeout;
        result = 31 * result + httpRetryCount;
        result = 31 * result + httpRetryIntervalSeconds;
        result = 31 * result + maxTotalConnections;
        result = 31 * result + defaultMaxPerRoute;
        result = 31 * result + (oAuthConsumerKey != null ? oAuthConsumerKey.hashCode() : 0);
        result = 31 * result + (oAuthConsumerSecret != null ? oAuthConsumerSecret.hashCode() : 0);
        result = 31 * result + (oAuthAccessToken != null ? oAuthAccessToken.hashCode() : 0);
        result = 31 * result + (oAuthAccessTokenSecret != null ? oAuthAccessTokenSecret.hashCode() : 0);
        result = 31 * result + (oAuthRequestTokenURL != null ? oAuthRequestTokenURL.hashCode() : 0);
        result = 31 * result + (oAuthAuthorizationURL != null ? oAuthAuthorizationURL.hashCode() : 0);
        result = 31 * result + (oAuthAccessTokenURL != null ? oAuthAccessTokenURL.hashCode() : 0);
        result = 31 * result + (oAuthAuthenticationURL != null ? oAuthAuthenticationURL.hashCode() : 0);
        result = 31 * result + (signingOAuthRequestTokenURL != null ? signingOAuthRequestTokenURL.hashCode() : 0);
        result = 31 * result + (signingOAuthAuthorizationURL != null ? signingOAuthAuthorizationURL.hashCode() : 0);
        result = 31 * result + (signingOAuthAccessTokenURL != null ? signingOAuthAccessTokenURL.hashCode() : 0);
        result = 31 * result + (signingOAuthAuthenticationURL != null ? signingOAuthAuthenticationURL.hashCode() : 0);
        result = 31 * result + (oAuthBaseURL != null ? oAuthBaseURL.hashCode() : 0);
        result = 31 * result + (signingOAuthBaseURL != null ? signingOAuthBaseURL.hashCode() : 0);
        result = 31 * result + (signingRestBaseURL != null ? signingRestBaseURL.hashCode() : 0);
        result = 31 * result + (signingUploadBaseURL != null ? signingUploadBaseURL.hashCode() : 0);
        result = 31 * result + (restBaseURL != null ? restBaseURL.hashCode() : 0);
        result = 31 * result + (uploadBaseURL != null ? uploadBaseURL.hashCode() : 0);
        result = 31 * result + (includeRTsEnabled ? 1 : 0);
        result = 31 * result + (includeReplyCountEnabled ? 1 : 0);
        result = 31 * result + (includeDescendentReplyCountEnabled ? 1 : 0);
        result = 31 * result + (includeEntitiesEnabled ? 1 : 0);
        result = 31 * result + (includeTwitterClientHeader ? 1 : 0);
        result = 31 * result + (clientVersion != null ? clientVersion.hashCode() : 0);
        result = 31 * result + (clientURL != null ? clientURL.hashCode() : 0);
        result = 31 * result + (clientName != null ? clientName.hashCode() : 0);
        result = 31 * result + (hostAddressResolverFactory != null ? hostAddressResolverFactory.hashCode() : 0);
        result = 31 * result + (includeCards ? 1 : 0);
        result = 31 * result + (cardsPlatform != null ? cardsPlatform.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationBase{" +
                "debug=" + debug +
                ", userAgent='" + userAgent + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", useSSL=" + useSSL +
                ", ignoreSSLError=" + ignoreSSLError +
                ", prettyDebug=" + prettyDebug +
                ", gzipEnabled=" + gzipEnabled +
                ", httpProxyHost='" + httpProxyHost + '\'' +
                ", httpProxyUser='" + httpProxyUser + '\'' +
                ", httpProxyPassword='" + httpProxyPassword + '\'' +
                ", httpProxyPort=" + httpProxyPort +
                ", httpConnectionTimeout=" + httpConnectionTimeout +
                ", httpReadTimeout=" + httpReadTimeout +
                ", httpRetryCount=" + httpRetryCount +
                ", httpRetryIntervalSeconds=" + httpRetryIntervalSeconds +
                ", maxTotalConnections=" + maxTotalConnections +
                ", defaultMaxPerRoute=" + defaultMaxPerRoute +
                ", oAuthConsumerKey='" + oAuthConsumerKey + '\'' +
                ", oAuthConsumerSecret='" + oAuthConsumerSecret + '\'' +
                ", oAuthAccessToken='" + oAuthAccessToken + '\'' +
                ", oAuthAccessTokenSecret='" + oAuthAccessTokenSecret + '\'' +
                ", oAuthRequestTokenURL='" + oAuthRequestTokenURL + '\'' +
                ", oAuthAuthorizationURL='" + oAuthAuthorizationURL + '\'' +
                ", oAuthAccessTokenURL='" + oAuthAccessTokenURL + '\'' +
                ", oAuthAuthenticationURL='" + oAuthAuthenticationURL + '\'' +
                ", signingOAuthRequestTokenURL='" + signingOAuthRequestTokenURL + '\'' +
                ", signingOAuthAuthorizationURL='" + signingOAuthAuthorizationURL + '\'' +
                ", signingOAuthAccessTokenURL='" + signingOAuthAccessTokenURL + '\'' +
                ", signingOAuthAuthenticationURL='" + signingOAuthAuthenticationURL + '\'' +
                ", oAuthBaseURL='" + oAuthBaseURL + '\'' +
                ", signingOAuthBaseURL='" + signingOAuthBaseURL + '\'' +
                ", signingRestBaseURL='" + signingRestBaseURL + '\'' +
                ", signingUploadBaseURL='" + signingUploadBaseURL + '\'' +
                ", restBaseURL='" + restBaseURL + '\'' +
                ", uploadBaseURL='" + uploadBaseURL + '\'' +
                ", includeRTsEnabled=" + includeRTsEnabled +
                ", includeReplyCountEnabled=" + includeReplyCountEnabled +
                ", includeDescendentReplyCountEnabled=" + includeDescendentReplyCountEnabled +
                ", includeEntitiesEnabled=" + includeEntitiesEnabled +
                ", includeTwitterClientHeader=" + includeTwitterClientHeader +
                ", clientVersion='" + clientVersion + '\'' +
                ", clientURL='" + clientURL + '\'' +
                ", clientName='" + clientName + '\'' +
                ", hostAddressResolverFactory=" + hostAddressResolverFactory +
                ", includeCards=" + includeCards +
                ", cardsPlatform='" + cardsPlatform + '\'' +
                '}';
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

    protected final void setIncludeEntitiesEnabled(final boolean enabled) {
        includeEntitiesEnabled = enabled;
    }

    protected final void setIncludeRTsEnabled(final boolean enabled) {
        includeRTsEnabled = enabled;
    }

    protected final void setIncludeReplyCountEnabled(final boolean enabled) {
        includeReplyCountEnabled = enabled;
    }

    protected final void setIncludeDescendentReplyCountEnabled(final boolean enabled) {
        includeDescendentReplyCountEnabled = enabled;
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

    public void setOAuthAuthorizationURL(String oAuthAuthorizationURL) {
        this.oAuthAuthorizationURL = oAuthAuthorizationURL;
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
