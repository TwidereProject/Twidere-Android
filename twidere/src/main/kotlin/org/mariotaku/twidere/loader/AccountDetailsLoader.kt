package org.mariotaku.twidere.loader

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.support.v4.content.FixedAsyncTaskLoader
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils

/**
 * Created by mariotaku on 2016/12/4.
 */
class AccountDetailsLoader(
        context: Context,
        val filter: (AccountDetails.() -> Boolean)? = null
) : FixedAsyncTaskLoader<List<AccountDetails>>(context) {
    private val am: AccountManager = AccountManager.get(context)
    private val accountUpdateListener = OnAccountsUpdateListener {
        onContentChanged()
    }

    override fun loadInBackground(): List<AccountDetails> {
        return AccountUtils.getAllAccountDetails(am, true).filter {
            filter?.invoke(it) ?: true
        }.sortedBy(AccountDetails::position)
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        am.removeOnAccountsUpdatedListenerSafe(accountUpdateListener)
    }

    override fun onStartLoading() {
        am.addOnAccountsUpdatedListenerSafe(accountUpdateListener, updateImmediately = true)
        if (takeContentChanged()) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}