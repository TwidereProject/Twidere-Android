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

package org.mariotaku.twidere.view.holder.message

import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.View
import kotlinx.android.synthetic.main.list_item_message_conversation_text.view.*
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.messagebubbleview.library.MessageBubbleView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.view.FixedTextView

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessageViewHolder(itemView: View, adapter: MessagesConversationAdapter) : AbsMessageViewHolder(itemView, adapter) {

    override val date: FixedTextView by lazy { itemView.date }
    override val messageContent: MessageBubbleView by lazy { itemView.messageContent }

    private val text by lazy { itemView.text }
    private val time by lazy { itemView.time }
    private val mediaPreview by lazy { itemView.mediaPreview }

    init {
        val textSize = adapter.textSize
        text.textSize = textSize
        time.textSize = textSize * 0.8f
        date.textSize = textSize * 0.9f
        mediaPreview.style = adapter.mediaPreviewStyle
    }

    override fun display(message: ParcelableMessage, showDate: Boolean) {
        super.display(message, showDate)
        messageContent.setOutgoing(message.is_outgoing)

        // Loop through text and spans to found non-space char count
        val hideText = run {

            fun String.nonSpaceCount(range: IntRange): Int {
                if (range.isEmpty()) return 0
                return range.count { !Character.isSpaceChar(this[it]) }
            }

            val text = message.text_unescaped

            var nonSpaceCount = 0
            var curPos = 0
            message.spans?.forEach { span ->
                if (message.media?.firstOrNull { media -> span.link == media.url } != null) {
                    // Skip if span is hidden
                    span.type = SpanItem.SpanType.HIDE
                } else {
                    nonSpaceCount += text.nonSpaceCount(curPos until span.end)
                }
                curPos = span.end
            }
            nonSpaceCount += text.nonSpaceCount(curPos..text.lastIndex)
            return@run nonSpaceCount == 0
        }


        text.text = SpannableStringBuilder.valueOf(message.text_unescaped).apply {
            message.spans?.applyTo(this)
            adapter.linkify.applyAllLinks(this, message.account_key, layoutPosition.toLong(),
                    false, adapter.linkHighlightingStyle, true)
        }

        text.visibility = if (hideText || text.empty) {
            View.GONE
        } else {
            View.VISIBLE
        }

        time.text = DateUtils.formatDateTime(adapter.context, message.timestamp,
                DateUtils.FORMAT_SHOW_TIME)

        if (message.media.isNullOrEmpty()) {
            mediaPreview.visibility = View.GONE
        } else {
            mediaPreview.visibility = View.VISIBLE
            mediaPreview.displayMedia(adapter.mediaLoader, message.media, message.account_key,
                    withCredentials = true)
        }
    }

    companion object {
        const val layoutResource = R.layout.list_item_message_conversation_text

        fun MessageBubbleView.setOutgoing(outgoing: Boolean) {
            setCaretPosition(if (outgoing) MessageBubbleView.BOTTOM_END else MessageBubbleView.BOTTOM_START)
        }
    }
}
