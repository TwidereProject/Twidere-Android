package org.mariotaku.ktextension

/**
 * Created by mariotaku on 2017/1/12.
 */


fun Array<*>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

fun Array<*>?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun <T> Array<T>.toNulls(): Array<T?> {
    @Suppress("UNCHECKED_CAST")
    return this as Array<T?>
}

fun <T> Array<T>.toStringArray(): Array<String> {
    return Array(size) { this[it].toString() }
}

inline fun <T, reified R> Array<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}

inline fun <reified R> LongArray.mapToArray(transform: (Long) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}