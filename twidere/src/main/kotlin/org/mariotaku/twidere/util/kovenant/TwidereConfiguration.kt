/*
 * Copyright (c) 2015 Mark Platvoet<mplatvoet@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * THE SOFTWARE.
 */
@file:JvmName("KovenantTwidere")

package org.mariotaku.twidere.util.kovenant

import android.os.AsyncTask
import android.os.Process
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.android.Disposable
import nl.komponents.kovenant.android.androidUiDispatcher
import nl.komponents.kovenant.buildJvmDispatcher
import nl.komponents.kovenant.ui.KovenantUi
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private val initCount = AtomicInteger(0)
private val disposable = AtomicReference<Disposable>(null)

fun startKovenant() {
    initCount.onlyFirst {
        disposable.set(configureKovenant())
    }
}


@JvmOverloads fun stopKovenant(force: Boolean = false) {
    val dispose = disposable.get()
    if (dispose != null && disposable.compareAndSet(dispose, null)) {
        dispose.close(force)
        initCount.set(0)
    }
}

/**
 * Configures Kovenant for common Android scenarios.
 *
 * @return `Disposable` to properly shutdown Kovenant
 */
fun configureKovenant(): Disposable {
    KovenantUi.uiContext {
        dispatcher = androidUiDispatcher()
    }

    val callbackDispatcher = buildJvmDispatcher {
        name = "kovenant-callback"
        concurrentTasks = 1

        pollStrategy {
            yielding(numberOfPolls = 100)
            blocking()
        }

        threadFactory = createThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)
    }
    val workerDispatcher = AsyncTaskDispatcher(AsyncTask.SERIAL_EXECUTOR)

    Kovenant.context {
        callbackContext {
            dispatcher = callbackDispatcher
        }
        workerContext {
            dispatcher = workerDispatcher
        }
    }
    return DispatchersDisposable(workerDispatcher, callbackDispatcher)
}

private fun createThreadFactory(priority: Int): (Runnable, String, Int) -> Thread = {
    target, dispatcherName, id ->
    val wrapper = Runnable {
        Process.setThreadPriority(priority)
        target.run()
    }
    Thread(wrapper, "$dispatcherName-$id")
}

private inline fun AtomicInteger.onlyFirst(body: () -> Unit) {
    val threadNumber = incrementAndGet()
    if (threadNumber == 1) {
        body()
    } else {
        decrementAndGet()
    }
}

private class DispatchersDisposable(private vararg val dispatcher: Dispatcher) : Disposable {
    override fun close(force: Boolean) {
        dispatcher.forEach {
            close(force, it)
        }
    }

    private fun close(force: Boolean, dispatcher: Dispatcher) {
        try {
            if (force) {
                dispatcher.stop(force = true)
            } else {
                dispatcher.stop(block = true)
            }
        } catch(e: Exception) {
            //ignore, nothing we can do
        }
    }

}

private class AsyncTaskDispatcher(val executor: Executor) : Dispatcher {
    override fun offer(task: () -> Unit): Boolean {
        if (stopped || terminated) return false
        executor.execute(task)
        return true
    }

    override fun tryCancel(task: () -> Unit): Boolean {
        return false
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        stopped = true
        return emptyList()
    }

    override var stopped: Boolean = false
        private set

    override var terminated: Boolean = false
        private set

}