package org.mariotaku.twidere.util.sync

import android.content.Context
import java.io.File

/**
 * Created by mariotaku on 2016/12/31.
 */
internal const val LOGTAG = "Twidere.Sync"

internal val Context.syncDataDir: File
    get() = File(filesDir, "sync_data")

internal fun File.mkdirIfNotExists(): File? {
    if (exists() || mkdirs()) return this
    return null
}