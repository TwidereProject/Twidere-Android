package org.mariotaku.twidere.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.WorkerThread
import android.text.TextUtils
import okhttp3.HttpUrl
import okhttp3.internal.Version
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.http.SimpleValueMap
import org.mariotaku.restfu.oauth.OAuthEndpoint
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.TWIDERE_APP_NAME
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ConsumerKeyType
import org.mariotaku.twidere.util.api.TwitterAndroidExtraHeaders
import org.mariotaku.twidere.util.api.TwitterMacExtraHeaders
import org.mariotaku.twidere.util.api.UserAgentExtraHeaders
import java.util.*
import java.util.regex.Pattern

object MicroBlogAPIFactory {

    val CARDS_PLATFORM_ANDROID_12 = "Android-12"


    val sTwitterConstantPool = SimpleValueMap()
    val sFanfouConstantPool = SimpleValueMap()

    init {
        sTwitterConstantPool.put("include_cards", "true")
        sTwitterConstantPool.put("cards_platform", CARDS_PLATFORM_ANDROID_12)
        sTwitterConstantPool.put("include_my_retweet", "true")
        sTwitterConstantPool.put("include_rts", "true")
        sTwitterConstantPool.put("include_reply_count", "true")
        sTwitterConstantPool.put("include_descendent_reply_count", "true")
        sTwitterConstantPool.put("full_text", "true")
        sTwitterConstantPool.put("model_version", "7")
        sTwitterConstantPool.put("skip_aggregation", "false")
        sTwitterConstantPool.put("include_ext_alt_text", "true")
        sTwitterConstantPool.put("tweet_mode", "extended")

        sFanfouConstantPool.put("format", "html")
    }

    fun getApiBaseUrl(format: String, domain: String?): String {
        val matcher = Pattern.compile("\\[(\\.?)DOMAIN(\\.?)]", Pattern.CASE_INSENSITIVE).matcher(format)
        val baseUrl: String
        if (!matcher.find()) {
            // For backward compatibility
            val formatCompat = substituteLegacyApiBaseUrl(format, domain)
            if (!formatCompat.endsWith("/1.1") && !formatCompat.endsWith("/1.1/")) {
                baseUrl = formatCompat
            } else {
                val versionSuffix = "/1.1"
                val suffixLength = versionSuffix.length
                val lastIndex = formatCompat.lastIndexOf(versionSuffix)
                baseUrl = formatCompat.substring(0, lastIndex) + formatCompat.substring(lastIndex + suffixLength)
            }
        } else if (TextUtils.isEmpty(domain)) {
            baseUrl = matcher.replaceAll("")
        } else {
            baseUrl = matcher.replaceAll("$1$domain$2")
        }
        // In case someone set invalid base url
        return if (HttpUrl.parse(baseUrl) == null) {
            getApiBaseUrl(Constants.DEFAULT_TWITTER_API_URL_FORMAT, domain)
        } else baseUrl
    }

    private fun substituteLegacyApiBaseUrl(format: String, domain: String?): String {
        val idxOfSlash = format.indexOf("://")
        // Not an url
        if (idxOfSlash < 0) return format
        val startOfHost = idxOfSlash + 3
        if (startOfHost < 0) return getApiBaseUrl("https://[DOMAIN.]twitter.com/", domain)
        val endOfHost = format.indexOf('/', startOfHost)
        val host = if (endOfHost != -1) format.substring(startOfHost, endOfHost) else format.substring(startOfHost)
        val sb = StringBuilder()
        sb.append(format.substring(0, startOfHost))
        if (host.equals("api.twitter.com", ignoreCase = true)) {
            if (domain != null) {
                sb.append(domain)
                sb.append(".twitter.com")
            } else {
                sb.append("twitter.com")
            }
        } else if (host.equals("api.fanfou.com", ignoreCase = true)) {
            if (domain != null) {
                sb.append(domain)
                sb.append(".fanfou.com")
            } else {
                sb.append("fanfou.com")
            }
        } else {
            return format
        }
        if (endOfHost != -1) {
            sb.append(format.substring(endOfHost))
        }
        return sb.toString()
    }

