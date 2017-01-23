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