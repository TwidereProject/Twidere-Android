package org.mariotaku.ktextension

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.os.Handler

/**
 * Created by mariotaku on 2016/12/13.
 */

fun AccountManager.addOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener,
                                                    handler: Handler? = null,
                                                    updateImmediately: Boolean = false): Boolean {
    try {
        this.addOnAccountsUpdatedListener(listener, handler, updateImmediately)
        return true
    } catch (e: IllegalStateException) {
        return false
    }
}

fun AccountManager.removeOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener): Boolean {
    try {
        this.removeOnAccountsUpdatedListener(listener)
        return true
    } catch (e: IllegalStateException) {
        return false
    }
}
