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
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.thenApply
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey


private val ACCOUNT_USER_DATA_KEYS = arrayOf(ACCOUNT_USER_DATA_KEY, ACCOUNT_USER_DATA_TYPE,
        ACCOUNT_USER_DATA_CREDS_TYPE, ACCOUNT_USER_DATA_ACTIVATED, ACCOUNT_USER_DATA_USER,
        ACCOUNT_USER_DATA_EXTRAS, ACCOUNT_USER_DATA_COLOR, ACCOUNT_USER_DATA_POSITION, ACCOUNT_USER_DATA_TEST)

fun AccountManager.hasPermission(): Boolean {
    try {
        ownedAccounts
    } catch (e: SecurityException) {
        return false
    }
    return true
}

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
        for (key in ACCOUNT_USER_DATA_KEYS) {
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

fun AccountManager.findAccount(byScreenName: String): Account? {
    return ownedAccounts.find { byScreenName == it.getAccountUser(this).screen_name }
}

fun AccountManager.getDetails(key: UserKey, getCredentials: Boolean): AccountDetails? {
    val account = findAccount(key) ?: return null
    return getDetails(account, getCredentials)
}

fun AccountManager.getDetails(account: Account, getCredentials: Boolean): AccountDetails? {
    val details = AccountDetails()
    try {
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
    } catch (e: Exception) {
        return null
    }
    return details
}

fun AccountManager.getDetailsOrThrow(account: Account, getCredentials: Boolean): AccountDetails {
    return getDetails(account, getCredentials) ?: throw AccountNotFoundException()
}

fun AccountManager.getDetailsOrThrow(key: UserKey, getCredentials: Boolean): AccountDetails {
    return getDetails(key, getCredentials) ?: throw AccountNotFoundException()
}

fun AccountManager.findMatchingDetailsOrThrow(key: UserKey): AccountDetails {
    return this.getAllDetails(ownedAccounts, true).firstOrNull {
        if (it.key == key) {
            return@firstOrNull true
        } else if (it.user.account_key == key) {
            return@firstOrNull true
        }
        return@firstOrNull false
    } ?: throw AccountNotFoundException()
}


fun AccountManager.getAllDetails(accounts: Array<Account>, getCredentials: Boolean): Array<AccountDetails> {
    return accounts.mapToArray { getDetailsOrThrow(it, getCredentials) }
}

fun AccountManager.getAllDetails(accountKeys: Array<UserKey>, getCredentials: Boolean): Array<AccountDetails?> {
    return accountKeys.mapToArray { getDetails(it, getCredentials) }
}

fun AccountManager.getAllDetails(getCredentials: Boolean): Array<AccountDetails> {
    return ownedAccounts.map { getDetailsOrThrow(it, getCredentials) }.sorted().toTypedArray()
}

fun isOfficial(context: Context, accountKey: UserKey): Boolean {
    val am = AccountManager.get(context)
    val account = am.findAccount(accountKey) ?: return false
    return account.isOfficial(am, context)
}

val AccountManager.ownedAccounts: Array<Account> get() = getAccountsByType(ACCOUNT_TYPE)

private object API22 {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun renameAccount(am: AccountManager, account: Account,
            newName: String,
            callback: AccountManagerCallback<Account>?,
            handler: Handler?): AccountManagerFuture<Account> {
        return am.renameAccount(account, newName, callback, handler)
    }
}