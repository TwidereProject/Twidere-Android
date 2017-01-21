package org.mariotaku.twidere.util.sync.google

import java.io.Closeable

/**
 * Created by mariotaku on 1/21/17.
 */

internal class CloseableAny<T>(val obj: T) : Closeable {
    override fun close() {
        if (obj is Closeable) {
            obj.close()
        }
    }
}
