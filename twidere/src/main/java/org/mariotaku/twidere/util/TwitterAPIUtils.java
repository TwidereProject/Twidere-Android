package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.RestHttpClient;
import org.mariotaku.simplerestapi.http.RestRequest;
import org.mariotaku.simplerestapi.http.RestResponse;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.OkHttpRestClient;
import org.mariotaku.twidere.api.twitter.auth.BasicAuthorization;
import org.mariotaku.twidere.api.twitter.auth.EmptyAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.net.TwidereHostResolverFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import twitter4j.Twitter;
import twitter4j.TwitterConstants;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.HostAddressResolver;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterAPIUtils implements TwidereConstants {

    @NonNull
    public static RestResponse getRedirectedHttpResponse(@NonNull final RestHttpClient client, @NonNull final String url,
                                                         final String signUrl, final Authorization auth,
                                                         final List<Pair<String, String>> additionalHeaders)
            throws IOException {
        final ArrayList<String> urls = new ArrayList<>();
        urls.add(url);
        RestResponse resp;
        RestRequest req = new RestRequest.Builder().method(GET.METHOD).url(url).headers(additionalHeaders).build();
        resp = client.execute(req);
        while (Utils.isRedirected(resp.getStatus())) {
            final String requestUrl = resp.getHeader("Location");
            if (requestUrl == null) return null;
            if (urls.contains(requestUrl)) throw new IOException("Too many redirects");
            urls.add(requestUrl);
            req = new RestRequest.Builder().method(GET.METHOD).url(requestUrl).headers(additionalHeaders).build();
            resp = client.execute(req);
        }
        return resp;
    }

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
        final ParcelableAccount.ParcelableCredentials credentials = ParcelableAccount.ParcelableCredentials.getCredentials(context, accountId);
        if (credentials == null) return null;
        final Configuration conf = getConfiguration(context, includeEntities, includeRetweets, credentials);
        return new TwitterFactory(conf).getInstance(getAuthorization(credentials), cls);
    }

    private static Configuration getConfiguration(Context context, boolean includeEntities, boolean includeRetweets, ParcelableAccount.ParcelableCredentials credentials) {
        final TwidereApplication app = TwidereApplication.getInstance(context);
        final SharedPreferences prefs = context.getSharedPreferences(TwidereConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int connectionTimeout = prefs.getInt(SharedPreferenceConstants.KEY_CONNECTION_TIMEOUT, 10) * 1000;
        final boolean enableGzip = prefs.getBoolean(SharedPreferenceConstants.KEY_GZIP_COMPRESSING, true);
        final boolean ignoreSslError = prefs.getBoolean(SharedPreferenceConstants.KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(SharedPreferenceConstants.KEY_ENABLE_PROXY, false);
        // Here I use old consumer key/secret because it's default key for older
        // versions
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setHostAddressResolverFactory(new TwidereHostResolverFactory(app));
        cb.setHttpConnectionTimeout(connectionTimeout);
        cb.setGZIPEnabled(enableGzip);
        cb.setIgnoreSSLError(ignoreSslError);
        cb.setIncludeCards(true);
        cb.setCardsPlatform("Android-12");
//            cb.setModelVersion(7);
        if (enableProxy) {
            final String proxy_host = prefs.getString(SharedPreferenceConstants.KEY_PROXY_HOST, null);
            final int proxy_port = ParseUtils.parseInt(prefs.getString(SharedPreferenceConstants.KEY_PROXY_PORT, "-1"));
            if (!isEmpty(proxy_host) && proxy_port > 0) {
                cb.setHttpProxyHost(proxy_host);
                cb.setHttpProxyPort(proxy_port);
            }
        }
        final String apiUrlFormat = credentials.api_url_format;
        final String consumerKey = Utils.trim(credentials.consumer_key);
        final String consumerSecret = Utils.trim(credentials.consumer_secret);
        final boolean sameOAuthSigningUrl = credentials.same_oauth_signing_url;
        final boolean noVersionSuffix = credentials.no_version_suffix;
        if (!isEmpty(apiUrlFormat)) {
            final String versionSuffix = noVersionSuffix ? null : "/1.1/";
            cb.setRestBaseURL(Utils.getApiUrl(apiUrlFormat, "api", versionSuffix));
            cb.setOAuthBaseURL(Utils.getApiUrl(apiUrlFormat, "api", "/oauth/"));
            cb.setUploadBaseURL(Utils.getApiUrl(apiUrlFormat, "upload", versionSuffix));
            cb.setOAuthAuthorizationURL(Utils.getApiUrl(apiUrlFormat, null, null));
            if (!sameOAuthSigningUrl) {
                cb.setSigningRestBaseURL(TwitterConstants.DEFAULT_SIGNING_REST_BASE_URL);
                cb.setSigningOAuthBaseURL(TwitterConstants.DEFAULT_SIGNING_OAUTH_BASE_URL);
                cb.setSigningUploadBaseURL(TwitterConstants.DEFAULT_SIGNING_UPLOAD_BASE_URL);
            }
        }
        Utils.setClientUserAgent(context, consumerKey, consumerSecret, cb);

        cb.setIncludeEntitiesEnabled(includeEntities);
        cb.setIncludeRTsEnabled(includeRetweets);
        cb.setIncludeReplyCountEnabled(true);
        cb.setIncludeDescendentReplyCountEnabled(true);
        return cb.build();
    }

    public static Authorization getAuthorization(ParcelableAccount.ParcelableCredentials credentials) {
        switch (credentials.auth_type) {
            case TwidereDataStore.Accounts.AUTH_TYPE_OAUTH:
            case TwidereDataStore.Accounts.AUTH_TYPE_XAUTH: {
                final String consumerKey = TextUtils.isEmpty(credentials.consumer_key) ?
                        TWITTER_CONSUMER_KEY_LEGACY : credentials.consumer_key;
                final String consumerSecret = TextUtils.isEmpty(credentials.consumer_secret) ?
                        TWITTER_CONSUMER_SECRET_LEGACY : credentials.consumer_secret;
                final OAuthToken accessToken = new OAuthToken(credentials.oauth_token, credentials.oauth_token_secret);
                return new OAuthAuthorization(consumerKey, consumerSecret, accessToken);
            }
            case TwidereDataStore.Accounts.AUTH_TYPE_BASIC: {
                final String screenName = credentials.screen_name;
                final String username = credentials.basic_auth_username;
                final String loginName = username != null ? username : screenName;
                final String password = credentials.basic_auth_password;
                if (isEmpty(loginName) || isEmpty(password)) return null;
                return new BasicAuthorization(loginName, password);
            }
        }
        return new EmptyAuthorization();
    }

    public static RestHttpClient getDefaultHttpClient(final Context context) {
        if (context == null) return null;
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        //TODO set user agent
        TwidereApplication.getInstance(context).getDefaultUserAgent();
        return createHttpClient(context, prefs);
    }

    public static RestHttpClient createHttpClient(final Context context, final SharedPreferencesWrapper prefs) {
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
        final HostAddressResolver resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
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
        return new OkHttpRestClient(client);
    }


    public static Proxy getProxy(final SharedPreferencesWrapper prefs) {
        final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
        final int proxyPort = ParseUtils.parseInt(prefs.getString(KEY_PROXY_PORT, "-1"));
        if (!isEmpty(proxyHost) && proxyPort >= 0 && proxyPort < 65535) {
            final SocketAddress addr = InetSocketAddress.createUnresolved(proxyHost, proxyPort);
            return new Proxy(Proxy.Type.HTTP, addr);
        }
        return Proxy.NO_PROXY;
    }
}
