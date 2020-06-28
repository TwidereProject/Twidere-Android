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

package org.mariotaku.twidere.extension.model.api

import android.text.Spanned
import android.text.style.URLSpan
import org.apache.commons.text.translate.EntityArrays
import org.apache.commons.text.translate.LookupTranslator
import org.mariotaku.commons.text.CodePointArray
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.twitter.model.EntitySupport
import org.mariotaku.microblog.library.twitter.model.ExtendedEntitySupport
import org.mariotaku.microblog.library.twitter.model.MediaEntity
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.exception.MalformedResponseException
import org.mariotaku.twidere.extension.model.addFilterFlag
import org.mariotaku.twidere.extension.model.toParcelable
import org.mariotaku.twidere.extension.model.updateContentFilterInfo
import org.mariotaku.twidere.extension.model.updateFilterInfo
import org.mariotaku.twidere.extension.toSpanItem
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.text.AcctMentionSpan
import org.mariotaku.twidere.text.HashtagSpan
import org.mariotaku.twidere.util.HtmlBuilder
import org.mariotaku.twidere.util.HtmlSpanBuilder
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils.getMediaUrl
import org.mariotaku.twidere.util.InternalTwitterContentUtils.getStartEndForEntity
import kotlin.math.max

fun Status.toParcelable(details: AccountDetails, profileImageSize: String = "normal",
        updateFilterInfoAction: (Status, ParcelableStatus) -> Unit = ::updateFilterInfoDefault): ParcelableStatus {
    return toParcelable(details.key, details.type, profileImageSize, updateFilterInfoAction).apply {
        account_color = details.color
    }
}

fun Status.toParcelable(accountKey: UserKey, accountType: String, profileImageSize: String = "normal",
        updateFilterInfoAction: (Status, ParcelableStatus) -> Unit = ::updateFilterInfoDefault): ParcelableStatus {
    val result = ParcelableStatus()
    applyTo(accountKey, accountType, profileImageSize, result, updateFilterInfoAction)
    return result
}

