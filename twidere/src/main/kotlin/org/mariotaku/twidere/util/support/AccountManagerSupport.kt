package org.mariotaku.twidere.util.support

import android.accounts.*
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2016/12/2.
 */

fun AccountManager.removeAccountSupport(
        account: Account,
        activity: Activity? = null,
        callback: AccountManagerCallback<Bundle>? = null,
        handler: Handler? = null
): AccountManagerFuture<Bundle> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        return AccountManagerSupportL.removeAccount(this, account, activity, callback, handler)
    }

    @Suppress("DEPRECATION")
    val future = this.removeAccount(account, { future ->
        callback?.run(BooleanToBundleAccountManagerFuture(future))
    }, handler)
    return BooleanToBundleAccountManagerFuture(future)
}

object AccountManagerSupportL {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    internal fun removeAccount(
            am: AccountManager, account: Account,
            activity: Activity?,
            callback: AccountManagerCallback<Bundle>?,
            handler: Handler?
    ): AccountManagerFuture<Bundle> {
        return am.removeAccount(account, activity, callback, handler)
    }

}

private class BooleanToBundleAccountManagerFuture internal constructor(private val future: AccountManagerFuture<Boolean>) : AccountManagerFuture<Bundle> {

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return future.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return future.isCancelled
    }

    override fun isDone(): Boolean {
        return future.isDone
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.result)
        return result
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(timeout: Long, unit: TimeUnit): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult(timeout, unit))
        return result
    }
}
