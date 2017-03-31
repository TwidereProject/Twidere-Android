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

package org.mariotaku.twidere.extension

import com.twitter.Extractor
import com.twitter.Validator
import java.text.Normalizer

/**
 * Created by mariotaku on 2017/3/31.
 */


fun Validator.getTweetLength(text: String, ignoreMentions: Boolean): Int {
    var temp = text
    temp = Normalizer.normalize(temp, Normalizer.Form.NFC)
    var length = temp.codePointCount(0, temp.length)

    if (ignoreMentions) {
        var nextExpectedPos = 0
        run {
            val mentions = InternalExtractor.extractMentionedScreennamesWithIndices(temp)
            mentions.forEachIndexed { index, entity ->
                // Limit to 50 mentions https://dev.twitter.com/overview/api/upcoming-changes-to-tweets
                if (index >= 50) return@run
                if (entity.start != nextExpectedPos) return@run
                nextExpectedPos = (entity.end..temp.indices.endInclusive).firstOrNull {
                    !temp[it].isWhitespace()
                } ?: temp.indices.endInclusive + 1
            }
        }
        length -= temp.codePointCount(0, nextExpectedPos)
    }

    for (urlEntity in InternalExtractor.extractURLsWithIndices(temp)) {
        length += urlEntity.start - urlEntity.end
        length += if (urlEntity.value.startsWith("https://", ignoreCase = true)) shortUrlLengthHttps else shortUrlLength
    }

    return length
}

private object InternalExtractor : Extractor()