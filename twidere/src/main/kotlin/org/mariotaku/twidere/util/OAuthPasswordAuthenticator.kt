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

package org.mariotaku.twidere.util

import android.text.TextUtils
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.attoparser.ParseException
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterOAuth
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.annotation.method.POST
import org.mariotaku.restfu.http.*
import org.mariotaku.restfu.http.mime.FormBody
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.restfu.okhttp3.OkHttpRestClient
import org.mariotaku.twidere.TwidereConstants.OAUTH_CALLBACK_OOB
import org.mariotaku.twidere.util.net.SimpleCookieJar
import java.io.IOException
import java.io.Reader

class OAuthPasswordAuthenticator(
        private val oauth: TwitterOAuth,
        private val loginVerificationCallback: LoginVerificationCallback,
        private val userAgent: String
) {
    private val client: RestHttpClient
    private val endpoint: Endpoint

    init {
        val restClient = RestAPIFactory.getRestClient(oauth)
        this.endpoint = restClient.endpoint

        val oldClient = (restClient.restClient as OkHttpRestClient).client
        val builder = oldClient.newBuilder()
        builder.cookieJar(SimpleCookieJar())
        builder.addNetworkInterceptor(EndpointInterceptor(endpoint))
        this.client = OkHttpRestClient(builder.build())
    }

    @Throws(AuthenticationException::class)
    fun getOAuthAccessToken(username: String, password: String): OAuthToken {
        val requestToken: OAuthToken
        try {
            requestToken = oauth.getRequestToken(OAUTH_CALLBACK_OOB)
        } catch (e: MicroBlogException) {
            if (e.isCausedByNetworkIssue) throw AuthenticationException(e)
            throw AuthenticityTokenException(e)
        }

        try {
            val authorizeRequestData = getAuthorizeRequestData(requestToken)
            var authorizeResponseData = getAuthorizeResponseData(requestToken,
                    authorizeRequestData, username, password)
            if (!TextUtils.isEmpty(authorizeResponseData.oauthPin)) {
                // Here we got OAuth PIN, just get access token directly
                return oauth.getAccessToken(requestToken, authorizeResponseData.oauthPin)
            } else if (authorizeResponseData.challenge == null) {
                // No OAuth pin, or verification challenge, so treat as wrong password
                throw WrongUserPassException()
            }
            // Go to password verification flow
            val challengeType = authorizeResponseData.challenge!!.challengeType ?:
                    throw LoginVerificationException()
            val loginVerification = loginVerificationCallback.getLoginVerification(challengeType)
            val verificationData = getVerificationData(authorizeResponseData,
                    loginVerification)
            authorizeResponseData = getAuthorizeResponseData(requestToken,
                    verificationData, username, password)
            if (TextUtils.isEmpty(authorizeResponseData.oauthPin)) {
                throw LoginVerificationException()
            }
            return oauth.getAccessToken(requestToken, authorizeResponseData.oauthPin)
        } catch (e: IOException) {
            throw AuthenticationException(e)
        } catch (e: NullPointerException) {
            throw AuthenticationException(e)
        } catch (e: MicroBlogException) {
            throw AuthenticationException(e)
        }

    }

    @Throws(IOException::class, LoginVerificationException::class)
    private fun getVerificationData(authorizeResponseData: AuthorizeResponseData,
            challengeResponse: String?): AuthorizeRequestData {
        try {
            val params = MultiValueMap<String>()
            val verification = authorizeResponseData.challenge!!
            params.add("authenticity_token", verification.authenticityToken)
            params.add("user_id", verification.userId)
            params.add("challenge_id", verification.challengeId)
            params.add("challenge_type", verification.challengeType)
            params.add("platform", verification.platform)
            params.add("redirect_after_login", verification.redirectAfterLogin)
            val requestHeaders = MultiValueMap<String>()
            requestHeaders.add("User-Agent", userAgent)

            if (!TextUtils.isEmpty(challengeResponse)) {
                params.add("challenge_response", challengeResponse)
            }
            val authorizationResultBody = FormBody(params)

            val authorizeResultBuilder = HttpRequest.Builder()
            authorizeResultBuilder.method(POST.METHOD)
            authorizeResultBuilder.url(endpoint.construct("/account/login_verification"))
            authorizeResultBuilder.headers(requestHeaders)
            authorizeResultBuilder.body(authorizationResultBody)
            return client.newCall(authorizeResultBuilder.build()).execute().use {
                val data = AuthorizeRequestData()
                parseAuthorizeRequestData(it, data)
                if (data.authenticityToken.isNullOrEmpty()) {
                    throw LoginVerificationException()
                }
                return@use data
            }
        } catch (e: ParseException) {
            throw LoginVerificationException("Login verification challenge failed", e)
        }
    }

    @Throws(ParseException::class, IOException::class)
    private fun parseAuthorizeRequestData(response: HttpResponse, data: AuthorizeRequestData) {
        val handler = object : AbstractSimpleMarkupHandler() {
            var isOAuthFormOpened: Boolean = false
            override fun handleStandaloneElement(elementName: String, attributes: MutableMap<String, String>?, minimized: Boolean, line: Int, col: Int) {
                handleOpenElement(elementName, attributes, line, col)
                handleCloseElement(elementName, line, col)
            }

            override fun handleOpenElement(elementName: String, attributes: MutableMap<String, String>?, line: Int, col: Int) {
                when (elementName) {
                    "form" -> {
                        if (attributes != null && "oauth_form" == attributes["id"]) {
                            isOAuthFormOpened = true
                        }
                    }
                    "input" -> {
                        if (attributes != null && isOAuthFormOpened) {
                            val name = attributes["name"]
                            val value = attributes["value"]
                            if (name == "authenticity_token") {
                                data.authenticityToken = value
                            } else if (name == "redirect_after_login") {
                                data.redirectAfterLogin = value
                            }
                        }
                    }
                }
            }

            override fun handleCloseElement(elementName: String, line: Int, col: Int) {
                if ("form" == elementName) {
                    isOAuthFormOpened = false
                }
            }
        }
        PARSER.parse(SimpleBody.reader(response.body), handler)
    }

    @Throws(IOException::class, AuthenticationException::class)
    private fun getAuthorizeResponseData(requestToken: OAuthToken,
            authorizeRequestData: AuthorizeRequestData,
            username: String, password: String): AuthorizeResponseData {
        try {
            val data = AuthorizeResponseData()
            val params = MultiValueMap<String>()
            params.add("oauth_token", requestToken.oauthToken)
            params.add("authenticity_token", authorizeRequestData.authenticityToken)
            params.add("redirect_after_login", authorizeRequestData.redirectAfterLogin)
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                params.add("session[username_or_email]", username)
                params.add("session[password]", password)
            }
            val authorizationResultBody = FormBody(params)
            val requestHeaders = MultiValueMap<String>()
            requestHeaders.add("User-Agent", userAgent)
            data.referer = authorizeRequestData.referer

            val authorizeResultBuilder = HttpRequest.Builder()
            authorizeResultBuilder.method(POST.METHOD)
            authorizeResultBuilder.url(endpoint.construct("/oauth/authorize"))
            authorizeResultBuilder.headers(requestHeaders)
            authorizeResultBuilder.body(authorizationResultBody)
            val handler = object : AbstractSimpleMarkupHandler() {
                var isOAuthPinDivOpened: Boolean = false
                var isChallengeFormOpened: Boolean = false

                override fun handleStandaloneElement(elementName: String, attributes: MutableMap<String, String>?,
                        minimized: Boolean, line: Int, col: Int) {
                    handleOpenElement(elementName, attributes, line, col)
                    handleCloseElement(elementName, line, col)
                }

                override fun handleCloseElement(elementName: String, line: Int, col: Int) {
                    when (elementName) {
                        "div" -> {
                            isOAuthPinDivOpened = false
                        }
                        "form" -> {
                            isChallengeFormOpened = false
                        }
                    }
                }

                override fun handleOpenElement(elementName: String, attributes: Map<String, String>?,
                        line: Int, col: Int) {
                    when (elementName) {
                        "div" -> {
                            if (attributes != null && "oauth_pin" == attributes["id"]) {
                                isOAuthPinDivOpened = true
                            }
                        }
                        "form" -> {
                            if (attributes != null) when (attributes["id"]) {
                                "login-verification-form", "login-challenge-form" -> {
                                    isChallengeFormOpened = true
                                }
                            }
                        }
                        "input" -> {
                            if (attributes != null && isChallengeFormOpened) {
                                val name = attributes["name"]
                                val value = attributes["value"]
                                when (name) {
                                    "authenticity_token" -> {
                                        ensureVerification()
                                        data.challenge!!.authenticityToken = value
                                    }
                                    "challenge_id" -> {
                                        ensureVerification()
                                        data.challenge!!.challengeId = value
                                    }
                                    "challenge_type" -> {
                                        ensureVerification()
                                        data.challenge!!.challengeType = value
                                    }
                                    "platform" -> {
                                        ensureVerification()
                                        data.challenge!!.platform = value
                                    }
                                    "user_id" -> {
                                        ensureVerification()
                                        data.challenge!!.userId = value
                                    }
                                    "redirect_after_login" -> {
                                        ensureVerification()
                                        data.challenge!!.redirectAfterLogin = value
                                    }
                                }
                            }
                        }
                    }
                }

                private fun ensureVerification() {
                    if (data.challenge == null) {
                        data.challenge = AuthorizeResponseData.Verification()
                    }
                }

                @Throws(ParseException::class)
                override fun handleText(buffer: CharArray?, offset: Int, len: Int, line: Int, col: Int) {
                    if (isOAuthPinDivOpened) {
                        val s = String(buffer!!, offset, len)
                        if (TextUtils.isDigitsOnly(s)) {
                            data.oauthPin = s
                        }
                    }
                }
            }
            client.newCall(authorizeResultBuilder.build()).execute().use {
                PARSER.parse(SimpleBody.reader(it.body), handler)
            }
            return data
        } catch (e: ParseException) {
            throw AuthenticationException("Malformed HTML", e)
        }
    }

    @Throws(IOException::class, AuthenticationException::class)
    private fun getAuthorizeRequestData(requestToken: OAuthToken): AuthorizeRequestData {
        try {
            val data = AuthorizeRequestData()
            val authorizePageBuilder = HttpRequest.Builder()
            authorizePageBuilder.method(GET.METHOD)
            authorizePageBuilder.url(endpoint.construct("/oauth/authorize",
                    arrayOf("oauth_token", requestToken.oauthToken)))
            data.referer = Endpoint.constructUrl("https://api.twitter.com/oauth/authorize",
                    arrayOf("oauth_token", requestToken.oauthToken))
            val requestHeaders = MultiValueMap<String>()
            requestHeaders.add("User-Agent", userAgent)
            authorizePageBuilder.headers(requestHeaders)
            val authorizePageRequest = authorizePageBuilder.build()
            client.newCall(authorizePageRequest).execute().use {
                parseAuthorizeRequestData(it, data)
            }
            if (data.authenticityToken.isNullOrEmpty()) {
                throw AuthenticationException()
            }
            return data
        } catch (e: ParseException) {
            throw AuthenticationException("Malformed HTML", e)
        }
    }

    interface LoginVerificationCallback {
        @Throws(MicroBlogException::class)
        fun getLoginVerification(challengeType: String): String?
    }

    open class AuthenticationException : Exception {

        constructor()

        constructor(cause: Exception) : super(cause)

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(message: String) : super(message)
    }

    class AuthenticityTokenException(e: Exception) : AuthenticationException(e)

    class WrongUserPassException : AuthenticationException {
        internal constructor() : super()

        internal constructor(cause: Exception) : super(cause)

        internal constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        internal constructor(message: String) : super(message)
    }

    class LoginVerificationException : AuthenticationException {
        internal constructor(message: String) : super(message)

        internal constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        internal constructor(cause: Exception) : super(cause)

        internal constructor() : super()
    }

    internal class AuthorizeResponseData {

        var referer: String? = null

        var oauthPin: String? = null
        var challenge: Verification? = null

        internal class Verification {

            var authenticityToken: String? = null
            var challengeId: String? = null
            var challengeType: String? = null
            var platform: String? = null
            var userId: String? = null
            var redirectAfterLogin: String? = null
        }
    }

    internal class AuthorizeRequestData {
        var authenticityToken: String? = null
        var redirectAfterLogin: String? = null

        var referer: String? = null
    }

    class OAuthPinData {

        var oauthPin: String? = null
    }

    private class EndpointInterceptor(private val endpoint: Endpoint) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            if (!response.isRedirect) {
                return response
            }
            val location = response.header("Location")
            val builder = response.newBuilder()
            if (!TextUtils.isEmpty(location) && !endpoint.checkEndpoint(location)) {
                val originalLocation = HttpUrl.parse("https://api.twitter.com/")?.resolve(location)!!
                val locationBuilder = HttpUrl.parse(endpoint.url)!!.newBuilder()
                for (pathSegments in originalLocation.pathSegments()) {
                    locationBuilder.addPathSegment(pathSegments)
                }
                for (i in 0 until originalLocation.querySize()) {
                    val name = originalLocation.queryParameterName(i)
                    val value = originalLocation.queryParameterValue(i)
                    locationBuilder.addQueryParameter(name, value)
                }
                val encodedFragment = originalLocation.encodedFragment()
                if (encodedFragment != null) {
                    locationBuilder.encodedFragment(encodedFragment)
                }
                val newLocation = locationBuilder.build()
                builder.header("Location", newLocation.toString())
            }
            return builder.build()
        }
    }

    companion object {

        private val PARSER = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())

        @Throws(ParseException::class, IOException::class)
        fun readOAuthPINFromHtml(reader: Reader, data: OAuthPinData) {
            val handler = object : AbstractSimpleMarkupHandler() {
                var isOAuthPinDivOpened: Boolean = false
                override fun handleStandaloneElement(elementName: String,
                        attributes: MutableMap<String, String>?,
                        minimized: Boolean, line: Int, col: Int) {
                    handleOpenElement(elementName, attributes, line, col)
                    handleCloseElement(elementName, line, col)
                }

                override fun handleOpenElement(elementName: String, attributes: Map<String, String>?, line: Int, col: Int) {
                    when (elementName) {
                        "div" -> {
                            if (attributes != null && "oauth_pin" == attributes["id"]) {
                                isOAuthPinDivOpened = true
                            }
                        }
                    }
                }

                override fun handleCloseElement(elementName: String, line: Int, col: Int) {
                    if ("div" == elementName) {
                        isOAuthPinDivOpened = false
                    }
                }

                override fun handleText(buffer: CharArray?, offset: Int, len: Int, line: Int, col: Int) {
                    if (isOAuthPinDivOpened) {
                        val s = String(buffer!!, offset, len)
                        if (TextUtils.isDigitsOnly(s)) {
                            data.oauthPin = s
                        }
                    }
                }
            }
            PARSER.parse(reader, handler)
        }
    }
}
