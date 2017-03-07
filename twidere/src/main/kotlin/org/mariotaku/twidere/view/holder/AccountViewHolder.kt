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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView

import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.AccountDetailsAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.iface.IColorLabelView

class AccountViewHolder(
        val adapter: AccountDetailsAdapter,
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val content = itemView as IColorLabelView
    private val name = itemView.findViewById(android.R.id.text1) as TextView
    private val screenName = itemView.findViewById(android.R.id.text2) as TextView
    private val profileImage = itemView.findViewById(android.R.id.icon) as ProfileImageView
    private val toggle = itemView.findViewById(android.R.id.toggle) as CompoundButton
    private val toggleContainer = itemView.findViewById(R.id.toggle_container)
    private val accountType = itemView.findViewById(R.id.account_type) as ImageView
    private val dragHandle = itemView.findViewById(R.id.drag_handle)

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
        name.text = details.user.name
        screenName.text = String.format("@%s", details.user.screen_name)
        setAccountColor(details.color)
        if (adapter.profileImageEnabled) {
            adapter.requestManager.loadProfileImage(adapter.context, details, adapter.profileImageStyle,
                    profileImage.cornerRadius, profileImage.cornerRadiusRatio).into(profileImage)
        } else {
            // TODO: display stub image?
        }
        accountType.setImageResource(AccountUtils.getAccountTypeIcon(details.type))
        toggle.isChecked = details.activated
        toggle.setOnCheckedChangeListener(adapter.checkedChangeListener)
        toggle.tag = layoutPosition
        toggleContainer.visibility = if (adapter.switchEnabled) View.VISIBLE else View.GONE
        setSortEnabled(adapter.sortEnabled)
    }
}
