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

package org.mariotaku.twidere.extension

import android.accounts.*
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.thenApply
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils

fun AccountManager.hasInvalidAccount(): Boolean {
    val accounts = ownedAccounts
    if (accounts.isEmpty()) return false
    return accounts.any { !isAccountValid(it) }
}

fun AccountManager.isAccountValid(account: Account): Boolean {
    return !getUserData(account, ACCOUNT_USER_DATA_KEY).isNullOrEmpty()
}

fun AccountManager.renameTwidereAccount(oldAccount: Account, newName: String,
        handler: Handler? = null): Promise<Account, Exception>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) task {
        return@task API21.renameAccount(this, oldAccount, newName, null, handler).result
    } else task {
        val newAccount = Account(newName, oldAccount.type)
        if (!addAccountExplicitly(newAccount, null, null)) {
            throw AccountsException()
        }
        for (key in AccountUtils.ACCOUNT_USER_DATA_KEYS) {
            setUserData(newAccount, key, getUserData(oldAccount, key))
        }
        setAuthToken(newAccount, ACCOUNT_AUTH_TOKEN_TYPE,
                peekAuthToken(oldAccount, ACCOUNT_AUTH_TOKEN_TYPE))
        return@task newAccount
    }.and(removeAccount(oldAccount, activity = null, handler = handler)).thenApply {
        return@thenApply first
    }

}


fun AccountManager.addOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener,
        handler: Handler? = null, updateImmediately: Boolean = false): Boolean {
    try {
        this.addOnAccountsUpdatedListener(listener, handler, updateImmediately)
        return true
    } catch (e: IllegalStateException) {
        return false
    } catch (e: IllegalArgumentException) {
        return false
    }
}

fun AccountManager.removeOnAccountsUpdatedListenerSafe(listener: OnAccountsUpdateListener): Boolean {
    try {
        this.removeOnAccountsUpdatedListener(listener)
        return true
    } catch (e: IllegalStateException) {
        return false
    } catch (e: IllegalArgumentException) {
        return false
    }
}


fun AccountManager.removeAccount(account: Account, activity: Activity? = null, handler: Handler? = null): Promise<Bundle, Exception> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) task {
        val future = API22.removeAccount(this, account, activity, null, handler)
        return@task future.result
    } else task {
        @Suppress("DEPRECATION")
        val future = this.removeAccount(account, null, handler)
        return@task Bundle {
            this[AccountManager.KEY_BOOLEAN_RESULT] = future.result
        }
    }
}

fun AccountManager.findAccount(byKey: UserKey): Account? {
    return ownedAccounts.find { byKey == it.getAccountKey(this) }
}

fun AccountManager.getDetails(key: UserKey, getCredentials: Boolean): AccountDetails? {
    val account = findAccount(key) ?: return null
    return getDetails(account, getCredentials)
}

fun AccountManager.getDetails(account: Account, getCredentials: Boolean): AccountDetails {
    val details = AccountDetails()
    details.key = account.getAccountKey(this)
    details.account = account
    details.color = account.getColor(this)
    details.position = account.getPosition(this)
    details.activated = account.isActivated(this)
    details.type = account.getAccountType(this)
    details.credentials_type = account.getCredentialsType(this)
    details.user = account.getAccountUser(this)
    details.user.color = details.color

    details.extras = account.getAccountExtras(this)

    if (getCredentials) {
        details.credentials = account.getCredentials(this)
    }
    return details
}

fun AccountManager.getDetailsOrThrow(key: UserKey, getCredentials: Boolean): AccountDetails {
    return getDetails(key, getCredentials) ?: throw AccountNotFoundException()
}

fun AccountManager.findMatchingDetailsOrThrow(key: UserKey): AccountDetails {
    return AccountUtils.getAllAccountDetails(this, ownedAccounts, true).firstOrNull {
        if (it.key == key) {
            return@firstOrNull true
        } else if (it.user.account_key == key) {
            return@firstOrNull true
        }
        return@firstOrNull false
    } ?: throw AccountNotFoundException()
}

val AccountManager.ownedAccounts: Array<Account> get() = getAccountsByType(ACCOUNT_TYPE)

private object API22 {
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

private object API21 {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal fun renameAccount(am: AccountManager, account: Account,
            newName: String,
            callback: AccountManagerCallback<Account>?,
            handler: Handler?): AccountManagerFuture<Account> {
        return am.renameAccount(account, newName, callback, handler)
    }
}