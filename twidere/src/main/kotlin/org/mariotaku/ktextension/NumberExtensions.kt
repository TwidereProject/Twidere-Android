package org.mariotaku.ktextension

/**
 * Created by mariotaku on 16/7/30.
 */

fun String?.toLong(def: Long): Long {
    try {
        return this?.toLong() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
}

fun String?.toInt(def: Int): Int {
    try {
        return this?.toInt() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
}

fun String?.toDouble(def: Double): Double {
    try {
        return this?.toDouble() ?: def
    } catch (e: NumberFormatException) {
        return def
    }
}

fun Int.coerceInOr(range: ClosedRange<Int>, or: Int): Int {
    if (range.isEmpty()) return or
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