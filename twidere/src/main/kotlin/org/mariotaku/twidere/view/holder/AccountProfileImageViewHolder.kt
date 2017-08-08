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

package org.mariotaku.twidere.view.holder

import android.view.View
import kotlinx.android.synthetic.main.adapter_item_dashboard_account.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.AccountSelectorAdapter
import org.mariotaku.twidere.adapter.RecyclerPagerAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.view.ShapedImageView

class AccountProfileImageViewHolder(
        val adapter: AccountSelectorAdapter,
        itemView: View
) : RecyclerPagerAdapter.ViewHolder(itemView), View.OnClickListener {

    val iconView: ShapedImageView = itemView.icon

    init {
        itemView.setOnClickListener(this)
        iconView.style = adapter.profileImageStyle
    }

    override fun onClick(v: View) {
        adapter.dispatchItemSelected(this)
    }

    fun display(account: AccountDetails) {
        iconView.setBorderColor(account.color)
        adapter.requestManager.loadProfileImage(itemView.context, account,
                adapter.profileImageStyle, iconView.cornerRadius,
                iconView.cornerRadiusRatio).into(iconView)
    }

    companion object {
        const val layoutResource = R.layout.adapter_item_dashboard_account
    }

}