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

package twitter4j;

import twitter4j.auth.AccessToken;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.StreamConfiguration;
import twitter4j.conf.StreamConfigurationContext;

/**
 * A factory class for TwitterFactory.<br>
 * An instance of this class is completely thread safe and can be re-used and
 * used concurrently.<br>
 * Note that TwitterStream is NOT compatible with Google App Engine as GAE is
 * not capable of handling requests longer than 30 seconds.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.0
 */
public final class TwitterStreamFactory implements java.io.Serializable {
	private static final long serialVersionUID = 8146074704915782233L;
	private final StreamConfiguration conf;
	private static final TwitterStream SINGLETON;

	static {
		SINGLETON = new TwitterStreamImpl(StreamConfigurationContext.getInstance(),
				TwitterFactory.DEFAULT_AUTHORIZATION);
	}

	/**
	 * Creates a TwitterStreamFactory with the root configuration.
	 */
	public TwitterStreamFactory() {
		this(StreamConfigurationContext.getInstance());
	}

	/**
	 * Creates a TwitterStreamFactory with the given configuration.
	 * 
	 * @param conf the configuration to use
	 * @since Twitter4J 2.1.1
	 */
	public TwitterStreamFactory(final StreamConfiguration conf) {
		this.conf = conf;
	}

	// implementations for BasicSupportFactory

	/**
	 * Returns a instance associated with the configuration bound to this
	 * factory.
	 * 
	 * @return default instance
	 */
	public TwitterStream getInstance() {
		return getInstance(AuthorizationFactory.getInstance(conf));
	}

	/**
	 * Returns a OAuth Authenticated instance.<br>
	 * consumer key and consumer Secret must be provided by
	 * twitter4j.properties, or system properties. Unlike
	 * {@link twitter4j.TwitterStream#setOAuthAccessToken(twitter4j.auth.AccessToken)},
	 * this factory method potentially returns a cached instance.
	 * 
	 * @param accessToken access token
	 * @return an instance
	 */
	public TwitterStream getInstance(final AccessToken accessToken) {
		final String consumerKey = conf.getOAuthConsumerKey();
		final String consumerSecret = conf.getOAuthConsumerSecret();
		if (null == consumerKey && null == consumerSecret)
			throw new IllegalStateException("Consumer key and Consumer secret not supplied.");
		final OAuthAuthorization oauth = new OAuthAuthorization(conf);
		oauth.setOAuthAccessToken(accessToken);
		return getInstance(conf, oauth);
	}

	/**
	 * Returns a instance.
	 * 
	 * @return an instance
	 */
	public TwitterStream getInstance(final Authorization auth) {
		return getInstance(conf, auth);
	}

	private TwitterStream getInstance(final StreamConfiguration conf, final Authorization auth) {
		return new TwitterStreamImpl(conf, auth);
	}

	/**
	 * Returns default singleton TwitterStream instance.
	 * 
	 * @return default singleton TwitterStream instance
	 * @since Twitter4J 2.2.4
	 */
	public static TwitterStream getSingleton() {
		return SINGLETON;
	}
}
