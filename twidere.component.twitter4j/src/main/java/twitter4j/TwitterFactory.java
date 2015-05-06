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

import android.net.SSLCertificateSocketFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.simplerestapi.RestAPIFactory;
import org.mariotaku.simplerestapi.RestMethod;
import org.mariotaku.simplerestapi.RestMethodInfo;
import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.RestRequest;
import org.mariotaku.twidere.api.twitter.OkHttpRestClient;
import org.mariotaku.twidere.api.twitter.TwitterConverter;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthEndpoint;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientConfiguration;

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

    public Twitter getInstance(final OAuthToken accessToken) {
        final String consumerKey = conf.getOAuthConsumerKey();
        final String consumerSecret = conf.getOAuthConsumerSecret();
        if (null == consumerKey && null == consumerSecret)
            throw new IllegalStateException("Consumer key and Consumer secret not supplied.");
        final OAuthAuthorization oauth = new OAuthAuthorization(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret(), accessToken);
        return getInstance(oauth);
    }

    public Twitter getInstance(final Authorization auth) {
        final OAuthEndpoint endpoint = new OAuthEndpoint(conf.getRestBaseURL(), conf.getSigningRestBaseURL());
        final RestAPIFactory factory = new RestAPIFactory();
        factory.setClient(new OkHttpRestClient(createHttpClient(conf)));
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(auth);
        factory.setRequestFactory(new RestRequest.Factory() {

            @Override
            public RestRequest create(@NonNull Endpoint endpoint, @NonNull RestMethodInfo info, @Nullable Authorization authorization) {
                final RestMethod restMethod = info.getMethod();
                final String url = Endpoint.constructUrl(endpoint.getUrl(), info);
                final ArrayList<Pair<String, String>> headers = new ArrayList<>(info.getHeaders());

                if (authorization != null && authorization.hasAuthorization()) {
                    headers.add(Pair.create("Authorization", authorization.getHeader(endpoint, info)));
                }
                headers.add(Pair.create("User-Agent", conf.getHttpUserAgent()));
                return new RestRequest(restMethod.value(), url, headers, info.getBody(), null);
            }
        });
        return factory.build(Twitter.class);
    }

    public Twitter getInstance() {
        return getInstance(new OAuthToken(conf.getOAuthConsumerKey(), conf.getOAuthConsumerSecret()));
    }


    private static OkHttpClient createHttpClient(HttpClientConfiguration conf) {
        final OkHttpClient client = new OkHttpClient();
        final boolean ignoreSSLError = conf.isSSLErrorIgnored();
        final SSLCertificateSocketFactory sslSocketFactory;
        final HostAddressResolverFactory resolverFactory = conf.getHostAddressResolverFactory();
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
            client.setProxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(conf.getHttpProxyHost(),
                    conf.getHttpProxyPort())));
        }
        if (resolverFactory != null) {
            final HostAddressResolver resolver = resolverFactory.getInstance(conf);
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
        }
        return client;
    }
}
