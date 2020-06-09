package org.mariotaku.twidere.extension.model

import android.accounts.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import android.text.TextUtils
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.ktextension.toIntOr
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.*
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.AccountUtils.ACCOUNT_USER_DATA_KEYS
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.model.AccountDetailsUtils
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


fun Account.getCredentials(am: AccountManager): Credentials {
    val authToken = AccountDataQueue.peekAuthToken(am, this, ACCOUNT_AUTH_TOKEN_TYPE) ?: run {
        for (i in 0 until 5) {
            Thread.sleep(50L)
            val token = AccountDataQueue.peekAuthToken(am, this, ACCOUNT_AUTH_TOKEN_TYPE)
            if (token != null) return@run token
        }
        return@run null
    } ?: throw NullPointerException("AuthToken is null for $this")
    return parseCredentials(authToken, getCredentialsType(am))
}

@Credentials.Type
fun Account.getCredentialsType(am: AccountManager): String {
    return AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: Credentials.Type.OAUTH
}

fun Account.getAccountKey(am: AccountManager): UserKey {
    return UserKey.valueOf(am.getNonNullUserData(this, ACCOUNT_USER_DATA_KEY))
}

fun Account.setAccountKey(am: AccountManager, accountKey: UserKey) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, accountKey.toString())
}

fun Account.getAccountUser(am: AccountManager): ParcelableUser {
    val user = JsonSerializer.parse(am.getNonNullUserData(this, ACCOUNT_USER_DATA_USER), ParcelableUser::class.java)
    user.is_cache = true
    return user
}

fun Account.setAccountUser(am: AccountManager, user: ParcelableUser) {
    am.setUserData(this, ACCOUNT_USER_DATA_USER, JsonSerializer.serialize(user))
}

@androidx.annotation.ColorInt
fun Account.getColor(am: AccountManager): Int {
    return ParseUtils.parseColor(AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun Account.getPosition(am: AccountManager): Int {
    return AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_POSITION).toIntOr(-1)
}

fun Account.getAccountExtras(am: AccountManager): AccountExtras? {
    val json = AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    return AccountDetailsUtils.parseAccountExtras(json, getAccountType(am))
}

@AccountType
fun Account.getAccountType(am: AccountManager): String {
    return AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_TYPE) ?: AccountType.TWITTER
}

fun Account.isActivated(am: AccountManager): Boolean {
    return AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_ACTIVATED)?.toBoolean() ?: true
}

fun Account.setActivated(am: AccountManager, activated: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, activated.toString())
}

fun Account.setColor(am: AccountManager, color: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(color, format = HexColorFormat.RGB))
}

fun Account.setPosition(am: AccountManager, position: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_POSITION, position.toString())
}

fun Account.isOfficial(am: AccountManager, context: Context): Boolean {
    val extras = getAccountExtras(am)
    if (extras is TwitterAccountExtras) {
        return extras.isOfficialCredentials
    }
    val credentials = getCredentials(am)
    if (credentials is OAuthCredentials) {
        return InternalTwitterContentUtils.isOfficialKey(context, credentials.consumer_key,
                credentials.consumer_secret)
    }
    return false
}

fun AccountManager.hasInvalidAccount(): Boolean {
    val accounts = AccountUtils.getAccounts(this)
    if (accounts.isEmpty()) return false
    return accounts.any { !isAccountValid(it) }
}

fun AccountManager.isAccountValid(account: Account): Boolean {
    if (TextUtils.isEmpty(AccountDataQueue.peekAuthToken(this, account, ACCOUNT_AUTH_TOKEN_TYPE))) return false
    if (TextUtils.isEmpty(AccountDataQueue.getUserData(this, account, ACCOUNT_USER_DATA_KEY))) return false
    if (TextUtils.isEmpty(AccountDataQueue.getUserData(this, account, ACCOUNT_USER_DATA_USER))) return false
    return true
}

fun AccountManager.renameTwidereAccount(oldAccount: Account, newName: String): AccountManagerFuture<Account>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return AccountExtensionFunctionsL.renameAccount(this, oldAccount, newName, null, null)
    }
    val newAccount = Account(newName, oldAccount.type)
    if (addAccountExplicitly(newAccount, null, null)) {
        for (key in ACCOUNT_USER_DATA_KEYS) {
            setUserData(newAccount, key, getUserData(oldAccount, key))
        }
        setAuthToken(newAccount, ACCOUNT_AUTH_TOKEN_TYPE,
                peekAuthToken(oldAccount, ACCOUNT_AUTH_TOKEN_TYPE))
        @Suppress("DEPRECATION")
        val booleanFuture = removeAccount(oldAccount, null, null)
        return AccountFuture(newAccount, booleanFuture)
    }
    return null
}

private fun AccountManager.getNonNullUserData(account: Account, key: String): String {
    return AccountDataQueue.getUserData(this, account, key) ?: run {
        // Rare case, assume account manager service is not running
        for (i in 0 until 5) {
            Thread.sleep(50L)
            val data = AccountDataQueue.getUserData(this, account, key)
            if (data != null) return data
        }
        throw NullPointerException("NonNull userData $key is null for $account")
    }
}

private fun parseCredentials(authToken: String, @Credentials.Type authType: String) = when (authType) {
    Credentials.Type.OAUTH, Credentials.Type.XAUTH -> JsonSerializer.parse(authToken, OAuthCredentials::class.java)
    Credentials.Type.BASIC -> JsonSerializer.parse(authToken, BasicCredentials::class.java)
    Credentials.Type.EMPTY -> JsonSerializer.parse(authToken, EmptyCredentials::class.java)
    Credentials.Type.OAUTH2 -> JsonSerializer.parse(authToken, OAuth2Credentials::class.java)
    else -> throw UnsupportedOperationException()
}

internal object AccountDataQueue {
    private val executor = Executors.newSingleThreadExecutor()

    fun getUserData(manager: AccountManager, account: Account, key: String): String? {
        val callable = Callable {
            manager.getUserData(account, key)
        }
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            return callable.call()
        }
        val future = executor.submit(callable)
        return try {
            future.get(1, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            manager.getUserData(account, key)
        }
    }

    fun peekAuthToken(manager: AccountManager, account: Account, authTokenType: String): String? {
        val callable = Callable {
            manager.peekAuthToken(account, authTokenType)
        }
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            return callable.call()
        }
        val future = executor.submit(callable)
        return try {
            future.get(1, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            manager.peekAuthToken(account, authTokenType)
        }
    }
}

private object AccountExtensionFunctionsL {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal fun renameAccount(am: AccountManager, account: Account,
            newName: String,
            callback: AccountManagerCallback<Account>?,
            handler: Handler?): AccountManagerFuture<Account> {
        return am.renameAccount(account, newName, callback, handler)
    }
}

private class AccountFuture internal constructor(
        private val account: Account,
        private val booleanFuture: AccountManagerFuture<Boolean>
) : AccountManagerFuture<Account> {

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return booleanFuture.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return booleanFuture.isCancelled
    }

    override fun isDone(): Boolean {
        return booleanFuture.isDone
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(): Account? {
        if (booleanFuture.result) {
            return account
        }
        return null
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(timeout: Long, unit: TimeUnit): Account? {
        if (booleanFuture.getResult(timeout, unit)) {
            return account
        }
        return null
    }
}