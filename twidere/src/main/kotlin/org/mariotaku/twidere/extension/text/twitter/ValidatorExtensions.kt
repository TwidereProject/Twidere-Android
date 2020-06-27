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
import com.twitter.twittertext.Validator
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2017/3/31.
 */


fun Validator.getTweetLength(text: String, ignoreMentions: Boolean, inReplyTo: ParcelableStatus?,
        accountKey: UserKey? = inReplyTo?.account_key): Int {
    if (!ignoreMentions || inReplyTo == null || accountKey == null) {
        return getTweetLength(text)
    }

    val (_, replyText, _, _, _) = InternalExtractor.extractReplyTextAndMentions(text, inReplyTo,
            accountKey)
    return getTweetLength(replyText)
}

private object InternalExtractor : Extractor()