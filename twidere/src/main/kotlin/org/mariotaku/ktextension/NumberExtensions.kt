package org.mariotaku.ktextension

import java.text.NumberFormat
import java.util.*

fun String?.toLongOr(def: Long) = try {
    this?.toLong() ?: def
} catch (e: NumberFormatException) {
    def
}

fun String?.toIntOr(def: Int) = try {
    this?.toInt() ?: def
} catch (e: NumberFormatException) {
    def
}

fun String?.toBooleanOr(def: Boolean) = try {
    this?.toBoolean() ?: def
} catch (e: NumberFormatException) {
    def
}

fun String?.toDoubleOr(def: Double) = try {
    this?.toDouble() ?: def
} catch (e: NumberFormatException) {
    def
}

fun Int.coerceInOr(range: ClosedRange<Int>, def: Int): Int {
    if (range.isEmpty()) return def
    return coerceIn(range)
}

/**
 * Convenience method checking int flags
 */
operator fun Int.contains(i: Int): Boolean = (this and i) == i

/**
 * Convenience method checking long flags
 */
operator fun Long.contains(i: Long): Boolean = (this and i) == i

fun Number.toString(locale: Locale = Locale.getDefault()): String {
    val nf = NumberFormat.getInstance(locale)
    return nf.format(this)
}

fun Double.toString(decimalDigits: Int): String {
    var str = String.format(Locale.US, "%." + decimalDigits + "f", this)
    val dotIdx = str.lastIndexOf('.')
    if (dotIdx == -1) return str
    str = str.substring(0, (dotIdx + decimalDigits + 1).coerceAtMost(str.length)).trimEnd('0')
    if (str.endsWith('.')) {
        str = str.substring(0, str.length - 1)
    }
    return str
}

val Int.nextPowerOf2: Int
    get() {
        var n = this
        if (n <= 0 || n > 1 shl 30) throw IllegalArgumentException("n is invalid: " + n)
        n -= 1
        n = n or (n shr 16)
        n = n or (n shr 8)
        n = n or (n shr 4)
        n = n or (n shr 2)
        n = n or (n shr 1)
        return n + 1
    }

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}