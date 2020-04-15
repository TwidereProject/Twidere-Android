package org.mariotaku.twidere.util.sync

import androidx.annotation.WorkerThread
import java.io.IOException

/**
 * Created by mariotaku on 2017/1/2.
 */
interface ISyncAction {
    @Throws(IOException::class)
    @WorkerThread
    fun execute(): Boolean
}