    fun getApiUrl(pattern: String, domain: String?, appendPath: String?): String {
        var urlBase = getApiBaseUrl(pattern, domain)
        if (urlBase.endsWith("/")) {
            urlBase = urlBase.substring(0, urlBase.length - 1)
        }
        if (appendPath == null) return urlBase + "/"
        if (appendPath.startsWith("/")) {
            return urlBase + "/" + appendPath.substring(1)
        }
        return urlBase + "/" + appendPath
    }

    @WorkerThread
    fun getExtraHeaders(context: Context, type: ConsumerKeyType): ExtraHeaders? {
        when (type) {
            ConsumerKeyType.TWITTER_FOR_ANDROID -> {
                return TwitterAndroidExtraHeaders
            }
            ConsumerKeyType.TWITTER_FOR_IPHONE, ConsumerKeyType.TWITTER_FOR_IPAD -> {
                return UserAgentExtraHeaders("Twitter/6.75.2 CFNetwork/811.4.18 Darwin/16.5.0")
            }
            ConsumerKeyType.TWITTER_FOR_MAC -> {
                return TwitterMacExtraHeaders
            }
            ConsumerKeyType.TWEETDECK -> {
                return UserAgentExtraHeaders(UserAgentUtils.getDefaultUserAgentStringSafe(context))
            }
            else -> return null
        }
    }

    fun getTwidereUserAgent(context: Context): String {
        val pm = context.packageManager
        try {
            val pi = pm.getPackageInfo(context.packageName, 0)
            return String.format(Locale.US, "%s/%s %s Android/%s", TWIDERE_APP_NAME,
                    pi.versionName, Version.userAgent(), Build.VERSION.RELEASE)
        } catch (e: PackageManager.NameNotFoundException) {
            throw AssertionError(e)
        }

    }

    fun getOAuthRestEndpoint(apiUrlFormat: String, @AccountType accountType: String?,
            sameOAuthSigningUrl: Boolean, noVersionSuffix: Boolean): Endpoint {
        return getOAuthEndpoint(apiUrlFormat, "api", if (noVersionSuffix) null else "1.1", accountType, sameOAuthSigningUrl)
    }

    fun getOAuthSignInEndpoint(apiUrlFormat: String, @AccountType accountType: String?,
            sameOAuthSigningUrl: Boolean): Endpoint {
        return getOAuthEndpoint(apiUrlFormat, "api", null, accountType, sameOAuthSigningUrl, true)
    }

    fun getOAuthEndpoint(apiUrlFormat: String, domain: String? = "api", versionSuffix: String? = null,
            @AccountType accountType: String?, sameOAuthSigningUrl: Boolean,
            fixUrl: Boolean = false): Endpoint {
        var endpointUrl = getApiUrl(apiUrlFormat, domain, versionSuffix)
        var signEndpointUrl = endpointUrl
        if (fixUrl) {
            val authorityRange = UriUtils.getAuthorityRange(endpointUrl)
            if (authorityRange != null && endpointUrl.regionMatches(authorityRange[0],
                    "api.fanfou.com", 0, authorityRange[1] - authorityRange[0])) {
                endpointUrl = endpointUrl.substring(0, authorityRange[0]) + "fanfou.com" +
                        endpointUrl.substring(authorityRange[1])
            }
        }
        if (sameOAuthSigningUrl) {
            signEndpointUrl = endpointUrl
        } else when (accountType) {
            AccountType.TWITTER -> {
                signEndpointUrl = getApiUrl(Constants.DEFAULT_TWITTER_API_URL_FORMAT, domain, versionSuffix)
            }
            AccountType.FANFOU -> {
                signEndpointUrl = endpointUrl.replace("https://", "http://")
            }
        }
        return OAuthEndpoint(endpointUrl, signEndpointUrl)
    }

    fun getOAuthToken(consumerKey: String, consumerSecret: String): OAuthToken {
        return if (isValidConsumerKeySecret(consumerKey) && isValidConsumerKeySecret(consumerSecret)) {
            OAuthToken(consumerKey, consumerSecret)
        } else {
            OAuthToken(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET)
        }
    }

    fun isValidConsumerKeySecret(text: CharSequence): Boolean {
        return text.all { isAsciiLetterOrDigit(it) }
    }

    private fun isAsciiLetterOrDigit(codePoint: Char): Boolean {
        return (codePoint in 'A'..'Z' || codePoint in 'a'..'z' || codePoint in '0'..'9')
    }

    interface ExtraHeaders {
        operator fun get(headers: MultiValueMap<String>): List<Pair<String, String>>
    }
}
