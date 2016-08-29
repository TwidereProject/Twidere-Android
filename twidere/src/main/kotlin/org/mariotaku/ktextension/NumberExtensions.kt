package org.mariotaku.ktextension

/**
 * Created by mariotaku on 16/7/30.
 */

fun String.toLong(def: Long): Long {
    try {
        return toLong()
    } catch (e: NumberFormatException) {
        return def
    }
}

fun String.toDoubleOrNull(): Double? {
    try {
        return toDouble()
    } catch (e: NumberFormatException) {
        return null
    }
}