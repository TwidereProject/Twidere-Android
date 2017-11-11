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

package org.mariotaku.twidere

import android.accounts.AccountManager
import android.support.test.InstrumentationRegistry
import android.support.test.annotation.UiThreadTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.extension.isAccountValid
import org.mariotaku.twidere.extension.ownedAccounts
import org.mariotaku.twidere.util.TestAccountUtils

@RunWith(AndroidJUnit4::class)
class AccountTests {
    @Test
    @UiThreadTest
    fun testAccountValidity() {
        val context = InstrumentationRegistry.getTargetContext()
        val am = AccountManager.get(context)
        val invalidAccounts = am.ownedAccounts.filterNot { account ->
            TestAccountUtils.testAccounts.any { it.account == account } || am.isAccountValid(account)
        }
        if (invalidAccounts.isNotEmpty()) {
            Assert.fail("$invalidAccounts are not valid")
        }
    }
}
