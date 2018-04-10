package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.alias.ItemClickListener
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.RecyclerViewTypes
import org.mariotaku.twidere.model.ParcelableHashtag
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.HashtagViewHolder
import org.mariotaku.twidere.view.holder.UserListViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder
import org.mariotaku.twidere.view.holder.status.StatusViewHolder

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
                PreferencesSingleton.get(this.context))
        dummyAdapter = DummyItemAdapter(context, TwidereLinkify(handler), this, requestManager)
        handler.setAdapter(dummyAdapter)
        dummyAdapter.updateOptions()
        loadMoreIndicatorPosition = LoadMorePosition.NONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            RecyclerViewTypes.STATUS -> {
                return ParcelableStatusesAdapter.createStatusViewHolder(dummyAdapter,
                        inflater, parent, TimelineStyle.PLAIN) as RecyclerView.ViewHolder
            }
            RecyclerViewTypes.USER -> {
                return ParcelableUsersAdapter.createUserViewHolder(dummyAdapter, inflater, parent)
            }
            RecyclerViewTypes.USER_LIST -> {
                return ParcelableUserListsAdapter.createUserListViewHolder(dummyAdapter, inflater,
                        parent)
            }
            RecyclerViewTypes.HASHTAG -> {
                val view = inflater.inflate(R.layout.list_item_two_line_small, parent, false)
                return HashtagViewHolder(view, hashtagClickListener)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = getItem(position)
        when (holder.itemViewType) {
            RecyclerViewTypes.STATUS -> {
                (holder as StatusViewHolder).display(obj as ParcelableStatus,
                        displayInReplyTo = true)
            }
            RecyclerViewTypes.USER -> {
                (holder as UserViewHolder).display(obj as ParcelableUser, null)
            }
            RecyclerViewTypes.USER_LIST -> {
                (holder as UserListViewHolder).display(obj as ParcelableUserList)
            }
            RecyclerViewTypes.HASHTAG -> {
                (holder as HashtagViewHolder).display(obj as ParcelableHashtag)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewType(getItem(position))
    }

    private fun getItemViewType(obj: Any): Int {
        when (obj) {
            is ParcelableStatus -> return RecyclerViewTypes.STATUS
            is ParcelableUser -> return RecyclerViewTypes.USER
            is ParcelableUserList -> return RecyclerViewTypes.USER_LIST
            is ParcelableHashtag -> return RecyclerViewTypes.HASHTAG
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

}
