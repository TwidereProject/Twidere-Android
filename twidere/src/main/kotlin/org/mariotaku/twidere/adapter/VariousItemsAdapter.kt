package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.alias.ItemClickListener
import org.mariotaku.twidere.model.ParcelableHashtag
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.HashtagViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.UserListViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder

/**
 * Created by mariotaku on 16/3/20.
 */
class VariousItemsAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager) {

    private val inflater = LayoutInflater.from(context)
    val dummyAdapter: DummyItemAdapter
    var hashtagClickListener: ItemClickListener? = null

    private var data: List<*>? = null

    init {
        val handler = StatusAdapterLinkClickHandler<Any>(context,
                preferences)
        dummyAdapter = DummyItemAdapter(context, TwidereLinkify(handler), this, requestManager)
        handler.setAdapter(dummyAdapter)
        dummyAdapter.updateOptions()
        loadMoreIndicatorPosition = ILoadMoreSupportAdapter.NONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_STATUS -> {
                return ListParcelableStatusesAdapter.createStatusViewHolder(dummyAdapter,
                        inflater, parent)
            }
            VIEW_TYPE_USER -> {
                return ParcelableUsersAdapter.createUserViewHolder(dummyAdapter, inflater, parent)
            }
            VIEW_TYPE_USER_LIST -> {
                return ParcelableUserListsAdapter.createUserListViewHolder(dummyAdapter, inflater,
                        parent)
            }
            VIEW_TYPE_HASHTAG -> {
                val view = inflater.inflate(R.layout.list_item_two_line_small, parent, false)
                return HashtagViewHolder(view, hashtagClickListener)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_STATUS -> {
                (holder as StatusViewHolder).display(obj as ParcelableStatus,
                        displayInReplyTo = true)
            }
            VIEW_TYPE_USER -> {
                (holder as UserViewHolder).display(obj as ParcelableUser)
            }
            VIEW_TYPE_USER_LIST -> {
                (holder as UserListViewHolder).display(obj as ParcelableUserList)
            }
            VIEW_TYPE_HASHTAG -> {
                (holder as HashtagViewHolder).display(obj as ParcelableHashtag)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewType(getItem(position))
    }

    private fun getItemViewType(obj: Any): Int {
        return when (obj) {
            is ParcelableStatus -> VIEW_TYPE_STATUS
            is ParcelableUser -> VIEW_TYPE_USER
            is ParcelableUserList -> VIEW_TYPE_USER_LIST
            is ParcelableHashtag -> VIEW_TYPE_HASHTAG
            else -> throw UnsupportedOperationException("Unsupported object $obj")
        }
    }

    fun setData(data: List<*>?) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        if (data == null) return 0
        return data!!.size
    }

    fun getItem(position: Int): Any {
        return data!![position]!!
    }

    companion object {

        const val VIEW_TYPE_STATUS = 1
        const val VIEW_TYPE_USER = 2
        const val VIEW_TYPE_USER_LIST = 3
        const val VIEW_TYPE_HASHTAG = 4
    }
}
