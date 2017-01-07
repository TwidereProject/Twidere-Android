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

import android.database.Cursor
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessageConversationAdapter
import org.mariotaku.twidere.model.ParcelableDirectMessageCursorIndices
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 15/4/25.
 */
class IncomingMessageViewHolder(
        adapter: MessageConversationAdapter,
        itemView: View
) : MessageViewHolder(adapter, itemView) {

    private val profileImageView = itemView.findViewById(R.id.profileImage) as ProfileImageView

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    override fun displayMessage(cursor: Cursor, indices: ParcelableDirectMessageCursorIndices) {
        super.displayMessage(cursor, indices)
        val wrapper = adapter.mediaLoader
        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            wrapper.displayProfileImage(profileImageView, cursor.getString(indices.sender_profile_image_url))
        } else {
            profileImageView.visibility = View.GONE
            wrapper.cancelDisplayTask(profileImageView)
        }
    }


}
