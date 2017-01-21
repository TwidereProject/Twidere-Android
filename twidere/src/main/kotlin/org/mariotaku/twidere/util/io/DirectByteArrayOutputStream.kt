package org.mariotaku.twidere.util.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class DirectByteArrayOutputStream : ByteArrayOutputStream() {

    fun inputStream(close: Boolean): InputStream {
        return DirectInputStream(this, close)
    }

    internal class DirectInputStream(
            val os: DirectByteArrayOutputStream,
            val close: Boolean
    ) : ByteArrayInputStream(os.buf, 0, os.count) {
        override fun close() {
            if (close) {
                os.close()
            }
            super.close()
        }
    }
}