package org.mariotaku.twidere.extension.model

import android.content.Context
import android.text.SpannableStringBuilder
import org.mariotaku.ktextension.addAllTo
import org.mariotaku.ktextension.appendTo
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.annotation.mastodon.StatusVisibility
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.view.ShortTimeView


inline val ParcelableStatus.originalId: String
    get() = if (is_retweet) (retweet_id ?: id) else id

val ParcelableStatus.media_type: Int
    get() = attachment?.media?.firstOrNull()?.type ?: 0

val ParcelableStatus.user: ParcelableUser
    get() = ParcelableUser(account_key, user_key, user_name, user_screen_name, user_profile_image_url)

val ParcelableStatus.referencedUsers: Array<ParcelableUser>
    get() {
        val resultList = mutableSetOf(user)
        val quoted = attachment?.quoted
        if (quoted != null) {
            resultList.add(ParcelableUser(account_key, quoted.user_key, quoted.user_name,
                    quoted.user_screen_name, quoted.user_profile_image))
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

inline val ParcelableStatus.user_acct: String
    get() = if (account_key.host == user_key.host) {
        user_screen_name
    } else {
        "$user_screen_name@${user_key.host}"
    }

inline val ParcelableStatus.retweeted_by_user_acct: String?
    get() = if (account_key.host == retweeted_by_user_key?.host) {
        retweeted_by_user_screen_name
    } else {
        "$retweeted_by_user_screen_name@${retweeted_by_user_key?.host}"
    }

inline val ParcelableStatusAttachment.QuotedStatus.user_acct: String?
    get() = if (account_key?.host == user_key?.host) {
        user_screen_name
    } else {
        "$user_screen_name@${user_key?.host}"
    }

inline val ParcelableStatus.isAccountRetweet: Boolean
    get() = account_key == retweeted_by_user_key || my_retweet_id != null

inline val ParcelableStatus.isAccountStatus: Boolean
    get() = account_key.maybeEquals(user_key)

inline val ParcelableStatus.canRetweet: Boolean
    get() {
        if (user_key.host == USER_TYPE_FANFOU_COM) return true
        val visibility = extras?.visibility ?: return !user_is_protected
        return when (visibility) {
            StatusVisibility.PRIVATE -> false
            StatusVisibility.DIRECT -> false
            else -> true
        }
    }

inline val ParcelableStatus.quoted: ParcelableStatusAttachment.QuotedStatus?
    get() = attachment?.quoted

val ParcelableStatus.retweet_sort_id: Long
    get() {
        if (!is_retweet) return -1
        return retweet_id.toLongOr(timestamp)
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
    retweeted_by_user_key = null
    retweeted_by_user_name = null
    retweeted_by_user_screen_name = null
    retweeted_by_user_profile_image = null
    retweet_timestamp = -1
    retweet_id = null
    is_retweet = false
    sort_id = id.toLongOr(timestamp)
}

fun ParcelableStatus.addFilterFlag(@ParcelableStatus.FilterFlags flags: Long) {
    filter_flags = filter_flags or flags
}

fun ParcelableStatus.updateFilterInfo(descriptions: Collection<String?>?) {
    updateContentFilterInfo()
    filter_users = setOf(user_key, quoted?.user_key, retweeted_by_user_key).filterNotNull().toTypedArray()
    filter_names = setOf(user_name, quoted?.user_name, retweeted_by_user_name).filterNotNull().toTypedArray()
    filter_descriptions = descriptions?.filterNotNull()?.joinToString("\n")
}

fun ParcelableStatus.updateContentFilterInfo() {
    filter_links = generateFilterLinks()
    filter_texts = generateFilterTexts()

    filter_sources = setOf(source?.plainText, quoted?.source?.plainText).filterNotNull().toTypedArray()
}

fun ParcelableStatus.generateFilterTexts(): String {
    val texts = StringBuilder()
    texts.appendNonEmptyLine(text_unescaped)
    texts.appendNonEmptyLine(quoted?.text_unescaped)
    attachment?.media?.forEach { item ->
        texts.appendNonEmptyLine(item.alt_text)
    }
    quoted?.media?.forEach { item ->
        texts.appendNonEmptyLine(item.alt_text)
    }
    return texts.toString()
}

fun ParcelableStatus.generateFilterLinks(): Array<String> {
    val links = mutableSetOf<String>()
    spans?.mapNotNullTo(links) { span ->
        if (span.type != SpanItem.SpanType.LINK) return@mapNotNullTo null
        return@mapNotNullTo span.link
    }
    quoted?.spans?.mapNotNullTo(links) { span ->
        if (span.type != SpanItem.SpanType.LINK) return@mapNotNullTo null
        return@mapNotNullTo span.link
    }
    return links.toTypedArray()
}

fun ParcelableStatus.updateExtraInformation(details: AccountDetails) {
    account_color = details.color
}

fun ParcelableStatus.contentDescription(context: Context, manager: UserColorNameManager,
        displayInReplyTo: Boolean, showAbsoluteTime: Boolean): String {
    val displayName = manager.getDisplayName(this)
    val displayTime = if (is_retweet) retweet_timestamp else timestamp
    val timeLabel = ShortTimeView.getTimeLabel(context, displayTime, showAbsoluteTime)
    when {
        retweet_id != null -> {
            val retweetedBy = manager.getDisplayName(retweeted_by_user_key!!,
                    retweeted_by_user_name, retweeted_by_user_acct!!)
            return context.getString(R.string.content_description_item_status_retweet, retweetedBy, displayName,
                    timeLabel, text_unescaped)
        }
        in_reply_to_status_id != null && in_reply_to_user_key != null && displayInReplyTo -> {
            val inReplyTo = manager.getDisplayName(in_reply_to_user_key!!,
                    in_reply_to_name, in_reply_to_screen_name)
            return context.getString(R.string.content_description_item_status_reply, displayName, inReplyTo,
                    timeLabel, text_unescaped)
        }
        else -> return context.getString(R.string.content_description_item_status, displayName, timeLabel,
                text_unescaped)
    }
}

fun ParcelableStatus.displayInfo(context: Context): ParcelableStatus.Display {
    if (display == null) {
        display = generateDisplayInfo(context)
    }
    return display
}

fun ParcelableStatus.generateDisplayInfo(context: Context): ParcelableStatus.Display {
    val colorNameManager = UserColorNameManager.get(context)
    val displayInReplyTo = true
    val showAbsoluteTime = true

    val info = ParcelableStatus.Display()

    val textWithSummary = SpannableStringBuilder()

    extras?.summary_text?.appendTo(textWithSummary)
    val start = textWithSummary.length
    textWithSummary.append(text_unescaped)
    spans?.applyTo(textWithSummary, extras?.emojis, start)

    info.text = textWithSummary

    info.contentDescription = contentDescription(context, colorNameManager, displayInReplyTo, showAbsoluteTime)
    info.profileImageContentDescription = context.getString(R.string.content_description_open_user_name_profile,
            colorNameManager.getDisplayName(this))
    return info
}

internal inline val String.plainText: String get() = HtmlEscapeHelper.toPlainText(this)

private fun parcelableUserMention(key: UserKey, name: String, screenName: String) = ParcelableUserMention().also {
    it.key = key
    it.name = name
    it.screen_name = screenName
}

private fun StringBuilder.appendNonEmptyLine(line: CharSequence?) {
    if (line.isNullOrEmpty()) return
    append(line)
    append('\n')
}