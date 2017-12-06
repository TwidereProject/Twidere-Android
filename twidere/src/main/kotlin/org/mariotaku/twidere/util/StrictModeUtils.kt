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

package org.mariotaku.twidere.util

import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy

object StrictModeUtils {

    fun detectAllThreadPolicy() {
        val threadPolicyBuilder = ThreadPolicy.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            threadPolicyBuilder.detectUnbufferedIo()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            threadPolicyBuilder.detectResourceMismatches()
        }
        threadPolicyBuilder.detectDiskReads()
        threadPolicyBuilder.detectDiskWrites()
        threadPolicyBuilder.detectCustomSlowCalls()
        threadPolicyBuilder.penaltyLog()
        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
    }

    fun detectAllVmPolicy() {
        val vmPolicyBuilder = VmPolicy.Builder()
        vmPolicyBuilder.detectAll()
        vmPolicyBuilder.penaltyLog()
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }

}
