package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.okhttp3.OkHttpRestClient;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.dagger.DependencyHolder;
import org.mariotaku.twidere.util.net.TwidereDns;
import org.mariotaku.twidere.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 16/1/27.
 */
public class HttpClientFactory implements Constants {

    public static RestHttpClient createRestHttpClient(final Context context,
                                                      final SharedPreferencesWrapper prefs, final Dns dns,
                                                      final ConnectionPool connectionPool) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        initOkHttpClient(context, prefs, builder, dns, connectionPool);
        return new OkHttpRestClient(builder.build());
    }

    public static void initOkHttpClient(final Context context, final SharedPreferencesWrapper prefs,
                                        final OkHttpClient.Builder builder, final Dns dns,
                                        final ConnectionPool connectionPool) {
        updateHttpClientConfiguration(context, builder, prefs, dns, connectionPool);
        DebugModeUtils.initForOkHttpClient(builder);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final OkHttpClient.Builder builder,
                                                     final SharedPreferencesWrapper prefs, final Dns dns,
                                                     final ConnectionPool connectionPool) {
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);
        builder.connectTimeout(prefs.getInt(KEY_CONNECTION_TIMEOUT, 10), TimeUnit.SECONDS);
        final boolean retryOnConnectionFailure = prefs.getBoolean(KEY_RETRY_ON_NETWORK_ISSUE);
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        builder.connectionPool(connectionPool);
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                final Proxy.Type type = getProxyType(proxyType);
                if (type != Proxy.Type.DIRECT) {
                    if (TwidereDns.isValidIpAddress(proxyHost) && !retryOnConnectionFailure) {
                        builder.proxy(new Proxy(type, InetSocketAddress.createUnresolved(proxyHost, proxyPort)));
                    } else {
                        builder.proxySelector(new TwidereProxySelector(context, type, proxyHost, proxyPort));
                    }
                }
            }
            final String username = prefs.getString(KEY_PROXY_USERNAME, null);
            final String password = prefs.getString(KEY_PROXY_PASSWORD, null);
            builder.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    final Request.Builder builder = response.request().newBuilder();
                    if (response.code() == 407) {
                        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                            final String credential = Credentials.basic(username, password);
                            builder.header("Proxy-Authorization", credential);
                        }
                    }
                    return builder.build();
                }

            });
        }
        builder.dns(dns);
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if (proxyType == null) return Proxy.Type.DIRECT;
        switch (proxyType.toLowerCase()) {
//            case "socks": {
//                return Proxy.Type.SOCKS;
//            }
            case "http": {
                return Proxy.Type.HTTP;
            }
        }
        return Proxy.Type.DIRECT;
    }

    public static void reloadConnectivitySettings(Context context) {
        final DependencyHolder holder = DependencyHolder.get(context);
        final RestHttpClient client = holder.getRestHttpClient();
        if (client instanceof OkHttpRestClient) {
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            initOkHttpClient(context, holder.getPreferences(), builder,
                    holder.getDns(), holder.getConnectionPoll());
            final OkHttpRestClient restClient = (OkHttpRestClient) client;
            restClient.setClient(builder.build());
        }
    }
}