fun Status.applyTo(accountKey: UserKey, accountType: String, profileImageSize: String = "normal",
        result: ParcelableStatus,
        updateFilterInfoAction: (Status, ParcelableStatus) -> Unit = ::updateFilterInfoDefault) {
    val extras = ParcelableStatus.Extras()
    result.account_key = accountKey
    result.id = id
    result.sort_id = sortId
    result.timestamp = createdAt?.time ?: 0

    extras.external_url = inferredExternalUrl
    extras.entities_url = entities?.urls?.map { it.expandedUrl }?.let {
        if (isQuoteStatus) {
            it.take(max(0, it.count() - 1))
        } else {
            it
        }
    }?.toTypedArray()
    extras.support_entities = entities != null
    extras.statusnet_conversation_id = statusnetConversationId
    extras.conversation_id = conversationId
    result.is_pinned_status = user.pinnedTweetIds?.contains(id) ?: false

    val retweetedStatus = retweetedStatus
    result.is_retweet = isRetweet
    result.retweeted = wasRetweeted()
    val status: Status
    if (retweetedStatus != null) {
        status = retweetedStatus
        val retweetUser = user
        result.retweet_id = retweetedStatus.id
        result.retweet_timestamp = retweetedStatus.createdAt?.time ?: 0
        result.retweeted_by_user_key = retweetUser.key
        result.retweeted_by_user_name = retweetUser.name
        result.retweeted_by_user_screen_name = retweetUser.screenName
        result.retweeted_by_user_profile_image = retweetUser.getProfileImageOfSize(profileImageSize)

        extras.retweeted_external_url = retweetedStatus.inferredExternalUrl

        if (retweetUser.isBlocking == true) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.BLOCKING_USER)
        }
        if (retweetUser.isBlockedBy == true) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.BLOCKED_BY_USER)
        }
        if (retweetedStatus.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBLY_SENSITIVE)
        }
    } else {
        status = this
        if (status.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBLY_SENSITIVE)
        }
    }

    val quoted = status.quotedStatus
    result.is_quote = status.isQuoteStatus
    result.quoted_id = status.quotedStatusId
    if (quoted != null) {
        val quotedUser = quoted.user ?: throw MalformedResponseException()
        result.quoted_id = quoted.id
        extras.quoted_external_url = quoted.inferredExternalUrl

        val quotedText = quoted.htmlText
        // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
        // We should treat this as an html
        if (quotedText.isHtml) {
            val html = HtmlSpanBuilder.fromHtml(quotedText, quoted.extendedText)
            result.quoted_text_unescaped = html?.toString()
            result.quoted_text_plain = result.quoted_text_unescaped
            result.quoted_spans = html?.spanItems
        } else {
            val textWithIndices = quoted.formattedTextWithIndices()
            result.quoted_text_plain = quotedText.twitterUnescaped()
            result.quoted_text_unescaped = textWithIndices.text
            result.quoted_spans = textWithIndices.spans
            extras.quoted_display_text_range = textWithIndices.range
        }

        result.quoted_timestamp = quoted.createdAt.time
        result.quoted_source = quoted.source
        result.quoted_media = ParcelableMediaUtils.fromStatus(quoted, accountKey, accountType)

        result.quoted_user_key = quotedUser.key
        result.quoted_user_name = quotedUser.name
        result.quoted_user_screen_name = quotedUser.screenName
        result.quoted_user_profile_image = quotedUser.getProfileImageOfSize(profileImageSize)
        result.quoted_user_is_protected = quotedUser.isProtected
        result.quoted_user_is_verified = quotedUser.isVerified

        if (quoted.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBLY_SENSITIVE)
        }
    } else if (status.isQuoteStatus) {
        result.addFilterFlag(ParcelableStatus.FilterFlags.QUOTE_NOT_AVAILABLE)
    }

    result.reply_count = status.replyCount
    result.retweet_count = status.retweetCount
    result.favorite_count = status.favoriteCount

    result.in_reply_to_name = status.inReplyToName
    result.in_reply_to_screen_name = status.inReplyToScreenName
    result.in_reply_to_status_id = status.inReplyToStatusId
    result.in_reply_to_user_key = status.getInReplyToUserKey(accountKey)

    val user = status.user ?: throw MalformedResponseException()
    result.user_key = user.key
    result.user_name = user.name
    result.user_screen_name = user.screenName
    result.user_profile_image_url = user.getProfileImageOfSize(profileImageSize)
    result.user_is_protected = user.isProtected
    result.user_is_verified = user.isVerified
    result.user_is_following = user.isFollowing == true
    extras.user_statusnet_profile_url = user.statusnetProfileUrl
    extras.user_profile_image_url_fallback = user.profileImageUrlHttps ?: user.profileImageUrl
    val text = status.htmlText
    // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
    // We should treat this as an html
    if (text.isHtml) {
        val html = HtmlSpanBuilder.fromHtml(text, status.extendedText)
        result.text_unescaped = html?.toString()
        result.text_plain = result.text_unescaped
        result.spans = html?.spanItems
    } else {
        val textWithIndices = status.formattedTextWithIndices()
        result.text_unescaped = textWithIndices.text
        result.text_plain = text.twitterUnescaped()
        result.spans = textWithIndices.spans
        extras.display_text_range = textWithIndices.range
    }

    result.media = ParcelableMediaUtils.fromStatus(status, accountKey, accountType)
    result.source = status.source
    result.location = status.parcelableLocation
    result.is_favorite = status.isFavorited
    if (result.account_key.maybeEquals(result.retweeted_by_user_key)) {
        result.my_retweet_id = result.id
    } else {
        result.my_retweet_id = status.currentUserRetweet
    }
    result.is_possibly_sensitive = status.isPossiblySensitive
    result.mentions = status.userMentionEntities?.mapToArray { it.toParcelable(user.host) }
    result.card = status.card?.toParcelable(accountKey, accountType)
    result.card_name = result.card?.name
    result.place_full_name = status.placeFullName
    result.lang = status.lang
    result.extras = extras

    if (result.media.isNotNullOrEmpty() || result.quoted_media.isNotNullOrEmpty()) {
        result.addFilterFlag(ParcelableStatus.FilterFlags.HAS_MEDIA)
    }

    updateFilterInfoAction(this, result)
}


