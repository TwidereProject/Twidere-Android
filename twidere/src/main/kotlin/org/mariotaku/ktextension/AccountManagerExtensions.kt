package org.mariotaku.ktextension

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.os.Handler

/**
 * Created by mariotaku on 2016/12/13.
 */

fun AccountManager.addOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener,
        handler: Handler? = null, updateImmediately: Boolean = false): Boolean {
    return try {
        this.addOnAccountsUpdatedListener(listener, handler, updateImmediately)
        true
    } catch (e: IllegalStateException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun AccountManager.removeOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener): Boolean {
    return try {
        this.removeOnAccountsUpdatedListener(listener)
        true
    } catch (e: IllegalStateException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }
}
