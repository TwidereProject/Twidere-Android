package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;
import android.text.TextUtils;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Dns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.okhttp.OkHttpRestClient;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

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
        final OkHttpClient client = new OkHttpClient();
        initDefaultHttpClient(context, prefs, client, dns);
        return new OkHttpRestClient(client);
    }

    public static void initDefaultHttpClient(Context context, SharedPreferences prefs, OkHttpClient client, Dns dns) {
        updateHttpClientConfiguration(context, prefs, dns, client);
        DebugModeUtils.initForHttpClient(client);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final SharedPreferences prefs,
                                                     Dns dns, final OkHttpClient client) {
        final int connectionTimeoutSeconds = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        client.setConnectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS);

        if (ignoreSslError) {
            // We use insecure connections intentionally
            client.setSslSocketFactory(SSLCertificateSocketFactory.getInsecure((int)
                            TimeUnit.SECONDS.toMillis(connectionTimeoutSeconds),
                    new SSLSessionCache(context)));
        } else {
            client.setSslSocketFactory(null);
        }
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRange(proxyPort, 0, 65535,
                    TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE)) {
                client.setProxy(null);
                client.setProxySelector(new TwidereProxySelector(context, getProxyType(proxyType),
                        proxyHost, proxyPort));
            }
            final String username = prefs.getString(KEY_PROXY_USERNAME, null);
            final String password = prefs.getString(KEY_PROXY_PASSWORD, null);
            client.setAuthenticator(new Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    final Request.Builder builder = response.request().newBuilder();
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        final String credential = Credentials.basic(username, password);
                        builder.header("Proxy-Authorization", credential);
                    }
                    return builder.build();
                }
            });
        } else {
            client.setProxy(null);
            client.setProxySelector(null);
            client.setAuthenticator(null);
        }
        if (dns != null) {
            client.setDns(dns);
        }
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if ("socks".equalsIgnoreCase(proxyType)) return Proxy.Type.SOCKS;
        return Proxy.Type.HTTP;
    }
}
