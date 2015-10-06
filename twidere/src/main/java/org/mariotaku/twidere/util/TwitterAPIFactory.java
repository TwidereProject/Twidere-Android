package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.SSLCertificateSocketFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.webkit.URLUtil;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.HttpRequestFactory;
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
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.Twitter;
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
import org.mariotaku.twidere.util.net.OkHttpRestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        return getDefaultHttpClient(context, ApplicationModule.get(context).getNetwork());
    }

    public static RestHttpClient getDefaultHttpClient(final Context context, final Network network) {
        if (context == null || network == null) return null;
        final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return createHttpClient(context, network, prefs);
    }

    public static RestHttpClient createHttpClient(final Context context, final Network network, final SharedPreferences prefs) {
        final OkHttpClient client = new OkHttpClient();
        updateHttpClientConfiguration(prefs, client);
        Internal.instance.setNetwork(client, network);
        return new OkHttpRestClient(context, client);
    }

    public static void updateHttpClientConfiguration(final SharedPreferences prefs, final OkHttpClient client) {
        final int connectionTimeout = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10);
        final boolean ignoreSslError = prefs.getBoolean(KEY_IGNORE_SSL_ERROR, false);
        final boolean enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false);

        client.setConnectTimeout(connectionTimeout, TimeUnit.SECONDS);
        final long connectionTimeoutMillis = TimeUnit.MILLISECONDS.convert(connectionTimeout, TimeUnit.SECONDS);
        final SSLSocketFactory sslSocketFactory;
        if (ignoreSslError) {
            sslSocketFactory = SSLCertificateSocketFactory.getInsecure((int) connectionTimeoutMillis, null);
            if (sslSocketFactory instanceof SSLCertificateSocketFactory) {

            }
            client.setSslSocketFactory(sslSocketFactory);
        } else {
            client.setSslSocketFactory(null);
        }
        if (enableProxy) {
            client.setProxy(getProxy(prefs));
        } else {
            client.setProxy(Proxy.NO_PROXY);
        }
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
                userAgent = getUserAgentName(officialKeyType);
            } else {
                userAgent = getTwidereUserAgent(context);
            }
        } else {
            userAgent = getTwidereUserAgent(context);
        }
        factory.setClient(ApplicationModule.get(context).getRestHttpClient());
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(auth);
        factory.setRequestInfoFactory(new TwidereRequestInfoFactory());
        factory.setHttpRequestFactory(new TwidereHttpRequestFactory(userAgent));
        factory.setExceptionFactory(new TwidereExceptionFactory());
        return factory.build(cls);
    }

    public static <T> T getInstance(final Context context, final Endpoint endpoint, final ParcelableCredentials credentials, Class<T> cls) {
        return TwitterAPIFactory.getInstance(context, endpoint, getAuthorization(credentials), cls);
    }

    static <T> T getInstance(final Context context, final ParcelableCredentials credentials, final Class<T> cls) {
        if (credentials == null) return null;
        return TwitterAPIFactory.getInstance(context, getEndpoint(credentials, cls), credentials, cls);
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

    private static void addParameter(List<Pair<String, String>> params, String name, Object value) {
        params.add(Pair.create(name, String.valueOf(value)));
    }

    private static void addPart(List<Pair<String, TypedData>> params, String name, Object value) {
        final TypedData typedData = new StringTypedData(String.valueOf(value), Charset.defaultCharset());
        params.add(Pair.create(name, typedData));
    }

    public static boolean verifyApiFormat(String format) {
        return URLUtil.isValidUrl(getApiBaseUrl(format, "test"));
    }

    public static String getApiBaseUrl(String format, final String domain) {
        if (format == null) return null;
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
        if (urlBase == null) return null;
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

    public static String getUserAgentName(ConsumerKeyType type) {
        switch (type) {
            case TWITTER_FOR_ANDROID: {
                return "TwitterAndroid";
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
                return "TweetDeck";
            }
        }
        return "Twitter";
    }

    public static String getTwidereUserAgent(final Context context) {
        final PackageManager pm = context.getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(TWIDERE_PACKAGE_NAME, 0);
            return TWIDERE_APP_NAME + " " + TWIDERE_PROJECT_URL + " / " + pi.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            return TWIDERE_APP_NAME + " " + TWIDERE_PROJECT_URL;
        }
    }

    public static Endpoint getOAuthRestEndpoint(String apiUrlFormat, boolean sameOAuthSigningUrl, boolean noVersionSuffix) {
        return getOAuthEndpoint(apiUrlFormat, "api", noVersionSuffix ? null : "1.1", sameOAuthSigningUrl);
    }

    public static Endpoint getOAuthEndpoint(String apiUrlFormat, String domain, String versionSuffix, boolean sameOAuthSigningUrl) {
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

    public static class TwidereRequestInfoFactory implements RequestInfoFactory {

        private static HashMap<String, String> sExtraParams = new HashMap<>();

        static {
            sExtraParams.put("include_cards", "true");
            sExtraParams.put("cards_platform", "Android-12");
            sExtraParams.put("include_entities", "true");
            sExtraParams.put("include_my_retweet", "1");
            sExtraParams.put("include_rts", "1");
            sExtraParams.put("include_reply_count", "true");
            sExtraParams.put("include_descendent_reply_count", "true");
            sExtraParams.put("full_text", "true");
            sExtraParams.put("model_version", "7");
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
                        addParameter(params, key, sExtraParams.get(key));
                    }
                } else {
                    for (String key : extraParamKeys) {
                        addPart(parts, key, sExtraParams.get(key));
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
            te.setResponse(response);
            return te;
        }
    }

}
