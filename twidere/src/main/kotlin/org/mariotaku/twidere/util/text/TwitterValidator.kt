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

package org.mariotaku.twidere.util.text

import com.twitter.Extractor
import org.mariotaku.twidere.extension.text.twitter.extractReplyTextAndMentions
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import java.text.Normalizer

object TwitterValidator {

    const val shortUrlLength = 23
    const val shortUrlLengthHttps = 23

    const val maxWeightedTweetLength: Int = 280

    const val defaultWeight: Int = 200

    var ranges: Array<WeightRange> = arrayOf(
            WeightRange(0, 4351, 100),
            WeightRange(8192, 8205, 100),
            WeightRange(8208, 8223, 100),
            WeightRange(8242, 8247, 100)
    )

    private val extractor = Extractor()

    fun getTweetLength(text: String): Int {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)
        var weightedLength = 0
        val inputLength = normalized.length

        var charOffset = 0
        while (charOffset < inputLength) {
            val codePoint = Character.codePointAt(normalized, charOffset)
            weightedLength += weightForCodePoint(codePoint)
            charOffset += Character.charCount(codePoint)
        }

        var length = weightedLength / 100

        for (urlEntity in extractor.extractURLsWithIndices(normalized)) {
            length += urlEntity.start - urlEntity.end
            length += if (urlEntity.value.toLowerCase().startsWith("https://")) shortUrlLengthHttps else shortUrlLength
        }

        return length
    }

    fun getTweetLength(text: String, ignoreMentions: Boolean, inReplyTo: ParcelableStatus?,
            accountKey: UserKey? = inReplyTo?.account_key): Int {
        if (!ignoreMentions || inReplyTo == null || accountKey == null) {
            return getTweetLength(text)
        }

        val (_, replyText, _, _, _) = extractor.extractReplyTextAndMentions(text, inReplyTo,
                accountKey)
        return getTweetLength(replyText)
    }

    fun isValidTweet(text: String): Boolean {
        // BOM: uFFFE, uFEFF
        // Special : uFFFF
        // Direction change [u202A, u202E]
        return text.none { c ->
            c == '\uFFFE' || c == '\uFEFF' || c == '\uFFFF' || c in '\u202A'..'\u202E'
        } && getTweetLength(text) in 0..maxWeightedTweetLength
    }

    private fun weightForCodePoint(codePoint: Int): Int {
        val range = ranges.find { codePoint in it } ?: return defaultWeight
        return range.weight
    }

    data class WeightRange(val start: Int, val end: Int, val weight: Int) {
        operator fun contains(codePoint: Int): Boolean {
            return codePoint in start..end
        }
    }

}
