package org.mariotaku.twidere.test.account

import android.accounts.Account
import android.accounts.AccountManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.ktextension.Bundle
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE
import org.mariotaku.twidere.extension.model.account_name
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.support.AccountManagerSupport

/**
 * Created by mariotaku on 2016/12/2.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test
    fun testMigration() {

        val context = InstrumentationRegistry.getTargetContext()

        val am = AccountManager.get(context)

        am.getAccountsByType(ACCOUNT_TYPE).map { account ->
            AccountManagerSupport.removeAccount(am, account, null, null, null)
        }

        ParcelableAccountUtils.getAccounts(context).forEach { pAccount ->
            val account = Account(pAccount.account_name, ACCOUNT_TYPE)
            val userdata = Bundle {
                this[Accounts.ACCOUNT_KEY]
            }
            am.addAccountExplicitly(account, null, userdata)
        }
    }
}