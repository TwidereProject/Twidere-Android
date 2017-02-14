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

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.list_item_message_entry.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter
import org.mariotaku.twidere.extension.model.getConversationName
import org.mariotaku.twidere.extension.model.getSummaryText
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessageEntryViewHolder(itemView: View, val adapter: MessagesEntriesAdapter) : RecyclerView.ViewHolder(itemView) {

    private val content by lazy { itemView.content }
    private val time by lazy { itemView.time }
    private val name by lazy { itemView.name }
    private val text by lazy { itemView.text }
    private val profileImage by lazy { itemView.profileImage }
    private val stateIndicator by lazy { itemView.stateIndicator }

    init {
        setup()
    }

    fun display(conversation: ParcelableMessageConversation) {
        if (adapter.drawAccountColors) {
            content.drawEnd(conversation.account_color)
        } else {
            content.drawEnd()
        }
        val (name, secondaryName) = conversation.getConversationName(itemView.context,
                adapter.userColorNameManager, adapter.nameFirst)
        this.time.time = conversation.timestamp
        this.name.name = name
        this.name.screenName = secondaryName
        this.name.updateText(adapter.bidiFormatter)
        this.text.text = conversation.getSummaryText(itemView.context, adapter.userColorNameManager,
                adapter.nameFirst)
        if (conversation.is_outgoing) {
            stateIndicator.visibility = View.VISIBLE
            stateIndicator.setImageResource(R.drawable.ic_activity_action_reply)
        } else {
            stateIndicator.visibility = View.GONE
        }
        if (conversation.conversation_type == ConversationType.ONE_TO_ONE) {
            val user = conversation.user
            if (user != null) {
                adapter.mediaLoader.displayProfileImage(profileImage, user)
            } else {
                adapter.mediaLoader.displayProfileImage(profileImage, null)
                // TODO display default profile image
            }
        } else {
            adapter.mediaLoader.displayGroupConversationAvatar(profileImage, conversation.conversation_avatar)
        }
    }

    private fun setup() {
        val textSize = adapter.textSize
        name.setPrimaryTextSize(textSize * 1.1f)
        name.setSecondaryTextSize(textSize)
        text.textSize = textSize
        time.textSize = textSize * 0.85f

        itemView.setOnClickListener {
            adapter.listener?.onConversationClick(layoutPosition)
        }
        profileImage.setOnClickListener {
            adapter.listener?.onProfileImageClick(layoutPosition)
        }
    }

    companion object {
        const val layoutResource = R.layout.list_item_message_entry
    }

}

