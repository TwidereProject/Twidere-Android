package org.mariotaku.ktextension

import java.io.InputStream
import java.nio.charset.Charset

/**
 * Created by mariotaku on 2016/12/7.
 */

fun InputStream.toString(charset: Charset, close: Boolean = false): String {
    val r = bufferedReader(charset)
    if (close) return r.use { it.readText() }
    return r.readText()
}
