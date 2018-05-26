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

import android.content.res.ColorStateList
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_status.view.*
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.microblog.library.annotation.mastodon.StatusVisibility
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.RecyclerViewTypes
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
import org.mariotaku.twidere.extension.inflate
import org.mariotaku.twidere.extension.model.displayInfo
import org.mariotaku.twidere.extension.model.retweeted_by_user_acct
import org.mariotaku.twidere.extension.model.type
import org.mariotaku.twidere.extension.model.user_acct
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.extension.text.appendCompat
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableLocation
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.placeholder.PlaceholderObject
import org.mariotaku.twidere.singleton.BidiFormatterSingleton
import org.mariotaku.twidere.task.CreateFavoriteTask
import org.mariotaku.twidere.task.DestroyFavoriteTask
import org.mariotaku.twidere.task.DestroyStatusTask
import org.mariotaku.twidere.task.RetweetStatusTask
import org.mariotaku.twidere.text.TwidereClickableSpan
import org.mariotaku.twidere.text.style.PlaceholderLineSpan
import org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText
import org.mariotaku.twidere.util.HtmlSpanBuilder
import org.mariotaku.twidere.util.UnitConvertUtils
import org.mariotaku.twidere.util.UriCreator
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils.getUserTypeIconRes
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.ShortTimeView
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * [ViewHolder] class for standard status list item
 */
class StatusViewHolder(var adapter: IStatusesAdapter, itemView: View, subtype: Int = 0) : ViewHolder(itemView), IStatusViewHolder {

    override val profileImageView: ProfileImageView = itemView.profileImage
    override val profileTypeView: ImageView = itemView.profileType

    private val itemContent = itemView.itemContent
    private val textView = itemView.text
    private val nameView = itemView.name
    private val itemMenu = itemView.itemMenu
    private val statusInfoLabel = itemView.statusInfoLabel
    private val statusInfoIcon = itemView.statusInfoIcon
    private val timeView = itemView.time
    private val replyButton = itemView.reply
    private val retweetButton = itemView.retweet
    private val favoriteButton = itemView.favorite
    private val itemActionsGroup = itemView.itemActionsGroup
    private val attachmentContainer = itemView.attachmentContainer

    private val eventHandler = EventHandler()

    private val attachmentHolder = when (subtype) {
        RecyclerViewTypes.STATUS_MEDIA -> {
            MediaAttachmentHolder(this, attachmentContainer.inflate(R.layout.layout_content_item_attachment_media) as ConstraintLayout)
        }
        RecyclerViewTypes.STATUS_QUOTE -> {
            QuotedAttachmentHolder(this, attachmentContainer.inflate(R.layout.layout_content_item_attachment_quote) as ConstraintLayout)
        }
        RecyclerViewTypes.STATUS_SUMMARY -> {
            SummaryAttachmentHolder(this, adapter, attachmentContainer.inflate(R.layout.layout_content_item_attachment_summary) as ConstraintLayout)
        }
        else -> null
    }

    private val toggleFullTextSpan = TwidereClickableSpan(adapter.linkHighlightingStyle) {
        if (adapter.isFullTextVisible(layoutPosition)) {
            hideFullText()
        } else {
            showFullText()
        }
    }

    private var statusClickListener: IStatusViewHolder.StatusClickListener? = null

