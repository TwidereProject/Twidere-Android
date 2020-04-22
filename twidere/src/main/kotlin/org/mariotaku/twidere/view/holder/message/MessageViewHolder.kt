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
import android.view.View
import kotlinx.android.synthetic.main.list_item_message_conversation_text.view.*
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.messagebubbleview.library.MessageBubbleView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.FixedTextView
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessageViewHolder(itemView: View, adapter: MessagesConversationAdapter) : AbsMessageViewHolder(itemView, adapter) {

    override val date: FixedTextView by lazy { itemView.date }
    override val messageContent: View by lazy { itemView.messageContent }
    override val profileImage: ProfileImageView by lazy { itemView.profileImage }
    override val nameTime: FixedTextView by lazy { itemView.nameTime }

    private val text by lazy { itemView.text }
    private val mediaPreview by lazy { itemView.mediaPreview }
    private val messageBubble by lazy { itemView.messageBubble }

    override fun setup() {
        super.setup()
        val textSize = adapter.textSize
        text.textSize = textSize
        mediaPreview.style = adapter.mediaPreviewStyle

        messageBubble.setOnLongClickListener {
            val listener = adapter.listener ?: return@setOnLongClickListener false
            return@setOnLongClickListener listener.onMessageLongClick(layoutPosition, this)
        }
    }

    override fun display(message: ParcelableMessage, showDate: Boolean) {
        super.display(message, showDate)

        messageBubble.bubbleColor = if (message.is_outgoing) {
            adapter.bubbleColorOutgoing
        } else {
            adapter.bubbleColorIncoming
        }
        messageBubble.setOutgoing(message.is_outgoing)

        val bubbleColor = messageBubble.bubbleColor
        if (bubbleColor != null) {
            text.setTextColor(ThemeUtils.getColorDependent(bubbleColor.defaultColor))
        }

        // Loop through text and spans to found non-space char count
        val hideText = run {

            fun String.nonSpaceCount(range: IntRange): Int {
                if (range.isEmpty()) return 0
                return range.count { !this[it].isWhitespace() }
            }

            val text = message.text_unescaped

            var nonSpaceCount = 0
            var curPos = 0
            message.spans?.forEach { span ->
                nonSpaceCount += text.nonSpaceCount(curPos until span.start)
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


        text.spannable = SpannableStringBuilder.valueOf(message.text_unescaped).apply {
            message.spans?.applyTo(this)
            adapter.linkify.applyAllLinks(this, message.account_key, layoutPosition.toLong(),
                    false, adapter.linkHighlightingStyle, true)
        }

        text.visibility = if (hideText || text.empty) {
            View.GONE
        } else {
            View.VISIBLE
        }

        if (message.media.isNullOrEmpty()) {
            mediaPreview.visibility = View.GONE
        } else {
            mediaPreview.visibility = View.VISIBLE
            mediaPreview.displayMedia(adapter.requestManager, message.media, message.account_key,
                    extraId = layoutPosition.toLong(), withCredentials = true,
                    mediaClickListener = adapter.mediaClickListener)
        }

    }

    companion object {
        const val layoutResource = R.layout.list_item_message_conversation_text

        fun MessageBubbleView.setOutgoing(outgoing: Boolean) {
            caretPosition = if (outgoing) {
                MessageBubbleView.TOP or MessageBubbleView.END
            } else {
                MessageBubbleView.BOTTOM or MessageBubbleView.START
            }
        }
    }
}
