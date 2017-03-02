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
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.getBestProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.holder.AccountViewHolder

class AccountDetailsAdapter(
        context: Context,
        getRequestManager: () -> RequestManager
) : BaseArrayAdapter<AccountDetails>(context, R.layout.list_item_account, getRequestManager = getRequestManager) {

    private var sortEnabled: Boolean = false
    private var switchEnabled: Boolean = false
    var accountToggleListener: ((Int, Boolean) -> Unit)? = null

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val position = buttonView.tag as? Int ?: return@OnCheckedChangeListener
        accountToggleListener?.invoke(position, isChecked)
    }

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val holder = view.tag as? AccountViewHolder ?: run {
            val h = AccountViewHolder(view)
            view.tag = h
            return@run h
        }
        val details = getItem(position)
        holder.name.text = details.user.name
        holder.screenName.text = String.format("@%s", details.user.screen_name)
        holder.setAccountColor(details.color)
        if (profileImageEnabled) {
            getRequestManager().loadProfileImage(context, details.user.getBestProfileImage(context)).into(holder.profileImage)
        } else {
            // TODO: display stub image?
        }
        val accountType = details.type
        holder.accountType.setImageResource(AccountUtils.getAccountTypeIcon(accountType))
        holder.toggle.isChecked = details.activated
        holder.toggle.setOnCheckedChangeListener(checkedChangeListener)
        holder.toggle.tag = position
        holder.toggleContainer.visibility = if (switchEnabled) View.VISIBLE else View.GONE
        holder.setSortEnabled(sortEnabled)
        return view
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).key.hashCode().toLong()
    }

    fun setSwitchEnabled(enabled: Boolean) {
        if (switchEnabled == enabled) return
        switchEnabled = enabled
        notifyDataSetChanged()
    }

    fun setSortEnabled(sortEnabled: Boolean) {
        if (this.sortEnabled == sortEnabled) return
        this.sortEnabled = sortEnabled
        notifyDataSetChanged()
    }

    fun drop(from: Int, to: Int) {
        val fromItem = getItem(from)
        removeAt(from)
        insert(fromItem, to)
    }

    fun findItem(key: UserKey): AccountDetails? {
        (0 until count).forEach { i ->
            val item = getItem(i)
            if (key == item.key) return item
        }
        return null
    }

}
