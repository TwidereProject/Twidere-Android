/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder

class ParcelableUsersAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager), IUsersAdapter<List<ParcelableUser>> {
    private val inflater = LayoutInflater.from(context)
    private var data: List<ParcelableUser>? = null

    override val showAccountsColor: Boolean = false
    override var userClickListener: IUsersAdapter.UserClickListener? = null
    override var requestClickListener: IUsersAdapter.RequestClickListener? = null
    override var friendshipClickListener: IUsersAdapter.FriendshipClickListener? = null
    override var simpleLayout: Boolean = false
    override var showFollow: Boolean = false

    fun getData(): List<ParcelableUser>? {
        return data
    }

    override fun setData(data: List<ParcelableUser>?): Boolean {
        this.data = data
        notifyDataSetChanged()
        return true
    }

    private fun bindUser(holder: UserViewHolder, position: Int) {
        holder.display(getUser(position)!!)
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = userCount
        if (position and ILoadMoreSupportAdapter.START != 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END != 0L) {
            count++
        }
        return count
    }

    override fun getUser(position: Int): ParcelableUser? {
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return null
        return data!![dataPosition]
    }

    val userStartIndex: Int
        get() {
            val position = loadMoreIndicatorPosition
            var start = 0
            if (position and ILoadMoreSupportAdapter.START != 0L) {
                start += 1
            }
            return start
        }

    override fun getUserId(position: Int): String? {
        if (position == userCount) return null
        return data!![position].key.id
    }

    override val userCount: Int
        get() {
            if (data == null) return 0
            return data!!.size
        }

    fun removeUserAt(position: Int): Boolean {
        val data = this.data as? MutableList ?: return false
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return false
        data.removeAt(dataPosition)
        notifyItemRemoved(position)
        return true
    }

    fun setUserAt(position: Int, user: ParcelableUser): Boolean {
        val data = this.data as? MutableList ?: return false
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return false
        data[dataPosition] = user
        notifyItemChanged(position)
        return true
    }

    fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        if (data == null) return RecyclerView.NO_POSITION
        for (i in userStartIndex until userStartIndex + userCount) {
            val user = data!![i]
            if (accountKey == user.account_key && userKey == user.key) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_USER -> {
                return createUserViewHolder(this, inflater, parent)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER -> {
                bindUser(holder as UserViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == userCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_USER
    }

    companion object {

        const val ITEM_VIEW_TYPE_USER = 2


        fun createUserViewHolder(adapter: IUsersAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): UserViewHolder {
            val view = inflater.inflate(R.layout.list_item_user, parent, false)
            val holder = UserViewHolder(view, adapter, adapter.simpleLayout, adapter.showFollow)
            holder.setOnClickListeners()
            holder.setupViewOptions()
            return holder
        }
    }
}
