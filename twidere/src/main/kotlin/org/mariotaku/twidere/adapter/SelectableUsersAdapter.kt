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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.SelectableUserViewHolder

class SelectableUsersAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context) {

    val ITEM_VIEW_TYPE_USER = 2

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val itemStates: MutableMap<UserKey, Boolean> = ArrayMap()
    var itemCheckedListener: ((Int, Boolean) -> Unit)? = null

    var data: List<ParcelableUser>? = null
        set(value) {
            field = value
            value?.forEach { item ->
                if (item.key !in itemStates && item.is_filtered) {
                    itemStates[item.key] = true
                }
            }
            notifyDataSetChanged()
        }

    private fun bindUser(holder: SelectableUserViewHolder, position: Int) {
        holder.displayUser(getUser(position)!!)
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = userCount
        if (position and ILoadMoreSupportAdapter.START !== 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END !== 0L) {
            count++
        }
        return count
    }

    fun getUser(position: Int): ParcelableUser? {
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return null
        return data!![dataPosition]
    }

    val userStartIndex: Int
        get() {
            val position = loadMoreIndicatorPosition
            var start = 0
            if (position and ILoadMoreSupportAdapter.START !== 0L) {
                start += 1
            }
            return start
        }

    fun getUserKey(position: Int): UserKey {
        return data!![position].key
    }

    val userCount: Int
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
                val view = inflater.inflate(R.layout.list_item_simple_user, parent, false)
                val holder = SelectableUserViewHolder(view, this)
                return holder
            }
            ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER -> {
                bindUser(holder as SelectableUserViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
            return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == userCount) {
            return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_USER
    }

    val checkedCount: Int get() {
        return data?.count { !it.is_filtered && itemStates[it.key] ?: false } ?: 0
    }

    fun setItemChecked(position: Int, value: Boolean) {
        val userKey = getUserKey(position)
        itemStates[userKey] = value
        itemCheckedListener?.invoke(position, value)
    }

    fun clearCheckState() {
        itemStates.clear()
    }

    fun setCheckState(userKey: UserKey, value: Boolean) {
        itemStates[userKey] = value
    }

    fun isItemChecked(position: Int): Boolean {
        return itemStates[getUserKey(position)] ?: false
    }

    fun clearSelection() {
        itemStates.clear()
    }
}