package org.mariotaku.ktextension

import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import org.mariotaku.twidere.util.DebugLog
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray
import kotlin.concurrent.schedule


fun <V> Promise<V, Exception>.deadline(time: Long, unit: TimeUnit): Promise<V, Exception> {
    val weakPromise = toWeak()
    WatchdogTimer.schedule(unit.toMillis(time)) {
        val promise = weakPromise.get() ?: return@schedule
        if (promise.isDone()) return@schedule
        if (!Kovenant.cancel(promise, DeadlineException())) {
            DebugLog.w(msg = "Unable to stop trigger deadline for $promise. Is it not cancellable?")
        }
    }
    return this
}


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

class DeadlineException : Exception()

private object WatchdogTimer : Timer("promise-deadline-watchdog")