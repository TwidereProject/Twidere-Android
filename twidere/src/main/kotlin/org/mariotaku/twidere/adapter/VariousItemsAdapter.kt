package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.UserListViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder

/**
 * Created by mariotaku on 16/3/20.
 */
class VariousItemsAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context) {

    private val mInflater: LayoutInflater
    val dummyAdapter: DummyItemAdapter

    private var data: List<*>? = null

    init {
        mInflater = LayoutInflater.from(context)
        val handler = StatusAdapterLinkClickHandler<Any>(context,
                preferences)
        dummyAdapter = DummyItemAdapter(context, TwidereLinkify(handler), this)
        handler.setAdapter(dummyAdapter)
        dummyAdapter.updateOptions()
        loadMoreIndicatorPosition = ILoadMoreSupportAdapter.NONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_STATUS -> {
                return ListParcelableStatusesAdapter.createStatusViewHolder(dummyAdapter,
                        mInflater, parent)
            }
            VIEW_TYPE_USER -> {
                return ParcelableUsersAdapter.createUserViewHolder(dummyAdapter, mInflater, parent)
            }
            VIEW_TYPE_USER_LIST -> {
                return ParcelableUserListsAdapter.createUserListViewHolder(dummyAdapter, mInflater,
                        parent)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_STATUS -> {
                (holder as StatusViewHolder).displayStatus(obj as ParcelableStatus, true)
            }
            VIEW_TYPE_USER -> {
                (holder as UserViewHolder).displayUser(obj as ParcelableUser)
            }
            VIEW_TYPE_USER_LIST -> {
                (holder as UserListViewHolder).displayUserList(obj as ParcelableUserList)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewType(getItem(position))
    }

    protected fun getItemViewType(obj: Any): Int {
        if (obj is ParcelableStatus) {
            return VIEW_TYPE_STATUS
        } else if (obj is ParcelableUser) {
            return VIEW_TYPE_USER
        } else if (obj is ParcelableUserList) {
            return VIEW_TYPE_USER_LIST
        }
        throw UnsupportedOperationException("Unsupported object " + obj)
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

        val VIEW_TYPE_STATUS = 1
        val VIEW_TYPE_USER = 2
        val VIEW_TYPE_USER_LIST = 3
    }
}
