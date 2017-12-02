/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model

import android.content.SharedPreferences
import android.support.annotation.IntRange
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.mariotaku.twidere.annotation.ProxyType
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.util.HttpClientFactory
import java.net.InetSocketAddress
import java.net.Proxy

interface ProxySettings {
    @ProxyType
    val type: String

    fun apply(builder: OkHttpClient.Builder)

    fun write(editor: SharedPreferences.Editor)

    class Http(
            val host: String,
            @IntRange(from = 0, to = 65535) val port: Int,
            val username: String?,
            val password: String?
    ) : ProxySettings {
        @ProxyType
        override val type: String = ProxyType.HTTP

        override fun apply(builder: OkHttpClient.Builder) {
            val address = InetSocketAddress.createUnresolved(host, port)

            builder.proxy(Proxy(Proxy.Type.HTTP, address))

            builder.authenticator { _, response ->
                val b = response.request().newBuilder()
                if (response.code() == 407) {
                    if (username != null && password != null) {
                        val credential = Credentials.basic(username, password)
                        b.header("Proxy-Authorization", credential)
                    }
                }
                b.build()
            }
        }

        override fun write(editor: SharedPreferences.Editor) {
            editor.putString(KEY_PROXY_ADDRESS, "$host:$port")
            editor.putString(KEY_PROXY_USERNAME, username)
            editor.putString(KEY_PROXY_PASSWORD, password)
        }

    }

    class Reverse(
            val url: String,
            val username: String?,
            val password: String?
    ) : ProxySettings {
        @ProxyType
        override val type: String = ProxyType.REVERSE

        override fun apply(builder: OkHttpClient.Builder) {
            builder.addInterceptor(HttpClientFactory.ReverseProxyInterceptor(url, username, password))
        }

        override fun write(editor: SharedPreferences.Editor) {
            editor.putString(KEY_PROXY_TYPE, type)
            editor.putString(KEY_PROXY_ADDRESS, url)
            editor.putString(KEY_PROXY_USERNAME, username)
            editor.putString(KEY_PROXY_PASSWORD, password)
        }

    }

}