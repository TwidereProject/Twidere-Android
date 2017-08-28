package org.mariotaku.twidere.util

import android.accounts.Account
import android.accounts.AccountManager
import android.database.sqlite.SQLiteDatabase
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.queryReference
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AuthTypeInt
import org.mariotaku.twidere.extension.model.component1
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts

/**
 * Migrate legacy credentials to system account framework
 * Created by mariotaku on 2016/12/3.
 */
@Suppress("deprecation")
fun migrateAccounts(am: AccountManager, db: SQLiteDatabase) {
    db.queryReference(Accounts.TABLE_NAME, Accounts.COLUMNS, null, null,
            null, null, null).use { (cur) ->
        if (cur == null) return
        val indices = ObjectCursor.indicesFrom(cur, ParcelableCredentials::class.java)
        cur.moveToFirst()
        while (!cur.isAfterLast) {
            val credentials = indices.newObject(cur)
            val account = Account(credentials.account_name, ACCOUNT_TYPE)
            // Don't add UserData in this method, see http://stackoverflow.com/a/29776224/859190
            am.addAccountExplicitly(account, null, null)
            am.setUserData(account, ACCOUNT_USER_DATA_KEY, credentials.account_key.toString())
            am.setUserData(account, ACCOUNT_USER_DATA_TYPE, credentials.account_type)
            am.setUserData(account, ACCOUNT_USER_DATA_ACTIVATED, credentials.is_activated.toString())
            am.setUserData(account, ACCOUNT_USER_DATA_CREDS_TYPE, credentials.getCredentialsType())
            am.setUserData(account, ACCOUNT_USER_DATA_COLOR, toHexColor(credentials.color, format = HexColorFormat.RGB))
            am.setUserData(account, ACCOUNT_USER_DATA_POSITION, credentials.sort_position)
            am.setUserData(account, ACCOUNT_USER_DATA_USER, JsonSerializer.serialize(credentials.account_user ?: run {
                val user = ParcelableUser()
                user.account_key = credentials.account_key
                user.key = credentials.account_key
                user.name = credentials.name
                user.screen_name = credentials.screen_name
                user.color = credentials.color
                user.profile_banner_url = credentials.profile_banner_url
                user.profile_image_url = credentials.profile_image_url
                return@run user
            }))
            am.setUserData(account, ACCOUNT_USER_DATA_EXTRAS, credentials.account_extras)
            am.setAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE, JsonSerializer.serialize(credentials.toCredentials()))
            cur.moveToNext()
        }
    }
}

@Suppress("deprecation")
private fun ParcelableCredentials.toCredentials(): Credentials {

    fun ParcelableCredentials.applyCommonProperties(credentials: Credentials) {
        credentials.api_url_format = api_url_format
        credentials.no_version_suffix = no_version_suffix
    }

    fun ParcelableCredentials.toOAuthCredentials(): OAuthCredentials {
        val result = OAuthCredentials()
        applyCommonProperties(result)
        result.consumer_key = consumer_key
        result.consumer_secret = consumer_secret
        result.access_token = oauth_token
        result.access_token_secret = oauth_token_secret
        result.same_oauth_signing_url = same_oauth_signing_url
        return result
    }

    fun ParcelableCredentials.toBasicCredentials(): BasicCredentials {
        val result = BasicCredentials()
        applyCommonProperties(result)
        result.username = basic_auth_username
        result.password = basic_auth_password
        return result
    }

    fun ParcelableCredentials.toEmptyCredentials(): EmptyCredentials {
        val result = EmptyCredentials()
        applyCommonProperties(result)
        return result
    }

    return when (auth_type) {
        AuthTypeInt.OAUTH, AuthTypeInt.XAUTH -> toOAuthCredentials()
        AuthTypeInt.BASIC -> toBasicCredentials()
        AuthTypeInt.TWIP_O_MODE -> toEmptyCredentials()
        AuthTypeInt.OAUTH2 -> throw UnsupportedOperationException("OAuth 2 credentials are not supported")
        else -> throw UnsupportedOperationException()
    }

}

@Credentials.Type
@Suppress("deprecation")
private fun ParcelableCredentials.getCredentialsType(): String {
    return AccountUtils.getCredentialsType(auth_type)
}

@Suppress("deprecation")
private val ParcelableCredentials.account_name: String
    get() = generateAccountName(screen_name, account_key.host)

fun generateAccountName(screenName: String, accountHost: String?): String {
    return UserKey(screenName, accountHost).toString()
}