package org.mariotaku.ktextension

import java.text.NumberFormat
import java.util.*

/**
 * Created by mariotaku on 16/7/30.
 */

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

fun Number.toLocalizedString(locale: Locale = Locale.getDefault()): String {
    val nf = NumberFormat.getInstance(locale)
    return nf.format(this)
}

val Int.nextPowerOf2: Int
    get() {
        var n = this
        if (n <= 0 || n > 1 shl 30) throw IllegalArgumentException("n is invalid: $n")
        n -= 1
        n = n or (n shr 16)
        n = n or (n shr 8)
        n = n or (n shr 4)
        n = n or (n shr 2)
        n = n or (n shr 1)
        return n + 1
    }