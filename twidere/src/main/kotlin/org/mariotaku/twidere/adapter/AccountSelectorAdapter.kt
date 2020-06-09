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

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.fragment.AccountsDashboardFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.view.holder.AccountProfileImageViewHolder
import org.mariotaku.twidere.view.transformer.AccountsSelectorTransformer
import java.util.*
import kotlin.math.max

class AccountSelectorAdapter(
        private val inflater: LayoutInflater,
        preferences: SharedPreferences,
        val requestManager: RequestManager
) : RecyclerPagerAdapter() {

    internal var profileImageStyle: Int = preferences[profileImageStyleKey]

    var listener: Listener? = null

    var accounts: Array<AccountDetails>? = null
        set(value) {
            if (value != null) {
                val previousAccounts = accounts
                field = if (previousAccounts != null) {
                    val tmpList = arrayListOf(*value)
                    val tmpResult = ArrayList<AccountDetails>()
                    previousAccounts.forEach { previousAccount ->
                        val prefIndexOfTmp = tmpList.indexOfFirst { previousAccount == it }
                        if (prefIndexOfTmp >= 0) {
                            tmpResult.add(tmpList.removeAt(prefIndexOfTmp))
                        }
                    }
                    tmpResult.addAll(tmpList)
                    tmpResult.toTypedArray()
                } else {
                    value
                }
            } else {
                field = null
            }
            notifyPagesChanged(invalidateCache = true)
        }


    fun getAdapterAccount(position: Int): AccountDetails? {
        return accounts?.getOrNull(position - accountStart + 1)
    }

    var selectedAccount: AccountDetails?
        get() {
            return accounts?.firstOrNull()
        }
        set(account) {
            val from = account ?: return
            val to = selectedAccount ?: return
            swap(from, to)
        }

    val ITEM_VIEW_TYPE_SPACE = 1
    val ITEM_VIEW_TYPE_ICON = 2

    override fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): ViewHolder {
        when (itemViewType) {
            ITEM_VIEW_TYPE_SPACE -> {
                val view = inflater.inflate(R.layout.adapter_item_dashboard_account_space, container, false)
                return AccountsDashboardFragment.AccountSpaceViewHolder(view)
            }
            ITEM_VIEW_TYPE_ICON -> {
                val view = inflater.inflate(AccountProfileImageViewHolder.layoutResource, container, false)
                return AccountProfileImageViewHolder(this, view)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, itemViewType: Int) {
        when (itemViewType) {
            ITEM_VIEW_TYPE_ICON -> {
                val account = getAdapterAccount(position)!!
                (holder as AccountProfileImageViewHolder).display(account)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < accountStart) {
            return ITEM_VIEW_TYPE_SPACE
        }
        return ITEM_VIEW_TYPE_ICON
    }

    override fun getCount(): Int {
        return max(3, accountsCount)
    }

    val accountStart: Int
        get() = max(0, 3 - accountsCount)

    val accountsCount: Int
        get() {
            val accounts = this.accounts ?: return 0
            return max(0, accounts.size - 1)
        }

    override fun getPageWidth(position: Int): Float {
        return 1f / AccountsSelectorTransformer.selectorAccountsCount
    }

    fun dispatchItemSelected(holder: AccountProfileImageViewHolder) {
        listener?.onAccountSelected(holder, getAdapterAccount(holder.position)!!)
    }

    private fun swap(from: AccountDetails, to: AccountDetails) {
        val accounts = accounts ?: return
        val fromIdx = accounts.indexOfFirst { it == from }
        val toIdx = accounts.indexOfFirst { it == to }
        if (fromIdx < 0 || toIdx < 0) return
        val temp = accounts[toIdx]
        accounts[toIdx] = accounts[fromIdx]
        accounts[fromIdx] = temp
        notifyPagesChanged(invalidateCache = false)
    }

    interface Listener {
        fun onAccountSelected(holder: AccountProfileImageViewHolder, details: AccountDetails)
    }

}