package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.view.holder.MessageConversationViewHolder

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessagesConversationsAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context),
        IItemCountsAdapter {
    override val itemCounts: ItemCounts = ItemCounts(1)

    var conversations: List<ParcelableMessageConversation>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var drawAccountColors: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: MessageConversationClickListener? = null

    override fun getItemCount(): Int {
        itemCounts[0] = conversations?.size ?: 0
        return itemCounts.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val conversation = getConversation(position)!!
                (holder as MessageConversationViewHolder).display(conversation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(MessageConversationViewHolder.layoutResource, parent, false)
        return MessageConversationViewHolder(itemView, this)
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM_TYPE_MESSAGE_ENTRY
    }

    fun getConversation(position: Int): ParcelableMessageConversation? {
        return conversations?.get(position - itemCounts.getItemStartPosition(0))
    }

    interface MessageConversationClickListener {
        fun onProfileImageClick(position: Int)
        fun onConversationClick(position: Int)

    }

    companion object {
        const val ITEM_TYPE_MESSAGE_ENTRY = 1

    }

}
