package twitter4j.conf;

import twitter4j.TwitterStreamConstants;

class StreamConfigurationBase extends ConfigurationBase implements StreamConfiguration, TwitterStreamConstants {

	private String dispatcherImpl;
	private String siteStreamBaseURL;
	private String userStreamBaseURL;
	private String streamBaseURL;

	private int httpStreamingReadTimeout;
	private int asyncNumThreads;

	private boolean userStreamRepliesAllEnabled;
	private boolean jsonStoreEnabled;
	private boolean stallWarningsEnabled;

	protected StreamConfigurationBase() {
		super();
		setAsyncNumThreads(1);
		setHttpStreamingReadTimeout(40 * 1000);
		setDispatcherImpl("twitter4j.internal.async.DispatcherImpl");
		setStreamBaseURL(DEFAULT_STREAM_BASE_URL);
		setUserStreamBaseURL(DEFAULT_USER_STREAM_BASE_URL);
		setSiteStreamBaseURL(DEFAULT_SITE_STREAM_BASE_URL);
		setUserStreamRepliesAllEnabled(false);
	}

	@Override
	public final int getAsyncNumThreads() {
		return asyncNumThreads;
	}

	@Override
	public String getDispatcherImpl() {
		return dispatcherImpl;
	}

	@Override
	public int getHttpStreamingReadTimeout() {
		return httpStreamingReadTimeout;
	}

	@Override
	public String getSiteStreamBaseURL() {
		return siteStreamBaseURL;
	}

	@Override
	public String getStreamBaseURL() {
		return streamBaseURL;
	}

	@Override
	public String getUserStreamBaseURL() {
		return userStreamBaseURL;
	}

	@Override
	public boolean isJSONStoreEnabled() {
		return jsonStoreEnabled;
	}

	@Override
	public boolean isStallWarningsEnabled() {
		return stallWarningsEnabled;
	}

	@Override
	public boolean isUserStreamRepliesAllEnabled() {
		return userStreamRepliesAllEnabled;
	}

	public void setStallWarningsEnabled(final boolean isStallWarningsEnabled) {
		stallWarningsEnabled = isStallWarningsEnabled;
	}

	protected final void setAsyncNumThreads(final int asyncNumThreads) {
		this.asyncNumThreads = asyncNumThreads;
	}

	protected final void setDispatcherImpl(final String dispatcherImpl) {
		this.dispatcherImpl = dispatcherImpl;
	}

	protected final void setHttpStreamingReadTimeout(final int httpStreamingReadTimeout) {
		this.httpStreamingReadTimeout = httpStreamingReadTimeout;
	}

	protected final void setJSONStoreEnabled(final boolean enabled) {
		jsonStoreEnabled = enabled;
	}

	protected final void setSiteStreamBaseURL(String siteStreamBaseURL) {
		if (isNullOrEmpty(siteStreamBaseURL)) {
			siteStreamBaseURL = DEFAULT_SITE_STREAM_BASE_URL;
		}
		this.siteStreamBaseURL = fixURLSlash(siteStreamBaseURL);
		fixSiteStreamBaseURL();
	}

	protected final void setStreamBaseURL(String streamBaseURL) {
		if (isNullOrEmpty(streamBaseURL)) {
			streamBaseURL = DEFAULT_STREAM_BASE_URL;
		}
		this.streamBaseURL = fixURLSlash(streamBaseURL);
		fixStreamBaseURL();
	}

	protected final void setUserStreamBaseURL(String userStreamBaseURL) {
		if (isNullOrEmpty(userStreamBaseURL)) {
			userStreamBaseURL = DEFAULT_USER_STREAM_BASE_URL;
		}
		this.userStreamBaseURL = fixURLSlash(userStreamBaseURL);
		fixUserStreamBaseURL();
	}

	protected final void setUserStreamRepliesAllEnabled(final boolean enabled) {
		userStreamRepliesAllEnabled = enabled;
	}

	private void fixSiteStreamBaseURL() {
		if (DEFAULT_SITE_STREAM_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, siteStreamBaseURL))) {
			siteStreamBaseURL = fixURL(isSSLEnabled(), siteStreamBaseURL);
		}
		if (siteStreamBaseURL != null && siteStreamBaseURL.equals(fixURL(DEFAULT_USE_SSL, siteStreamBaseURL))) {
			siteStreamBaseURL = fixURL(isSSLEnabled(), siteStreamBaseURL);
		}
		initRequestHeaders();
	}

	private void fixStreamBaseURL() {
		if (DEFAULT_STREAM_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, streamBaseURL))) {
			streamBaseURL = fixURL(isSSLEnabled(), streamBaseURL);
		}
		if (streamBaseURL != null && streamBaseURL.equals(fixURL(DEFAULT_USE_SSL, streamBaseURL))) {
			streamBaseURL = fixURL(isSSLEnabled(), streamBaseURL);
		}
		initRequestHeaders();
	}

	private void fixUserStreamBaseURL() {
		if (DEFAULT_USER_STREAM_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, userStreamBaseURL))) {
			userStreamBaseURL = fixURL(isSSLEnabled(), userStreamBaseURL);
		}
		if (userStreamBaseURL != null && userStreamBaseURL.equals(fixURL(DEFAULT_USE_SSL, userStreamBaseURL))) {
			userStreamBaseURL = fixURL(isSSLEnabled(), userStreamBaseURL);
		}
		initRequestHeaders();
	}

}
