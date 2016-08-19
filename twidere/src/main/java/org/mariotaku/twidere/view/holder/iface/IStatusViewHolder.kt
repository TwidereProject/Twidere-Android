/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view.holder.iface

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import org.mariotaku.twidere.adapter.iface.ContentCardClickListener
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.CardMediaContainer
import org.mariotaku.twidere.view.holder.GapViewHolder

/**
 * Created by mariotaku on 15/10/26.
 */
interface IStatusViewHolder : CardMediaContainer.OnMediaClickListener {

    fun displayStatus(status: ParcelableStatus,
                      displayInReplyTo: Boolean = true,
                      shouldDisplayExtraType: Boolean = true)

    val profileImageView: ImageView?

    val profileTypeView: ImageView?

    override fun onMediaClick(view: View, media: ParcelableMedia, accountKey: UserKey, extraId: Long)

    fun setStatusClickListener(listener: StatusClickListener?)

    fun setTextSize(textSize: Float)

    fun playLikeAnimation(listener: LikeAnimationDrawable.OnLikedListener)

    interface StatusClickListener : ContentCardClickListener, IGapSupportedAdapter.GapClickListener {

        fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int)

        fun onStatusClick(holder: IStatusViewHolder, position: Int)

        fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean

        fun onUserProfileClick(holder: IStatusViewHolder, position: Int)
    }

    abstract class SimpleStatusClickListener : StatusClickListener {
        override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {

        }

        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {

        }

        override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
            return false
        }

        override fun onGapClick(holder: GapViewHolder, position: Int) {

        }

        override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {

        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {

        }

        override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {

        }
    }
}
