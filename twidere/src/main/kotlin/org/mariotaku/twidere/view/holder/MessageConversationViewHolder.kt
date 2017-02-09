package org.mariotaku.twidere.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.list_item_message_entry.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationsAdapter
import org.mariotaku.twidere.extension.model.getConversationName
import org.mariotaku.twidere.extension.model.getSummaryText
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessageConversationViewHolder(itemView: View, val adapter: MessagesConversationsAdapter) : RecyclerView.ViewHolder(itemView) {

    private val content by lazy { itemView.content }
    private val time by lazy { itemView.time }
    private val name by lazy { itemView.name }
    private val text by lazy { itemView.text }
    private val profileImage by lazy { itemView.profileImage }

    init {
        setup()
    }

    fun display(conversation: ParcelableMessageConversation) {
        if (adapter.drawAccountColors) {
            content.drawEnd(conversation.account_color)
        }else {
            content.drawEnd()
        }
        val (name, secondaryName) = conversation.getConversationName(itemView.context)
        this.time.time = conversation.timestamp
        this.name.name = name
        this.name.screenName = secondaryName
        this.name.updateText(adapter.bidiFormatter)
        this.text.text = conversation.getSummaryText(itemView.context)
        if (conversation.conversation_type == ConversationType.ONE_TO_ONE) {
            val user = conversation.user
            if (user != null) {
                adapter.mediaLoader.displayProfileImage(profileImage, user)
            } else {
                adapter.mediaLoader.cancelDisplayTask(profileImage)
                // TODO display default profile image
            }
        } else {
            adapter.mediaLoader.cancelDisplayTask(profileImage)
            // TODO display default profile image
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

