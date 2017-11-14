package org.mariotaku.twidere.extension.model

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toBooleanOr
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.ktextension.toIntOr
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.*
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.model.AccountDetailsUtils


fun Account.getCredentials(am: AccountManager): Credentials {
    val authToken = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE) ?:
            throw NullPointerException("AuthToken is null for ${this}")
    return parseCredentials(authToken, getCredentialsType(am))
}

@Credentials.Type
fun Account.getCredentialsType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: Credentials.Type.OAUTH
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

@android.support.annotation.ColorInt
fun Account.getColor(am: AccountManager): Int {
    return ParseUtils.parseColor(am.getUserData(this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun Account.getPosition(am: AccountManager): Int {
    return am.getUserData(this, ACCOUNT_USER_DATA_POSITION).toIntOr(-1)
}

fun Account.getAccountExtras(am: AccountManager): AccountExtras? {
    val json = am.getUserData(this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    return AccountDetailsUtils.parseAccountExtras(json, getAccountType(am))
}

@AccountType
fun Account.getAccountType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_TYPE) ?: AccountType.TWITTER
}

fun Account.isActivated(am: AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_ACTIVATED).toBooleanOr(true)
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

fun Account.isTest(am: AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_TEST).toBooleanOr(false)
}

fun Account.setTest(am: AccountManager, test: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_TEST, test.toString())
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

private fun AccountManager.getNonNullUserData(account: Account, key: String): String {
    return getUserData(account, key) ?: throw NullPointerException("NonNull userData `$key` is null for $account")
}

private fun parseCredentials(authToken: String, @Credentials.Type authType: String) = when (authType) {
    Credentials.Type.OAUTH, Credentials.Type.XAUTH -> JsonSerializer.parse(authToken, OAuthCredentials::class.java)
    Credentials.Type.BASIC -> JsonSerializer.parse(authToken, BasicCredentials::class.java)
    Credentials.Type.EMPTY -> JsonSerializer.parse(authToken, EmptyCredentials::class.java)
    Credentials.Type.OAUTH2 -> JsonSerializer.parse(authToken, OAuth2Credentials::class.java)
    else -> throw UnsupportedOperationException()
}