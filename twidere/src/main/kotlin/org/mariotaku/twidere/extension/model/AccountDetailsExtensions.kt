package org.mariotaku.twidere.extension.model

import android.content.Context
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
import org.mariotaku.twidere.util.text.FanfouValidator
import org.mariotaku.twidere.util.text.MastodonValidator
import org.mariotaku.twidere.util.text.TwitterValidator
import kotlin.math.min

fun AccountDetails.isOfficial(context: Context?): Boolean {
    if (context == null) {
        return false
    }
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

val AccountDetails.hasDm: Boolean
    get() = type in arrayOf(AccountType.FANFOU, AccountType.TWITTER)

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
        AccountType.MASTODON -> {
            val imageLimit = AccountExtras.ImageLimit().apply {
                maxSizeSync = 8 * 1024 * 1024
                maxSizeAsync = 8 * 1024 * 1024
                maxHeight = 1280
                maxWidth = 1280
            }
            val videoLimit = AccountExtras.VideoLimit.twitterDefault()
            return UpdateStatusTask.SizeLimit(imageLimit, videoLimit)
        }
        else -> return null
    }
}

/**
 * Text limit when composing a status, 0 for no limit
 */
val AccountDetails.textLimit: Int
    get() = when (type) {
        AccountType.STATUSNET -> {
            (this.extras as? StatusNetAccountExtras)?.textLimit ?: 140
        }
        AccountType.MASTODON -> {
            (this.extras as? MastodonAccountExtras)?.statusTextLimit ?: MastodonValidator.textLimit
        }
        AccountType.FANFOU -> {
            FanfouValidator.textLimit
        }
        AccountType.TWITTER -> {
            TwitterValidator.maxWeightedTweetLength
        }
        else -> 140
    }


val Array<AccountDetails>.textLimit: Int
    get() {
        var limit = -1
        forEach { details ->
            val currentLimit = details.textLimit
            if (currentLimit != 0) {
                limit = if (limit <= 0) {
                    currentLimit
                } else {
                    min(limit, currentLimit)
                }
            }
        }
        return limit
    }
