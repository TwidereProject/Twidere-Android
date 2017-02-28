package org.mariotaku.twidere.extension.model

import android.content.Context
import com.twitter.Validator
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
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
        extraRequestParams: Map<String, String>? = MicroBlogAPIFactory.getExtraParams(type,
                includeEntities, includeRetweets),
        cls: Class<T>
): T {
    return credentials.newMicroBlogInstance(context, type, extraRequestParams, cls)
}

val AccountDetails.isOAuth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH

val AccountDetails.mediaSizeLimit: UpdateStatusTask.SizeLimit
    get() = when (type) {
        AccountType.TWITTER -> {
            val imageLimit = AccountExtras.ImageLimit.ofSize(2048, 1536)
            val videoLimit = AccountExtras.VideoLimit.twitterDefault()
            UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        else -> UpdateStatusTask.SizeLimit(AccountExtras.ImageLimit(), AccountExtras.VideoLimit.unsupported())
    }
/**
 * Text limit when composing a status, 0 for no limit
 */
val AccountDetails.textLimit: Int get() {
    if (type == null) {
        return Validator.MAX_TWEET_LENGTH
    }
    when (type) {
        AccountType.STATUSNET -> {
            val extras = this.extras as? StatusNetAccountExtras
            if (extras != null) {
                return extras.textLimit
            }
        }
    }
    return Validator.MAX_TWEET_LENGTH
}

val Array<AccountDetails>.textLimit: Int
    get() {
        var limit = -1
        forEach { details ->
            val currentLimit = details.textLimit
            if (currentLimit != 0) {
                if (limit <= 0) {
                    limit = currentLimit
                } else {
                    limit = Math.min(limit, currentLimit)
                }
            }
        }
        return limit
    }