package org.mariotaku.twidere.extension.model

import android.accounts.Account
import android.accounts.AccountManager
import android.support.annotation.ColorInt
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.ktextension.toInt
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.toHexColor


fun android.accounts.Account.getCredentials(am: android.accounts.AccountManager): org.mariotaku.twidere.model.account.cred.Credentials {
    val authToken = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE) ?: run {
        throw IllegalStateException("AuthToken is null for ${this}")
    }
    return org.mariotaku.twidere.extension.model.parseCredentials(authToken, getCredentialsType(am))
}

@org.mariotaku.twidere.model.account.cred.Credentials.Type
fun android.accounts.Account.getCredentialsType(am: android.accounts.AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: org.mariotaku.twidere.model.account.cred.Credentials.Type.OAUTH
}

fun android.accounts.Account.getAccountKey(am: android.accounts.AccountManager): org.mariotaku.twidere.model.UserKey {
    val accountKeyString = am.getUserData(this, ACCOUNT_USER_DATA_KEY) ?: run {
        throw IllegalStateException("UserKey is null for ${this}")
    }
    return org.mariotaku.twidere.model.UserKey.valueOf(accountKeyString)
}

fun android.accounts.Account.setAccountKey(am: android.accounts.AccountManager, accountKey: org.mariotaku.twidere.model.UserKey) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, accountKey.toString())
}

fun android.accounts.Account.getAccountUser(am: android.accounts.AccountManager): org.mariotaku.twidere.model.ParcelableUser {
    val user = com.bluelinelabs.logansquare.LoganSquare.parse(am.getUserData(this, ACCOUNT_USER_DATA_USER), org.mariotaku.twidere.model.ParcelableUser::class.java)
    user.is_cache = true
    return user
}

fun android.accounts.Account.setAccountUser(am: android.accounts.AccountManager, user: org.mariotaku.twidere.model.ParcelableUser) {
    am.setUserData(this, ACCOUNT_USER_DATA_USER, com.bluelinelabs.logansquare.LoganSquare.serialize(user))
}

@android.support.annotation.ColorInt
fun android.accounts.Account.getColor(am: android.accounts.AccountManager): Int {
    return org.mariotaku.twidere.util.ParseUtils.parseColor(am.getUserData(this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun android.accounts.Account.getPosition(am: android.accounts.AccountManager): Int {
    return am.getUserData(this, ACCOUNT_USER_DATA_POSITION).toInt(-1)
}

fun android.accounts.Account.getAccountExtras(am: android.accounts.AccountManager): org.mariotaku.twidere.model.account.AccountExtras? {
    val json = am.getUserData(this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    when (getAccountType(am)) {
        org.mariotaku.twidere.annotation.AccountType.TWITTER -> {
            return com.bluelinelabs.logansquare.LoganSquare.parse(json, org.mariotaku.twidere.model.account.TwitterAccountExtras::class.java)
        }
        org.mariotaku.twidere.annotation.AccountType.STATUSNET -> {
            return com.bluelinelabs.logansquare.LoganSquare.parse(json, org.mariotaku.twidere.model.account.StatusNetAccountExtras::class.java)
        }
    }
    return null
}

@org.mariotaku.twidere.annotation.AccountType
fun android.accounts.Account.getAccountType(am: android.accounts.AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_TYPE) ?: org.mariotaku.twidere.annotation.AccountType.TWITTER
}

fun android.accounts.Account.isActivated(am: android.accounts.AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_ACTIVATED).orEmpty().toBoolean()
}

fun android.accounts.Account.setActivated(am: android.accounts.AccountManager, activated: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, activated.toString())
}

fun android.accounts.Account.setColor(am: android.accounts.AccountManager, color: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, org.mariotaku.twidere.util.toHexColor(color))
}

fun android.accounts.Account.setPosition(am: android.accounts.AccountManager, position: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_POSITION, position.toString())
}


private fun parseCredentials(authToken: String, @org.mariotaku.twidere.model.account.cred.Credentials.Type authType: String): org.mariotaku.twidere.model.account.cred.Credentials {
    when (authType) {
        org.mariotaku.twidere.model.account.cred.Credentials.Type.OAUTH, org.mariotaku.twidere.model.account.cred.Credentials.Type.XAUTH -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, org.mariotaku.twidere.model.account.cred.OAuthCredentials::class.java)
        org.mariotaku.twidere.model.account.cred.Credentials.Type.BASIC -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, org.mariotaku.twidere.model.account.cred.BasicCredentials::class.java)
        org.mariotaku.twidere.model.account.cred.Credentials.Type.EMPTY -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, org.mariotaku.twidere.model.account.cred.EmptyCredentials::class.java)
    }
    throw UnsupportedOperationException()
}