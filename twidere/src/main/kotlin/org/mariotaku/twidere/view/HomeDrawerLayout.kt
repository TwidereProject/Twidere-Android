/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view

import android.content.Context
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.util.AttributeSet
import android.view.MotionEvent

import org.mariotaku.chameleon.view.ChameleonDrawerLayout

class HomeDrawerLayout(context: Context, attrs: AttributeSet? = null) : ChameleonDrawerLayout(context, attrs) {

    private var shouldDisableDecider: ShouldDisableDecider? = null
    private var startLockMode: Int = 0
    private var endLockMode: Int = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startLockMode = getDrawerLockMode(GravityCompat.START)
                endLockMode = getDrawerLockMode(GravityCompat.END)
                if (isDrawerOpen(GravityCompat.START) || isDrawerOpen(GravityCompat.END)) {
                    // Opened, disable close if requested
                    if (shouldDisableDecider != null && shouldDisableDecider!!.shouldDisableTouch(ev)) {
                        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.START)
                        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setDrawerLockMode(startLockMode, GravityCompat.START)
                setDrawerLockMode(endLockMode, GravityCompat.END)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setShouldDisableDecider(shouldDisableDecider: ShouldDisableDecider) {
        this.shouldDisableDecider = shouldDisableDecider
    }


    interface ShouldDisableDecider {
        fun shouldDisableTouch(e: MotionEvent): Boolean
    }
}
