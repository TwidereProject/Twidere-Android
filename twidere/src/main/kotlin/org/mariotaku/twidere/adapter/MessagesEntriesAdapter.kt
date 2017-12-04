package org.mariotaku.twidere.adapter

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapterHelper
import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.adapter.callback.ItemCountsAdapterListUpdateCallback
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.util.paging.DiffCallbacks
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.message.MessageEntryViewHolder

class MessagesEntriesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IItemCountsAdapter {
    override val itemCounts: ItemCounts = ItemCounts(2)

    override var loadMoreIndicatorPosition: Int
        get() = super.loadMoreIndicatorPosition
        set(value) {
            super.loadMoreIndicatorPosition = value
            updateItemCounts()
        }

    var conversations: PagedList<ParcelableMessageConversation>?
        get() = pagedEntriesHelper.currentList
        set(value) {
            pagedEntriesHelper.setList(value)
            if (value == null) {
                itemCounts[0] = 0
            }
        }

    var drawAccountColors: Boolean = false
        set(value) {
            field = value
            updateItemCounts()
            notifyDataSetChanged()
        }

    val nameFirst: Boolean = preferences[nameFirstKey]

    var listener: MessageConversationClickListener? = null

    private var pagedEntriesHelper = PagedListAdapterHelper<ParcelableMessageConversation>(ItemCountsAdapterListUpdateCallback(this, 0),
            ListAdapterConfig.Builder<ParcelableMessageConversation>().setDiffCallback(DiffCallbacks.conversation).build())

    override fun getItemCount(): Int {
        return itemCounts.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val conversation = getConversation(position)
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
        val countIndex = itemCounts.getItemCountIndex(position)
        when (countIndex) {
            0 -> return ITEM_TYPE_MESSAGE_ENTRY
            1 -> return ITEM_VIEW_TYPE_LOAD_INDICATOR
            else -> throw UnsupportedCountIndexException(countIndex, position)
        }
    }

    override fun updateItemCounts() {
        itemCounts[1] = if (LoadMorePosition.END in loadMoreIndicatorPosition) 1 else 0
    }

    fun getConversation(position: Int): ParcelableMessageConversation {
        val dataPosition = position - itemCounts.getItemStartPosition(0)
        return pagedEntriesHelper.getItem(dataPosition)!!
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
