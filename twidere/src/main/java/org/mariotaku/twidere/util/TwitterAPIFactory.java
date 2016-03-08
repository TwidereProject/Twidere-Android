package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.sqliteqb.library.Expression;
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
import org.mariotaku.twidere.api.twitter.util.TwitterConverterFactory;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterAPIFactory implements TwidereConstants {

    public static final String CARDS_PLATFORM_ANDROID_12 = "Android-12";


    private static final SimpleValueMap sConstantPoll = new SimpleValueMap();

    static {
        sConstantPoll.put("include_cards", "true");
        sConstantPoll.put("cards_platform", CARDS_PLATFORM_ANDROID_12);
        sConstantPoll.put("include_entities", "true");
        sConstantPoll.put("include_my_retweet", "true");
        sConstantPoll.put("include_rts", "true");
        sConstantPoll.put("include_reply_count", "true");
        sConstantPoll.put("include_descendent_reply_count", "true");
        sConstantPoll.put("full_text", "true");
        sConstantPoll.put("model_version", "7");
        sConstantPoll.put("skip_aggregation", "false");
    }

    @WorkerThread
    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities) {
        if (context == null) return null;
        return getDefaultTwitterInstance(context, includeEntities, true);
    }

    @WorkerThread
    public static Twitter getDefaultTwitterInstance(final Context context, final boolean includeEntities,
                                                    final boolean includeRetweets) {
        if (context == null) return null;
        final UserKey accountKey = Utils.getDefaultAccountKey(context);
        if (accountKey == null) return null;
        return getTwitterInstance(context, accountKey, includeEntities, includeRetweets);
    }

    @WorkerThread
    public static Twitter getTwitterInstance(@NonNull final Context context,
                                             @NonNull final UserKey accountKey,
                                             final boolean includeEntities) {
        return getTwitterInstance(context, accountKey, includeEntities, true);
    }

    @Nullable
    @WorkerThread
    public static Twitter getTwitterInstance(@NonNull final Context context,
                                             @NonNull final UserKey accountKey,
                                             final boolean includeEntities,
                                             final boolean includeRetweets) {
        return getTwitterInstance(context, accountKey, includeEntities, includeRetweets, Twitter.class);
    }

    @Nullable
    public static Twitter getTwitterInstance(@NonNull final Context context,
                                             @NonNull final ParcelableCredentials credentials,
                                             final boolean includeEntities, final boolean includeRetweets) {
        return getTwitterInstance(context, credentials, includeEntities, includeRetweets, Twitter.class);
    }


    @Nullable
    @WorkerThread
    public static <T> T getTwitterInstance(@NonNull final Context context,
                                           @NonNull final UserKey accountKey,
                                           final boolean includeEntities,
                                           final boolean includeRetweets,
                                           @NonNull Class<T> cls) {
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context, accountKey);
        if (credentials == null) return null;
        return getTwitterInstance(context, credentials, includeEntities, includeRetweets, cls);
    }

    @Nullable
    public static <T> T getTwitterInstance(@NonNull final Context context,
                                           @NonNull final ParcelableCredentials credentials,
                                           final boolean includeEntities, final boolean includeRetweets,
                                           @NonNull Class<T> cls) {
        final HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("include_entities", String.valueOf(includeEntities));
        extraParams.put("include_retweets", String.valueOf(includeRetweets));
        return getInstance(context, credentials, extraParams, cls);
    }


    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final Authorization auth, final Map<String, String> extraRequestParams,
                                    final Class<T> cls, boolean twitterExtraQueries) {
        final RestAPIFactory<TwitterException> factory = new RestAPIFactory<>();
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
        DependencyHolder holder = DependencyHolder.get(context);
        factory.setHttpClient(holder.getRestHttpClient());
        factory.setAuthorization(auth);
        factory.setEndpoint(endpoint);
        if (twitterExtraQueries) {
            factory.setConstantPool(sConstantPoll);
        } else {
            factory.setConstantPool(new SimpleValueMap());
        }
        final TwitterConverterFactory converterFactory = new TwitterConverterFactory();
        factory.setRestConverterFactory(converterFactory);
        factory.setHttpRequestFactory(new TwidereHttpRequestFactory(userAgent));
        factory.setExceptionFactory(new TwidereExceptionFactory(converterFactory));
        return factory.build(cls);
    }

    @WorkerThread
    public static <T> T getInstance(final Context context, final Endpoint endpoint,
                                    final Authorization auth, final Class<T> cls) {
        return getInstance(context, endpoint, auth, null, cls, true);
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
        return getInstance(context, endpoint, getAuthorization(credentials), extraRequestParams, cls,
                isTwitterCredentials(credentials));
    }

    public static boolean isTwitterCredentials(Context context, UserKey accountId) {
        return isTwitterCredentials(ParcelableAccountUtils.getAccount(context, accountId));
    }

    public static boolean isTwitterCredentials(ParcelableAccount credentials) {
        return credentials.account_type == null ||
                ParcelableCredentials.ACCOUNT_TYPE_TWITTER.equals(credentials.account_type);
    }

    public static boolean isStatusNetCredentials(ParcelableAccount credentials) {
        return ParcelableCredentials.ACCOUNT_TYPE_STATUSNET.equals(credentials.account_type);
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
            versionSuffix = "/oauth";
        } else if (TwitterUserStream.class.isAssignableFrom(cls)) {
            domain = "userstream";
            versionSuffix = noVersionSuffix ? null : "/1.1/";
        } else if (TwitterCaps.class.isAssignableFrom(cls)) {
            domain = "caps";
            versionSuffix = null;
        } else {
            throw new TwitterConverterFactory.UnsupportedTypeException(cls);
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

    @Nullable
    public static Authorization getAuthorization(@Nullable ParcelableCredentials credentials) {
        if (credentials == null) return null;
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
        return matcher.replaceAll("$1" + domain + "$2");
    }

    @NonNull
    static String substituteLegacyApiBaseUrl(@NonNull String format, String domain) {
        final int startOfHost = format.indexOf("://") + 3;
        if (startOfHost < 0) return getApiBaseUrl("https://[DOMAIN.]twitter.com/", domain);
        final int endOfHost = format.indexOf('/', startOfHost);
        final String host = endOfHost != -1 ? format.substring(startOfHost, endOfHost) : format.substring(startOfHost);
        if (!host.equalsIgnoreCase("api.twitter.com")) return format;
        final StringBuilder sb = new StringBuilder();
        sb.append(format.substring(0, startOfHost));
        sb.append(domain);
        sb.append(".twitter.com");
        if (endOfHost != -1) {
            sb.append(format.substring(endOfHost));
        }
        return sb.toString();
    }

    @NonNull
    public static String getApiUrl(final String pattern, final String domain, String appendPath) {
        String urlBase = getApiBaseUrl(pattern, domain);
        if (urlBase.endsWith("/")) {
            urlBase = urlBase.substring(0, urlBase.length() - 1);
        }
        if (appendPath == null) return urlBase + "/";
        if (appendPath.startsWith("/")) {
            appendPath = appendPath.substring(1);
        }
        return urlBase + "/" + appendPath;
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
                return String.format(Locale.ROOT, "TwitterAndroid /%s (%s) %s/%d (%s;%s;%s;%s;%d)",
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

    @NonNull
    public static ConsumerKeyType getOfficialKeyType(final Context context, final UserKey accountKey) {
        if (context == null) return ConsumerKeyType.UNKNOWN;
        final String[] projection = {Accounts.CONSUMER_KEY, Accounts.CONSUMER_SECRET, Accounts.AUTH_TYPE};
        final String selection = Expression.equalsArgs(Accounts.ACCOUNT_KEY).getSQL();
        final String[] selectionArgs = {accountKey.toString()};
        final Cursor c = context.getContentResolver().query(Accounts.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if (c == null) return ConsumerKeyType.UNKNOWN;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            if (c.moveToPosition(0) && ParcelableCredentialsUtils.isOAuth(c.getInt(2))) {
                return TwitterContentUtils.getOfficialKeyType(context, c.getString(0), c.getString(1));
            }
        } finally {
            c.close();
        }
        return ConsumerKeyType.UNKNOWN;
    }

    public static class TwidereHttpRequestFactory implements HttpRequest.Factory {

        private final String userAgent;

        public TwidereHttpRequestFactory(final String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public <E extends Exception> HttpRequest create(@NonNull Endpoint endpoint, @NonNull RestRequest info,
                                                        @Nullable Authorization authorization,
                                                        RestConverter.Factory<E> converterFactory)
                throws IOException, RestConverter.ConvertException, E {
            final String restMethod = info.getMethod();
            final String url = Endpoint.constructUrl(endpoint.getUrl(), info);
            MultiValueMap<String> headers = info.getHeaders();
            if (headers == null) {
                headers = new MultiValueMap<>();
            }

            if (authorization != null && authorization.hasAuthorization()) {
                headers.add("Authorization", RestFuUtils.sanitizeHeader(authorization.getHeader(endpoint, info)));
            }
            headers.add("User-Agent", RestFuUtils.sanitizeHeader(userAgent));
            return new HttpRequest(restMethod, url, headers, info.getBody(converterFactory), null);
        }
    }

    public static class TwidereExceptionFactory implements ExceptionFactory<TwitterException> {

        private final TwitterConverterFactory converterFactory;

        TwidereExceptionFactory(TwitterConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
        }

        @Override
        public TwitterException newException(Throwable cause, HttpRequest request, HttpResponse response) {
            final TwitterException te;
            if (cause != null) {
                te = new TwitterException(cause);
            } else {
                te = parseTwitterException(response);
            }
            te.setHttpRequest(request);
            te.setHttpResponse(response);
            return te;
        }


        public TwitterException parseTwitterException(HttpResponse resp) {
            try {
                return (TwitterException) converterFactory.forResponse(TwitterException.class).convert(resp);
            } catch (JsonParseException e) {
                return new TwitterException("Malformed JSON Data", e);
            } catch (IOException e) {
                return new TwitterException("IOException while throwing exception", e);
            } catch (RestConverter.ConvertException e) {
                return new TwitterException(e);
            } catch (TwitterException e) {
                return e;
            }
        }
    }

}