fun Status.formattedTextWithIndices(): StatusTextWithIndices {
    val source = CodePointArray(this.fullText ?: this.text!!)
    val builder = HtmlBuilder(source,
        throwExceptions = false,
        sourceIsEscaped = true,
        shouldReEscape = false
    )
    builder.addEntities(this)
    val textWithIndices = StatusTextWithIndices()
    val (text, spans) = builder.buildWithIndices()
    textWithIndices.text = text
    textWithIndices.spans = spans

    // Display text range
    val range = displayTextRange?.takeIf { it.size == 2 }
    if (range != null) {
        textWithIndices.range = intArrayOf(
                source.findResultRangeLength(spans, 0, range[0]),
                text.length - source.findResultRangeLength(spans, range[1], source.length())
        )
    }
    return textWithIndices
}

fun CodePointArray.findResultRangeLength(spans: Array<SpanItem>, origStart: Int, origEnd: Int): Int {
    val findResult = findByOrigRange(spans, origStart, origEnd)
    if (findResult.isEmpty()) {
        return charCount(origStart, origEnd)
    }
    val first = findResult.first()
    val last = findResult.last()
    if (first.orig_start == -1 || last.orig_end == -1)
        return charCount(origStart, origEnd)
    return charCount(origStart, first.orig_start) + (last.end - first.start) + charCount(first.orig_end, origEnd)
}

fun HtmlBuilder.addEntities(entities: EntitySupport) {
    // Format media.
    var mediaEntities: Array<MediaEntity>? = null
    if (entities is ExtendedEntitySupport) {
        mediaEntities = entities.extendedMediaEntities
    }
    if (mediaEntities == null) {
        mediaEntities = entities.mediaEntities
    }
    val startEnd = IntArray(2)
    mediaEntities?.forEach { mediaEntity ->
        val mediaUrl = getMediaUrl(mediaEntity)
        if (mediaUrl != null && getStartEndForEntity(mediaEntity, startEnd)) {
            addLink(mediaEntity.expandedUrl, mediaEntity.displayUrl,
                    startEnd[0], startEnd[1], false)
        }
    }
    entities.urlEntities?.forEach { urlEntity ->
        val expandedUrl = urlEntity.expandedUrl
        if (expandedUrl != null && getStartEndForEntity(urlEntity, startEnd)) {
            addLink(expandedUrl, urlEntity.displayUrl, startEnd[0],
                    startEnd[1], false)
        }
    }
}


fun updateFilterInfoDefault(status: Status, result: ParcelableStatus) {
    result.updateFilterInfo(setOf(
            status.userDescriptionUnescaped,
            status.userUrlExpanded,
            status.userLocation,
            status.retweetedStatus?.userDescriptionUnescaped,
            status.retweetedStatus?.userLocation,
            status.retweetedStatus?.userUrlExpanded,
            status.quotedStatus?.userDescriptionUnescaped,
            status.quotedStatus?.userLocation,
            status.quotedStatus?.userUrlExpanded
    ))
}

/**
 * Ignores status user info
 */
fun updateFilterInfoForUserTimeline(status: Status, result: ParcelableStatus) {
    result.updateContentFilterInfo()

    if (result.is_retweet) {
        result.filter_users = setOf(result.user_key, result.quoted_user_key).filterNotNull().toTypedArray()
        result.filter_names = setOf(result.user_name, result.quoted_user_name).filterNotNull().toTypedArray()
        result.filter_descriptions = setOf(
                status.retweetedStatus?.userDescriptionUnescaped,
                status.retweetedStatus?.userUrlExpanded,
                status.retweetedStatus?.userLocation,
                status.quotedStatus?.userDescriptionUnescaped,
                status.quotedStatus?.userLocation,
                status.quotedStatus?.userUrlExpanded
        ).filterNotNull().joinToString("\n")
    } else {
        result.filter_users = setOf(result.quoted_user_key).filterNotNull().toTypedArray()
        result.filter_names = setOf(result.quoted_user_name).filterNotNull().toTypedArray()
        result.filter_descriptions = setOf(
                status.quotedStatus?.userDescriptionUnescaped,
                status.quotedStatus?.userLocation,
                status.quotedStatus?.userUrlExpanded
        ).filterNotNull().joinToString("\n")
    }
}

