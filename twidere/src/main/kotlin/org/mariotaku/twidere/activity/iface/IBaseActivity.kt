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
import androidx.fragment.app.FragmentActivity
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import java.util.*

/**
 * Created by mariotaku on 15/12/28.
 */
interface IBaseActivity<out A : FragmentActivity> {

    fun executeAfterFragmentResumed(useHandler: Boolean = false, action: (A) -> Unit): Promise<Unit, Exception>

    class ActionHelper<A : FragmentActivity> {

        private var fragmentResumed: Boolean = false
        private val actionQueue = LinkedList<ExecuteInfo<A>>()
        private val handler = Handler(Looper.getMainLooper())

        fun dispatchOnPause() {
            fragmentResumed = false
        }

        fun dispatchOnResumeFragments(activity: A) {
            fragmentResumed = true
            executePending(activity)
        }


        private fun executePending(activity: A) {
            if (!fragmentResumed) return
            var info: ExecuteInfo<A>?
            do {
                info = actionQueue.poll()
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
            actionQueue.add(info)
            executePending(activity)
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
