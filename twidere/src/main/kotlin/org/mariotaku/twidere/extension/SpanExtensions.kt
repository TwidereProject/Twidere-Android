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

import android.text.Spanned
import android.text.style.URLSpan
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.text.AcctMentionSpan
import org.mariotaku.twidere.text.HashtagSpan
import org.mariotaku.twidere.text.placeholder.CustomEmojiShortCodeSpan

fun URLSpan.toSpanItem(spanned: Spanned) = createSpanItem(spanned) { item ->
    item.link = url
    item.type = when (this) {
        is AcctMentionSpan -> SpanItem.SpanType.ACCT_MENTION
        is HashtagSpan -> SpanItem.SpanType.HASHTAG
        else -> SpanItem.SpanType.LINK
    }
}

fun CustomEmojiShortCodeSpan.toSpanItem(spanned: Spanned) = createSpanItem(spanned) { item ->
    item.link = shortCode
    item.type = SpanItem.SpanType.EMOJI
}

private inline fun Any.createSpanItem(spanned: Spanned, setup: (item: SpanItem) -> Unit): SpanItem {
    val spanItem = SpanItem()
    setup(spanItem)
    spanItem.start = spanned.getSpanStart(this)
    spanItem.end = spanned.getSpanEnd(this)
    return spanItem
}