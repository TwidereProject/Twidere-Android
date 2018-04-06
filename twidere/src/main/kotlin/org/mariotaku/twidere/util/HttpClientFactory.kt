package org.mariotaku.twidere.util

import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import okhttp3.*
import org.mariotaku.kpreferences.get
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.okhttp3.OkHttpRestClient
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_CONNECTION_TIMEOUT
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_ENABLE_PROXY
import org.mariotaku.twidere.constant.cacheSizeLimitKey
import org.mariotaku.twidere.constant.proxyKey
import org.mariotaku.twidere.util.net.TLSSocketFactory
import java.io.IOException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


object HttpClientFactory {

    fun createRestHttpClient(conf: HttpClientConfiguration, dns: Dns, cache: Cache): RestHttpClient {
        val builder = OkHttpClient.Builder()
        initOkHttpClient(conf, builder, dns, cache)
        return OkHttpRestClient(builder.build())
    }

    fun initOkHttpClient(conf: HttpClientConfiguration, builder: OkHttpClient.Builder, dns: Dns,
            cache: Cache) {
        updateHttpClientConfiguration(builder, conf, dns, cache)
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.JELLY_BEAN until Build.VERSION_CODES.LOLLIPOP) {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.single() !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
            }
            val trustManager = trustManagers[0] as X509TrustManager

