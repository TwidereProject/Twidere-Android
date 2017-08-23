package org.mariotaku.twidere.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by mariotaku on 2017/1/24.
 */

fun tempFileInputStream(context: Context, write: (OutputStream) -> Unit): InputStream {
    val file = File.createTempFile("twidere__temp_is_file", ".tmp", context.cacheDir)
    file.outputStream().use { write(it) }
    return TempFileInputStream(file)
}

internal class TempFileInputStream(val file: File) : FileInputStream(file) {
    override fun close() {
        try {
            super.close()
        } finally {
            file.delete()
        }
    }
}