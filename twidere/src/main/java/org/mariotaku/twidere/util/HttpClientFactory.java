package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.text.TextUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.inetaddrjni.library.InetAddressUtils;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.okhttp.OkHttpRestClient;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Authenticator;
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
    public static RestHttpClient getDefaultHttpClient(final Context context, SharedPreferences prefs, Dns dns) {
        if (context == null) return null;
        return createHttpClient(context, prefs, dns);
    }

    public static RestHttpClient createHttpClient(final Context context, final SharedPreferences prefs, Dns dns) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        initDefaultHttpClient(context, prefs, builder, dns);
        return new OkHttpRestClient(builder.build());
    }

    public static void initDefaultHttpClient(Context context, SharedPreferences prefs, OkHttpClient.Builder builder, Dns dns) {
        updateHttpClientConfiguration(context, prefs, dns, builder);
        DebugModeUtils.initForHttpClient(builder);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final SharedPreferences prefs,
                                                     Dns dns, final OkHttpClient.Builder builder) {
        final int connectionTimeoutSeconds = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        builder.connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS);

        if (ignoreSslError) {
            // We use insecure connections intentionally
            final int sslConnectTimeout = ((int) TimeUnit.SECONDS.toMillis(connectionTimeoutSeconds));
            builder.sslSocketFactory(SSLCertificateSocketFactory.getInsecure(sslConnectTimeout, null));
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                final Proxy.Type type = getProxyType(proxyType);
                if (InetAddressUtils.getInetAddressType(proxyHost) != 0) {
                    builder.proxy(new Proxy(type, InetSocketAddress.createUnresolved(proxyHost, proxyPort)));
                } else {
                    builder.proxySelector(new TwidereProxySelector(context, type, proxyHost, proxyPort));
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
        if (dns != null) {
            builder.dns(dns);
        }
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if ("socks".equalsIgnoreCase(proxyType)) return Proxy.Type.SOCKS;
        return Proxy.Type.HTTP;
    }
}
