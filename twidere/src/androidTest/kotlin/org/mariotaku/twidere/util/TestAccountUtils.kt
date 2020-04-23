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

import android.accounts.AccountManager
import androidx.test.platform.app.InstrumentationRegistry
import org.mariotaku.twidere.extension.model.updateDetails
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.test.R
import org.mariotaku.twidere.util.support.removeAccountSupport

/**
 * Created by mariotaku on 2017/4/16.
 */
object TestAccountUtils {

    private val accountResources = intArrayOf(R.raw.account_4223092274_twitter_com)

    fun insertTestAccounts() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = InstrumentationRegistry.getInstrumentation().context
        val am = AccountManager.get(targetContext)
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        accountResources.forEach { resId ->
            val details = context.resources.openRawResource(resId).use {
                JsonSerializer.parse(it, AccountDetails::class.java)
            }
            if (existingAccounts.any { it.account == details.account || it.key == details.key }) {
                return@forEach
            }
            am.addAccountExplicitly(details.account, null, null)
            details.account.updateDetails(am, details)
        }
    }

    fun removeTestAccounts() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val am = AccountManager.get(targetContext)
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        existingAccounts.filter { it.test }.forEach { am.removeAccountSupport(it.account) }
    }
}