package org.mariotaku.twidere.extension.model

import android.content.Context
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.util.TwitterContentUtils

fun AccountDetails.isOfficial(context: Context): Boolean {
    val extra = this.extras
    if (extra is TwitterAccountExtras) {
        return extra.isOfficialCredentials
    }
    val credentials = this.credentials
    if (credentials is OAuthCredentials) {
        return TwitterContentUtils.isOfficialKey(context,
                credentials.consumer_key, credentials.consumer_secret);
    }
    return false
}

val AccountDetails.is_oauth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH