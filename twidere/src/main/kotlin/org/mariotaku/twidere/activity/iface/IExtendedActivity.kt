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
interface IExtendedActivity<out A : FragmentActivity> {

    fun executeAfterFragmentResumed(action: (A) -> Unit)

    class ActionHelper<out A : FragmentActivity>(private val activity: A) {

        private var fragmentResumed: Boolean = false
        private val actionQueue = LinkedList<(A) -> Unit>()
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
            var action: ((A) -> Unit)?
            do {
                action = actionQueue.poll()
                // Sometimes actions called too fast, so we wait for next loop
                handler.post {
                    action?.invoke(activity)
                }
            } while (action != null)
        }

        fun executeAfterFragmentResumed(action: (A) -> Unit) {
            actionQueue.add(action)
            executePending()
        }
    }
}
