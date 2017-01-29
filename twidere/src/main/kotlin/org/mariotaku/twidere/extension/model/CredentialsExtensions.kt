package org.mariotaku.twidere.extension.model

import android.content.Context
import android.text.TextUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.*
import org.mariotaku.microblog.library.twitter.auth.BasicAuthorization
import org.mariotaku.microblog.library.twitter.auth.EmptyAuthorization
import org.mariotaku.microblog.library.twitter.util.TwitterConverterFactory
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.http.Authorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.SimpleValueMap
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthEndpoint
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.twidere.TwidereConstants.DEFAULT_TWITTER_API_URL_FORMAT
import org.mariotaku.twidere.model.ConsumerKeyType
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.util.HttpClientFactory
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.MicroBlogAPIFactory.sTwitterConstantPool
import org.mariotaku.twidere.util.TwitterContentUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder

/**
 * Created by mariotaku on 2016/12/3.
 */
fun Credentials.getAuthorization(): Authorization {
    when (this) {
        is OAuthCredentials -> {
            return OAuthAuthorization(consumer_key, consumer_secret, OAuthToken(access_token,
                    access_token_secret))
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
    val apiUrlFormat: String
    val noVersionSuffix = this.no_version_suffix
    if (!TextUtils.isEmpty(this.api_url_format)) {
        apiUrlFormat = this.api_url_format
    } else {
        apiUrlFormat = DEFAULT_TWITTER_API_URL_FORMAT
    }
    val domain: String
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
        TwitterUserStream::class.java.isAssignableFrom(cls) -> {
            domain = "userstream"
            versionSuffix = if (noVersionSuffix) null else "/1.1/"
        }
        TwitterCaps::class.java.isAssignableFrom(cls) -> {
            domain = "caps"
            versionSuffix = null
        }
        else -> throw TwitterConverterFactory.UnsupportedTypeException(cls)
    }
    val endpointUrl = MicroBlogAPIFactory.getApiUrl(apiUrlFormat, domain, versionSuffix)
    if (this is OAuthCredentials) {
        val signEndpointUrl: String
        if (same_oauth_signing_url) {
            signEndpointUrl = endpointUrl
        } else {
            signEndpointUrl = MicroBlogAPIFactory.getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix)
        }
        return OAuthEndpoint(endpointUrl, signEndpointUrl)
    }
    return Endpoint(endpointUrl)
}

fun <T> Credentials.newMicroBlogInstance(
        context: Context,
        twitterExtraQueries: Boolean = true,
        extraRequestParams: Map<String, String>? = null,
        cls: Class<T>
): T {
    return newMicroBlogInstance(context, getEndpoint(cls), getAuthorization(),
            twitterExtraQueries, extraRequestParams, cls)
}

fun <T> newMicroBlogInstance(
        context: Context,
        endpoint: Endpoint,
        auth: Authorization,
        twitterExtraQueries: Boolean = true,
        extraRequestParams: Map<String, String>? = null,
        cls: Class<T>
): T {
    val factory = RestAPIFactory<MicroBlogException>()
    val userAgent: String
    if (auth is OAuthAuthorization) {
        val officialKeyType = TwitterContentUtils.getOfficialKeyType(context,
                auth.consumerKey, auth.consumerSecret)
        if (officialKeyType != ConsumerKeyType.UNKNOWN) {
            userAgent = MicroBlogAPIFactory.getUserAgentName(context, officialKeyType)
        } else {
            userAgent = MicroBlogAPIFactory.getTwidereUserAgent(context)
        }
    } else {
        userAgent = MicroBlogAPIFactory.getTwidereUserAgent(context)
    }
    val holder = DependencyHolder.get(context)
    when (cls) {
        TwitterUpload::class -> {
            val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
            // Use longer timeout for uploading
            conf.readTimeoutSecs = 30
            conf.writeTimeoutSecs = 30
            conf.connectionTimeoutSecs = 60
            val uploadHttpClient = HttpClientFactory.createRestHttpClient(conf, holder.dns,
                    holder.connectionPool)
            factory.setHttpClient(uploadHttpClient)
        }
        else -> {
            factory.setHttpClient(holder.restHttpClient)
        }
    }
    factory.setAuthorization(auth)
    factory.setEndpoint(endpoint)
    if (twitterExtraQueries) {
        factory.setConstantPool(sTwitterConstantPool)
    } else {
        factory.setConstantPool(SimpleValueMap())
    }
    val converterFactory = TwitterConverterFactory()
    factory.setRestConverterFactory(converterFactory)
    factory.setRestRequestFactory(MicroBlogAPIFactory.TwidereRestRequestFactory(extraRequestParams))
    factory.setHttpRequestFactory(MicroBlogAPIFactory.TwidereHttpRequestFactory(userAgent))
    factory.setExceptionFactory(MicroBlogAPIFactory.TwidereExceptionFactory(converterFactory))
    return factory.build<T>(cls)
}