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

package org.mariotaku.twidere.util.net

import okhttp3.ConnectionSpec
import okhttp3.internal.Internal
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * @author fkrauthan
 */
class TLSSocketFactory : SSLSocketFactory() {

    private val delegate: SSLSocketFactory

    init {
        val context = SSLContext.getInstance("TLS").apply {
            init(null, null, null)
        }
        delegate = context.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return delegate.createSocket(s, host, port, autoClose).applyTLS()
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return delegate.createSocket(host, port).applyTLS()
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return delegate.createSocket(host, port, localHost, localPort).applyTLS()
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return delegate.createSocket(host, port).applyTLS()
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return delegate.createSocket(address, port, localAddress, localPort).applyTLS()
    }

    private fun Socket.applyTLS(): Socket {
        if (this !is SSLSocket) return this
        this.enabledProtocols = this.supportedProtocols
        this.enabledCipherSuites = this.enabledCipherSuites
        Internal.instance.apply(ConnectionSpec.MODERN_TLS, this, false)
        return this
    }

}