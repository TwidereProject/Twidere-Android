package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;

import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.util.TwitterConverter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.util.net.OkHttpRestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterAPIFactory implements TwidereConstants {

    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities) {
        if (context == null) return null;
        return getDefaultTwitterInstance(context, includeEntities, true);
    }

    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities,
                                                    final boolean includeRetweets) {
        if (context == null) return null;
        return getTwitterInstance(context, Utils.getDefaultAccountId(context), includeEntities, includeRetweets);
    }

    public static Twitter getTwitterInstance(final Context context, final long accountId,
                                             final boolean includeEntities) {
        return getTwitterInstance(context, accountId, includeEntities, true);
    }

    @Nullable
    public static Twitter getTwitterInstance(final Context context, final long accountId,
                                             final boolean includeEntities,
                                             final boolean includeRetweets) {
        return getTwitterInstance(context, accountId, includeEntities, includeRetweets, Twitter.class);
    }

    @Nullable
    public static <T> T getTwitterInstance(final Context context, final long accountId,
                                           final boolean includeEntities,
                                           final boolean includeRetweets, Class<T> cls) {
        if (context == null) return null;
        final ParcelableCredentials credentials = ParcelableCredentials.getCredentials(context, accountId);
        return getInstance(context, credentials, cls);
    }

    public static RestHttpClient getDefaultHttpClient(final Context context) {
        if (context == null) return null;
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return createHttpClient(context, prefs);
    }

    public static RestHttpClient createHttpClient(final Context context, final SharedPreferences prefs) {
        final int connectionTimeout = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        final OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(connectionTimeout, TimeUnit.SECONDS);
        if (ignoreSslError) {
            client.setSslSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
        } else {
            client.setSslSocketFactory(SSLCertificateSocketFactory.getDefault(0, null));
        }
        if (enableProxy) {
            client.setProxy(getProxy(prefs));
        }
        Internal.instance.setNetwork(client, TwidereApplication.getInstance(context).getNetwork());
        return new OkHttpRestClient(client);
    }


    public static Proxy getProxy(final SharedPreferences prefs) {
        final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
        final int proxyPort = ParseUtils.parseInt(prefs.getString(KEY_PROXY_PORT, "-1"));
        if (!isEmpty(proxyHost) && proxyPort >= 0 && proxyPort < 65535) {
            final SocketAddress addr = InetSocketAddress.createUnresolved(proxyHost, proxyPort);
            return new Proxy(Proxy.Type.HTTP, addr);
        }
        return Proxy.NO_PROXY;
    }

    public static <T> T getInstance(final Context context, final Endpoint endpoint, final Authorization auth, Class<T> cls) {
        final RestAPIFactory factory = new RestAPIFactory();
        final String userAgent;
        if (auth instanceof OAuthAuthorization) {
            final String consumerKey = ((OAuthAuthorization) auth).getConsumerKey();
            final String consumerSecret = ((OAuthAuthorization) auth).getConsumerSecret();
            final ConsumerKeyType officialKeyType = TwitterContentUtils.getOfficialKeyType(context, consumerKey, consumerSecret);
            if (officialKeyType != ConsumerKeyType.UNKNOWN) {
                userAgent = TwitterAPIUtils.getUserAgentName(officialKeyType);
            } else {
                userAgent = TwitterAPIUtils.getTwidereUserAgent(context);
            }
        } else {
            userAgent = TwitterAPIUtils.getTwidereUserAgent(context);
        }
        factory.setClient(getDefaultHttpClient(context));
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(auth);
        factory.setRequestInfoFactory(new TwitterAPIUtils.TwidereRequestInfoFactory());
        factory.setHttpRequestFactory(new TwitterAPIUtils.TwidereHttpRequestFactory(userAgent));
        factory.setExceptionFactory(new TwitterAPIUtils.TwidereExceptionFactory());
        return factory.build(cls);
    }

    public static <T> T getInstance(final Context context, final Endpoint endpoint, final ParcelableCredentials credentials, Class<T> cls) {
        return TwitterAPIFactory.getInstance(context, endpoint, TwitterAPIUtils.getAuthorization(credentials), cls);
    }

    static <T> T getInstance(final Context context, final ParcelableCredentials credentials, final Class<T> cls) {
        if (credentials == null) return null;
        return TwitterAPIFactory.getInstance(context, TwitterAPIUtils.getEndpoint(credentials, cls), credentials, cls);
    }
}
