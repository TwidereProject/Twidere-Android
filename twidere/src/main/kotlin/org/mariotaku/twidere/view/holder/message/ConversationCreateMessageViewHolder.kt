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

import android.view.View
import kotlinx.android.synthetic.main.list_item_message_conversation_notice.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.view.FixedTextView

class ConversationCreateMessageViewHolder(itemView: View, adapter: MessagesConversationAdapter) : AbsMessageViewHolder(itemView, adapter) {
    override val messageContent: View = itemView
    override val date: FixedTextView by lazy { itemView.date }

    private val text by lazy { itemView.text }

    override fun display(message: ParcelableMessage, showDate: Boolean) {
        super.display(message, showDate)
        text.setText(R.string.message_conversation_created)
    }

    override fun setMessageContentGravity(view: View, outgoing: Boolean) {
        // No-op
    }

    companion object {
        const val layoutResource = R.layout.list_item_message_conversation_notice
    }

}