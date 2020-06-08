package org.mariotaku.twidere.extension.model

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.MastodonOAuth2
import org.mariotaku.microblog.library.twitter.*
import org.mariotaku.microblog.library.twitter.auth.BasicAuthorization
import org.mariotaku.microblog.library.twitter.auth.EmptyAuthorization
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.Authorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthEndpoint
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.restfu.oauth2.OAuth2Authorization
import org.mariotaku.twidere.TwidereConstants.DEFAULT_TWITTER_API_URL_FORMAT
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.account.cred.*
import org.mariotaku.twidere.util.HttpClientFactory
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.MicroBlogAPIFactory.sFanfouConstantPool
import org.mariotaku.twidere.util.MicroBlogAPIFactory.sTwitterConstantPool
import org.mariotaku.twidere.util.api.*
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.media.TwidereMediaDownloader

/**
 * Creates [MicroBlog] instances
 *
 * Created by mariotaku on 2016/12/3.
 */
fun Credentials.getAuthorization(cls: Class<*>?): Authorization {
    if (cls != null) {
        when {
            TwitterWeb::class.java.isAssignableFrom(cls) -> {
                return EmptyAuthorization()
            }
        }
    }
    when (this) {
        is OAuthCredentials -> {
            return OAuthAuthorization(consumer_key, consumer_secret, OAuthToken(access_token,
                    access_token_secret))
        }
        is OAuth2Credentials -> {
            return OAuth2Authorization(access_token)
        }
        is BasicCredentials -> {
            return BasicAuthorization(username, password)
        }
        is EmptyCredentials -> {
            return EmptyAuthorization()
        }
    }
    throw UnsupportedOperationException()
}

fun Credentials.getEndpoint(cls: Class<*>): Endpoint {
    val noVersionSuffix = this.no_version_suffix
    val apiUrlFormat: String = if (!TextUtils.isEmpty(this.api_url_format)) {
        this.api_url_format
    } else {
        DEFAULT_TWITTER_API_URL_FORMAT
    }
    val domain: String?
    val versionSuffix: String?
    when {
        MicroBlog::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        Twitter::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterUpload::class.java.isAssignableFrom(cls) -> {
            domain = "upload"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterOAuth::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = null
        }
        TwitterOAuth2::class.java.isAssignableFrom(cls) -> {
            domain = "api"
            versionSuffix = null
        }
        TwitterCaps::class.java.isAssignableFrom(cls) -> {
            domain = "caps"
            versionSuffix = null
        }
        TwitterWeb::class.java.isAssignableFrom(cls) -> {
            domain = null
            versionSuffix = null
        }
        Mastodon::class.java.isAssignableFrom(cls) -> {
            domain = null
            versionSuffix = null
        }
        MastodonOAuth2::class.java.isAssignableFrom(cls) -> {
            domain = null
            versionSuffix = null
        }
        else -> throw UnsupportedOperationException("Unsupported class $cls")
    }
    val endpointUrl = MicroBlogAPIFactory.getApiUrl(apiUrlFormat, domain, versionSuffix)
    if (this is OAuthCredentials) {
        val signEndpointUrl: String = if (same_oauth_signing_url) {
            endpointUrl
        } else {
            MicroBlogAPIFactory.getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix)
        }
        return OAuthEndpoint(endpointUrl, signEndpointUrl)
    }
    return Endpoint(endpointUrl)
}

fun <T> Credentials.newMicroBlogInstance(context: Context, @AccountType accountType: String? = null,
        cls: Class<T>): T {
    return newMicroBlogInstance(context, getEndpoint(cls), getAuthorization(cls), accountType, cls)
}

fun <T> newMicroBlogInstance(context: Context, endpoint: Endpoint, auth: Authorization,
        @AccountType accountType: String? = null, cls: Class<T>): T {
    val factory = RestAPIFactory<MicroBlogException>()
    val extraHeaders = run {
        if (auth !is OAuthAuthorization) return@run null
        val officialKeyType = InternalTwitterContentUtils.getOfficialKeyType(context,
                auth.consumerKey, auth.consumerSecret)
        return@run MicroBlogAPIFactory.getExtraHeaders(context, officialKeyType)
    } ?: UserAgentExtraHeaders(MicroBlogAPIFactory.getTwidereUserAgent(context))
    val holder = DependencyHolder.get(context)
    var extraRequestParams: Map<String, String>? = null
    when (cls) {
        TwitterUpload::class.java -> {
            val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
            // Use longer timeout for uploading
            conf.readTimeoutSecs = 30
            conf.writeTimeoutSecs = 30
            conf.connectionTimeoutSecs = 60
            val uploadHttpClient = HttpClientFactory.createRestHttpClient(conf, holder.dns,
                    holder.connectionPool, holder.cache)
            factory.setHttpClient(uploadHttpClient)
        }
        else -> {
            factory.setHttpClient(holder.restHttpClient)
        }
    }
    factory.setAuthorization(auth)
    factory.setEndpoint(endpoint)
    when (accountType) {
        AccountType.TWITTER -> {
            factory.setConstantPool(sTwitterConstantPool)
        }
        AccountType.FANFOU -> {
            factory.setConstantPool(sFanfouConstantPool)
            extraRequestParams = mapOf("format" to "html")
        }
    }
    factory.setRestConverterFactory(TwitterConverterFactory)
    factory.setExceptionFactory(TwidereExceptionFactory)
    factory.setRestRequestFactory(TwidereRestRequestFactory(extraRequestParams))
    factory.setHttpRequestFactory(TwidereHttpRequestFactory(extraHeaders))
    return factory.build<T>(cls)
}

internal fun Credentials.authorizationHeader(
        uri: Uri,
        modifiedUri: Uri = TwidereMediaDownloader.getReplacedUri(uri, api_url_format) ?: uri,
        cls: Class<*>? = null
): String {
    val auth = getAuthorization(cls)
    val endpoint: Endpoint
    endpoint = if (auth is OAuthAuthorization) {
        OAuthEndpoint(TwidereMediaDownloader.getEndpoint(modifiedUri),
            TwidereMediaDownloader.getEndpoint(uri))
    } else {
        Endpoint(TwidereMediaDownloader.getEndpoint(modifiedUri))
    }
    val queries = MultiValueMap<String>()
    for (name in uri.queryParameterNames) {
        for (value in uri.getQueryParameters(name)) {
            queries.add(name, value)
        }
    }
    val info = RestRequest("GET", false, uri.path!!, null, queries, null, null, null, null)
    return auth.getHeader(endpoint, info)
}