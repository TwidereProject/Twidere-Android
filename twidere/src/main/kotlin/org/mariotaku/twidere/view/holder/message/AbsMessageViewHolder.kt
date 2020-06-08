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

package org.mariotaku.twidere.view.holder.message

import android.os.Build
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 2017/2/9.
 */

abstract class AbsMessageViewHolder(itemView: View, val adapter: MessagesConversationAdapter) : RecyclerView.ViewHolder(itemView) {

    protected abstract val date: TextView
    protected abstract val messageContent: View
    protected open val profileImage: ProfileImageView? = null
    protected open val nameTime: TextView? = null

    open fun setup() {
        val textSize = adapter.textSize
        date.textSize = textSize * 0.9f
        nameTime?.textSize = textSize * 0.8f
        profileImage?.style = adapter.profileImageStyle
    }

    open fun display(message: ParcelableMessage, showDate: Boolean) {
        val context = adapter.context
        val manager = adapter.userColorNameManager

        setMessageContentGravity(messageContent, message.is_outgoing)
        if (showDate) {
            date.visibility = View.VISIBLE
            date.text = DateUtils.getRelativeTimeSpanString(message.timestamp, System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE)
        } else {
            date.visibility = View.GONE
        }
        val sender = message.sender_key?.let { adapter.findUser(it) }

        nameTime?.apply {
            val time = DateUtils.formatDateTime(context, message.timestamp, DateUtils.FORMAT_SHOW_TIME)
            if (message.is_outgoing) {
                this.text = time
            } else if (adapter.displaySenderProfile && sender != null) {
                val senderName = manager.getDisplayName(sender, adapter.nameFirst)
                this.text = context.getString(R.string.message_format_sender_time, senderName, time)
            } else {
                this.text = time
            }
        }

        profileImage?.apply {
            if (adapter.displaySenderProfile && adapter.profileImageEnabled && sender != null
                    && !message.is_outgoing) {
                this.visibility = View.VISIBLE
                adapter.requestManager.loadProfileImage(context, sender,
                        adapter.profileImageStyle).into(this)
            } else {
                this.visibility = View.GONE
            }
        }
    }

    open fun setMessageContentGravity(view: View, outgoing: Boolean) {
        when (val lp = view.layoutParams) {
            is FrameLayout.LayoutParams -> {
                lp.gravity = if (outgoing) GravityCompat.END else GravityCompat.START
            }
            is LinearLayout.LayoutParams -> {
                lp.gravity = if (outgoing) GravityCompat.END else GravityCompat.START
            }
            is RelativeLayout.LayoutParams -> {
                val endRule = if (outgoing) 1 else 0
                val startRule = if (outgoing) 0 else 1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    lp.addRule(RelativeLayout.ALIGN_PARENT_START, startRule)
                    lp.addRule(RelativeLayout.ALIGN_PARENT_END, endRule)
                } else {
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, startRule)
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, endRule)
                }
            }
        }
    }

}
