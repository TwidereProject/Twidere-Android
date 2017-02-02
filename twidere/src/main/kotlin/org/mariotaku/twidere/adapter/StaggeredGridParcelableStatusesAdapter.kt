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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.commonsware.cwac.layouts.AspectLockedFrameLayout
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.view.MediaPreviewImageView
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/19.
 */
class StaggeredGridParcelableStatusesAdapter(context: Context) : ParcelableStatusesAdapter(context) {

    override val progressViewIds: IntArray
        get() = intArrayOf(R.id.media_image_progress)

    override fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder {
        val view = inflater.inflate(R.layout.adapter_item_media_status, parent, false)
        val holder = MediaStatusViewHolder(this, view)
        holder.setOnClickListeners()
        holder.setupViewOptions()
        return holder
    }

    class MediaStatusViewHolder(private val adapter: IStatusesAdapter<*>, itemView: View) : RecyclerView.ViewHolder(itemView), IStatusViewHolder, View.OnClickListener, View.OnLongClickListener {
        private val aspectRatioSource = SimpleAspectRatioSource().apply {
            setSize(100, 100)
        }

        private val mediaImageContainer: AspectLockedFrameLayout
        private val mediaImageView: MediaPreviewImageView
        override val profileImageView: ImageView
        private val mediaTextView: TextView
        private var listener: IStatusViewHolder.StatusClickListener? = null

        init {
            mediaImageContainer = itemView.findViewById(R.id.media_image_container) as AspectLockedFrameLayout
            mediaImageContainer.setAspectRatioSource(aspectRatioSource)
            mediaImageView = itemView.findViewById(R.id.media_image) as MediaPreviewImageView
            profileImageView = itemView.findViewById(R.id.media_profile_image) as ImageView
            mediaTextView = itemView.findViewById(R.id.media_text) as TextView
        }


        override fun displayStatus(status: ParcelableStatus, displayInReplyTo: Boolean, shouldDisplayExtraType: Boolean) {
            val loader = adapter.mediaLoader
            val media = status.media ?: return
            if (media.isEmpty()) return
            val firstMedia = media[0]
            mediaTextView.text = status.text_unescaped
            if (firstMedia.width > 0 && firstMedia.height > 0) {
                aspectRatioSource.setSize(firstMedia.width, firstMedia.height)
            } else {
                aspectRatioSource.setSize(100, 100)
            }
            mediaImageContainer.tag = firstMedia
            mediaImageContainer.requestLayout()

            mediaImageView.setHasPlayIcon(ParcelableMediaUtils.hasPlayIcon(firstMedia.type))
            loader.displayProfileImage(profileImageView, status)
            loader.displayPreviewImageWithCredentials(mediaImageView, firstMedia.preview_url,
                    status.account_key, adapter.mediaLoadingHandler)
        }

        override val profileTypeView: ImageView?
            get() = null

        override fun onClick(v: View) {
            when (v.id) {
                R.id.itemContent -> {
                    listener?.onStatusClick(this, layoutPosition)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            return false
        }

        override fun onMediaClick(view: View, media: ParcelableMedia, accountKey: UserKey, extraId: Long) {
        }

        override fun setStatusClickListener(listener: IStatusViewHolder.StatusClickListener?) {
            this.listener = listener
            itemView.findViewById(R.id.itemContent).setOnClickListener(this)
        }

        override fun setTextSize(textSize: Float) {

        }

        override fun playLikeAnimation(listener: LikeAnimationDrawable.OnLikedListener) {

        }

        fun setOnClickListeners() {
            setStatusClickListener(adapter.statusClickListener)
        }

        fun setupViewOptions() {
            setTextSize(adapter.textSize)
        }


        private class SimpleAspectRatioSource : AspectLockedFrameLayout.AspectRatioSource {
            private var width: Int = 0
            private var height: Int = 0

            override fun getWidth(): Int {
                return width
            }

            override fun getHeight(): Int {
                return height
            }

            fun setSize(width: Int, height: Int) {
                this.width = width
                this.height = height
            }

        }
    }
}
