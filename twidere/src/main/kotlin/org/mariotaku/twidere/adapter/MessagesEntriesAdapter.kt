package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.message.MessageEntryViewHolder

/**
 * Created by mariotaku on 2017/2/9.
 */

class MessagesEntriesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IItemCountsAdapter {
    override val itemCounts: ItemCounts = ItemCounts(2)

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

    val nameFirst: Boolean = preferences[nameFirstKey]

    var listener: MessageConversationClickListener? = null

    override fun getItemCount(): Int {
        itemCounts[0] = conversations?.size ?: 0
        itemCounts[1] = if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.END != 0L) 1 else 0
        return itemCounts.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val conversation = getConversation(position)!!
                (holder as MessageEntryViewHolder).display(conversation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val itemView = inflater.inflate(MessageEntryViewHolder.layoutResource, parent, false)
                return MessageEntryViewHolder(itemView, this)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val itemView = inflater.inflate(LoadIndicatorViewHolder.layoutResource, parent, false)
                return LoadIndicatorViewHolder(itemView)

            }
        }
        throw UnsupportedOperationException()
    }

    override fun getItemViewType(position: Int): Int {
        when (itemCounts.getItemCountIndex(position)) {
            0 -> return ITEM_TYPE_MESSAGE_ENTRY
            1 -> return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        throw UnsupportedOperationException()
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
        const val ITEM_VIEW_TYPE_LOAD_INDICATOR = 2
    }


}
