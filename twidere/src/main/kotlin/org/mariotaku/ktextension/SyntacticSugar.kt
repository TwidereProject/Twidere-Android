package org.mariotaku.ktextension

import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 16/8/17.
 */

inline fun <T> configure(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}

fun rangeOfSize(start: Int, size: Int): IntRange {
    return start until start + size
}

fun LongArray.toStringArray(): Array<String> {
    return Array(this.size) { idx -> this[idx].toString() }
}

fun <T> T.weak(): WeakReference<T> = WeakReference(this)