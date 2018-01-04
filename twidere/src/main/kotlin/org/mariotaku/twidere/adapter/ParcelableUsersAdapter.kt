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

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapterHelper
import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.ktextension.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.callback.ItemCountsAdapterListUpdateCallback
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.placeholder.ParcelableUserPlaceholder
import org.mariotaku.twidere.util.paging.DiffCallbacks
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.UserViewHolder

class ParcelableUsersAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IUsersAdapter, IItemCountsAdapter {

    override val showAccountsColor: Boolean = false
    override var userClickListener: IUsersAdapter.UserClickListener? = null
    override var requestClickListener: IUsersAdapter.RequestClickListener? = null
    override var friendshipClickListener: IUsersAdapter.FriendshipClickListener? = null
    override var simpleLayout: Boolean = false
    override var showFollow: Boolean = false
    var showCheckbox: Boolean = false
    override val itemCounts: ItemCounts = ItemCounts(3)

    var users: PagedList<ParcelableUser>?
        get() = pagedStatusesHelper.currentList
        set(value) {
            pagedStatusesHelper.setList(value)
            if (value == null) {
                itemCounts[ITEM_INDEX_USER] = 0
            }
        }

    var itemCheckedListener: ((Int, Boolean) -> Boolean)? = null

    val userStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_USER)

    val checkedCount: Int
        get() = states.count { (_, v) -> !v.locked && v.checked == `true` }

    private val states: MutableMap<UserKey, SelectionState> = ArrayMap()

    private val inflater = LayoutInflater.from(context)

    private var pagedStatusesHelper = PagedListAdapterHelper<ParcelableUser>(ItemCountsAdapterListUpdateCallback(this, ITEM_INDEX_USER),
            ListAdapterConfig.Builder<ParcelableUser>().setDiffCallback(DiffCallbacks.user).build())

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = userCount
        if (LoadMorePosition.START in position) {
            count++
        }
        if (LoadMorePosition.END in position) {
            count++
        }
        return count
    }

    override fun getUser(position: Int): ParcelableUser {
        return getUserInternal(position = position)
    }

    override fun getUserId(position: Int): String? {
        if (position == userCount) return null
        return getUser(position).key.id
    }

    override val userCount: Int
        get() {
            if (users == null) return 0
            return users!!.size
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
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER -> {
                val user = getUserInternal(loadAround = true, position = position)
                (holder as UserViewHolder).display(user, obtainState(getUserKey(position)))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val countIndex = getItemCountIndex(position)
        return when (countIndex) {
            ITEM_INDEX_LOAD_START_INDICATOR, ITEM_INDEX_LOAD_END_INDICATOR -> {
                ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            ITEM_INDEX_USER -> ITEM_VIEW_TYPE_USER
            else -> throw UnsupportedCountIndexException(countIndex, position)
        }
    }

    fun removeUserAt(position: Int): Boolean {
        // TODO: Remove user
        return true
    }

    fun setUserAt(position: Int, user: ParcelableUser): Boolean {
        // TODO: Update user
        return true
    }

    fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        if (users == null) return RecyclerView.NO_POSITION
        for (i in rangeOfSize(userStartIndex, userCount)) {
            val user = getUserInternal(false, i)
            if (accountKey == user.account_key && userKey == user.key) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    fun setItemChecked(position: Int, value: Boolean) {
        val userKey = getUserKey(position)
        setCheckState(userKey, value)
        if (itemCheckedListener?.invoke(position, value) == false) {
            setCheckState(userKey, !value)
            notifyItemChanged(position)
        }
    }

    fun clearCheckState() {
        states.values.forEach { it.checked = unspecified }
    }

    fun clearLockedState() {
        states.values.forEach { it.locked = false }
    }

    fun setCheckState(userKey: UserKey, value: Boolean) {
        obtainState(userKey).checked = value.trilean
    }

    fun setLockedState(userKey: UserKey, locked: Boolean) {
        obtainState(userKey).locked = locked
    }

    fun removeLockedState(userKey: UserKey) {
        states[userKey]?.locked = false
    }

    fun isItemChecked(position: Int): Boolean {
        return isItemChecked(getUser(position).key)
    }

    fun isItemChecked(userKey: UserKey): Boolean {
        return states[userKey]?.checked == `true`
    }

    fun isItemLocked(position: Int): Boolean {
        return isItemLocked(getUserKey(position))
    }

    fun isItemLocked(userKey: UserKey): Boolean {
        return states[userKey]?.locked == true
    }

    private fun getUserKey(position: Int) = getUser(position).key

    private fun obtainState(userKey: UserKey): SelectionState {
        return states.getOrPut(userKey, { SelectionState() })
    }

    private fun getUserInternal(loadAround: Boolean = false, position: Int,
            countIndex: Int = getItemCountIndex(position)): ParcelableUser {
        when (countIndex) {
            ITEM_INDEX_USER -> {
                val dataPosition = position - userStartIndex
                return if (loadAround) {
                    pagedStatusesHelper.getItem(dataPosition) ?: ParcelableUserPlaceholder
                } else {
                    pagedStatusesHelper.currentList?.get(dataPosition) ?: ParcelableUserPlaceholder
                }
            }
        }
        val validStart = userStartIndex
        val validEnd = validStart + userCount
        throw IndexOutOfBoundsException("index: $position, valid range is $validStart..$validEnd")
    }

    data class SelectionState(var locked: Boolean = false, var checked: Trilean = unspecified)

    companion object {

        const val ITEM_VIEW_TYPE_USER = 2
        const val ITEM_INDEX_LOAD_START_INDICATOR = 0
        const val ITEM_INDEX_USER = 1
        const val ITEM_INDEX_LOAD_END_INDICATOR = 2

        fun createUserViewHolder(adapter: IUsersAdapter, inflater: LayoutInflater, parent: ViewGroup): UserViewHolder {
            val view = inflater.inflate(R.layout.list_item_user, parent, false)
            val holder = UserViewHolder(view, adapter, adapter.simpleLayout, adapter.showFollow)
            if (adapter is ParcelableUsersAdapter) {
                holder.showCheckbox = adapter.showCheckbox
            }
            holder.setOnClickListeners()
            holder.setupViewOptions()
            return holder
        }
    }
}
