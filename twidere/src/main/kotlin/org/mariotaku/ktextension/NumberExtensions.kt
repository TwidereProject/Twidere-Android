package org.mariotaku.ktextension

import java.text.NumberFormat
import java.util.*

/**
 * Created by mariotaku on 16/7/30.
 */

fun String?.toLongOr(def: Long): Long {
    try {
        return this?.toLong() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
}

fun String?.toIntOr(def: Int): Int {
    try {
        return this?.toInt() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
}

fun String?.toDoubleOr(def: Double): Double {
    try {
        return this?.toDouble() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
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