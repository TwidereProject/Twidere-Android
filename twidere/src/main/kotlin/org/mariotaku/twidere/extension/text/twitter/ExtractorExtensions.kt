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

package org.mariotaku.twidere.extension.text.twitter

import com.twitter.twittertext.Extractor
import org.mariotaku.twidere.extension.model.replyMentions
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.UserKey

fun Extractor.extractMentionsAndNonMentionStartIndex(text: String, mentions: Array<ParcelableUserMention>?): MentionsAndNonMentionStartIndex {
    var nextExpectedPos = 0
    val entities = extractMentionedScreennamesWithIndices(text)
    @Suppress("LoopToCallChain")
    for (entity in entities) {
        if (entity.start != nextExpectedPos) break
        // Break at first mention not found in `inReplyTo.mentions`
        if (mentions?.none { entity.value.equals(it.screen_name, ignoreCase = true) } == true) break
        nextExpectedPos = (entity.end..text.indices.last).firstOrNull {
            !text[it].isWhitespace()
        } ?: text.indices.last + 1
    }
    return MentionsAndNonMentionStartIndex(entities, nextExpectedPos)
}

fun Extractor.extractReplyTextAndMentions(text: String, inReplyTo: ParcelableStatus,
        accountKey: UserKey = inReplyTo.account_key): ReplyTextAndMentions {
    // First extract mentions and 'real text' start index
    val (textMentions, index) = extractMentionsAndNonMentionStartIndex(text, inReplyTo.replyMentions)

    val replyMentions = run {
        val mentions = inReplyTo.mentions?.toMutableList() ?: mutableListOf()
        if (inReplyTo.is_retweet) {
            mentions.add(ParcelableUserMention().also {
                it.key = inReplyTo.retweeted_by_user_key
                it.name = inReplyTo.retweeted_by_user_name
                it.screen_name = inReplyTo.retweeted_by_user_screen_name
            })
        }
        return@run mentions
    }

    // Find mentions that `inReplyTo` doesn't have and add to `extraMentions` list

    val extraMentions = textMentions.filter { entity ->
        if (entity.value.equals(inReplyTo.user_screen_name, ignoreCase = true)) {
            return@filter false
        }
        return@filter replyMentions.none { mention ->
            entity.value.equals(mention.screen_name, ignoreCase = true)
        }
    }
    // Find removed mentions from `inReplyTo` and add to `excludedMentions` list
    val excludedMentions = replyMentions.filter { mention ->
        return@filter textMentions.none { entity ->
            entity.value.equals(mention.screen_name, ignoreCase = true)
        }
    }
    // Find reply text contains mention to `inReplyTo.user`
    val mentioningUser = accountKey == inReplyTo.user_key || textMentions.any {
        it.value.equals(inReplyTo.user_screen_name, ignoreCase = true)
    }
    if (!mentioningUser) {
        /*
         * remember to process status without mentioning user of `inReplyTo`
         * then this status should be treated at a mention referring to `inReplyTo`, all other mentions
         * counts.
         */
        return ReplyTextAndMentions(index, text, emptyList(), emptyList(), mentioningUser)
    }
    val overrideText = run {
        val sb = StringBuilder()
        extraMentions.forEach { entity ->
            if (entity.start >= index) return@forEach
            sb.append('@')
            sb.append(entity.value)
            sb.append(' ')
        }
        sb.append(text, index, text.length)
        return@run sb.toString()
    }
    return ReplyTextAndMentions(index, overrideText, extraMentions, excludedMentions, mentioningUser)
}

data class MentionsAndNonMentionStartIndex(val mentions: List<Extractor.Entity>, val index: Int)

data class ReplyTextAndMentions(
        val replyStartIndex: Int,
        val replyText: String,
        val extraMentions: List<Extractor.Entity> = emptyList(),
        val excludedMentions: List<ParcelableUserMention> = emptyList(),
        val replyToOriginalUser: Boolean = false
)