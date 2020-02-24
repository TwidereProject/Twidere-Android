package org.mariotaku.twidere.loader

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils
import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 2016/12/4.
 */
class AccountDetailsLoader(
        context: Context,
        val filter: (AccountDetails.() -> Boolean)? = null
) : FixedAsyncTaskLoader<List<AccountDetails>>(context) {

    private var accountUpdateListener: OnAccountsUpdateListener? = null
        set(value) {
            val am: AccountManager = AccountManager.get(context)
            field?.let {
                am.removeOnAccountsUpdatedListenerSafe(it)
            }
            if (value != null) {
                am.addOnAccountsUpdatedListenerSafe(value, updateImmediately = true)
            }
        }

    override fun loadInBackground(): List<AccountDetails> {
        val am: AccountManager = AccountManager.get(context)
        return AccountUtils.getAllAccountDetails(am, true).filter {
            filter?.invoke(it) ?: true
        }.sortedBy(AccountDetails::position)
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        accountUpdateListener = null
    }

    override fun onStartLoading() {
        val weakThis = WeakReference(this)
        accountUpdateListener = OnAccountsUpdateListener {
            weakThis.get()?.onContentChanged()
        }
        if (takeContentChanged()) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}