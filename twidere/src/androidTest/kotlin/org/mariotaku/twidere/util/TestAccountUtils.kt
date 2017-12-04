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
import com.bluelinelabs.logansquare.LoganSquare
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import org.mariotaku.ktextension.deadline
import org.mariotaku.twidere.extension.getAllDetails
import org.mariotaku.twidere.extension.isAccountValid
import org.mariotaku.twidere.extension.model.updateDetails
import org.mariotaku.twidere.extension.ownedAccounts
import org.mariotaku.twidere.extension.removeAccount
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.test.R
import java.io.InputStream
import java.lang.Exception
import java.util.concurrent.TimeUnit

object TestAccountUtils {

    val testAccounts: List<AccountDetails> by lazy {
        val context = InstrumentationRegistry.getContext()
        return@lazy accountResources.map {
            return@map context.resources.openRawResource(it).use<InputStream?, AccountDetails> {
                LoganSquare.parse(it, AccountDetails::class.java)
            }
        }
    }

    private val accountResources = intArrayOf(R.raw.account_4223092274_twitter_com,
            R.raw.account_200092_mastodon_social)

    fun insertTestAccounts() {
        val targetContext = InstrumentationRegistry.getTargetContext()
        val testAccounts = testAccounts
        val am = AccountManager.get(targetContext)
        val result = all(am.ownedAccounts.mapNotNull { account ->
            if (am.isAccountValid(account) || testAccounts.none { account == it.account }) {
                return@mapNotNull null
            }
            return@mapNotNull am.removeAccount(account).deadline(1, TimeUnit.SECONDS)
        }, cancelOthersOnError = false).get()
        DebugLog.d(msg = "Removed accounts: $result")
        val existingAccounts = am.getAllDetails(false)
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
        val existingAccounts = am.getAllDetails(false)
        return all(existingAccounts.mapNotNull {
            if (!it.test) return@mapNotNull null
            return@mapNotNull am.removeAccount(it.account)
        })
    }

}