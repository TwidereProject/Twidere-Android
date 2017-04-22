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

import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.mastodon.model.Status
import org.mariotaku.twidere.extension.model.api.spanItems
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableStatusUtils.addFilterFlag
import org.mariotaku.twidere.util.HtmlSpanBuilder

fun Status.toParcelable(details: AccountDetails): ParcelableStatus {
    return toParcelable(details.key).apply {
        account_color = details.color
    }
}

fun Status.toParcelable(accountKey: UserKey): ParcelableStatus {
    val result = ParcelableStatus()
    val extras = ParcelableStatus.Extras()
    result.account_key = accountKey
    result.id = id
    result.sort_id = sortId
    result.timestamp = createdAt?.time ?: 0

    extras.summary_text = spoilerText
    extras.visibility = visibility
    extras.external_url = url

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
        result.retweeted_by_user_name = retweetAccount.displayName
        result.retweeted_by_user_screen_name = retweetAccount.username
        result.retweeted_by_user_profile_image = retweetAccount.avatar

        extras.retweeted_external_url = retweetedStatus.url

        if (retweetedStatus.isSensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBILITY_SENSITIVE)
        }
    } else {
        status = this
        if (status.isSensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBILITY_SENSITIVE)
        }
    }

    result.reply_count = -1
    result.retweet_count = status.reblogsCount
    result.favorite_count = status.favouritesCount

    result.in_reply_to_status_id = status.inReplyToId
    result.in_reply_to_user_key = status.inReplyToAccountId?.let { UserKey(it, accountKey.host) }

    val account = status.account
    result.user_key = account.getKey(accountKey.host)
    result.user_name = account.displayName
    result.user_screen_name = account.username
    result.user_profile_image_url = account.avatar
    result.user_is_protected = account.isLocked
    // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
    // We should treat this as an html
    val html = HtmlSpanBuilder.fromHtml(status.content, status.content)
    result.text_unescaped = html?.toString()
    result.text_plain = result.text_unescaped
    result.spans = html?.spanItems
    result.media = status.mediaAttachments?.mapToArray { it.toParcelable() }
    result.source = status.application?.sourceHtml
    result.is_favorite = status.isFavourited
    result.is_possibly_sensitive = status.isSensitive
    result.mentions = status.mentions?.mapToArray { it.toParcelable(accountKey) }

    extras.display_text_range = calculateDisplayTextRange(result.spans, result.media)

    result.extras = extras
    return result
}

private fun calculateDisplayTextRange(spans: Array<SpanItem>?, media: Array<ParcelableMedia>?): IntArray? {
    if (spans == null || media == null) return null
    val lastMatch = spans.lastOrNull { span -> media.any { span.link == it.page_url } } ?: return null
    return intArrayOf(0, lastMatch.start)
}