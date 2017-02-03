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

package org.mariotaku.twidere.view.holder

import android.database.Cursor
import android.graphics.Typeface
import android.support.annotation.UiThread
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessageEntriesAdapter
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries
import org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText
import org.mariotaku.twidere.view.NameView
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.ShortTimeView
import org.mariotaku.twidere.view.iface.IColorLabelView

class MessageEntryViewHolder(private val adapter: MessageEntriesAdapter, itemView: View) : ViewHolder(itemView), OnClickListener {

    val profileImageView: ProfileImageView
    val nameView: NameView
    val textView: TextView
    val timeView: ShortTimeView
    private val content: IColorLabelView

    init {
        content = itemView.findViewById(R.id.content) as IColorLabelView
        profileImageView = itemView.findViewById(R.id.profileImage) as ProfileImageView
        nameView = itemView.findViewById(R.id.name) as NameView
        textView = itemView.findViewById(R.id.text) as TextView
        timeView = itemView.findViewById(R.id.time) as ShortTimeView

        profileImageView.style = adapter.profileImageStyle
        setTextSize(adapter.textSize)
        itemView.setOnClickListener(this)
        profileImageView.setOnClickListener(this)
    }

    @UiThread
    fun displayMessage(cursor: Cursor, isUnread: Boolean) {
        val context = adapter.context
        val loader = adapter.mediaLoader
        val manager = adapter.userColorNameManager

        val accountKey = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_ACCOUNT_KEY))
        val conversationId = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_CONVERSATION_ID))
        val timestamp = cursor.getLong(ConversationEntries.IDX_MESSAGE_TIMESTAMP)
        val isOutgoing = cursor.getInt(ConversationEntries.IDX_IS_OUTGOING) == 1

        val name = cursor.getString(ConversationEntries.IDX_NAME)
        val screenName = cursor.getString(ConversationEntries.IDX_SCREEN_NAME)

        nameView.name = manager.getUserNickname(conversationId, name)
        nameView.screenName = "@$screenName"
        nameView.updateText(adapter.bidiFormatter)
        textView.text = toPlainText(cursor.getString(ConversationEntries.IDX_TEXT_UNESCAPED))
        timeView.time = timestamp
        if (isOutgoing) {
            timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_sent, 0)
        } else {
            timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        nameView.setTypeface(null, if (isUnread && !isOutgoing) Typeface.BOLD else Typeface.NORMAL)
        textView.setTypeface(null, if (isUnread && !isOutgoing) Typeface.BOLD else Typeface.NORMAL)
        if (adapter.shouldShowAccountsColor()) {
            // FIXME draw account color
        } else {
            content.drawEnd()
        }
        content.drawStart(manager.getUserColor(conversationId))

        val profileImage = cursor.getString(ConversationEntries.IDX_PROFILE_IMAGE_URL)
        loader.displayProfileImage(this.profileImageView, profileImage)
    }

    override fun onClick(v: View) {
        when (v) {
            profileImageView -> {
                adapter.onUserProfileClick(layoutPosition)
            }
            itemView -> {
                adapter.onMessageClick(layoutPosition)
            }
        }
    }

    fun setTextSize(textSize: Float) {
        nameView.setPrimaryTextSize(textSize * 1.1f)
        nameView.setSecondaryTextSize(textSize)
        textView.textSize = textSize
        timeView.textSize = textSize * 0.85f
    }

}
