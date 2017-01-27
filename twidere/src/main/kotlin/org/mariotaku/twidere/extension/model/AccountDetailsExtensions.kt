package org.mariotaku.twidere.extension.model

import android.content.Context
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.TwitterContentUtils

fun AccountDetails.isOfficial(context: Context): Boolean {
    val extra = this.extras
    if (extra is TwitterAccountExtras) {
        return extra.isOfficialCredentials
    }
    val credentials = this.credentials
    if (credentials is OAuthCredentials) {
        return TwitterContentUtils.isOfficialKey(context,
                credentials.consumer_key, credentials.consumer_secret)
    }
    return false
}

val AccountExtras.official: Boolean
    get() {
        if (this is TwitterAccountExtras) {
            return isOfficialCredentials
        }
        return false
    }


@JvmOverloads
fun <T> AccountDetails.newMicroBlogInstance(
        context: Context,
        includeEntities: Boolean = true,
        includeRetweets: Boolean = true,
        extraRequestParams: Map<String, String>? = MicroBlogAPIFactory.getExtraParams(type, includeEntities, includeRetweets),
        cls: Class<T>
): T {
    return credentials.newMicroBlogInstance(context, type == AccountType.TWITTER, extraRequestParams, cls)
}

val AccountDetails.is_oauth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH

val AccountDetails.size_limit: UpdateStatusTask.SizeLimit
    get() = when (type) {
        AccountType.TWITTER -> {
            val imageLimit = AccountExtras.ImageLimit.ofSize(2048, 1536)
            val videoLimit = AccountExtras.VideoLimit.twitterDefault()
            UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        else -> UpdateStatusTask.SizeLimit(AccountExtras.ImageLimit(), AccountExtras.VideoLimit.unsupported())
    }
