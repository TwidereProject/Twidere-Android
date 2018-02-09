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
import com.twitter.Validator
import java.text.Normalizer

object TwitterValidator : Validator() {

    const val maxWeightedTweetLength: Int = 280

    const val defaultWeight: Int = 200

    var ranges: Array<WeightRange> = arrayOf(
            WeightRange(0, 4351, 100),
            WeightRange(8192, 8205, 100),
            WeightRange(8208, 8223, 100),
            WeightRange(8242, 8247, 100)
    )

    private val extractor = Extractor()

    override fun getTweetLength(text: String): Int {
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

    override fun isValidTweet(text: String?): Boolean {
        if (text == null || text.isEmpty()) {
            return false
        }

        for (c in text.toCharArray()) {
            if (c == '\uFFFE' || c == '\uFEFF' ||   // BOM
                    c == '\uFFFF' ||                     // Special
                    c in '\u202A'..'\u202E') {  // Direction change
                return false
            }
        }

        return getTweetLength(text) <= maxWeightedTweetLength
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
