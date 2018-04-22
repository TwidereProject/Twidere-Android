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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.displayProfileImageKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.singleton.PreferencesSingleton

class AccountsSpinnerAdapter(
        val context: Context,
        val itemViewResource: Int = R.layout.list_item_simple_user,
        val dropDownViewResource: Int = R.layout.list_item_simple_user,
        val requestManager: RequestManager
) : ViewHolderListAdapter<AccountsSpinnerAdapter.AccountSpinnerViewHolder>() {

    var dummyItemText: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var accounts: List<AccountDetails>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var profileImageEnabled: Boolean
    private var profileImageStyle: Int

    init {
        val preferences = PreferencesSingleton.get(context)
        profileImageEnabled = preferences[displayProfileImageKey]
        profileImageStyle = preferences[profileImageStyleKey]
    }

    override fun getCount(): Int {
        return accounts?.size ?: 0
    }

    override fun getItem(position: Int): AccountDetails {
        return accounts!![position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountSpinnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemViewResource, parent, false)
        return AccountSpinnerViewHolder(this, view)
    }

    override fun onCreateDropDownViewHolder(parent: ViewGroup, viewType: Int): AccountSpinnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(dropDownViewResource, parent, false)
        return AccountSpinnerDropDownViewHolder(this, view)
    }

    override fun onBindViewHolder(holder: AccountSpinnerViewHolder, position: Int) {
        holder.display(getItem(position))
    }

    fun findPositionByKey(key: UserKey): Int {
        return (0 until count).indexOfFirst { key == getItem(it).key }
    }

    open class AccountSpinnerViewHolder(val adapter: AccountsSpinnerAdapter, itemView: View) : ViewHolderListAdapter.ViewHolder(itemView) {

        private val text1 = itemView.name
        private val text2 = itemView.screenName
        private val icon = itemView.profileImage

        fun display(account: AccountDetails) {
            if (!account.dummy) {
                text1?.visibility = View.VISIBLE
                text1?.spannable = account.user.name
                text2?.visibility = View.VISIBLE
                text2?.spannable = "@${account.user.screen_name}"
                if (icon != null) {
                    if (adapter.profileImageEnabled) {
                        icon.visibility = View.VISIBLE
                        icon.style = adapter.profileImageStyle
                        adapter.requestManager.loadProfileImage(account.user, adapter.profileImageStyle).into(icon)
                    } else {
                        icon.visibility = View.GONE
                    }
                }
            } else {
                text1?.visibility = View.VISIBLE
                text1?.spannable = adapter.dummyItemText
                text2?.visibility = View.GONE
                icon?.visibility = View.GONE
            }
        }
    }

    class AccountSpinnerDropDownViewHolder(adapter: AccountsSpinnerAdapter, itemView: View) : AccountSpinnerViewHolder(adapter, itemView) {

    }
}
