/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_user_list.view.*
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ColorLabelRelativeLayout
import org.mariotaku.twidere.view.ProfileImageView
import java.util.*

/**
 * Created by mariotaku on 15/4/29.
 */
class UserListViewHolder(
        itemView: View,
        private val adapter: IUserListsAdapter<*>
) : ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    private val itemContent: ColorLabelRelativeLayout = itemView.itemContent
    private val profileImageView: ProfileImageView = itemView.profileImage
    private val nameView: TextView = itemView.name
    private val createdByView: TextView = itemView.createdBy
    private val descriptionView: TextView = itemView.description
    private val membersCountView: TextView = itemView.membersCount
    private val subscribersCountView: TextView = itemView.subscribersCount

    private var userListClickListener: IUserListsAdapter.UserListClickListener? = null

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    fun display(userList: ParcelableUserList) {
        val context = itemView.context
        val manager = adapter.userColorNameManager

        itemContent.drawStart(manager.getUserColor(userList.user_key))
        nameView.spannable = userList.name
        val nameFirst = adapter.nameFirst
        val createdByDisplayName = manager.getDisplayName(userList, nameFirst)
        createdByView.spannable = context.getString(R.string.created_by, createdByDisplayName)

        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            adapter.requestManager.loadProfileImage(context, userList, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio).into(profileImageView)
        } else {
            profileImageView.visibility = View.GONE
        }
        descriptionView.spannable = userList.description
        descriptionView.hideIfEmpty()
        membersCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), userList.members_count)
        subscribersCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), userList.subscribers_count)
    }

    fun setOnClickListeners() {
        setUserListClickListener(adapter.userListClickListener)
    }

    override fun onClick(v: View) {
        val listener = userListClickListener ?: return
        when (v.id) {
            R.id.itemContent -> {
                listener.onUserListClick(this, layoutPosition)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        val listener = userListClickListener ?: return false
        when (v.id) {
            R.id.itemContent -> {
                return listener.onUserListLongClick(this, layoutPosition)
            }
        }
        return false
    }

    fun setUserListClickListener(listener: IUserListsAdapter.UserListClickListener?) {
        userListClickListener = listener
        itemContent.setOnClickListener(this)
        itemContent.setOnLongClickListener(this)
    }

    fun setupViewOptions() {
        profileImageView.style = adapter.profileImageStyle
        setTextSize(adapter.textSize)
    }

    fun setTextSize(textSize: Float) {
        nameView.textSize = textSize
        createdByView.textSize = textSize * 0.75f
    }

}
