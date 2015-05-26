/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.HttpRequestFactory;
import org.mariotaku.restfu.RequestInfoFactory;
import org.mariotaku.restfu.RestMethodInfo;
import org.mariotaku.restfu.RestRequestInfo;
import org.mariotaku.restfu.annotation.RestMethod;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.FileValue;
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

/**
 * Created by mariotaku on 15/5/26.
 */
public class TwitterAPIUtils implements TwidereConstants {

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
                final OAuthToken accessToken = new OAuthToken(credentials.oauth_token, credentials.oauth_token_secret);
                return new OAuthAuthorization(consumerKey, consumerSecret, accessToken);
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

    public static String getApiBaseUrl(String format, final String domain) {
        if (format == null) return null;
        final Matcher matcher = Pattern.compile("\\[(\\.?)DOMAIN(\\.?)\\]").matcher(format);
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

    public static class TwidereRequestInfoFactory implements RequestInfoFactory {
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
            if (parts.isEmpty()) {
                final List<Pair<String, String>> params = method.hasBody() ? forms : queries;
                addParameter(params, "include_cards", true);
                addParameter(params, "cards_platform", "Android-12");
                addParameter(params, "include_entities", true);
                addParameter(params, "include_my_retweet", 1);
                addParameter(params, "include_rts", 1);
                addParameter(params, "include_reply_count", true);
                addParameter(params, "include_descendent_reply_count", true);
            } else {
                addPart(parts, "include_cards", true);
                addPart(parts, "cards_platform", "Android-12");
                addPart(parts, "include_entities", true);
                addPart(parts, "include_my_retweet", 1);
                addPart(parts, "include_rts", 1);
                addPart(parts, "include_reply_count", true);
                addPart(parts, "include_descendent_reply_count", true);
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
            return new RestHttpRequest(restMethod, url, headers, info.getBody(), null);
        }
    }

    public static class TwidereExceptionFactory implements ExceptionFactory {
        @Override
        public Exception newException(Throwable cause, RestHttpRequest request, RestHttpResponse response) {
            final TwitterException te = new TwitterException(cause);
            te.setResponse(response);
            return te;
        }
    }
}
