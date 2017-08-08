/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.extension.model.api.mastodon

import android.net.Uri
import android.text.Editable
import android.text.Spanned
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.mastodon.model.Status
import org.mariotaku.twidere.extension.model.addFilterFlag
import org.mariotaku.twidere.extension.model.api.spanItems
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.text.AcctMentionSpan
import org.mariotaku.twidere.text.HashtagSpan
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.HtmlSpanBuilder
import org.mariotaku.twidere.util.emoji.EmojioneTranslator

fun Status.toParcelable(details: AccountDetails): ParcelableStatus {
    return toParcelable(details.key).apply {
        account_color = details.color
    }
}
fun Status.toParcelable(accountKey: UserKey): ParcelableStatus {
    val result = ParcelableStatus()
    applyTo(accountKey, result)
    return result
}

fun Status.applyTo(accountKey: UserKey, result: ParcelableStatus) {
    val extras = ParcelableStatus.Extras()
    result.account_key = accountKey
    result.id = id
    result.sort_id = sortId
    result.timestamp = createdAt?.time ?: 0

    val retweetedStatus = reblog
    result.is_retweet = retweetedStatus != null
    result.retweeted = isReblogged
    val status: Status
    if (retweetedStatus != null) {
        status = retweetedStatus
        val retweetAccount = account
        result.retweet_id = retweetedStatus.id
        result.retweet_timestamp = retweetedStatus.createdAt?.time ?: 0
        result.retweeted_by_user_key = retweetAccount.getKey(accountKey.host)
        result.retweeted_by_user_name = retweetAccount.name
        result.retweeted_by_user_screen_name = retweetAccount.username
        result.retweeted_by_user_profile_image = retweetAccount.avatar

        extras.retweeted_external_url = retweetedStatus.url

        if (retweetedStatus.isSensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBLY_SENSITIVE)
        }
    } else {
        status = this
        if (status.isSensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBLY_SENSITIVE)
        }
    }

    result.reply_count = -1
    result.retweet_count = status.reblogsCount
    result.favorite_count = status.favouritesCount

    result.in_reply_to_status_id = status.inReplyToId
    result.in_reply_to_user_key = status.inReplyToAccountId?.let { UserKey(it, null) }

    val account = status.account
    result.user_key = account.getKey(accountKey.host)
    result.user_name = account.name
    result.user_screen_name = account.username
    result.user_profile_image_url = account.avatar
    result.user_is_protected = account.isLocked
    // Mastodon has HTML formatted content text
    val html = HtmlSpanBuilder.fromHtml(status.content, status.content, MastodonSpanProcessor)
    result.text_unescaped = html?.toString()
    result.text_plain = result.text_unescaped
    result.spans = html?.spanItems
    result.media = status.mediaAttachments?.mapToArray { it.toParcelable() }
    result.source = status.application?.sourceHtml
    result.is_favorite = status.isFavourited
    result.is_possibly_sensitive = status.isSensitive
    result.mentions = status.mentions?.mapToArray { it.toParcelable(accountKey) }

    extras.display_text_range = calculateDisplayTextRange(result.text_unescaped, result.spans,
            result.media)
    extras.summary_text = status.spoilerText?.let(EmojioneTranslator::translate)
    extras.visibility = status.visibility
    extras.external_url = status.url

    // Try to complete mastodon `in_reply_to` info
    val inReplyToMention = result.mentions?.firstOrNull {
        it.key.id == result.in_reply_to_user_key?.id
    }
    if (inReplyToMention != null) {
        result.in_reply_to_name = inReplyToMention.name
        result.in_reply_to_screen_name = inReplyToMention.screen_name
    }
    if (result.media.isNotNullOrEmpty()) {
        result.addFilterFlag(ParcelableStatus.FilterFlags.HAS_MEDIA)
    }

    result.extras = extras
}

private fun calculateDisplayTextRange(text: String, spans: Array<SpanItem>?, media: Array<ParcelableMedia>?): IntArray? {
    if (spans == null || media == null) return null
    val lastMatch = spans.lastOrNull { span -> media.any { span.link == it.page_url } } ?: return null
    if (lastMatch.end < text.length) return null
    return intArrayOf(0, lastMatch.start)
}

object MastodonSpanProcessor : HtmlSpanBuilder.SpanProcessor {

    override fun appendText(text: Editable, buffer: CharArray, start: Int, len: Int,
            info: HtmlSpanBuilder.TagInfo?): Boolean {
        val unescaped = HtmlEscapeHelper.unescape(String(buffer, start, len))
        text.append(EmojioneTranslator.translate(unescaped))
        return true
    }

    override fun applySpan(text: Editable, start: Int, end: Int, info: HtmlSpanBuilder.TagInfo): Boolean {
        val clsAttr = info.getAttribute("class")?.split(" ") ?: emptyList()
        when {
            "tag" in clsAttr || info.isTag(text) -> {
                text.setSpan(HashtagSpan(text.substring(start, end)), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return true
            }
            "mention" in clsAttr -> {
                val hrefAttr = info.getAttribute("href") ?: return false
                if (text[start] != '@') return false
                text.setSpan(AcctMentionSpan(text.substring(start + 1, end), Uri.parse(hrefAttr).host),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return true
            }
        }
        return false
    }

    fun HtmlSpanBuilder.TagInfo.isTag(text: CharSequence): Boolean {
        return start > 0 && text[start - 1] == '#'
    }
}
