/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment.iface

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import org.mariotaku.twidere.constant.IntentConstants
import java.util.*

interface IBaseFragment<out F : Fragment> {
    val extraConfiguration: Bundle?
        get() = null

    val tabPosition: Int
        get() = (this as Fragment).arguments?.getInt(IntentConstants.EXTRA_TAB_POSITION, -1) ?: -1

    val tabId: Long
        get() = (this as Fragment).arguments?.getLong(IntentConstants.EXTRA_TAB_ID, -1L) ?: -1L

    val insetsCallback: SystemWindowInsetsCallback?
        get() {
            val fragment = this as Fragment
            val activity = fragment.activity
            val parentFragment = fragment.parentFragment
            if (parentFragment is SystemWindowInsetsCallback) {
                return parentFragment
            } else if (activity is SystemWindowInsetsCallback) {
                return activity
            }
            return null
        }

    fun requestApplyInsets() {
        val fragment = this as Fragment
        val callback = insetsCallback ?: return
        val insets = Rect()
        if (callback.getSystemWindowInsets(fragment, insets)) {
            onApplySystemWindowInsets(insets)
        }
    }

    fun onApplySystemWindowInsets(insets: Rect) {
        this as Fragment
        view?.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }

    interface SystemWindowInsetsCallback {
        fun getSystemWindowInsets(caller: Fragment, insets: Rect): Boolean
    }

    fun executeAfterFragmentResumed(useHandler: Boolean = false, action: (F) -> Unit): Promise<Unit, Exception>

    class ActionHelper<F : Fragment> {

        private val handler: Handler = Handler(Looper.getMainLooper())

        private var fragmentResumed: Boolean = false
        private val actionQueue = LinkedList<ExecuteInfo<F>>()

        fun dispatchOnPause() {
            fragmentResumed = false
        }

        fun dispatchOnResumeFragments(fragment: F) {
            fragmentResumed = true
            executePending(fragment)
        }

        private fun executePending(fragment: F) {
            if (!fragmentResumed) return
            var info: ExecuteInfo<F>?
            do {
                info = actionQueue.poll()
                info?.let { i ->
                    if (i.useHandler) {
                        handler.post { i.invoke(fragment) }
                    } else {
                        i.invoke(fragment)
                    }
                }
            } while (info != null)
        }

        fun executeAfterFragmentResumed(fragment: F, useHandler: Boolean = false, action: (F) -> Unit)
                : Promise<Unit, Exception> {
            val info = ExecuteInfo(action, useHandler)
            actionQueue.add(info)
            executePending(fragment)
            return info.promise
        }

        private data class ExecuteInfo<in F : Fragment>(private val action: (F) -> Unit, val useHandler: Boolean) {
            private val deferredInstance = deferred<Unit, Exception>()

            val promise: Promise<Unit, Exception> get() = deferredInstance.promise

            fun invoke(fragment: F) {
                action(fragment)
                deferredInstance.resolve(Unit)
            }
        }
    }
}
