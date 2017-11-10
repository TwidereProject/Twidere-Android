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
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import org.mariotaku.twidere.extension.isAccountValid
import org.mariotaku.twidere.extension.model.updateDetails
import org.mariotaku.twidere.extension.removeAccount
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.test.R
import java.lang.Exception

object TestAccountUtils {

    private val accountResources = intArrayOf(R.raw.account_4223092274_twitter_com)

    fun insertTestAccounts() {
        val targetContext = InstrumentationRegistry.getTargetContext()
        val context = InstrumentationRegistry.getContext()
        val testAccounts = accountResources.map {
            return@map context.resources.openRawResource(it).use {
                JsonSerializer.parse(it, AccountDetails::class.java)
            }
        }
        val am = AccountManager.get(targetContext)
        val result = all(AccountUtils.getAccounts(am).mapNotNull { account ->
            if (am.isAccountValid(account) || testAccounts.none { account == it.account }) {
                return@mapNotNull null
            }
            return@mapNotNull am.removeAccount(account)
        }, cancelOthersOnError = false).get()
        DebugLog.d(msg = "Removed accounts: $result")
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        testAccounts.forEach { details ->
            if (existingAccounts.any { it.account == details.account || it.key == details.key }) {
                return@forEach
            }
            am.addAccountExplicitly(details.account, null, null)
            details.account.updateDetails(am, details)
        }
    }

    fun removeTestAccounts(): Promise<List<Bundle>, Exception> {
        val targetContext = InstrumentationRegistry.getTargetContext()
        val am = AccountManager.get(targetContext)
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        return all(existingAccounts.mapNotNull {
            if (!it.test) return@mapNotNull null
            return@mapNotNull am.removeAccount(it.account)
        })
    }

}