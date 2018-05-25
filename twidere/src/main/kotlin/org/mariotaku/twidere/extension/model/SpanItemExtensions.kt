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

package org.mariotaku.twidere.extension.model

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.Spanned
import android.text.style.URLSpan
import org.mariotaku.twidere.model.CustomEmoji
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.text.AcctMentionSpan
import org.mariotaku.twidere.text.HashtagSpan
import org.mariotaku.twidere.text.ZeroWidthSpan
import org.mariotaku.twidere.text.style.CustomEmojiSpan

val SpanItem.length: Int get() = end - start

@SuppressLint("SwitchIntDef")
fun Array<SpanItem>.applyTo(spannable: Spannable, emojis: Map<String, CustomEmoji>?, offset: Int = 0,
        length: Int = spannable.length - offset) {
    val stopIndex = 0 + length
    forEach { span ->
        val start = span.start + offset
        val end = (span.end + offset).coerceAtMost(stopIndex)
        if (end <= start) return@forEach
        when (span.type) {
            SpanItem.SpanType.HIDE -> {
                spannable.setSpan(ZeroWidthSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            SpanItem.SpanType.ACCT_MENTION -> {
                spannable.setSpan(AcctMentionSpan(span.link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            SpanItem.SpanType.HASHTAG -> {
                spannable.setSpan(HashtagSpan(span.link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            SpanItem.SpanType.EMOJI -> {
                val shortCode = span.link ?: return@forEach
                val emoji = emojis?.get(shortCode) ?: return@forEach
                spannable.setSpan(CustomEmojiSpan(emoji.url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                spannable.setSpan(URLSpan(span.link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