            val tlsSocketFactory = TLSSocketFactory(trustManagers)
            builder.sslSocketFactory(tlsSocketFactory, trustManager)
        }
        updateTLSConnectionSpecs(builder)
        DebugModeUtils.initForOkHttpClient(builder)
    }

    /**
     * # Supported patterns
     *
     * * `[SCHEME]`: E.g. `http` or `https`
     * * `[HOST]`: Host address
     * * `[PORT]`: Port number
     * * `[AUTHORITY]`: `[HOST]`:`[PORT]` or `[HOST]` if port is default. Colon **will be** URL encoded
     * * `[PATH]`: Raw path part, **without leading slash**
     * * `[/PATH]`: Raw path part, **with leading slash**
     * * `[PATH_ENCODED]`: Path, **will be** URL encoded again
     * * `[QUERY]`: Raw query part
     * * `[?QUERY]`: Raw query part, with `?` prefix
     * * `[QUERY_ENCODED]`: Raw query part, **will be** URL encoded again
     * * `[FRAGMENT]`: Raw fragment part
     * * `[#FRAGMENT]`: Raw fragment part, with `#` prefix
     * * `[FRAGMENT_ENCODED]`: Raw fragment part, **will be** URL encoded again
     * * `[URL_ENCODED]`: URL Encoded `url` itself
     * * `[URL_BASE64]`: Base64 Encoded `url` itself
     *
     * # Null values
     * `[PATH]`, `[/PATH]`, `[QUERY]`, `[?QUERY]`, `[FRAGMENT]`, `[#FRAGMENT]` will be empty when
     * it's null, values and base64-encoded will be string `"null"`.
     *
     * A valid format looks like
     *
     * `https://proxy.com/[SCHEME]/[AUTHORITY]/[PATH][?QUERY][#FRAGMENT]`,
     *
     * A request
     *
     * `https://example.com:8080/path?query=value#fragment`
     *
     * Will be transformed to
     *
     * `https://proxy.com/https/example.com%3A8080/path?query=value#fragment`
     */
    @Suppress("KDocUnresolvedReference")
    fun replaceUrl(url: HttpUrl, format: String): String {
        val sb = StringBuffer()
        var startIndex = 0
        while (startIndex != -1) {
            val find = format.findAnyOf(urlSupportedPatterns, startIndex) ?: break
            sb.append(format, startIndex, find.first)
            sb.append(when (find.second) {
                "[SCHEME]" -> url.scheme()
                "[HOST]" -> url.host()
                "[PORT]" -> url.port()
                "[AUTHORITY]" -> url.authority()
                "[PATH]" -> url.encodedPath()?.removePrefix("/").orEmpty()
                "[/PATH]" -> url.encodedPath().orEmpty()
                "[PATH_ENCODED]" -> url.encodedPath()?.removePrefix("/")?.urlEncoded()
                "[QUERY]" -> url.encodedQuery().orEmpty()
                "[?QUERY]" -> url.encodedQuery()?.prefix("?").orEmpty()
                "[QUERY_ENCODED]" -> url.encodedQuery()?.urlEncoded()
                "[FRAGMENT]" -> url.encodedFragment().orEmpty()
                "[#FRAGMENT]" -> url.encodedFragment()?.prefix("#").orEmpty()
                "[FRAGMENT_ENCODED]" -> url.encodedFragment()?.urlEncoded()
                "[URL_ENCODED]" -> url.toString().urlEncoded()
                "[URL_BASE64]" -> Base64.encodeToString(url.toString().toByteArray(Charsets.UTF_8),
                        Base64.URL_SAFE)
                else -> throw AssertionError()
            })
            startIndex = find.first + find.second.length
        }
        sb.append(format, startIndex, format.length)
        return sb.toString()
    }

    private fun updateHttpClientConfiguration(builder: OkHttpClient.Builder, conf: HttpClientConfiguration,
            dns: Dns, cache: Cache) {
        conf.applyTo(builder)
        builder.dns(dns)
        builder.cache(cache)
    }

    private fun updateTLSConnectionSpecs(builder: OkHttpClient.Builder) {
        //Default spec list from OkHttpClient.DEFAULT_CONNECTION_SPECS
        val specList: ArrayList<ConnectionSpec> = ArrayList()
        specList.add(ConnectionSpec.MODERN_TLS)
        nougatECCFix(specList)
        specList.add(ConnectionSpec.CLEARTEXT)
        builder.connectionSpecs(specList)
    }

    private fun nougatECCFix(specList: ArrayList<ConnectionSpec>) {
        // Shamelessly stolen from Tusky
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.N) {
            return
        }
        val sslContext = try {
            SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            Log.e("HttpClientFactory", "Failed obtaining TLS Context.")
            return
        }

        sslContext.init(null, null, null)
        val cipherSuites = sslContext.socketFactory.defaultCipherSuites
        val allowedList = cipherSuites.filterNotTo(ArrayList<String>()) { it.contains("ECDH") }
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(*allowedList.toTypedArray())
                .supportsTlsExtensions(true)
                .build()
        specList.add(spec)
    }

    private fun HttpUrl.authority(): String {
        val host = host()
        val port = port()
        if (port == HttpUrl.defaultPort(scheme())) return host
        return "$host%3A$port"
    }

    private fun String.urlEncoded() = Uri.encode(this)

    private fun String.prefix(prefix: String) = prefix + this

    private val urlSupportedPatterns = listOf("[SCHEME]", "[HOST]", "[PORT]", "[AUTHORITY]",
            "[PATH]", "[/PATH]", "[PATH_ENCODED]", "[QUERY]", "[?QUERY]", "[QUERY_ENCODED]",
            "[FRAGMENT]", "[#FRAGMENT]", "[FRAGMENT_ENCODED]", "[URL_ENCODED]", "[URL_BASE64]")

    class HttpClientConfiguration(val prefs: SharedPreferences) {

        var readTimeoutSecs: Long = -1
        var writeTimeoutSecs: Long = -1
        var connectionTimeoutSecs: Long = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10).toLong()
        var cacheSize: Int = prefs[cacheSizeLimitKey]

        internal fun applyTo(builder: OkHttpClient.Builder) {
            if (connectionTimeoutSecs >= 0) {
                builder.connectTimeout(connectionTimeoutSecs, TimeUnit.SECONDS)
            }
            if (writeTimeoutSecs >= 0) {
                builder.writeTimeout(writeTimeoutSecs, TimeUnit.SECONDS)
            }
            if (readTimeoutSecs >= 0) {
                builder.readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
            }
            if (prefs.getBoolean(KEY_ENABLE_PROXY, false)) {
                prefs[proxyKey]?.apply(builder)
            }
        }

    }

    /**
     * Intercept and replace proxy patterns to real URL
     */
    class ReverseProxyInterceptor(val proxyFormat: String, val proxyUsername: String?,
            val proxyPassword: String?) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val url = request.url()
            val builder = request.newBuilder()
            val replacedUrl = HttpUrl.parse(replaceUrl(url, proxyFormat)) ?: run {
                throw IOException("Invalid reverse proxy format")
            }
            builder.url(replacedUrl)
            if (proxyUsername != null && proxyPassword != null) {
                val headerValue = Base64.encodeToString("$proxyUsername:$proxyPassword".toByteArray(Charsets.UTF_8),
                        Base64.URL_SAFE)
                builder.addHeader("Proxy-Authorization", headerValue)
            }
            return chain.proceed(builder.build())
        }

    }

}
