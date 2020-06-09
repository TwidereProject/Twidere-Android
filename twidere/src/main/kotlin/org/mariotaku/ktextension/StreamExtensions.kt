package org.mariotaku.ktextension

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

/**
 * Created by mariotaku on 2016/12/7.
 */

fun InputStream.toString(charset: Charset, close: Boolean = false): String {
    val r = bufferedReader(charset)
    if (close) return r.use { it.readText() }
    return r.readText()
}

fun OutputStream.writeLine(string: String = "", charset: Charset = Charset.defaultCharset(),
        crlf: Boolean = false) {
    write(string.toByteArray(charset))
    if (crlf) {
        write("\r\n".toByteArray(charset))
    } else {
        write("\n".toByteArray(charset))
    }
}

fun InputStream.expectLine(string: String = "", charset: Charset = Charset.defaultCharset(),
        crlf: Boolean = false): Boolean {
    if (!expectBytes(string.toByteArray(charset))) return false
    if (crlf) {
        if (!expectBytes("\r\n".toByteArray(charset))) return false
    } else {
        if (!expectBytes("\n".toByteArray(charset))) return false
    }
    return true
}


fun InputStream.expectBytes(bytes: ByteArray): Boolean {
    val readBytes = ByteArray(bytes.size)
    read(readBytes)
    return readBytes.contentEquals(bytes)
}