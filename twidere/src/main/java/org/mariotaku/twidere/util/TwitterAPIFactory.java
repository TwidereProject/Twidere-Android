package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.HttpRequestFactory;
import org.mariotaku.restfu.Pair;
import org.mariotaku.restfu.RequestInfoFactory;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestMethodInfo;
import org.mariotaku.restfu.RestRequestInfo;
import org.mariotaku.restfu.annotation.RestMethod;
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.FileValue;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.StringTypedData;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.restfu.okhttp.OkHttpRestClient;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterCaps;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterOAuth;
import org.mariotaku.twidere.api.twitter.TwitterUpload;
import org.mariotaku.twidere.api.twitter.TwitterUserStream;
import org.mariotaku.twidere.api.twitter.auth.BasicAuthorization;
import org.mariotaku.twidere.api.twitter.auth.EmptyAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthEndpoint;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.util.TwitterConverter;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.RequestType;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DependencyHolder;
import org.mariotaku.twidere.util.net.NetworkUsageUtils;
import org.mariotaku.twidere.util.net.TwidereProxySelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterAPIFactory implements TwidereConstants {

    public static final String CARDS_PLATFORM_ANDROID_12 = "Android-12";

    @WorkerThread
    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities) {
        if (context == null) return null;
        return getDefaultTwitterInstance(context, includeEntities, true);
    }

    @WorkerThread
    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities,
                                                    final boolean includeRetweets) {
        if (context == null) return null;
        return getTwitterInstance(context, Utils.getDefaultAccountId(context), includeEntities, includeRetweets);
    }

    @WorkerThread
    public static Twitter getTwitterInstance(final Context context, final long accountId,
                                             final boolean includeEntities) {
        return getTwitterInstance(context, accountId, includeEntities, true);
    }

    @Nullable
    @WorkerThread
    public static Twitter getTwitterInstance(final Context context, final long accountId,
                                             final boolean includeEntities,
                                             final boolean includeRetweets) {
        return getTwitterInstance(context, accountId, includeEntities, includeRetweets, Twitter.class);
    }

    @Nullable
    @WorkerThread
    public static <T> T getTwitterInstance(final Context context, final long accountId,
                                           final boolean includeEntities,
                                           final boolean includeRetweets, Class<T> cls) {
        if (context == null) return null;
        final ParcelableCredentials credentials = ParcelableCredentials.getCredentials(context, accountId);
        final HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("include_entities", String.valueOf(includeEntities));
        extraParams.put("include_retweets", String.valueOf(includeRetweets));
        return getInstance(context, credentials, extraParams, cls);
    }

    public static RestHttpClient getDefaultHttpClient(final Context context) {
        if (context == null) return null;
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return createHttpClient(context, prefs);
    }

    public static RestHttpClient createHttpClient(final Context context, final SharedPreferences prefs) {
        final OkHttpClient client = new OkHttpClient();
        updateHttpClientConfiguration(context, prefs, client);
        DebugModeUtils.initForHttpClient(client);
        NetworkUsageUtils.initForHttpClient(context, client);
        return new OkHttpRestClient(client);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static void updateHttpClientConfiguration(final Context context,
                                                     final SharedPreferences prefs, final OkHttpClient client) {
        final int connectionTimeout = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        client.setConnectTimeout(connectionTimeout, TimeUnit.SECONDS);
        client.setReadTimeout(0, TimeUnit.SECONDS);
        client.setWriteTimeout(0, TimeUnit.SECONDS);
        final SSLSocketFactory sslSocketFactory;
        if (ignoreSslError) {
            // We intentionally use insecure connections
            sslSocketFactory = SSLCertificateSocketFactory.getInsecure(0, null);
            if (sslSocketFactory instanceof SSLCertificateSocketFactory) {

            }
            client.setSslSocketFactory(sslSocketFactory);
        } else {
            client.setSslSocketFactory(null);
        }
        if (enableProxy) {
            final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
            final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
            final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
            if (!isEmpty(proxyHost) && TwidereMathUtils.inRangeInclusiveInclusive(proxyPort, 0, 65535)) {
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
            client.setProxySelector(null);
            client.setAuthenticator(null);
        }
    }


    public static Proxy getProxy(final SharedPreferences prefs) {
        final String proxyType = prefs.getString(KEY_PROXY_TYPE, null);
        final String proxyHost = prefs.getString(KEY_PROXY_HOST, null);
        final int proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1);
        if (!isEmpty(proxyHost) && TwidereMathUtils.inRangeInclusiveInclusive(proxyPort, 0, 65535)) {
            final SocketAddress addr = InetSocketAddress.createUnresolved(proxyHost, proxyPort);
            return new Proxy(getProxyType(proxyType), addr);
        }
        return Proxy.NO_PROXY;
    }

    private static Proxy.Type getProxyType(String proxyType) {
        if ("socks".equalsIgnoreCase(proxyType)) return Proxy.Type.SOCKS;
        return Proxy.Type.HTTP;
    }

    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final Authorization auth, final Map<String, String> extraRequestParams,
                                    final Class<T> cls) {
        final RestAPIFactory factory = new RestAPIFactory();
        final String userAgent;
        if (auth instanceof OAuthAuthorization) {
            final String consumerKey = ((OAuthAuthorization) auth).getConsumerKey();
            final String consumerSecret = ((OAuthAuthorization) auth).getConsumerSecret();
            final ConsumerKeyType officialKeyType = TwitterContentUtils.getOfficialKeyType(context, consumerKey, consumerSecret);
            if (officialKeyType != ConsumerKeyType.UNKNOWN) {
                userAgent = getUserAgentName(context, officialKeyType);
            } else {
                userAgent = getTwidereUserAgent(context);
            }
        } else {
            userAgent = getTwidereUserAgent(context);
        }
        DependencyHolder holder = new DependencyHolder(context);
        factory.setClient(holder.getRestHttpClient());
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(auth);
        factory.setRequestInfoFactory(new TwidereRequestInfoFactory(extraRequestParams));
        factory.setHttpRequestFactory(new TwidereHttpRequestFactory(userAgent));
        factory.setExceptionFactory(new TwidereExceptionFactory());
        return factory.build(cls);
    }

    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final Authorization auth, final Class<T> cls) {
        return getInstance(context, endpoint, auth, null, cls);
    }

    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final ParcelableCredentials credentials,
                                    final Class<T> cls) {
        return getInstance(context, endpoint, credentials, null, cls);
    }

    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final ParcelableCredentials credentials,
                                    final Map<String, String> extraRequestParams, final Class<T> cls) {
        return TwitterAPIFactory.getInstance(context, endpoint, getAuthorization(credentials),
                extraRequestParams, cls);
    }

    @WorkerThread
    static <T> T getInstance(final Context context, final ParcelableCredentials credentials,
                             final Class<T> cls) {
        return getInstance(context, credentials, null, cls);
    }

    @WorkerThread
    static <T> T getInstance(final Context context, final ParcelableCredentials credentials,
                             final Map<String, String> extraRequestParams, final Class<T> cls) {
        if (credentials == null) return null;
        return TwitterAPIFactory.getInstance(context, getEndpoint(credentials, cls), credentials,
                extraRequestParams, cls);
    }

    public static Endpoint getEndpoint(ParcelableCredentials credentials, Class<?> cls) {
        final String apiUrlFormat;
        final boolean sameOAuthSigningUrl = credentials.same_oauth_signing_url;
        final boolean noVersionSuffix = credentials.no_version_suffix;
        if (!isEmpty(credentials.api_url_format)) {
            apiUrlFormat = credentials.api_url_format;
        } else {
            apiUrlFormat = DEFAULT_TWITTER_API_URL_FORMAT;
        }
        final String domain, versionSuffix;
        if (Twitter.class.isAssignableFrom(cls)) {
            domain = "api";
            versionSuffix = noVersionSuffix ? null : "/1.1/";
        } else if (TwitterUpload.class.isAssignableFrom(cls)) {
            domain = "upload";
            versionSuffix = noVersionSuffix ? null : "/1.1/";
        } else if (TwitterOAuth.class.isAssignableFrom(cls)) {
            domain = "api";
            versionSuffix = "oauth";
        } else if (TwitterUserStream.class.isAssignableFrom(cls)) {
            domain = "userstream";
            versionSuffix = noVersionSuffix ? null : "/1.1/";
        } else if (TwitterCaps.class.isAssignableFrom(cls)) {
            domain = "caps";
            versionSuffix = null;
        } else {
            throw new TwitterConverter.UnsupportedTypeException(cls);
        }
        final String endpointUrl;
        endpointUrl = getApiUrl(apiUrlFormat, domain, versionSuffix);
        if (credentials.auth_type == ParcelableCredentials.AUTH_TYPE_XAUTH || credentials.auth_type == ParcelableCredentials.AUTH_TYPE_OAUTH) {
            final String signEndpointUrl;
            if (!sameOAuthSigningUrl) {
                signEndpointUrl = getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix);
            } else {
                signEndpointUrl = endpointUrl;
            }
            return new OAuthEndpoint(endpointUrl, signEndpointUrl);
        }
        return new Endpoint(endpointUrl);
    }

    public static Authorization getAuthorization(ParcelableCredentials credentials) {
        switch (credentials.auth_type) {
            case ParcelableCredentials.AUTH_TYPE_OAUTH:
            case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                final String consumerKey = TextUtils.isEmpty(credentials.consumer_key) ?
                        TWITTER_CONSUMER_KEY_LEGACY : credentials.consumer_key;
                final String consumerSecret = TextUtils.isEmpty(credentials.consumer_secret) ?
                        TWITTER_CONSUMER_SECRET_LEGACY : credentials.consumer_secret;
                final OAuthToken accessToken = new OAuthToken(credentials.oauth_token,
                        credentials.oauth_token_secret);
                if (isValidConsumerKeySecret(consumerKey) && isValidConsumerKeySecret(consumerSecret))
                    return new OAuthAuthorization(consumerKey, consumerSecret, accessToken);
                return new OAuthAuthorization(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, accessToken);
            }
            case ParcelableCredentials.AUTH_TYPE_BASIC: {
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

    private static void addParam(List<Pair<String, String>> params, String name, Object value) {
        params.add(Pair.create(name, String.valueOf(value)));
    }

    private static void addPart(List<Pair<String, TypedData>> params, String name, Object value) {
        final TypedData typedData = new StringTypedData(String.valueOf(value), Charset.defaultCharset());
        params.add(Pair.create(name, typedData));
    }

    public static boolean verifyApiFormat(@NonNull String format) {
        return URLUtil.isValidUrl(getApiBaseUrl(format, "test"));
    }

    @NonNull
    public static String getApiBaseUrl(@NonNull String format, @Nullable final String domain) {
        final Matcher matcher = Pattern.compile("\\[(\\.?)DOMAIN(\\.?)\\]", Pattern.CASE_INSENSITIVE).matcher(format);
        if (!matcher.find()) {
            // For backward compatibility
            format = substituteLegacyApiBaseUrl(format, domain);
            if (!format.endsWith("/1.1") && !format.endsWith("/1.1/")) {
                return format;
            }
            final String versionSuffix = "/1.1";
            final int suffixLength = versionSuffix.length();
            final int lastIndex = format.lastIndexOf(versionSuffix);
            return format.substring(0, lastIndex) + format.substring(lastIndex + suffixLength);
        }
        if (TextUtils.isEmpty(domain)) return matcher.replaceAll("");
        return matcher.replaceAll(String.format("$1%s$2", domain));
    }

    private static String substituteLegacyApiBaseUrl(@NonNull String format, String domain) {
        final int startOfHost = format.indexOf("://") + 3, endOfHost = format.indexOf('/', startOfHost);
        final String host = endOfHost != -1 ? format.substring(startOfHost, endOfHost) : format.substring(startOfHost);
        if (!host.equalsIgnoreCase("api.twitter.com")) return format;
        return format.substring(0, startOfHost) + domain + ".twitter.com" + format.substring(endOfHost);
    }

    public static String getApiUrl(final String pattern, final String domain, final String appendPath) {
        final String urlBase = getApiBaseUrl(pattern, domain);
        if (appendPath == null) return urlBase.endsWith("/") ? urlBase : urlBase + "/";
        final StringBuilder sb = new StringBuilder(urlBase);
        if (urlBase.endsWith("/")) {
            sb.append(appendPath.startsWith("/") ? appendPath.substring(1) : appendPath);
        } else {
            if (appendPath.startsWith("/")) {
                sb.append(appendPath);
            } else {
                sb.append('/');
                sb.append(appendPath);
            }
        }
        return sb.toString();
    }

    @WorkerThread
    public static String getUserAgentName(Context context, ConsumerKeyType type) {
        switch (type) {
            case TWITTER_FOR_ANDROID: {
                final String versionName = "5.2.4";
                final String internalVersionName = "524-r1";
                final String model = Build.MODEL;
                final String manufacturer = Build.MANUFACTURER;
                final int sdkInt = Build.VERSION.SDK_INT;
                final String device = Build.DEVICE;
                final String brand = Build.BRAND;
                final String product = Build.PRODUCT;
                final int debug = BuildConfig.DEBUG ? 1 : 0;
                return String.format(Locale.ROOT, "TwitterAndroid/%s (%s) %s/%d (%s;%s;%s;%s;%d)",
                        versionName, internalVersionName, model, sdkInt, manufacturer, device, brand,
                        product, debug);
            }
            case TWITTER_FOR_IPHONE: {
                return "Twitter-iPhone";
            }
            case TWITTER_FOR_IPAD: {
                return "Twitter-iPad";
            }
            case TWITTER_FOR_MAC: {
                return "Twitter-Mac";
            }
            case TWEETDECK: {
                return UserAgentUtils.getDefaultUserAgentStringSafe(context);
            }
        }
        return "Twitter";
    }

    public static String getTwidereUserAgent(final Context context) {
        final PackageManager pm = context.getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return String.format("%s %s / %s", TWIDERE_APP_NAME, TWIDERE_PROJECT_URL, pi.versionName);
        } catch (final PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static Endpoint getOAuthRestEndpoint(String apiUrlFormat, boolean sameOAuthSigningUrl, boolean noVersionSuffix) {
        return getOAuthEndpoint(apiUrlFormat, "api", noVersionSuffix ? null : "1.1", sameOAuthSigningUrl);
    }

    public static Endpoint getOAuthEndpoint(String apiUrlFormat, @Nullable String domain,
                                            @Nullable String versionSuffix, boolean sameOAuthSigningUrl) {
        String endpointUrl, signEndpointUrl;
        endpointUrl = getApiUrl(apiUrlFormat, domain, versionSuffix);
        if (!sameOAuthSigningUrl) {
            signEndpointUrl = getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix);
        } else {
            signEndpointUrl = endpointUrl;
        }
        return new OAuthEndpoint(endpointUrl, signEndpointUrl);
    }

    public static OAuthToken getOAuthToken(String consumerKey, String consumerSecret) {
        if (isValidConsumerKeySecret(consumerKey) && isValidConsumerKeySecret(consumerSecret))
            return new OAuthToken(consumerKey, consumerSecret);
        return new OAuthToken(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
    }

    public static boolean isValidConsumerKeySecret(@NonNull CharSequence text) {
        for (int i = 0, j = text.length(); i < j; i++) {
            if (!isAsciiLetterOrDigit(text.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isAsciiLetterOrDigit(int codePoint) {
        return ('A' <= codePoint && codePoint <= 'Z') || ('a' <= codePoint && codePoint <= 'z') || '0' <= codePoint && codePoint <= '9';
    }

    public static class Options {

        final HashMap<String, String> extras = new HashMap<>();

        public void putExtra(String key, String value) {
            extras.put(key, value);
        }
    }

    public static class TwidereRequestInfoFactory implements RequestInfoFactory {

        private static Map<String, String> sDefaultRequestParams;

        static {
            final HashMap<String, String> map = new HashMap<>();
            try {
                map.put("include_cards", "true");
                map.put("cards_platform", CARDS_PLATFORM_ANDROID_12);
                map.put("include_entities", "true");
                map.put("include_my_retweet", "true");
                map.put("include_rts", "true");
                map.put("include_reply_count", "true");
                map.put("include_descendent_reply_count", "true");
                map.put("full_text", "true");
                map.put("model_version", "7");
            } finally {
                sDefaultRequestParams = Collections.unmodifiableMap(map);
            }
        }

        private final Map<String, String> extraRequestParams;

        TwidereRequestInfoFactory(Map<String, String> extraRequestParams) {
            this.extraRequestParams = extraRequestParams;
        }


        @Override
        public RestRequestInfo create(RestMethodInfo methodInfo) {
            final RestMethod method = methodInfo.getMethod();
            final String path = methodInfo.getPath();
            final List<Pair<String, String>> queries = new ArrayList<>(methodInfo.getQueries());
            final List<Pair<String, String>> forms = new ArrayList<>(methodInfo.getForms());
            final List<Pair<String, String>> headers = methodInfo.getHeaders();
            final List<Pair<String, TypedData>> parts = methodInfo.getParts();
            final FileValue file = methodInfo.getFile();
            final Map<String, Object> extras = methodInfo.getExtras();
            final MethodExtra methodExtra = methodInfo.getMethodExtra();
            if (methodExtra != null && "extra_params".equals(methodExtra.name())) {
                final String[] extraParamKeys = methodExtra.values();
                if (parts.isEmpty()) {
                    final List<Pair<String, String>> params = method.hasBody() ? forms : queries;
                    for (String key : extraParamKeys) {
                        if (extraRequestParams != null && extraRequestParams.containsKey(key)) {
                            addParam(params, key, extraRequestParams.get(key));
                        } else {
                            addParam(params, key, sDefaultRequestParams.get(key));
                        }
                    }
                } else {
                    for (String key : extraParamKeys) {
                        if (extraRequestParams != null && extraRequestParams.containsKey(key)) {
                            addPart(parts, key, extraRequestParams.get(key));
                        } else {
                            addPart(parts, key, sDefaultRequestParams.get(key));
                        }
                    }
                }
            }
            return new RestRequestInfo(method.value(), path, queries, forms, headers, parts, file,
                    methodInfo.getBody(), extras);
        }
    }

    public static class TwidereHttpRequestFactory implements HttpRequestFactory {

        private final String userAgent;

        public TwidereHttpRequestFactory(final String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public RestHttpRequest create(@NonNull Endpoint endpoint, @NonNull RestRequestInfo info,
                                      @Nullable Authorization authorization) {
            final String restMethod = info.getMethod();
            final String url = Endpoint.constructUrl(endpoint.getUrl(), info);
            final ArrayList<Pair<String, String>> headers = new ArrayList<>(info.getHeaders());

            if (authorization != null && authorization.hasAuthorization()) {
                headers.add(Pair.create("Authorization", authorization.getHeader(endpoint, info)));
            }
            headers.add(Pair.create("User-Agent", userAgent));
            return new RestHttpRequest(restMethod, url, headers, info.getBody(), RequestType.API);
        }
    }

    public static class TwidereExceptionFactory implements ExceptionFactory {
        @Override
        public Exception newException(Throwable cause, RestHttpRequest request, RestHttpResponse response) {
            final TwitterException te;
            if (cause != null) {
                te = new TwitterException(cause);
            } else {
                te = TwitterConverter.parseTwitterException(response);
            }
            te.setHttpResponse(response);
            return te;
        }
    }

}
