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

import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.card_item_group_compact.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ColorLabelRelativeLayout
import org.mariotaku.twidere.view.NameView
import org.mariotaku.twidere.view.ProfileImageView
import java.util.*

/**
 * Created by mariotaku on 15/4/29.
 */
class GroupViewHolder(private val adapter: IGroupsAdapter<*>, itemView: View) : ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    private val itemContent: ColorLabelRelativeLayout
    private val profileImageView: ProfileImageView
    private val nameView: NameView
    private val externalIndicator: TextView
    private val descriptionView: TextView
    private val membersCountView: TextView
    private val adminsCountView: TextView

    private var groupClickListener: IGroupsAdapter.GroupAdapterListener? = null

    init {
        itemContent = itemView.itemContent
        profileImageView = itemView.profileImage
        nameView = itemView.name
        externalIndicator = itemView.externalIndicator
        descriptionView = itemView.description
        membersCountView = itemView.membersCount
        adminsCountView = itemView.adminsCount
    }

    fun displayGroup(group: ParcelableGroup) {
        val context = itemView.context
        val formatter = adapter.bidiFormatter

        nameView.name = group.fullname
        nameView.screenName = "!${group.nickname}"

        nameView.updateText(formatter)
        val groupHost = UserKeyUtils.getUserHost(group.url, group.account_key.host)
        if (UserKeyUtils.isSameHost(group.account_key.host, groupHost)) {
            externalIndicator.visibility = View.GONE
        } else {
            externalIndicator.visibility = View.VISIBLE
            externalIndicator.text = context.getString(R.string.external_group_host_format,
                    groupHost)
        }
        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            adapter.getRequestManager().load(group.homepage_logo).into(profileImageView)
        } else {
            profileImageView.visibility = View.GONE
        }
        descriptionView.visibility = if (TextUtils.isEmpty(group.description)) View.GONE else View.VISIBLE
        descriptionView.text = formatter.unicodeWrap(group.description)
        membersCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), group.member_count)
        adminsCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), group.admin_count)
    }

    fun setOnClickListeners() {
        setGroupClickListener(adapter.groupAdapterListener)
    }

    override fun onClick(v: View) {
        val listener = groupClickListener ?: return
        when (v.id) {
            R.id.itemContent -> {
                listener.onGroupClick(this, layoutPosition)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        val listener = groupClickListener ?: return false
        when (v.id) {
            R.id.itemContent -> {
                return listener.onGroupLongClick(this, layoutPosition)
            }
        }
        return false
    }

    fun setGroupClickListener(listener: IGroupsAdapter.GroupAdapterListener?) {
        groupClickListener = listener
        itemContent.setOnClickListener(this)
        itemContent.setOnLongClickListener(this)
    }

    fun setupViewOptions() {
        profileImageView.style = adapter.profileImageStyle
        setTextSize(adapter.textSize)
    }

    fun setTextSize(textSize: Float) {
        descriptionView.textSize = textSize
        externalIndicator.textSize = textSize
        nameView.setPrimaryTextSize(textSize)
        nameView.setSecondaryTextSize(textSize * 0.75f)
        membersCountView.textSize = textSize
        adminsCountView.textSize = textSize
    }

}
