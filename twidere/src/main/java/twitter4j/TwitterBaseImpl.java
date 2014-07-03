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

package twitter4j;

import static twitter4j.http.HttpResponseCode.ENHANCE_YOUR_CLAIM;
import static twitter4j.http.HttpResponseCode.SERVICE_UNAVAILABLE;
import twitter4j.auth.AccessToken;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.NullAuthorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.OAuthSupport;
import twitter4j.auth.RequestToken;
import twitter4j.auth.XAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;
import twitter4j.http.HttpResponseEvent;
import twitter4j.http.HttpResponseListener;
import twitter4j.internal.json.InternalJSONFactory;
import twitter4j.internal.json.InternalJSONFactoryImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class of Twitter / AsyncTwitter / TwitterStream supports OAuth.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
abstract class TwitterBaseImpl implements OAuthSupport, HttpResponseListener, TwitterConstants {
	protected Configuration conf;
	protected transient String screenName = null;
	protected transient long id = 0;

	protected transient HttpClientWrapper http;
	private List<RateLimitStatusListener> rateLimitStatusListeners = new ArrayList<RateLimitStatusListener>(0);

	protected InternalJSONFactory factory;

	protected Authorization auth;

	/* package */TwitterBaseImpl(final Configuration conf, final Authorization auth) {
		this.conf = conf;
		this.auth = auth;
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addRateLimitStatusListener(final RateLimitStatusListener listener) {
		rateLimitStatusListeners.add(listener);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof TwitterBaseImpl)) return false;

		final TwitterBaseImpl that = (TwitterBaseImpl) o;

		if (auth != null ? !auth.equals(that.auth) : that.auth != null) return false;
		if (!conf.equals(that.conf)) return false;
		if (http != null ? !http.equals(that.http) : that.http != null) return false;
		if (!rateLimitStatusListeners.equals(that.rateLimitStatusListeners)) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Authorization getAuthorization() {
		return auth;
	}

