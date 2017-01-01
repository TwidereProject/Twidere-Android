package org.mariotaku.ktextension

/**
 * Created by mariotaku on 16/8/17.
 */

inline fun <T> configure(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}

inline fun <F, T> F.convert(block: (F) -> T): T {
    return block(this)
}

fun rangeOfSize(start: Int, size: Int): IntRange {
    return IntRange(start, start + size)
}

fun LongArray.toStringArray(): Array<String> {
    return Array(this.size) { idx -> this[idx].toString() }
}