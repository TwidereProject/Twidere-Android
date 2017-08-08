package org.mariotaku.twidere.extension.model

import org.mariotaku.ktextension.addAllTo
import org.mariotaku.microblog.library.mastodon.annotation.StatusVisibility
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.Utils


inline val ParcelableStatus.originalId: String
    get() = if (is_retweet) (retweet_id ?: id) else id

val ParcelableStatus.media_type: Int
    get() = media?.firstOrNull()?.type ?: 0

val ParcelableStatus.user: ParcelableUser
    get() = ParcelableUser(account_key, user_key, user_name, user_screen_name, user_profile_image_url)

val ParcelableStatus.referencedUsers: Array<ParcelableUser>
    get() {
        val resultList = mutableSetOf(user)
        if (quoted_user_key != null) {
            resultList.add(ParcelableUser(account_key, quoted_user_key, quoted_user_name,
                    quoted_user_screen_name, quoted_user_profile_image))
        }
        if (retweeted_by_user_key != null) {
            resultList.add(ParcelableUser(account_key, retweeted_by_user_key, retweeted_by_user_name,
                    retweeted_by_user_screen_name, retweeted_by_user_profile_image))
        }
        mentions?.forEach { mention ->
            resultList.add(ParcelableUser(account_key, mention.key, mention.name,
                    mention.screen_name, null))
        }
        return resultList.toTypedArray()
    }

val ParcelableStatus.replyMentions: Array<ParcelableUserMention>
    get() {
        val result = ArrayList<ParcelableUserMention>()
        result.add(parcelableUserMention(user_key, user_name, user_screen_name))
        if (is_retweet) retweeted_by_user_key?.let { key ->
            result.add(parcelableUserMention(key, retweeted_by_user_name,
                    retweeted_by_user_screen_name))
        }
        mentions?.addAllTo(result)
        return result.toTypedArray()
    }

inline val ParcelableStatus.user_acct: String get() = if (account_key.host == user_key.host) {
    user_screen_name
} else {
    "$user_screen_name@${user_key.host}"
}

inline val ParcelableStatus.retweeted_by_user_acct: String? get() = if (account_key.host == retweeted_by_user_key?.host) {
    retweeted_by_user_screen_name
} else {
    "$retweeted_by_user_screen_name@${retweeted_by_user_key?.host}"
}

inline val ParcelableStatus.quoted_user_acct: String? get() = if (account_key.host == quoted_user_key?.host) {
    quoted_user_screen_name
} else {
    "$quoted_user_screen_name@${quoted_user_key?.host}"
}

inline val ParcelableStatus.is_my_retweet: Boolean
    get() = Utils.isMyRetweet(account_key, retweeted_by_user_key, my_retweet_id)

inline val ParcelableStatus.can_retweet: Boolean
    get() {
        if (user_key.host == USER_TYPE_FANFOU_COM) return true
        val visibility = extras?.visibility ?: return !user_is_protected
        return when (visibility) {
            StatusVisibility.PRIVATE -> false
            StatusVisibility.DIRECT -> false
            else -> true
        }
    }

fun ParcelableStatus.toSummaryLine(): ParcelableActivity.SummaryLine {
    val result = ParcelableActivity.SummaryLine()
    result.key = user_key
    result.name = user_name
    result.screen_name = user_screen_name
    result.content = text_unescaped
    return result
}

fun ParcelableStatus.extractFanfouHashtags(): List<String> {
    return spans?.filter { span ->
        var link = span.link
        if (link.startsWith("/")) {
            link = "http://fanfou.com$link"
        }
        if (UriUtils.getAuthority(link) != "fanfou.com") {
            return@filter false
        }
        if (span.start <= 0 || span.end > text_unescaped.lastIndex) return@filter false
        if (text_unescaped[span.start - 1] == '#' && text_unescaped[span.end] == '#') {
            return@filter true
        }
        return@filter false
    }?.map { text_unescaped.substring(it.start, it.end) }.orEmpty()
}


fun ParcelableStatus.makeOriginal() {
    if (!is_retweet) return
    id = retweet_id
    is_retweet = false
    retweeted_by_user_key = null
    retweeted_by_user_name = null
    retweeted_by_user_screen_name = null
    retweeted_by_user_profile_image = null
    retweet_timestamp = -1
    retweet_id = null
}

fun ParcelableStatus.addFilterFlag(@ParcelableStatus.FilterFlags flags: Long) {
    filter_flags = filter_flags or flags
}

fun ParcelableStatus.updateExtraInformation(details: AccountDetails) {
    account_color = details.color
}

val ParcelableStatus.quoted: ParcelableStatus?
    get() {
        val obj = ParcelableStatus()
        obj.account_key = account_key
        obj.id = quoted_id ?: return null
        obj.timestamp = quoted_timestamp
        obj.user_key = quoted_user_key ?: return null
        obj.user_name = quoted_user_name ?: return null
        obj.user_screen_name = quoted_user_screen_name ?: return null
        obj.user_profile_image_url = quoted_user_profile_image ?: return null
        obj.user_is_protected = quoted_user_is_protected
        obj.user_is_verified = quoted_user_is_verified
        obj.text_plain = quoted_text_plain
        obj.text_unescaped = quoted_text_unescaped
        obj.source = quoted_source
        obj.spans = quoted_spans
        obj.media = quoted_media
        return obj
    }

private fun parcelableUserMention(key: UserKey, name: String, screenName: String) = ParcelableUserMention().also {
    it.key = key
    it.name = name
    it.screen_name = screenName
}