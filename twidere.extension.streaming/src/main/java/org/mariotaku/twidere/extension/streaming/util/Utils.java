package org.mariotaku.twidere.extension.streaming.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.SSLCertificateSocketFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;

import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.TwidereSharedPreferences;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.util.TwitterConverter;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterAPIUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;

public class Utils implements TwidereConstants {


    public static void closeSilently(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignore) {

        }
    }



    public static RestHttpClient getDefaultHttpClient(final Context context) {
        if (context == null) return null;
        final SharedPreferences prefs = Twidere.getSharedPreferences(context);
        return createHttpClient(context, prefs);
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
        Internal.instance.setNetwork(client, TwidereHostAddressResolver.getInstance(context));
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



    public static long[] getActivatedAccountIds(final Context context) {
        long[] accounts = new long[0];
        if (context == null) return accounts;
        final String[] cols = new String[]{Accounts.ACCOUNT_ID};
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
                null, Accounts.ACCOUNT_ID);
        if (cur != null) {
            final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
            cur.moveToFirst();
            accounts = new long[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                accounts[i] = cur.getLong(idx);
                i++;
                cur.moveToNext();
            }
            cur.close();
        }
        return accounts;
    }

    public static String getNonEmptyString(final TwidereSharedPreferences pref, final String key, final String def) {
        if (pref == null) return def;
        final String val = pref.getString(key, def);
        return isEmpty(val) ? def : val;
    }

    public static String replaceLast(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) return text;
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

}
