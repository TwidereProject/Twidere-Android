/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.iface

import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import java.util.*

interface IBaseActivity<out A : FragmentActivity> {

    fun executeAfterFragmentResumed(useHandler: Boolean = false, action: (A) -> Unit): Promise<Unit, Exception>

    fun executeBeforeFragmentPaused(useHandler: Boolean = false, action: (A) -> Unit): Promise<Unit, Exception>

    fun executeAfterFragmentNextResumed(useHandler: Boolean = false, action: (A) -> Unit): Promise<Unit, Exception> {
        val deferred = deferred<Unit, Exception>()
        executeBeforeFragmentPaused(useHandler) {
            executeAfterFragmentResumed(useHandler) { a ->
                action(a)
                deferred.resolve(Unit)
            }
        }
        return deferred.promise
    }

    class ActionHelper<A : FragmentActivity> {

        private var fragmentResumed: Boolean = false
        private val resumeActionQueue = LinkedList<ExecuteInfo<A>>()
        private val pauseActionQueue = LinkedList<ExecuteInfo<A>>()
        private val handler = Handler(Looper.getMainLooper())

        fun dispatchOnPause(activity: A) {
            fragmentResumed = false
            pauseActionQueue.executePending(activity)
        }

        fun dispatchOnResumeFragments(activity: A) {
            fragmentResumed = true
            resumeActionQueue.executePending(activity)
        }

        private fun Queue<ExecuteInfo<A>>.executePending(activity: A, condition: Boolean = true) {
            if (!condition) return
            var info: ExecuteInfo<A>?
            do {
                info = poll()
                info?.let { i ->
                    if (i.useHandler) {
                        handler.post { i.invoke(activity) }
                    } else {
                        i.invoke(activity)
                    }
                }
            } while (info != null)
        }

        fun executeAfterFragmentResumed(activity: A, useHandler: Boolean = false, action: (A) -> Unit)
                : Promise<Unit, Exception> {
            val info = ExecuteInfo(action, useHandler)
            resumeActionQueue.add(info)
            resumeActionQueue.executePending(activity, fragmentResumed)
            return info.promise
        }

        fun executeBeforeFragmentPaused(activity: A, useHandler: Boolean = false, action: (A) -> Unit)
                : Promise<Unit, Exception> {
            val info = ExecuteInfo(action, useHandler)
            pauseActionQueue.add(info)
            pauseActionQueue.executePending(activity, !fragmentResumed)
            return info.promise
        }

        private data class ExecuteInfo<in A : FragmentActivity>(private val action: (A) -> Unit, val useHandler: Boolean) {

            private val deferredInstance = deferred<Unit, Exception>()

            val promise: Promise<Unit, Exception> get() = deferredInstance.promise

            fun invoke(activity: A) {
                action(activity)
                deferredInstance.resolve(Unit)
            }
        }
    }
}
