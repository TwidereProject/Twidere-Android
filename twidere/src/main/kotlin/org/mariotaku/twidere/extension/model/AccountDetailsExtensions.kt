package org.mariotaku.twidere.extension.model

import android.content.Context
import com.twitter.Validator
import org.mariotaku.microblog.library.twitter.annotation.MediaCategory
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.MastodonAccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.InternalTwitterContentUtils

fun AccountDetails.isOfficial(context: Context): Boolean {
    val extra = this.extras
    if (extra is TwitterAccountExtras) {
        return extra.isOfficialCredentials
    }
    val credentials = this.credentials
    if (credentials is OAuthCredentials) {
        return InternalTwitterContentUtils.isOfficialKey(context,
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

fun <T> AccountDetails.newMicroBlogInstance(context: Context, cls: Class<T>): T {
    return credentials.newMicroBlogInstance(context, type, cls)
}

val AccountDetails.isOAuth: Boolean
    get() = credentials_type == Credentials.Type.OAUTH || credentials_type == Credentials.Type.XAUTH

fun AccountDetails.getMediaSizeLimit(@MediaCategory mediaCategory: String? = null): UpdateStatusTask.SizeLimit? {
    when (type) {
        AccountType.TWITTER -> {
            val imageLimit = AccountExtras.ImageLimit.twitterDefault(mediaCategory)
            val videoLimit = AccountExtras.VideoLimit.twitterDefault()
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        AccountType.FANFOU -> {
            val imageLimit = AccountExtras.ImageLimit.ofSize(5 * 1024 * 1024)
            val videoLimit = AccountExtras.VideoLimit.unsupported()
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        AccountType.STATUSNET -> {
            val extras = extras as? StatusNetAccountExtras ?: return null
            val imageLimit = AccountExtras.ImageLimit().apply {
                maxSizeSync = extras.uploadLimit
                maxSizeAsync = extras.uploadLimit
            }
            val videoLimit = AccountExtras.VideoLimit().apply {
                maxSizeSync = extras.uploadLimit
                maxSizeAsync = extras.uploadLimit
            }
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        else -> return null
    }
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
        AccountType.MASTODON -> {
            val extras = this.extras as? MastodonAccountExtras
            if (extras != null) {
                return extras.statusTextLimit
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


val AccountDetails.isStreamingSupported: Boolean
    get() = type == AccountType.TWITTER