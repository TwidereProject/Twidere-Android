package org.mariotaku.ktextension

/**
 * Created by mariotaku on 2017/1/2.
 */

fun <K, V> MutableMap<K, V>.removeAll(keys: Collection<K>) {
    keys.forEach { remove(it) }
}
