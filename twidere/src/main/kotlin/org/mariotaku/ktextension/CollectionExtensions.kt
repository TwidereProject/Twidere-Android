package org.mariotaku.ktextension

/**
 * Created by mariotaku on 2016/12/24.
 */

fun Collection<*>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

fun Collection<*>?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun <T> MutableCollection<T>.addAllIgnoreDuplicates(collection: Collection<T>) {
    addAll(collection.filter { it !in this })
}