	/**
	 * {@inheritDoc}
	 */
	public Configuration getConfiguration() {
		return conf;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getId() throws TwitterException, IllegalStateException {
		if (!auth.isEnabled())
			throw new IllegalStateException(
					"Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
		if (0 == id) {
			fillInIDAndScreenName();
		}
		// retrieve the screen name if this instance is authenticated with OAuth
		// or email address
		return id;
	}

	/**
	 * {@inheritDoc} Basic authenticated instance of this class will try
	 * acquiring an AccessToken using xAuth.<br>
	 * In order to get access acquire AccessToken using xAuth, you must apply by
	 * sending an email to <a href="mailto:api@twitter.com">api@twitter.com</a>
	 * all other applications will receive an HTTP 401 error. Web-based
	 * applications will not be granted access, except on a temporary basis for
	 * when they are converting from basic-authentication support to full OAuth
	 * support.<br>
	 * Storage of Twitter usernames and passwords is forbidden. By using xAuth,
	 * you are required to store only access tokens and access token secrets. If
	 * the access token expires or is expunged by a user, you must ask for their
	 * login and password again before exchanging the credentials for an access
	 * token.
	 * 
	 * @throws TwitterException When Twitter service or network is unavailable,
	 *             when the user has not authorized, or when the client
	 *             application is not permitted to use xAuth
	 * @see <a href="https://dev.twitter.com/docs/oauth/xauth">xAuth | Twitter
	 *      Developers</a>
	 */
	@Override
	public synchronized AccessToken getOAuthAccessToken() throws TwitterException {
		Authorization auth = getAuthorization();
		AccessToken oauthAccessToken;
		if (auth instanceof BasicAuthorization) {
			final BasicAuthorization basicAuth = (BasicAuthorization) auth;
			auth = AuthorizationFactory.getInstance(conf);
			if (auth instanceof OAuthAuthorization) {
				this.auth = auth;
				final OAuthAuthorization oauthAuth = (OAuthAuthorization) auth;
				oauthAccessToken = oauthAuth.getOAuthAccessToken(basicAuth.getUserId(), basicAuth.getPassword());
			} else
				throw new IllegalStateException("consumer key / secret combination not supplied.");
		} else {
			if (auth instanceof XAuthAuthorization) {
				final XAuthAuthorization xauth = (XAuthAuthorization) auth;
				this.auth = xauth;
				final OAuthAuthorization oauthAuth = new OAuthAuthorization(conf);
				oauthAuth.setOAuthConsumer(xauth.getConsumerKey(), xauth.getConsumerSecret());
				oauthAccessToken = oauthAuth.getOAuthAccessToken(xauth.getUserId(), xauth.getPassword());
			} else {
				oauthAccessToken = getOAuth().getOAuthAccessToken();
			}
		}
		screenName = oauthAccessToken.getScreenName();
		id = oauthAccessToken.getUserId();
		return oauthAccessToken;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException when AccessToken has already been retrieved
	 *             or set
	 */
	@Override
	public synchronized AccessToken getOAuthAccessToken(final RequestToken requestToken) throws TwitterException {
		final OAuthSupport oauth = getOAuth();
		final AccessToken oauthAccessToken = oauth.getOAuthAccessToken(requestToken);
		screenName = oauthAccessToken.getScreenName();
		return oauthAccessToken;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException when AccessToken has already been retrieved
	 *             or set
	 */
	@Override
	public synchronized AccessToken getOAuthAccessToken(final RequestToken requestToken, final String oauthVerifier)
			throws TwitterException {
		return getOAuth().getOAuthAccessToken(requestToken, oauthVerifier);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException when AccessToken has already been retrieved
	 *             or set
	 */
	@Override
	public synchronized AccessToken getOAuthAccessToken(final String oauthVerifier) throws TwitterException {
		final AccessToken oauthAccessToken = getOAuth().getOAuthAccessToken(oauthVerifier);
		screenName = oauthAccessToken.getScreenName();
		return oauthAccessToken;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized AccessToken getOAuthAccessToken(final String screenName, final String password)
			throws TwitterException {
		return getOAuth().getOAuthAccessToken(screenName, password);
	}

	/* OAuth support methods */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestToken getOAuthRequestToken() throws TwitterException {
		return getOAuthRequestToken(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestToken getOAuthRequestToken(final String callbackUrl) throws TwitterException {
		return getOAuth().getOAuthRequestToken(callbackUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestToken getOAuthRequestToken(final String callbackUrl, final String xAuthAccessType)
			throws TwitterException {
		return getOAuth().getOAuthRequestToken(callbackUrl, xAuthAccessType);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getScreenName() throws TwitterException, IllegalStateException {
		if (!auth.isEnabled())
			throw new IllegalStateException(
					"Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
		if (null == screenName) {
			if (auth instanceof BasicAuthorization) {
				screenName = ((BasicAuthorization) auth).getUserId();
				if (-1 != screenName.indexOf("@")) {
					screenName = null;
				}
			}
			if (null == screenName) {
				// retrieve the screen name if this instance is authenticated
				// with OAuth or email address
				fillInIDAndScreenName();
			}
		}
		return screenName;
	}

	// methods declared in OAuthSupport interface

	@Override
	public int hashCode() {
		int result = conf.hashCode();
		result = 31 * result + (http != null ? http.hashCode() : 0);
		result = 31 * result + rateLimitStatusListeners.hashCode();
		result = 31 * result + (auth != null ? auth.hashCode() : 0);
		return result;
	}

	@Override
	public void httpResponseReceived(final HttpResponseEvent event) {
		if (rateLimitStatusListeners.size() != 0) {
			final HttpResponse res = event.getResponse();
			final TwitterException te = event.getTwitterException();
			RateLimitStatus rateLimitStatus;
			int statusCode;
			if (te != null) {
				rateLimitStatus = te.getRateLimitStatus();
				statusCode = te.getStatusCode();
			} else {
				rateLimitStatus = InternalJSONFactoryImpl.createRateLimitStatusFromResponseHeader(res);
				statusCode = res.getStatusCode();
			}
			if (rateLimitStatus != null) {
				final RateLimitStatusEvent statusEvent = new RateLimitStatusEvent(this, rateLimitStatus,
						event.isAuthenticated());
				if (statusCode == ENHANCE_YOUR_CLAIM || statusCode == SERVICE_UNAVAILABLE) {
					// EXCEEDED_RATE_LIMIT_QUOTA is returned by Rest API
					// SERVICE_UNAVAILABLE is returned by Search API
					for (final RateLimitStatusListener listener : rateLimitStatusListeners) {
						listener.onRateLimitStatus(statusEvent);
						listener.onRateLimitReached(statusEvent);
					}
				} else {
					for (final RateLimitStatusListener listener : rateLimitStatusListeners) {
						listener.onRateLimitStatus(statusEvent);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setOAuthAccessToken(final AccessToken accessToken) {
		getOAuth().setOAuthAccessToken(accessToken);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setOAuthConsumer(final String consumerKey, final String consumerSecret) {
		if (null == consumerKey) throw new NullPointerException("consumer key is null");
		if (null == consumerSecret) throw new NullPointerException("consumer secret is null");
		if (auth instanceof NullAuthorization) {
			final OAuthAuthorization oauth = new OAuthAuthorization(conf);
			oauth.setOAuthConsumer(consumerKey, consumerSecret);
			auth = oauth;
		} else if (auth instanceof BasicAuthorization) {
			final XAuthAuthorization xauth = new XAuthAuthorization((BasicAuthorization) auth);
			xauth.setOAuthConsumer(consumerKey, consumerSecret);
			auth = xauth;
		} else if (auth instanceof OAuthAuthorization)
			throw new IllegalStateException("consumer key/secret pair already set.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		if (http != null) {
			http.shutdown();
		}
	}

	@Override
	public String toString() {
		return "TwitterBase{" + "conf=" + conf + ", http=" + http + ", rateLimitStatusListeners="
				+ rateLimitStatusListeners + ", auth=" + auth + '}';
	}

	protected void addParameterToList(final List<HttpParameter> params, final String paramName, final Boolean param) {
		if (param != null) {
			params.add(new HttpParameter(paramName, param));
		}
	}

	protected void addParameterToList(final List<HttpParameter> params, final String paramName, final Double param) {
		if (param != null) {
			params.add(new HttpParameter(paramName, param));
		}
	}

	protected void addParameterToList(final List<HttpParameter> params, final String paramName, final Integer param) {
		if (param != null) {
			params.add(new HttpParameter(paramName, param));
		}
	}

	protected void addParameterToList(final List<HttpParameter> params, final String paramName, final String param) {
		if (param != null) {
			params.add(new HttpParameter(paramName, param));
		}
	}

	/**
	 * Check the existence, and the type of the specified file.
	 * 
	 * @param image image to be uploaded
	 * @throws TwitterException when the specified file is not found
	 *             (FileNotFoundException will be nested) , or when the
	 *             specified file object is not representing a file(IOException
	 *             will be nested).
	 */
	protected void checkFileValidity(final File image) throws TwitterException {
		if (!image.exists()) // noinspection ThrowableInstanceNeverThrown
			throw new TwitterException(new FileNotFoundException(image + " is not found."));
		if (!image.isFile()) // noinspection ThrowableInstanceNeverThrown
			throw new TwitterException(new IOException(image + " is not a file."));
	}

	protected final void ensureAuthorizationEnabled() {
		if (!auth.isEnabled())
			throw new IllegalStateException(
					"Authentication credentials are missing. See http://twitter4j.org/configuration.html for the detail.");
	}

	protected final void ensureOAuthEnabled() {
		if (!(auth instanceof OAuthAuthorization))
			throw new IllegalStateException(
					"OAuth required. Authentication credentials are missing. See http://twitter4j.org/configuration.html for the detail.");
	}

	protected User fillInIDAndScreenName() throws TwitterException {
		ensureAuthorizationEnabled();
		final HttpParameter[] params = { new HttpParameter("include_entities", conf.isIncludeEntitiesEnabled()) };
		final User user = factory.createUser(http.get(conf.getRestBaseURL() + ENDPOINT_ACCOUNT_VERIFY_CREDENTIALS,
				conf.getSigningRestBaseURL() + ENDPOINT_ACCOUNT_VERIFY_CREDENTIALS, params, auth));
		screenName = user.getScreenName();
		id = user.getId();
		return user;
	}

	protected HttpResponse get(final String url, final String sign_url, final HttpParameter... parameters)
			throws TwitterException {
		// intercept HTTP call for monitoring purposes
		HttpResponse response = null;
		final long start = System.currentTimeMillis();
		try {
			response = http.get(url, sign_url, parameters, auth);
		} finally {
			final long elapsedTime = System.currentTimeMillis() - start;
			TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
		}
		return response;
	}

	protected boolean isOk(final HttpResponse response) {
		return response != null && response.getStatusCode() < 300;
	}

	protected HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter... params2) {
		if (params1 != null && params2 != null) {
			final HttpParameter[] params = new HttpParameter[params1.length + params2.length];
			System.arraycopy(params1, 0, params, 0, params1.length);
			System.arraycopy(params2, 0, params, params1.length, params2.length);
			return params;
		}
		if (null == params1 && null == params2) return new HttpParameter[0];
		if (params1 != null)
			return params1;
		else
			return params2;
	}

	protected HttpResponse post(final String url, final String sign_url, final HttpParameter... parameters)
			throws TwitterException {
		// intercept HTTP call for monitoring purposes
		HttpResponse response = null;
		final long start = System.currentTimeMillis();
		try {
			response = http.post(url, sign_url, parameters, auth);
		} finally {
			final long elapsedTime = System.currentTimeMillis() - start;
			TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
		}
		return response;
	}

	protected void setFactory() {
		factory = new InternalJSONFactoryImpl(conf);
	}

	private OAuthSupport getOAuth() {
		if (!(auth instanceof OAuthSupport))
			throw new IllegalStateException("OAuth consumer key/secret combination not supplied");
		return (OAuthSupport) auth;
	}

	private void init() {
		if (null == auth) {
			// try to populate OAuthAuthorization if available in the
			// configuration
			final String consumerKey = conf.getOAuthConsumerKey();
			final String consumerSecret = conf.getOAuthConsumerSecret();
			// try to find oauth tokens in the configuration
			if (consumerKey != null && consumerSecret != null) {
				final OAuthAuthorization oauth = new OAuthAuthorization(conf);
				final String accessToken = conf.getOAuthAccessToken();
				final String accessTokenSecret = conf.getOAuthAccessTokenSecret();
				if (accessToken != null && accessTokenSecret != null) {
					oauth.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
				}
				auth = oauth;
			} else {
				auth = NullAuthorization.getInstance();
			}
		}
		http = new HttpClientWrapper(conf);
		http.setHttpResponseListener(this);
		setFactory();
	}

	@SuppressWarnings("unchecked")
	private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
		conf = (Configuration) stream.readObject();
		auth = (Authorization) stream.readObject();
		rateLimitStatusListeners = (List<RateLimitStatusListener>) stream.readObject();
		http = new HttpClientWrapper(conf);
		http.setHttpResponseListener(this);
		setFactory();
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.writeObject(conf);
		out.writeObject(auth);
		final List<RateLimitStatusListener> serializableRateLimitStatusListeners = new ArrayList<RateLimitStatusListener>(
				0);
		for (final RateLimitStatusListener listener : rateLimitStatusListeners) {
			if (listener instanceof Serializable) {
				serializableRateLimitStatusListeners.add(listener);
			}
		}
		out.writeObject(serializableRateLimitStatusListeners);
	}
}
