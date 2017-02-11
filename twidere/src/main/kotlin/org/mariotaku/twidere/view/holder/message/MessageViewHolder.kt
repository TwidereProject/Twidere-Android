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

import android.support.v4.view.GravityCompat
import android.text.format.DateUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.list_item_message_conversation_text.view.*
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.messagebubbleview.library.MessageBubbleView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.ParcelableMessage

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessageViewHolder(itemView: View, adapter: MessagesConversationAdapter) : AbsMessageViewHolder(itemView, adapter) {

    private val date by lazy { itemView.date }
    private val text by lazy { itemView.text }
    private val time by lazy { itemView.time }
    private val mediaPreview by lazy { itemView.mediaPreview }
    private val messageContent by lazy { itemView.messageContent }

    init {
        val textSize = adapter.textSize
        text.textSize = textSize
        time.textSize = textSize * 0.8f
        date.textSize = textSize * 0.9f
    }

    override fun display(message: ParcelableMessage, showDate: Boolean) {
        super.display(message, showDate)
        setOutgoingStatus(messageContent, message.is_outgoing)
        text.text = message.text_unescaped
        if (showDate) {
            date.visibility = View.VISIBLE
            date.text = DateUtils.getRelativeTimeSpanString(message.timestamp, System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE)
        } else {
            date.visibility = View.GONE
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

        fun setOutgoingStatus(view: MessageBubbleView, outgoing: Boolean) {
            view.setCaretPosition(if (outgoing) MessageBubbleView.BOTTOM_END else MessageBubbleView.BOTTOM_START)
            setMessageContentGravity(view, outgoing)
        }

        fun setMessageContentGravity(view: View, outgoing: Boolean) {
            val lp = view.layoutParams
            when (lp) {
                is FrameLayout.LayoutParams -> {
                    lp.gravity = if (outgoing) GravityCompat.END else GravityCompat.START
                }
                is LinearLayout.LayoutParams -> {
                    lp.gravity = if (outgoing) GravityCompat.END else GravityCompat.START
                }
            }
        }
    }
}