private fun String.twitterUnescaped(): String {
    return TwitterRawTextTranslator.translate(this)
}

private inline val Status.userDescriptionUnescaped: String?
    get() = user?.let { InternalTwitterContentUtils.formatUserDescription(it)?.first }

private inline val Status.userUrlExpanded: String?
    get() = user?.urlEntities?.firstOrNull()?.expandedUrl

private inline val Status.userLocation: String?
    get() = user?.location

/**
 * @param spans Ordered spans
 * *
 * @param start orig_start
 * *
 * @param end   orig_end
 */
internal fun findByOrigRange(spans: Array<SpanItem>, start: Int, end: Int): List<SpanItem> {
    return spans.filter { it.orig_start >= start && it.orig_end <= end }
}

internal inline val CharSequence.spanItems
    get() = (this as? Spanned)?.let { text ->
        text.getSpans(0, length, URLSpan::class.java).mapToArray {
            val item = it.toSpanItem(text)
            when (it) {
                is AcctMentionSpan -> item.type = SpanItem.SpanType.ACCT_MENTION
                is HashtagSpan -> item.type = SpanItem.SpanType.HASHTAG
            }
            return@mapToArray item
        }
    }

internal inline val String.isHtml get() = contains('<') && contains('>')

private inline val Status.inReplyToName
    get() = userMentionEntities?.firstOrNull {
        inReplyToUserId == it.id
    }?.name ?: attentions?.firstOrNull {
        inReplyToUserId == it.id
    }?.fullName ?: inReplyToScreenName


private inline val Status.placeFullName
    get() = place?.fullName ?: location?.takeIf {
        ParcelableLocation.valueOf(location) == null
    }

private inline val Status.inferredExternalUrl
    get() = externalUrl ?: uri?.let { uri ->
        noticeUriRegex.matchEntire(uri)?.let { result: MatchResult ->
            "https://${result.groups[1]?.value}/notice/${result.groups[3]?.value}"
        }
    }

private val Status.parcelableLocation: ParcelableLocation?
    get() {
        val geoLocation = geoLocation
        if (geoLocation != null) {
            return ParcelableLocationUtils.fromGeoLocation(geoLocation)
        }
        val locationString = location ?: return null
        val location = ParcelableLocation.valueOf(locationString)
        if (location != null) {
            return location
        }
        return null
    }

private fun Status.getInReplyToUserKey(accountKey: UserKey): UserKey? {
    val inReplyToUserId = inReplyToUserId ?: return null
    val entities = userMentionEntities
    if (entities != null) {
        if (entities.any { inReplyToUserId == it.id }) {
            return UserKey(inReplyToUserId, accountKey.host)
        }
    }
    val attentions = attentions
    if (attentions != null) {
        attentions.firstOrNull { inReplyToUserId == it.id }?.let {
            val host = getUserHost(it.ostatusUri, accountKey.host)
            return UserKey(inReplyToUserId, host)
        }
    }
    return UserKey(inReplyToUserId, accountKey.host)
}

private val noticeUriRegex = Regex("tag:([\\w\\d.]+),(\\d{4}-\\d{2}-\\d{2}):noticeId=(\\d+):objectType=(\\w+)")

private object TwitterRawTextTranslator : LookupTranslator(EntityArrays.BASIC_UNESCAPE)

class StatusTextWithIndices {
    var text: String? = null
    var spans: Array<SpanItem>? = null
    var range: IntArray? = null
}