    fun preview() {
        val profileImageEnabled = adapter.profileImageEnabled
        profileImageView.visibility = if (profileImageEnabled) View.VISIBLE else View.GONE

        profileImageView.profileImage = UriCreator.resourceIdString(itemView.context.packageName,
                R.drawable.ic_profile_image_twidere)
        nameView.name = TWIDERE_PREVIEW_NAME
        nameView.screenName = "@$TWIDERE_PREVIEW_SCREEN_NAME"
        nameView.updateText(BidiFormatterSingleton.get())
        if (adapter.linkHighlightingStyle == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.spannable = toPlainText(TWIDERE_PREVIEW_TEXT_HTML)
        } else {
            val linkify = adapter.twidereLinkify
            val text = HtmlSpanBuilder.fromHtml(TWIDERE_PREVIEW_TEXT_HTML)
            linkify.applyAllLinks(text, null, -1, false, adapter.linkHighlightingStyle, true)
            textView.spannable = text
        }
        timeView.time = System.currentTimeMillis()
        val showCardActions = isCardActionsShown
        if (adapter.mediaPreviewEnabled) {
            attachmentContainer.visibility = View.VISIBLE
        } else {
            attachmentContainer.visibility = View.GONE
        }

        itemActionsGroup.setVisible(showCardActions)
    }

    fun placeholder() {
        val showCardActions = isCardActionsShown
        val actionButtonsAlpha = PlaceholderLineSpan.placeholderAlpha / 255f

        profileImageView.profileImage = null

        timeView.time = ShortTimeView.PLACEHOLDER
        textView.spannable = placeholderText
        nameView.placeholder = true
        nameView.updateText()


        replyButton.alpha = actionButtonsAlpha
        retweetButton.alpha = actionButtonsAlpha
        favoriteButton.alpha = actionButtonsAlpha
        itemMenu.alpha = actionButtonsAlpha

        replyButton.isActivated = false
        retweetButton.isActivated = false
        favoriteButton.isActivated = false

        replyButton.text = null
        retweetButton.text = null
        favoriteButton.text = null

        itemActionsGroup.setVisible(showCardActions)

        profileTypeView.visibility = View.GONE
        attachmentContainer.visibility = View.GONE
        statusInfoIcon.visibility = View.GONE
        statusInfoLabel.visibility = View.GONE
    }

