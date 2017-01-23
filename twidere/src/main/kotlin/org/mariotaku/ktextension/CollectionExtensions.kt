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

fun <T> MutableCollection<T>.addAllEnhanced(collection: Collection<T>, ignoreDuplicates: Boolean): Boolean {
    if (ignoreDuplicates) {
        return addAll(collection.filter { it !in this })
    } else {
        return addAll(collection)
    }
}

fun <T> Collection<T>.addAllTo(collection: MutableCollection<T>): Boolean {
    return collection.addAll(this)
}

fun <E> Collection<E>?.nullableContentEquals(other: Collection<E>?): Boolean {
    if (this == null) return other.isNullOrEmpty()
    return contentEquals(other!!)
}

fun <E> Collection<E>.contentEquals(other: Collection<E>): Boolean {
    if (this === other) return true
    if (this.size != other.size) return false
    return this.containsAll(other) && other.containsAll(this)
}