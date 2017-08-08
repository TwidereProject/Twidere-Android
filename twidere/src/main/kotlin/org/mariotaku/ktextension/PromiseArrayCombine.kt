package org.mariotaku.ktextension

import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * Created by mariotaku on 2016/12/2.
 */
fun <V, E> combine(promises: List<Promise<V, E>>): Promise<List<V>, E> {
    return concreteCombine(promises)
}

fun <V, E> concreteCombine(promises: List<Promise<V, E>>): Promise<List<V>, E> {
    val deferred = deferred<List<V>, E>()

    val results = AtomicReferenceArray<V>(promises.size)
    val successCount = AtomicInteger(promises.size)

    fun createArray(): List<V> {
        return (0 until results.length()).map { results[it] }
    }

    fun Promise<V, *>.registerSuccess(idx: Int) {
        success { v ->
            results.set(idx, v)
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createArray())
            }
        }
    }

    fun <V, E> Deferred<V, E>.registerFail(promises: List<Promise<*, E>>) {
        val failCount = AtomicInteger(0)
        promises.forEach { promise ->
            promise.fail { e ->
                if (failCount.incrementAndGet() == 1) {
                    this.reject(e)
                }
            }
        }
    }

    promises.forEachIndexed { idx, promise ->
        promise.registerSuccess(idx)
    }
    deferred.registerFail(promises)

    return deferred.promise
}
