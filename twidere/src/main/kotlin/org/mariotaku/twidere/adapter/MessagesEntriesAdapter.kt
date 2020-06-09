package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
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
            updateItemCounts()
            notifyDataSetChanged()
        }

    var drawAccountColors: Boolean = false
        set(value) {
            field = value
            updateItemCounts()
            notifyDataSetChanged()
        }

    val nameFirst: Boolean = preferences[nameFirstKey]

    var listener: MessageConversationClickListener? = null

    private val reuseEntry = ParcelableMessageConversation()

    override fun getItemCount(): Int {
        return itemCounts.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val conversation = getConversation(position, reuse = true)
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
        return when (val countIndex = itemCounts.getItemCountIndex(position)) {
            0 -> ITEM_TYPE_MESSAGE_ENTRY
            1 -> ITEM_VIEW_TYPE_LOAD_INDICATOR
            else -> throw UnsupportedCountIndexException(countIndex, position)
        }
    }

    private fun updateItemCounts() {
        itemCounts[0] = conversations?.size ?: 0
        itemCounts[1] = if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.END != 0L) 1 else 0
    }

    fun getConversation(position: Int, reuse: Boolean = false): ParcelableMessageConversation {
        val conversations = this.conversations!!
        val dataPosition = position - itemCounts.getItemStartPosition(0)
        if (reuse && conversations is ObjectCursor) {
            return conversations.setInto(dataPosition, reuseEntry)
        }
        return conversations[dataPosition]
    }

    interface MessageConversationClickListener {
        fun onProfileImageClick(position: Int)
        fun onConversationClick(position: Int)
        fun onConversationLongClick(position: Int): Boolean

    }

    companion object {
        const val ITEM_TYPE_MESSAGE_ENTRY = 1
        const val ITEM_VIEW_TYPE_LOAD_INDICATOR = 2
    }


}
