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

package org.mariotaku.twidere.util


import android.app.Activity
import android.app.Application
import android.os.Bundle
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.SessionEvent
import org.apache.commons.collections.primitives.ArrayIntList
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.activity.HomeActivity

/**
 * Created by mariotaku on 15/10/5.
 */
class ActivityTracker : Application.ActivityLifecycleCallbacks {

    private val internalStack = ArrayIntList()

    private var sessionEvent: SessionEvent? = null

    var isHomeActivityStarted: Boolean = false
        private set
    var isHomeActivityLaunched: Boolean = false
        private set

    private fun isSwitchingInSameTask(hashCode: Int): Boolean {
        return internalStack.lastIndexOf(hashCode) < internalStack.size() - 1
    }

    fun size(): Int {
        return internalStack.size()
    }

    val isEmpty: Boolean
        get() = internalStack.isEmpty

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is HomeActivity) {
            isHomeActivityLaunched = true
        }
    }

    override fun onActivityStarted(activity: Activity) {
        internalStack.add(System.identityHashCode(activity))
        if (activity is HomeActivity) {
            isHomeActivityStarted = true
        }
        // BEGIN HotMobi
        if (sessionEvent == null && BuildConfig.HOTMOBI_LOG_ENABLED) {
            sessionEvent = SessionEvent.create(activity)
        }
        // END HotMobi
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        val hashCode = System.identityHashCode(activity)
        if (activity is HomeActivity) {
            isHomeActivityStarted = false
        }
        // BEGIN HotMobi
        val event = sessionEvent
        if (event != null && !isSwitchingInSameTask(hashCode)) {
            event.markEnd()
            HotMobiLogger.getInstance(activity).log(event, SessionEvent::dumpPreferences)
            sessionEvent = null
        }
        // END HotMobi

        internalStack.removeElement(hashCode)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is HomeActivity && activity.isFinishing()) {
            isHomeActivityLaunched = false
        }
    }
}
