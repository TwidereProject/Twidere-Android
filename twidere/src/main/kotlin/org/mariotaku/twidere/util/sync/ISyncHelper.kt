package org.mariotaku.twidere.util.sync

import java.io.IOException

/**
 * Created by mariotaku on 2017/1/2.
 */
interface ISyncHelper {
    @Throws(IOException::class)
    fun performSync(): Boolean
}