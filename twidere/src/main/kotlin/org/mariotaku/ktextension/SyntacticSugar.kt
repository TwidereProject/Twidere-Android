package org.mariotaku.ktextension

import java.lang.ref.WeakReference

fun rangeOfSize(start: Int, size: Int): IntRange {
    return start until start + size
}

fun LongArray.toStringArray(): Array<String> {
    return Array(this.size) { idx -> this[idx].toString() }
}

fun <T> T.toWeak(): WeakReference<T> = WeakReference(this)