    override fun display(status: ParcelableStatus, displayInReplyTo: Boolean, displayPinned: Boolean) {
        if (status is PlaceholderObject) {
            placeholder()
            return
        }
        val context = itemView.context
        val formatter = BidiFormatterSingleton.get()
        val colorNameManager = UserColorNameManager.get(context)
        val showCardActions = isCardActionsShown

        replyButton.alpha = 1f
        retweetButton.alpha = 1f
        favoriteButton.alpha = 1f
        itemMenu.alpha = 1f

        itemActionsGroup.setVisible(showCardActions)

        val replyCount = status.reply_count
        val retweetCount = status.retweet_count
        val favoriteCount = status.favorite_count

        if (displayPinned && status.is_pinned_status) {
            statusInfoLabel.setText(R.string.pinned_status)
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_pinned)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE
        } else if (status.retweet_id != null) {
            val retweetedBy = colorNameManager.getDisplayName(status.retweeted_by_user_key!!,
                    status.retweeted_by_user_name, status.retweeted_by_user_acct!!)
            statusInfoLabel.spannable = context.getString(R.string.name_retweeted, formatter.unicodeWrap(retweetedBy))
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_retweet)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE
        } else if (status.in_reply_to_status_id != null && status.in_reply_to_user_key != null && displayInReplyTo) {
            if (status.in_reply_to_name != null && status.in_reply_to_screen_name != null) {
                val inReplyTo = colorNameManager.getDisplayName(status.in_reply_to_user_key!!,
                        status.in_reply_to_name, status.in_reply_to_screen_name)
                statusInfoLabel.spannable = context.getString(R.string.in_reply_to_name, formatter.unicodeWrap(inReplyTo))
            } else {
                statusInfoLabel.spannable = context.getString(R.string.label_status_type_reply)
            }
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_reply)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE
        } else {
            statusInfoLabel.visibility = View.GONE
            statusInfoIcon.visibility = View.GONE
        }

        val display = status.displayInfo(context)
        val userColor = display.userColor

        if (status.is_retweet) {
            val retweetUserColor = display.retweetUserColor
            when {
                retweetUserColor == 0 -> itemContent.drawStart(userColor)
                userColor == 0 -> itemContent.drawStart(retweetUserColor)
                else -> itemContent.drawStart(retweetUserColor, userColor)
            }
        } else {
            itemContent.drawStart(userColor)
        }

        timeView.time = if (status.is_retweet) {
            status.retweet_timestamp
        } else {
            status.timestamp
        }

        nameView.placeholder = false
        nameView.name = colorNameManager.getUserNickname(status.user_key, status.user_name)
        nameView.screenName = "@${status.user_acct}"

        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            profileImageView.profileImage = status.user_profile_image_url

            profileTypeView.setImageResource(getUserTypeIconRes(status.user_is_verified, status.user_is_protected))
            profileTypeView.visibility = View.VISIBLE
        } else {
            profileImageView.visibility = View.GONE

            profileTypeView.setImageDrawable(null)
            profileTypeView.visibility = View.GONE
        }

        if (adapter.showAccountsColor) {
            itemContent.drawEnd(status.account_color)
        } else {
            itemContent.drawEnd()
        }


        textView.spannable = display.text
        textView.hideIfEmpty()

        if (replyCount > 0) {
            val properCount = UnitConvertUtils.calculateProperCount(replyCount)
            replyButton.text = properCount
            replyButton.contentDescription = context.resources.getQuantityString(R.plurals.N_replies_abbrev,
                    replyCount.toInt(), properCount)
        } else {
            replyButton.text = null
            replyButton.contentDescription = context.getString(R.string.action_reply)
        }
        replyButton.drawable?.mutate()

        when (status.extras?.visibility) {
            StatusVisibility.PRIVATE -> {
                retweetButton.setImageResource(R.drawable.ic_action_lock)
            }
            StatusVisibility.DIRECT -> {
                retweetButton.setImageResource(R.drawable.ic_action_message)
            }
            else -> {
                retweetButton.setImageResource(R.drawable.ic_action_retweet)
            }
        }
        retweetButton.drawable?.mutate()

        retweetButton.isActivated = isRetweetIconActivated(status)

        if (retweetCount > 0) {
            val properCount = UnitConvertUtils.calculateProperCount(retweetCount)
            retweetButton.text = properCount
            retweetButton.contentDescription = context.resources.getQuantityString(R.plurals.N_retweets_abbrev,
                    retweetCount.toInt(), properCount)
        } else {
            retweetButton.text = null
            retweetButton.contentDescription = context.getString(R.string.action_retweet)
        }

        favoriteButton.isActivated = isFavoriteIconActivated(status)

        if (favoriteCount > 0) {
            val properCount = UnitConvertUtils.calculateProperCount(favoriteCount)
            favoriteButton.text = properCount
            if (adapter.useStarsForLikes) {
                favoriteButton.contentDescription = context.resources.getQuantityString(R.plurals.N_favorites_abbrev,
                        favoriteCount.toInt(), properCount)
            } else {
                favoriteButton.contentDescription = context.resources.getQuantityString(R.plurals.N_likes_abbrev,
                        favoriteCount.toInt(), properCount)
            }
        } else {
            favoriteButton.text = null
            if (adapter.useStarsForLikes) {
                favoriteButton.contentDescription = context.getString(R.string.action_favorite)
            } else {
                favoriteButton.contentDescription = context.getString(R.string.action_like)
            }
        }

        nameView.updateText(formatter)

        itemView.contentDescription = display.contentDescription

        profileImageView.contentDescription = display.profileImageContentDescription

        attachmentContainer.setVisible(attachmentHolder != null)
        attachmentHolder?.display(status)
    }

    override fun onMediaClick(view: View, current: ParcelableMedia, accountKey: UserKey?, id: Long) {
    }

    override fun setStatusClickListener(listener: IStatusViewHolder.StatusClickListener?) {
        statusClickListener = listener
        itemContent.setOnClickListener(eventHandler)
        itemContent.setOnLongClickListener(eventHandler)

        itemMenu.setOnClickListener(eventHandler)
        profileImageView.setOnClickListener(eventHandler)
        replyButton.setOnClickListener(eventHandler)
        retweetButton.setOnClickListener(eventHandler)
        favoriteButton.setOnClickListener(eventHandler)
        retweetButton.setOnLongClickListener(eventHandler)
        favoriteButton.setOnLongClickListener(eventHandler)

        attachmentContainer.setOnClickListener(eventHandler)
    }


    override fun setTextSize(textSize: Float) {
        nameView.setPrimaryTextSize(textSize)
        textView.textSize = textSize
        nameView.setSecondaryTextSize(textSize * 0.85f)
        timeView.textSize = textSize * 0.85f
        statusInfoLabel.textSize = textSize * 0.75f

        replyButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        retweetButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        favoriteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        nameView.updateTextAppearance()

        attachmentHolder?.setTextSize(textSize)
    }

    fun setupViewOptions(adapter: IStatusesAdapter) {
        setTextSize(adapter.textSize)

        profileImageView.style = adapter.profileImageStyle

        val nameFirst = adapter.nameFirst
        nameView.nameFirst = nameFirst

        val context = itemView.context
        val favoriteTint: ColorStateList
        val drawable: LikeAnimationDrawable
        if (adapter.useStarsForLikes) {
            favoriteTint = ContextCompat.getColorStateList(context, R.color.btn_tint_like_stateful)!!
            drawable = LikeAnimationDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_star),
                    LikeAnimationDrawable.Style.FAVORITE)
        } else {
            favoriteTint = ContextCompat.getColorStateList(context, R.color.btn_tint_like_stateful)!!
            drawable = LikeAnimationDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_heart),
                    LikeAnimationDrawable.Style.LIKE)
        }
        drawable.mutate()
        favoriteButton.setImageDrawable(drawable)
        ImageViewCompat.setImageTintList(favoriteButton, favoriteTint)

        timeView.showAbsoluteTime = adapter.showAbsoluteTime

        nameView.applyFontFamily(adapter.lightFont)
        timeView.applyFontFamily(adapter.lightFont)
        textView.applyFontFamily(adapter.lightFont)
        attachmentHolder?.setupViewOptions(adapter)
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

    private val isCardActionsShown: Boolean
        get() = adapter.isCardActionsShown(layoutPosition)

    private val isFullTextVisible: Boolean
        get() = adapter.isFullTextVisible(layoutPosition)

    private fun showCardActions() {
        adapter.showCardActions(layoutPosition)
    }

    private fun hideTempCardActions(): Boolean {
        adapter.showCardActions(RecyclerView.NO_POSITION)
        return !adapter.isCardActionsShown(RecyclerView.NO_POSITION)
    }

    private fun showFullText() {
        adapter.setFullTextVisible(layoutPosition, true)
    }

    private fun hideFullText(): Boolean {
        adapter.setFullTextVisible(layoutPosition, false)
        return !adapter.isFullTextVisible(RecyclerView.NO_POSITION)
    }

    private fun TextView.displayAttachmentLabel(cardName: String?, media: Array<ParcelableMedia?>?,
            location: ParcelableLocation?, placeFullName: String?, sensitive: Boolean): Boolean {
        var result = false
        when {
            media != null && media.isNotEmpty() -> {
                when {
                    sensitive -> {
                        setLabelIcon(R.drawable.ic_label_warning)
                        setText(R.string.label_sensitive_content)
                    }
                    media.type in videoTypes -> {
                        setLabelIcon(R.drawable.ic_label_video)
                        setText(R.string.label_video)
                    }
                    media.size > 1 -> {
                        setLabelIcon(R.drawable.ic_label_gallery)
                        setText(R.string.label_photos)
                    }
                    else -> {
                        setLabelIcon(R.drawable.ic_label_gallery)
                        setText(R.string.label_photo)
                    }
                }
                result = true
            }
            cardName != null -> if (cardName.startsWith("poll")) {
                setLabelIcon(R.drawable.ic_label_poll)
                setText(R.string.label_poll)
                result = true
            }
//            placeFullName != null -> {
//                setLabelIcon(R.drawable.ic_label_location)
//                text = placeFullName
//                result = true
//            }
//            location != null -> {
//                setLabelIcon(R.drawable.ic_label_location)
//                setText(R.string.action_view_map)
//                result = true
//            }
        }
        refreshDrawableState()
        setVisible(result)
        return result
    }

    private fun TextView.setLabelIcon(@DrawableRes icon: Int) {
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, icon, 0, 0, 0)
    }

    abstract class AttachmentHolder(val parent: StatusViewHolder, val view: ConstraintLayout) {
        open fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {}
        open fun setupViewOptions(adapter: IStatusesAdapter) {}
        open fun setTextSize(textSize: Float) {}
        abstract fun display(status: ParcelableStatus)
    }

    private inner class EventHandler : OnClickListener, OnLongClickListener {

        override fun onClick(v: View) {
            val listener = statusClickListener ?: return
            val position = layoutPosition
            when (v) {
                itemContent -> {
                    listener.onStatusClick(this@StatusViewHolder, position)
                }
                itemMenu -> {
                    listener.onItemMenuClick(this@StatusViewHolder, v, position)
                }
                profileImageView -> {
                    listener.onUserProfileClick(this@StatusViewHolder, position)
                }
                replyButton -> {
                    listener.onItemActionClick(this@StatusViewHolder, R.id.reply, position)
                }
                retweetButton -> {
                    listener.onItemActionClick(this@StatusViewHolder, R.id.retweet, position)
                }
                favoriteButton -> {
                    listener.onItemActionClick(this@StatusViewHolder, R.id.favorite, position)
                }
                else -> {
                    attachmentHolder?.onClick(listener, this@StatusViewHolder, v, position)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            val listener = statusClickListener ?: return false
            val position = layoutPosition
            when (v) {
                itemContent -> {
                    if (!isCardActionsShown) {
                        showCardActions()
                        return true
                    } else if (hideTempCardActions()) {
                        return true
                    }
                    return listener.onStatusLongClick(this@StatusViewHolder, position)
                }
                favoriteButton -> {
                    return listener.onItemActionLongClick(this@StatusViewHolder, R.id.favorite, position)
                }
                retweetButton -> {
                    return listener.onItemActionLongClick(this@StatusViewHolder, R.id.retweet, position)
                }
            }
            return false
        }
    }

    companion object {
        const val layoutResource = R.layout.list_item_status

        private val videoTypes = intArrayOf(ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF,
                ParcelableMedia.Type.EXTERNAL_PLAYER)

        val placeholderText: Spanned = SpannableStringBuilder().apply {
            appendCompat(" ", PlaceholderLineSpan(1f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            appendln()
            appendCompat(" ", PlaceholderLineSpan(1f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            appendln()
            appendCompat(" ", PlaceholderLineSpan(1f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            appendln()
            appendCompat(" ", PlaceholderLineSpan(.5f), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        fun isRetweetIconActivated(status: ParcelableStatus): Boolean {
            return !DestroyStatusTask.isRunning(status.account_key, status.my_retweet_id) &&
                    (RetweetStatusTask.isRunning(status.account_key, status.id) ||
                            status.retweeted || status.account_key == status.retweeted_by_user_key ||
                            status.my_retweet_id != null)
        }

        fun isFavoriteIconActivated(status: ParcelableStatus): Boolean {
            return !DestroyFavoriteTask.isRunning(status.account_key, status.id) &&
                    (CreateFavoriteTask.isRunning(status.account_key, status.id) || status.is_favorite)
        }

    }
}

