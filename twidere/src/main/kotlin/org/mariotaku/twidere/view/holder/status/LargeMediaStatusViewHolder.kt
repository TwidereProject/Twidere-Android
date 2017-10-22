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

package org.mariotaku.twidere.view.holder.status

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.adapter_item_large_media_status.view.*
import kotlinx.android.synthetic.main.adapter_item_large_media_status_preview_item.view.*
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.ktextension.toLocalizedString
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.RecyclerPagerAdapter
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.aspect_ratio
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class LargeMediaStatusViewHolder(private val adapter: IStatusesAdapter, itemView: View) :
        RecyclerView.ViewHolder(itemView), IStatusViewHolder {
    override val profileImageView: ProfileImageView = itemView.profileImage
    override val profileTypeView: ImageView? = null

    private val mediaPreviewAdapter: ImagePagerAdapter
    private val mediaPreviewPager = itemView.mediaPreviewPager
    private val nameView = itemView.nameView
    private val textView = itemView.text
    private val timeView = itemView.time
    private val countsLabel = itemView.countsLabel
    private val replyButton = itemView.reply
    private val favoriteButton = itemView.favorite
    private val retweetButton = itemView.retweet
    private val itemMenuButton = itemView.itemMenu

    private val eventHandler = EventHandler()

    private var statusClickListener: IStatusViewHolder.StatusClickListener? = null


    init {
        mediaPreviewAdapter = ImagePagerAdapter(adapter, this)
        mediaPreviewPager.adapter = mediaPreviewAdapter
    }

    override fun display(status: ParcelableStatus, displayInReplyTo: Boolean,
            displayPinned: Boolean) {
        val context = itemView.context
        val linkify = adapter.twidereLinkify

        adapter.requestManager.loadProfileImage(context, status,
                adapter.profileImageStyle, profileImageView.cornerRadius,
                profileImageView.cornerRadiusRatio).into(profileImageView)

        nameView.name = status.user_name
        nameView.screenName = "@${status.user_screen_name}"
        nameView.updateText(adapter.bidiFormatter)

        timeView.time = status.timestamp

        val skipLinksInText = status.extras?.support_entities ?: false

        val text: CharSequence
        val displayEnd: Int
        if (adapter.linkHighlightingStyle != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            text = SpannableStringBuilder.valueOf(status.text_unescaped).apply {
                status.spans?.applyTo(this)
                linkify.applyAllLinks(this, status.account_key, layoutPosition.toLong(),
                        status.is_possibly_sensitive, adapter.linkHighlightingStyle,
                        skipLinksInText)
            }
            displayEnd = status.extras?.display_text_range?.getOrNull(1) ?: -1
        } else {
            text = status.text_unescaped
            displayEnd = status.extras?.display_text_range?.getOrNull(1) ?: -1
        }

        if (displayEnd != -1 && displayEnd <= text.length) {
            textView.spannable = text.subSequence(0, displayEnd)
        } else {
            textView.spannable = text
        }
        textView.hideIfEmpty()

        replyButton.isActivated = StatusViewHolder.isRetweetIconActivated(status)
        favoriteButton.isActivated = StatusViewHolder.isFavoriteIconActivated(status)

        val aspectRatio = status.media?.fold(Double.NaN) { acc, media ->
            if (acc.isNaN()) return@fold media.aspect_ratio
            return@fold (acc + media.aspect_ratio) / 2
        } ?: Double.NaN

        val countsTexts = mutableListOf<String>()
        if (status.favorite_count > 0) {
            if (adapter.useStarsForLikes) {
                countsTexts.add(context.resources.getQuantityString(R.plurals.N_favorites_abbrev,
                        status.favorite_count.toInt(), status.favorite_count.toLocalizedString()))
            } else {
                countsTexts.add(context.resources.getQuantityString(R.plurals.N_likes_abbrev,
                        status.favorite_count.toInt(), status.favorite_count.toLocalizedString()))
            }
        }
        if (status.retweet_count > 0) {
            countsTexts.add(context.resources.getQuantityString(R.plurals.N_retweets_abbrev,
                    status.retweet_count.toInt(), status.retweet_count.toLocalizedString()))
        }
        countsLabel.text = countsTexts.joinToString(separator = context.getString(R.string.label_item_separator_comma_localized))
        countsLabel.hideIfEmpty()

        mediaPreviewPager.setAspectRatio(if (aspectRatio > 0) (1 / aspectRatio).coerceIn(0.5, 1.5) else 1.0)

        mediaPreviewAdapter.media = status.media
    }

    override fun onMediaClick(view: View, current: ParcelableMedia, accountKey: UserKey?, id: Long) {
    }

    override fun setStatusClickListener(listener: IStatusViewHolder.StatusClickListener?) {
        statusClickListener = listener
        itemView.itemContent.setOnClickListener(eventHandler)
        replyButton.setOnClickListener(eventHandler)
        retweetButton.setOnClickListener(eventHandler)
        favoriteButton.setOnClickListener(eventHandler)
        itemMenuButton.setOnClickListener(eventHandler)
    }

    override fun setTextSize(textSize: Float) {
        nameView.setPrimaryTextSize(textSize * 0.85f)
        nameView.setSecondaryTextSize(textSize * 0.75f)
        textView.textSize = textSize * 0.9f
        countsLabel.textSize = textSize * 0.85f
        timeView.textSize = textSize * 0.8f
    }

    override fun playLikeAnimation(listener: LikeAnimationDrawable.OnLikedListener) {
        var handled = false
        val drawable = favoriteButton.drawable
        if (drawable is LikeAnimationDrawable) {
            drawable.setOnLikedListener(listener)
            drawable.start()
            handled = true
        }
        if (!handled) {
            listener.onLiked()
        }
    }

    fun setOnClickListeners() {
        setStatusClickListener(adapter.statusClickListener)
    }

    fun setupViewOptions() {
        profileImageView.style = adapter.profileImageStyle
        nameView.nameFirst = adapter.nameFirst
        timeView.showAbsoluteTime = adapter.showAbsoluteTime

        val context = itemView.context
        val drawable = if (adapter.useStarsForLikes) {
            LikeAnimationDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star),
                    favoriteButton.defaultColor, ContextCompat.getColor(context, R.color.highlight_favorite),
                    LikeAnimationDrawable.Style.FAVORITE)
        } else {
            LikeAnimationDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_heart),
                    favoriteButton.defaultColor, ContextCompat.getColor(context, R.color.highlight_like),
                    LikeAnimationDrawable.Style.LIKE)
        }
        drawable.mutate()

        favoriteButton.setImageDrawable(drawable.mutate())
        favoriteButton.activatedColor = drawable.activatedColor

        nameView.applyFontFamily(adapter.lightFont)
        textView.applyFontFamily(adapter.lightFont)
        countsLabel.applyFontFamily(adapter.lightFont)
        timeView.applyFontFamily(adapter.lightFont)

        setTextSize(adapter.textSize)
    }

    private fun View.setAspectRatio(ratio: Double) {
        val lp = layoutParams as? ConstraintLayout.LayoutParams ?: return
        lp.dimensionRatio = "w,$ratio:1"
    }

    private class ImagePagerAdapter(
            val parentAdapter: IStatusesAdapter,
            val parentHolder: LargeMediaStatusViewHolder
    ) : RecyclerPagerAdapter<LargeMediaItemHolder>() {
        var media: Array<ParcelableMedia>? = null
            set(value) {
                field = value
                notifyPagesChanged()
            }

        override fun getCount() = media?.size ?: 0

        override fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): LargeMediaItemHolder {
            return LargeMediaItemHolder(this, LayoutInflater.from(container.context)
                    .inflate(LargeMediaItemHolder.layoutResource, container, false))
        }

        override fun onBindViewHolder(holder: LargeMediaItemHolder, position: Int, itemViewType: Int) {
            holder.display(media!![position])
        }

        fun getMedia(position: Int): ParcelableMedia = media!![position]

    }

    private class LargeMediaItemHolder(val adapter: ImagePagerAdapter, itemView: View) : RecyclerPagerAdapter.ViewHolder(itemView) {

        val detector = GestureDetector(itemView.context, OnGestureHandler())

        init {
            itemView.setOnTouchListener { _, ev -> detector.onTouchEvent(ev) }

        }

        private val mediaPreview = itemView.mediaPreview

        fun display(media: ParcelableMedia) {
            adapter.parentAdapter.requestManager.load(media.preview_url).centerCrop().into(mediaPreview)
            mediaPreview.hasPlayIcon = ParcelableMediaUtils.hasPlayIcon(media.type)
        }

        private inner class OnGestureHandler : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent?) = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val listener = adapter.parentAdapter.statusClickListener ?: return false
                listener.onMediaClick(adapter.parentHolder, itemView, adapter.getMedia(position),
                        adapter.parentHolder.layoutPosition)
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                if (e.action != MotionEvent.ACTION_UP) return false
                val listener = adapter.parentAdapter.statusClickListener ?: return false
                listener.onItemActionClick(adapter.parentHolder, R.id.favorite,
                        adapter.parentHolder.layoutPosition)
                return true
            }
        }

        companion object {
            val layoutResource = R.layout.adapter_item_large_media_status_preview_item
        }
    }

    private inner class EventHandler : View.OnClickListener, View.OnLongClickListener {

        override fun onClick(v: View) {
            val listener = statusClickListener ?: return
            val position = layoutPosition
            when (v) {
                itemView.itemContent -> {
                    listener.onStatusClick(this@LargeMediaStatusViewHolder, position)
                }
                profileImageView -> {
                    listener.onUserProfileClick(this@LargeMediaStatusViewHolder, position)
                }
                replyButton -> {
                    listener.onItemActionClick(this@LargeMediaStatusViewHolder, R.id.reply, position)
                }
                retweetButton -> {
                    listener.onItemActionClick(this@LargeMediaStatusViewHolder, R.id.retweet, position)
                }
                favoriteButton -> {
                    listener.onItemActionClick(this@LargeMediaStatusViewHolder, R.id.favorite, position)
                }
                itemMenuButton -> {
                    listener.onItemMenuClick(this@LargeMediaStatusViewHolder, v, position)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            return false
        }

    }

    companion object {
        const val layoutResource = R.layout.adapter_item_large_media_status
    }
}