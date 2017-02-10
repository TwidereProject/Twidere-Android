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

fun <T : Any> Array<T>.toNulls(): Array<T?> {
    @Suppress("UNCHECKED_CAST")
    return this as Array<T?>
}