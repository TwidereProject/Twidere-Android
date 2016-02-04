/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.support.annotation.Nullable;
import android.text.TextUtils;


import org.attoparser.AttoParseException;
import org.attoparser.IAttoHandler;
import org.attoparser.IAttoParser;
import org.attoparser.markup.MarkupAttoParser;
import org.attoparser.markup.html.AbstractStandardNonValidatingHtmlAttoHandler;
import org.attoparser.markup.html.HtmlParsingConfiguration;
import org.attoparser.markup.html.elements.IHtmlElement;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestClient;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.restfu.http.mime.FormBody;
import org.mariotaku.restfu.okhttp.OkHttpRestClient;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterOAuth;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.model.RequestType;
import org.mariotaku.twidere.util.net.JavaNetCookieJar;

import java.io.IOException;
import java.io.Reader;
import java.net.CookieManager;
import java.net.URI;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class OAuthPasswordAuthenticator implements Constants {

    private static final IAttoParser PARSER = new MarkupAttoParser();

    private final TwitterOAuth oauth;
    private final OkHttpRestClient client;
    private final Endpoint endpoint;
    private final LoginVerificationCallback loginVerificationCallback;
    private final String userAgent;

    public OAuthPasswordAuthenticator(final TwitterOAuth oauth,
                                      final LoginVerificationCallback loginVerificationCallback,
                                      final String userAgent) {
        final RestClient restClient = RestAPIFactory.getRestClient(oauth);
        this.oauth = oauth;
        this.client = (OkHttpRestClient) restClient.getRestClient();
        final OkHttpClient.Builder builder = client.getClient().newBuilder();
        builder.cookieJar(new JavaNetCookieJar(new CookieManager()));
        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Response response = chain.proceed(chain.request());
                if (!response.isRedirect()) {
                    return response;
                }
                final String location = response.header("Location");
                final Response.Builder builder = response.newBuilder();
                if (!TextUtils.isEmpty(location) && !endpoint.checkEndpoint(location)) {
                    final HttpUrl originalLocation = HttpUrl.get(URI.create("https://api.twitter.com/").resolve(location));
                    final HttpUrl.Builder locationBuilder = HttpUrl.parse(endpoint.getUrl()).newBuilder();
                    for (String pathSegments : originalLocation.pathSegments()) {
                        locationBuilder.addPathSegment(pathSegments);
                    }
                    for (int i = 0, j = originalLocation.querySize(); i < j; i++) {
                        final String name = originalLocation.queryParameterName(i);
                        final String value = originalLocation.queryParameterValue(i);
                        locationBuilder.addQueryParameter(name, value);
                    }
                    final String encodedFragment = originalLocation.encodedFragment();
                    if (encodedFragment != null) {
                        locationBuilder.encodedFragment(encodedFragment);
                    }
                    final HttpUrl newLocation = locationBuilder.build();
                    builder.header("Location", newLocation.toString());
                }
                return builder.build();
            }
        });
        this.endpoint = restClient.getEndpoint();
        this.loginVerificationCallback = loginVerificationCallback;
        this.userAgent = userAgent;
    }

    public OAuthToken getOAuthAccessToken(final String username, final String password) throws AuthenticationException {
        final OAuthToken requestToken;
        try {
            requestToken = oauth.getRequestToken(OAUTH_CALLBACK_OOB);
        } catch (final TwitterException e) {
            if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
            throw new AuthenticityTokenException(e);
        }
        try {
            final AuthorizeRequestData authorizeRequestData = getAuthorizeRequestData(requestToken);
            AuthorizeResponseData authorizeResponseData = getAuthorizeResponseData(requestToken,
                    authorizeRequestData, username, password);
            if (!TextUtils.isEmpty(authorizeResponseData.oauthPin)) {
                // Here we got OAuth PIN, just get access token directly
                return oauth.getAccessToken(requestToken, authorizeResponseData.oauthPin);
            } else if (authorizeResponseData.verification == null) {
                // No OAuth pin, or verification challenge, so treat as wrong password
                throw new WrongUserPassException();
            }
            // Go to password verification flow
            final String challengeType = authorizeResponseData.verification.challengeType;
            final String loginVerification = loginVerificationCallback.getLoginVerification(challengeType);
            final AuthorizeRequestData verificationData = getVerificationData(authorizeResponseData,
                    loginVerification);
            authorizeResponseData = getAuthorizeResponseData(requestToken,
                    verificationData, username, password);
            if (TextUtils.isEmpty(authorizeResponseData.oauthPin)) {
                throw new LoginVerificationException();
            }
            return oauth.getAccessToken(requestToken, authorizeResponseData.oauthPin);
        } catch (final IOException | NullPointerException | TwitterException e) {
            throw new AuthenticationException(e);
        }
    }

    private AuthorizeRequestData getVerificationData(AuthorizeResponseData authorizeResponseData,
                                                     @Nullable String challengeResponse) throws IOException, LoginVerificationException {
        HttpResponse response = null;
        try {
            final AuthorizeRequestData data = new AuthorizeRequestData();
            final MultiValueMap<String> params = new MultiValueMap<>();
            final AuthorizeResponseData.Verification verification = authorizeResponseData.verification;
            params.add("authenticity_token", verification.authenticityToken);
            params.add("user_id", verification.userId);
            params.add("challenge_id", verification.challengeId);
            params.add("challenge_type", verification.challengeType);
            params.add("platform", verification.platform);
            params.add("redirect_after_login", verification.redirectAfterLogin);
            final MultiValueMap<String> requestHeaders = new MultiValueMap<>();
            requestHeaders.add("User-Agent", userAgent);

            if (!TextUtils.isEmpty(challengeResponse)) {
                params.add("challenge_response", challengeResponse);
            }
            final FormBody authorizationResultBody = new FormBody(params);

            final HttpRequest.Builder authorizeResultBuilder = new HttpRequest.Builder();
            authorizeResultBuilder.method(POST.METHOD);
            authorizeResultBuilder.url(endpoint.construct("/account/login_verification"));
            authorizeResultBuilder.headers(requestHeaders);
            authorizeResultBuilder.body(authorizationResultBody);
            authorizeResultBuilder.tag(RequestType.API);
            response = client.newCall(authorizeResultBuilder.build()).execute();
            parseAuthorizeRequestData(response, data);
            if (TextUtils.isEmpty(data.authenticityToken)) {
                throw new LoginVerificationException();
            }
            return data;
        } catch (AttoParseException e) {
            throw new LoginVerificationException("Login verification challenge failed", e);
        } finally {
            Utils.closeSilently(response);
        }
    }

    private void parseAuthorizeRequestData(HttpResponse response, final AuthorizeRequestData data) throws AttoParseException, IOException {
        final HtmlParsingConfiguration conf = new HtmlParsingConfiguration();
        final IAttoHandler handler = new AbstractStandardNonValidatingHtmlAttoHandler(conf) {
            boolean isOAuthFormOpened;

            @Override
            public void handleHtmlStandaloneElement(IHtmlElement element, boolean minimized,
                                                    String elementName, Map<String, String> attributes,
                                                    int line, int col) {
                handleHtmlOpenElement(element, elementName, attributes, line, col);
                handleHtmlCloseElement(element, elementName, line, col);
            }

            @Override
            public void handleHtmlOpenElement(IHtmlElement element, String elementName,
                                              Map<String, String> attributes, int line, int col) {
                switch (elementName) {
                    case "form": {
                        if (attributes != null && "oauth_form".equals(attributes.get("id"))) {
                            isOAuthFormOpened = true;
                        }
                        break;
                    }
                    case "input": {
                        if (isOAuthFormOpened && attributes != null) {
                            final String name = attributes.get("name");
                            if (TextUtils.isEmpty(name)) break;
                            final String value = attributes.get("value");
                            if (name.equals("authenticity_token")) {
                                data.authenticityToken = value;
                            } else if (name.equals("redirect_after_login")) {
                                data.redirectAfterLogin = value;
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void handleHtmlCloseElement(IHtmlElement element, String elementName, int line, int col) {
                if ("form".equals(elementName)) {
                    isOAuthFormOpened = false;
                }
            }
        };
        PARSER.parse(SimpleBody.reader(response.getBody()), handler);
    }

    private AuthorizeResponseData getAuthorizeResponseData(OAuthToken requestToken,
                                                           AuthorizeRequestData authorizeRequestData,
                                                           String username, String password) throws IOException, AuthenticationException {
        HttpResponse response = null;
        try {
            final AuthorizeResponseData data = new AuthorizeResponseData();
            final MultiValueMap<String> params = new MultiValueMap<>();
            params.add("oauth_token", requestToken.getOauthToken());
            params.add("authenticity_token", authorizeRequestData.authenticityToken);
            params.add("redirect_after_login", authorizeRequestData.redirectAfterLogin);
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                params.add("session[username_or_email]", username);
                params.add("session[password]", password);
            }
            final FormBody authorizationResultBody = new FormBody(params);
            final MultiValueMap<String> requestHeaders = new MultiValueMap<>();
            requestHeaders.add("User-Agent", userAgent);
            data.referer = authorizeRequestData.referer;

            final HttpRequest.Builder authorizeResultBuilder = new HttpRequest.Builder();
            authorizeResultBuilder.method(POST.METHOD);
            authorizeResultBuilder.url(endpoint.construct("/oauth/authorize"));
            authorizeResultBuilder.headers(requestHeaders);
            authorizeResultBuilder.body(authorizationResultBody);
            authorizeResultBuilder.tag(RequestType.API);
            response = client.newCall(authorizeResultBuilder.build()).execute();
            final HtmlParsingConfiguration conf = new HtmlParsingConfiguration();
            final IAttoHandler handler = new AbstractStandardNonValidatingHtmlAttoHandler(conf) {
                boolean isOAuthPinDivOpened;
                boolean isLoginVerificationFormOpened;

                @Override
                public void handleHtmlStandaloneElement(IHtmlElement element, boolean minimized,
                                                        String elementName, Map<String, String> attributes,
                                                        int line, int col) {
                    handleHtmlOpenElement(element, elementName, attributes, line, col);
                    handleHtmlCloseElement(element, elementName, line, col);
                }

                @Override
                public void handleHtmlCloseElement(IHtmlElement element, String elementName, int line, int col) {
                    switch (elementName) {
                        case "div": {
                            isOAuthPinDivOpened = false;
                            break;
                        }
                        case "form": {
                            isLoginVerificationFormOpened = false;
                            break;
                        }
                    }
                }

                @Override
                public void handleHtmlOpenElement(IHtmlElement element, String elementName,
                                                  Map<String, String> attributes, int line, int col) {
                    switch (elementName) {
                        case "div": {
                            if (attributes != null && "oauth_pin".equals(attributes.get("id"))) {
                                isOAuthPinDivOpened = true;
                            }
                            break;
                        }
                        case "form": {
                            if (attributes != null && "login-verification-form".equals(attributes.get("id"))) {
                                isLoginVerificationFormOpened = true;
                            }
                            break;
                        }
                        case "input":
                            if (isLoginVerificationFormOpened && attributes != null) {
                                final String name = attributes.get("name");
                                if (TextUtils.isEmpty(name)) break;
                                final String value = attributes.get("value");
                                switch (name) {
                                    case "authenticity_token": {
                                        ensureVerification();
                                        data.verification.authenticityToken = value;
                                        break;
                                    }
                                    case "challenge_id": {
                                        ensureVerification();
                                        data.verification.challengeId = value;
                                        break;
                                    }
                                    case "challenge_type": {
                                        ensureVerification();
                                        data.verification.challengeType = value;
                                        break;
                                    }
                                    case "platform": {
                                        ensureVerification();
                                        data.verification.platform = value;
                                        break;
                                    }
                                    case "user_id": {
                                        ensureVerification();
                                        data.verification.userId = value;
                                        break;
                                    }
                                    case "redirect_after_login": {
                                        ensureVerification();
                                        data.verification.redirectAfterLogin = value;
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                }

                private void ensureVerification() {
                    if (data.verification == null) {
                        data.verification = new AuthorizeResponseData.Verification();
                    }
                }

                @Override
                public void handleText(char[] buffer, int offset, int len, int line, int col) throws AttoParseException {
                    if (isOAuthPinDivOpened) {
                        final String s = new String(buffer, offset, len);
                        if (TextUtils.isDigitsOnly(s)) {
                            data.oauthPin = s;
                        }
                    }
                }
            };
            PARSER.parse(SimpleBody.reader(response.getBody()), handler);
            return data;
        } catch (AttoParseException e) {
            throw new AuthenticationException("Malformed HTML", e);
        } finally {
            Utils.closeSilently(response);
        }
    }

    private AuthorizeRequestData getAuthorizeRequestData(OAuthToken requestToken) throws IOException,
            AuthenticationException {
        HttpResponse response = null;
        try {
            final AuthorizeRequestData data = new AuthorizeRequestData();
            final HttpRequest.Builder authorizePageBuilder = new HttpRequest.Builder();
            authorizePageBuilder.method(GET.METHOD);
            authorizePageBuilder.url(endpoint.construct("/oauth/authorize", new String[]{"oauth_token",
                    requestToken.getOauthToken()}));
            data.referer = Endpoint.constructUrl("https://api.twitter.com/oauth/authorize",
                    new String[]{"oauth_token", requestToken.getOauthToken()});
            final MultiValueMap<String> requestHeaders = new MultiValueMap<>();
            requestHeaders.add("User-Agent", userAgent);
            authorizePageBuilder.headers(requestHeaders);
            authorizePageBuilder.tag(RequestType.API);
            final HttpRequest authorizePageRequest = authorizePageBuilder.build();
            response = client.newCall(authorizePageRequest).execute();
            parseAuthorizeRequestData(response, data);
            if (TextUtils.isEmpty(data.authenticityToken)) {
                throw new AuthenticationException();
            }
            return data;
        } catch (AttoParseException e) {
            throw new AuthenticationException("Malformed HTML", e);
        } finally {
            Utils.closeSilently(response);
        }
    }

    public static void readOAuthPINFromHtml(Reader reader, final OAuthPinData data) throws AttoParseException, IOException {
        final HtmlParsingConfiguration conf = new HtmlParsingConfiguration();
        final IAttoHandler handler = new AbstractStandardNonValidatingHtmlAttoHandler(conf) {
            boolean isOAuthPinDivOpened;

            @Override
            public void handleHtmlStandaloneElement(IHtmlElement element, boolean minimized,
                                                    String elementName, Map<String, String> attributes,
                                                    int line, int col) {
                handleHtmlOpenElement(element, elementName, attributes, line, col);
                handleHtmlCloseElement(element, elementName, line, col);
            }

            @Override
            public void handleHtmlOpenElement(IHtmlElement element, String elementName, Map<String, String> attributes, int line, int col) {
                switch (elementName) {
                    case "div": {
                        if (attributes != null && "oauth_pin".equals(attributes.get("id"))) {
                            isOAuthPinDivOpened = true;
                        }
                        break;
                    }
                }
            }

            @Override
            public void handleHtmlCloseElement(IHtmlElement element, String elementName, int line, int col) {
                if ("div".equals(elementName)) {
                    isOAuthPinDivOpened = false;
                }
            }

            @Override
            public void handleText(char[] buffer, int offset, int len, int line, int col) {
                if (isOAuthPinDivOpened) {
                    final String s = new String(buffer, offset, len);
                    if (TextUtils.isDigitsOnly(s)) {
                        data.oauthPin = s;
                    }
                }
            }
        };
        PARSER.parse(reader, handler);
    }

    public interface LoginVerificationCallback {
        String getLoginVerification(String challengeType);
    }

    public static class AuthenticationException extends Exception {

        AuthenticationException() {
        }

        AuthenticationException(final Exception cause) {
            super(cause);
        }

        AuthenticationException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        AuthenticationException(final String message) {
            super(message);
        }
    }

    public static final class AuthenticityTokenException extends AuthenticationException {

        public AuthenticityTokenException(Exception e) {
            super(e);
        }
    }

    public static final class WrongUserPassException extends AuthenticationException {
        WrongUserPassException() {
            super();
        }

        WrongUserPassException(Exception cause) {
            super(cause);
        }

        WrongUserPassException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        WrongUserPassException(String message) {
            super(message);
        }
    }

    public static final class LoginVerificationException extends AuthenticationException {
        LoginVerificationException(String message) {
            super(message);
        }

        LoginVerificationException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        LoginVerificationException(Exception cause) {
            super(cause);
        }

        LoginVerificationException() {
            super();
        }
    }

    static class AuthorizeResponseData {

        String referer;

        public String oauthPin;
        public Verification verification;

        static class Verification {

            String authenticityToken;
            String challengeId;
            String challengeType;
            String platform;
            String userId;
            String redirectAfterLogin;
        }
    }

    static class AuthorizeRequestData {
        public String authenticityToken;
        public String redirectAfterLogin;

        public String referer;
    }

    public static class OAuthPinData {

        public String oauthPin;
    }
}
