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

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.twidere.api.twitter.TwitterAPIFactory;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * A factory class for Twitter. <br>
 * An instance of this class is completely thread safe and can be re-used and
 * used concurrently.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.0
 */
public final class TwitterFactory {
    /* AsyncTwitterFactory and TWitterStream will access this field */

    private final Configuration conf;

    /**
     * Creates a TwitterFactory with the root configuration.
     */
    public TwitterFactory() {
        this(ConfigurationContext.getInstance());
    }

    /**
     * Creates a TwitterFactory with the given configuration.
     *
     * @param conf the configuration to use
     * @since Twitter4J 2.1.1
     */
    public TwitterFactory(final Configuration conf) {
        if (conf == null) throw new NullPointerException("configuration cannot be null");
        this.conf = conf;
    }

    /**
     * Returns a OAuth Authenticated instance.<br>
     * consumer key and consumer Secret must be provided by
     * twitter4j.properties, or system properties.<br>
     * Unlike {@link twitter4j.Twitter#setOAuthAccessToken(twitter4j.auth.AccessToken)} ,
     * this factory method potentially returns a cached instance.
     *
     * @param accessToken access token
     * @return an instance
     * @since Twitter4J 2.1.9
     */
    public Twitter getInstance(final OAuthToken accessToken) {
        final String consumerKey = conf.getOAuthConsumerKey();
        final String consumerSecret = conf.getOAuthConsumerSecret();
        if (null == consumerKey && null == consumerSecret)
            throw new IllegalStateException("Consumer key and Consumer secret not supplied.");
        final OAuthAuthorization oauth = new OAuthAuthorization(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret(), accessToken);
        return getInstance(oauth);
    }

    public Twitter getInstance(final Authorization auth) {
        return TwitterAPIFactory.getInstance(auth);
    }

}
