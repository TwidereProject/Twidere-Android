package org.mariotaku.twidere.model

import android.accounts.Account
import android.accounts.AccountManager
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.twidere.annotation.AuthTypeInt
import org.mariotaku.twidere.extension.getAccountExtras
import org.mariotaku.twidere.extension.getCredentials
import org.mariotaku.twidere.extension.getCredentialsType
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials

/**
 * Created by mariotaku on 2016/12/3.
 */

fun Account.toParcelableCredentials(am: AccountManager): ParcelableCredentials {
    val credentials = ParcelableCredentials()
    writeParcelableCredentials(am, credentials)
    return credentials
}

internal fun Account.writeParcelableCredentials(am: AccountManager, credentials: ParcelableCredentials) {
    writeParcelableAccount(am, credentials)
    credentials.auth_type = when (getCredentialsType(am)) {
        Credentials.Type.OAUTH -> AuthTypeInt.OAUTH
        Credentials.Type.XAUTH -> AuthTypeInt.XAUTH
        Credentials.Type.BASIC -> AuthTypeInt.BASIC
        Credentials.Type.EMPTY -> AuthTypeInt.TWIP_O_MODE
        Credentials.Type.OAUTH2 -> AuthTypeInt.OAUTH2
        else -> AuthTypeInt.OAUTH
    }
    val extras = getAccountExtras(am)
    if (extras != null) {
        credentials.account_extras = LoganSquare.serialize(extras)
    }

    val creds = getCredentials(am)
    credentials.api_url_format = creds.api_url_format
    credentials.no_version_suffix = creds.no_version_suffix
    when (creds) {
        is OAuthCredentials -> {
            credentials.same_oauth_signing_url = creds.same_oauth_signing_url
            credentials.oauth_token = creds.access_token
            credentials.oauth_token_secret = creds.access_token_secret
            credentials.consumer_key = creds.consumer_key
            credentials.consumer_secret = creds.consumer_secret
        }
        is BasicCredentials -> {
            credentials.basic_auth_username = creds.username
            credentials.basic_auth_password = creds.password
        }
    }
}
