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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.adapter_item_large_media_status.view.*
import kotlinx.android.synthetic.main.adapter_item_large_media_status_preview_item.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.RecyclerPagerAdapter
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class LargeMediaStatusViewHolder(private val adapter: IStatusesAdapter, itemView: View) :
        RecyclerView.ViewHolder(itemView), IStatusViewHolder, View.OnClickListener, View.OnLongClickListener {
    override val profileImageView: ProfileImageView = itemView.profileImage
    override val profileTypeView: ImageView? = null

    private val mediaPreviewAdapter: ImagePagerAdapter
    private val mediaPreviewPager = itemView.mediaPreviewPager
    private val nameView = itemView.nameView

    private var listener: IStatusViewHolder.StatusClickListener? = null


    init {
        mediaPreviewAdapter = ImagePagerAdapter(adapter.requestManager)
        mediaPreviewPager.adapter = mediaPreviewAdapter
    }

    override fun display(status: ParcelableStatus, displayInReplyTo: Boolean,
            displayPinned: Boolean) {
        val context = itemView.context
        adapter.requestManager.loadProfileImage(context, status,
                adapter.profileImageStyle, profileImageView.cornerRadius,
                profileImageView.cornerRadiusRatio).into(profileImageView)

        nameView.name = status.user_name
        nameView.screenName = "@${status.user_screen_name}"
        nameView.updateText(adapter.bidiFormatter)

        mediaPreviewAdapter.media = status.media
    }

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

    override fun onMediaClick(view: View, current: ParcelableMedia, accountKey: UserKey?, id: Long) {
    }

    override fun setStatusClickListener(listener: IStatusViewHolder.StatusClickListener?) {
        this.listener = listener
        itemView.itemContent.setOnClickListener(this)
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
        nameView.nameFirst = adapter.nameFirst
    }

    private class ImagePagerAdapter(val requestManager: RequestManager) : RecyclerPagerAdapter<LargeMediaItemHolder>() {
        var media: Array<ParcelableMedia>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount() = media?.size ?: 0

        override fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): LargeMediaItemHolder {
            return LargeMediaItemHolder(this, LayoutInflater.from(container.context)
                    .inflate(LargeMediaItemHolder.layoutResource, container, false))
        }

        override fun onBindViewHolder(holder: LargeMediaItemHolder, position: Int, itemViewType: Int) {
            holder.display(media!![position])
        }

    }

    private class LargeMediaItemHolder(val adapter: ImagePagerAdapter, itemView: View) : RecyclerPagerAdapter.ViewHolder(itemView) {
        private val mediaPreview = itemView.mediaPreview
        fun display(media: ParcelableMedia) {
            adapter.requestManager.load(media.preview_url).into(mediaPreview)
        }

        companion object {
            val layoutResource = R.layout.adapter_item_large_media_status_preview_item
        }
    }

    companion object {
        const val layoutResource = R.layout.adapter_item_large_media_status
    }
}