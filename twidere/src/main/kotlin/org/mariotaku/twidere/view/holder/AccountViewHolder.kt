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
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.AccountDetailsAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.iface.IColorLabelView

class AccountViewHolder(
        val adapter: AccountDetailsAdapter,
        itemView: View,
        val showType: Boolean
) : RecyclerView.ViewHolder(itemView) {

    private val content = itemView as IColorLabelView
    private val name: TextView = itemView.findViewById(android.R.id.text1)
    private val screenName: TextView = itemView.findViewById(android.R.id.text2)
    private val profileImage: ProfileImageView = itemView.findViewById(android.R.id.icon)
    private val toggle: CompoundButton = itemView.findViewById(android.R.id.toggle)
    private val toggleContainer: View = itemView.findViewById(R.id.toggle_container)
    private val accountType: ImageView = itemView.findViewById(R.id.account_type)
    private val dragHandle: View = itemView.findViewById(R.id.drag_handle)

    init {
        profileImage.style = adapter.profileImageStyle
    }

    fun setAccountColor(color: Int) {
        content.drawEnd(color)
    }

    fun setSortEnabled(enabled: Boolean) {
        dragHandle.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    fun display(details: AccountDetails) {
        name.spannable = details.user.name
        screenName.spannable = if (details.type == AccountType.MASTODON || details.type == AccountType.STATUSNET) {
            details.account.name
        } else {
            "${if (showType) details.type else ""}@${details.user.screen_name}"
        }
        setAccountColor(details.color)
        profileImage.visibility = View.VISIBLE
        adapter.requestManager.loadProfileImage(adapter.context, details, adapter.profileImageStyle,
                profileImage.cornerRadius, profileImage.cornerRadiusRatio).into(profileImage)
        accountType.setImageResource(AccountUtils.getAccountTypeIcon(details.type))
        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = details.activated
        toggle.setOnCheckedChangeListener(adapter.checkedChangeListener)
        toggle.tag = layoutPosition
        toggleContainer.visibility = if (adapter.switchEnabled) View.VISIBLE else View.GONE
        setSortEnabled(adapter.sortEnabled)
    }
}
