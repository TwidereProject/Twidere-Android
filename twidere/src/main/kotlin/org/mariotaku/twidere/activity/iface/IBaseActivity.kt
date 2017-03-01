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
import java.util.*

/**
 * Created by mariotaku on 15/12/28.
 */
interface IBaseActivity<out A : FragmentActivity> {

    fun executeAfterFragmentResumed(useHandler: Boolean = false, action: (A) -> Unit)

    class ActionHelper<out A : FragmentActivity>(private val activity: A) {

        private var fragmentResumed: Boolean = false
        private val actionQueue = LinkedList<ExecuteInfo<A>>()
        private val handler = Handler(Looper.getMainLooper())

        fun dispatchOnPause() {
            fragmentResumed = false
        }

        fun dispatchOnResumeFragments() {
            fragmentResumed = true
            executePending()
        }


        private fun executePending() {
            if (!fragmentResumed) return
            var info: ExecuteInfo<A>?
            do {
                val cur = actionQueue.poll()
                cur?.let { cur ->
                    if (cur.useHandler) {
                        handler.post { cur.action(activity) }
                    } else {
                        cur.action(activity)
                    }
                }
                info = cur
            } while (info != null)
        }

        fun executeAfterFragmentResumed(useHandler: Boolean = false, action: (A) -> Unit) {
            actionQueue.add(ExecuteInfo(action, useHandler))
            executePending()
        }

        private data class ExecuteInfo<in A : FragmentActivity>(val action: (A) -> Unit, val useHandler: Boolean)
    }
}
