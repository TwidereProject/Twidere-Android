package org.mariotaku.twidere.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils.isEmpty
import okhttp3.ConnectionPool
import okhttp3.Credentials
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.okhttp3.OkHttpRestClient
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.util.dagger.DependencyHolder
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 16/1/27.
 */
object HttpClientFactory {

    fun createRestHttpClient(conf: HttpClientConfiguration, dns: Dns,
                             connectionPool: ConnectionPool): RestHttpClient {
        val builder = OkHttpClient.Builder()
        initOkHttpClient(conf, builder, dns, connectionPool)
        return OkHttpRestClient(builder.build())
    }

    fun initOkHttpClient(conf: HttpClientConfiguration, builder: OkHttpClient.Builder,
                         dns: Dns, connectionPool: ConnectionPool) {
        updateHttpClientConfiguration(builder, conf, dns, connectionPool)
        DebugModeUtils.initForOkHttpClient(builder)
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    internal fun updateHttpClientConfiguration(builder: OkHttpClient.Builder,
                                               conf: HttpClientConfiguration, dns: Dns,
                                               connectionPool: ConnectionPool) {
        conf.applyTo(builder)
        builder.connectionPool(connectionPool)
        builder.dns(dns)
    }

    private fun getProxyType(proxyType: String?): Proxy.Type {
        if (proxyType == null) return Proxy.Type.DIRECT
        when (proxyType.toLowerCase()) {
//            "socks" -> {
//                return Proxy.Type.SOCKS
//            }
            "http" -> {
                return Proxy.Type.HTTP
            }
        }
        return Proxy.Type.DIRECT
    }

    class HttpClientConfiguration(val prefs: SharedPreferencesWrapper) {

        var readTimeoutSecs: Long = -1
        var writeTimeoutSecs: Long = -1
        var connectionTimeoutSecs: Long = prefs.getInt(KEY_CONNECTION_TIMEOUT, 10).toLong()

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
            val enableProxy = prefs.getBoolean(KEY_ENABLE_PROXY, false)
            if (enableProxy) {
                configProxy(builder)
            }
        }

        private fun configProxy(builder: OkHttpClient.Builder) {
            val proxyType = prefs.getString(KEY_PROXY_TYPE, null)
            val proxyHost = prefs.getString(KEY_PROXY_HOST, null)
            val proxyPort = NumberUtils.toInt(prefs.getString(KEY_PROXY_PORT, null), -1)
            if (!isEmpty(proxyHost) && proxyPort in (0..65535)) {
                val type = getProxyType(proxyType)
                if (type != Proxy.Type.DIRECT) {
                    builder.proxy(Proxy(type, InetSocketAddress.createUnresolved(proxyHost, proxyPort)))
                }
            }
            val username = prefs.getString(KEY_PROXY_USERNAME, null)
            val password = prefs.getString(KEY_PROXY_PASSWORD, null)
            builder.authenticator { route, response ->
                val b = response.request().newBuilder()
                if (response.code() == 407) {
                    if (!isEmpty(username) && !isEmpty(password)) {
                        val credential = Credentials.basic(username, password)
                        b.header("Proxy-Authorization", credential)
                    }
                }
                b.build()
            }
        }

    }

    fun reloadConnectivitySettings(context: Context) {
        val holder = DependencyHolder.get(context)
        val client = holder.restHttpClient as? OkHttpRestClient ?: return
        val builder = OkHttpClient.Builder()
        initOkHttpClient(HttpClientConfiguration(holder.preferences), builder, holder.dns,
                holder.connectionPool)
        client.client = builder.build()
    }
}
