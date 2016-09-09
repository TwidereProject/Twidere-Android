package org.mariotaku.twidere.view.holder

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_item_status.view.*
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ParcelableLocation
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText
import org.mariotaku.twidere.util.Utils.getUserTypeIconRes
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.lang.ref.WeakReference

/**
 * IDE gives me warning if I don't change default comment, so I wrote this XD
 *
 *
 * Created by mariotaku on 14/11/19.
 */
class StatusViewHolder(private val adapter: IStatusesAdapter<*>, itemView: View) : ViewHolder(itemView), Constants, IStatusViewHolder {

    override val profileImageView: ImageView by lazy { itemView.profileImage }
    override val profileTypeView: ImageView by lazy { itemView.profileType }

    private val itemContent by lazy { itemView.itemContent }
    private val mediaPreview by lazy { itemView.mediaPreview }
    private val statusContentUpperSpace by lazy { itemView.statusContentUpperSpace }
    private val textView by lazy { itemView.text }
    private val nameView by lazy { itemView.name }
    private val itemMenu by lazy { itemView.itemMenu }
    private val statusInfoLabel by lazy { itemView.statusInfoLabel }
    private val statusInfoIcon by lazy { itemView.statusInfoIcon }
    private val extraTypeView by lazy { itemView.extraType }
    private val quotedNameView by lazy { itemView.quotedName }
    private val timeView by lazy { itemView.time }
    private val replyCountView by lazy { itemView.replyCount }
    private val retweetCountView by lazy { itemView.retweetCount }
    private val quotedView by lazy { itemView.quotedView }
    private val quotedTextView by lazy { itemView.quotedText }
    private val actionButtons by lazy { itemView.actionButtons }
    private val mediaLabel by lazy { itemView.mediaLabel }
    private val quotedMediaLabel by lazy { itemView.quotedMediaLabel }
    private val statusContentLowerSpace by lazy { itemView.statusContentLowerSpace }
    private val quotedMediaPreview by lazy { itemView.quotedMediaPreview }
    private val favoriteIcon by lazy { itemView.favoriteIcon }
    private val retweetIcon by lazy { itemView.retweetIcon }
    private val favoriteCountView by lazy { itemView.favoriteCount }
    private val mediaLabelTextView by lazy { itemView.mediaLabelText }
    private val quotedMediaLabelTextView by lazy { itemView.quotedMediaLabelText }
    private val replyButton by lazy { itemView.reply }
    private val retweetButton by lazy { itemView.retweet }
    private val favoriteButton by lazy { itemView.favorite }

    private val eventListener: EventListener

    private var statusClickListener: IStatusViewHolder.StatusClickListener? = null


    init {
        this.eventListener = EventListener(this)

        if (adapter.mediaPreviewEnabled) {
            View.inflate(mediaPreview.context, R.layout.layout_card_media_preview,
                    itemView.mediaPreview)
            View.inflate(quotedMediaPreview.context, R.layout.layout_card_media_preview,
                    itemView.quotedMediaPreview)
        }
    }


    fun displaySampleStatus() {
        val profileImageEnabled = adapter.profileImageEnabled
        profileImageView.visibility = if (profileImageEnabled) View.VISIBLE else View.GONE
        statusContentUpperSpace.visibility = View.VISIBLE

        profileImageView.setImageResource(R.mipmap.ic_launcher)
        nameView.setName(Constants.TWIDERE_PREVIEW_NAME)
        nameView.setScreenName("@" + Constants.TWIDERE_PREVIEW_SCREEN_NAME)
        nameView.updateText(adapter.bidiFormatter)
        if (adapter.linkHighlightingStyle == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            val linkify = adapter.twidereLinkify
            val text = HtmlSpanBuilder.fromHtml(Constants.TWIDERE_PREVIEW_TEXT_HTML)
            linkify.applyAllLinks(text, null, -1, false, adapter.linkHighlightingStyle, true)
            textView.text = text
        } else {
            textView.text = toPlainText(Constants.TWIDERE_PREVIEW_TEXT_HTML)
        }
        timeView.setTime(System.currentTimeMillis())
        val showCardActions = isCardActionsShown
        if (adapter.mediaPreviewEnabled) {
            mediaPreview.visibility = View.VISIBLE
            mediaLabel.visibility = View.GONE
        } else {
            mediaPreview.visibility = View.GONE
            mediaLabel.visibility = View.VISIBLE
        }
        actionButtons.visibility = if (showCardActions) View.VISIBLE else View.GONE
        itemMenu.visibility = if (showCardActions) View.VISIBLE else View.GONE
        statusContentLowerSpace.visibility = if (showCardActions) View.GONE else View.VISIBLE
        quotedMediaPreview.visibility = View.GONE
        mediaPreview.displayMedia(R.drawable.nyan_stars_background)
        extraTypeView.setImageResource(R.drawable.ic_action_gallery)
    }

    override fun displayStatus(status: ParcelableStatus, displayInReplyTo: Boolean,
                               shouldDisplayExtraType: Boolean) {

        val loader = adapter.mediaLoader
        val twitter = adapter.twitterWrapper
        val linkify = adapter.twidereLinkify
        val formatter = adapter.bidiFormatter
        val context = adapter.context
        val nameFirst = adapter.nameFirst
        val showCardActions = isCardActionsShown

        actionButtons.visibility = if (showCardActions) View.VISIBLE else View.GONE
        itemMenu.visibility = if (showCardActions) View.VISIBLE else View.GONE
        statusContentLowerSpace.visibility = if (showCardActions) View.GONE else View.VISIBLE

        val replyCount = status.reply_count
        val retweetCount: Long
        val favoriteCount: Long

        if (status.is_pinned_status) {
            statusInfoLabel.setText(R.string.pinned_status)
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_pinned)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE

            statusContentUpperSpace.visibility = View.GONE
        } else if (TwitterCardUtils.isPoll(status)) {
            statusInfoLabel.setText(R.string.label_poll)
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_poll)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE

            statusContentUpperSpace.visibility = View.GONE
        } else if (status.retweet_id != null) {
            val retweetedBy = UserColorNameManager.decideDisplayName(status.retweet_user_nickname,
                    status.retweeted_by_user_name, status.retweeted_by_user_screen_name, nameFirst)
            statusInfoLabel.text = context.getString(R.string.name_retweeted, formatter.unicodeWrap(retweetedBy))
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_retweet)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE

            statusContentUpperSpace.visibility = View.GONE
        } else if (status.in_reply_to_status_id != null && status.in_reply_to_user_id != null && displayInReplyTo) {
            val inReplyTo = UserColorNameManager.decideDisplayName(status.in_reply_to_user_nickname,
                    status.in_reply_to_name, status.in_reply_to_screen_name, nameFirst)
            statusInfoLabel.text = context.getString(R.string.in_reply_to_name, formatter.unicodeWrap(inReplyTo))
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_reply)
            statusInfoLabel.visibility = View.VISIBLE
            statusInfoIcon.visibility = View.VISIBLE

            statusContentUpperSpace.visibility = View.GONE
        } else {
            statusInfoLabel.visibility = View.GONE
            statusInfoIcon.visibility = View.GONE

            statusContentUpperSpace.visibility = View.VISIBLE
        }

        val skipLinksInText = status.extras != null && status.extras.support_entities
        if (status.is_quote) {

            quotedView.visibility = View.VISIBLE

            val quoteContentAvailable = status.quoted_text_plain != null && status.quoted_text_unescaped != null
            if (quoteContentAvailable) {

                quotedNameView.visibility = View.VISIBLE
                quotedTextView.visibility = View.VISIBLE

                quotedNameView.setName(UserColorNameManager.decideNickname(status.quoted_user_nickname,
                        status.quoted_user_name))
                quotedNameView.setScreenName("@" + status.quoted_user_screen_name)

                var quotedDisplayEnd = -1
                if (status.extras.quoted_display_text_range != null) {
                    quotedDisplayEnd = status.extras.quoted_display_text_range!![1]
                }
                val quotedText: CharSequence
                if (adapter.linkHighlightingStyle != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                    quotedText = SpannableStringBuilder.valueOf(status.quoted_text_unescaped)
                    ParcelableStatusUtils.applySpans(quotedText as Spannable, status.quoted_spans)
                    linkify.applyAllLinks(quotedText, status.account_key, layoutPosition.toLong(),
                            status.is_possibly_sensitive, adapter.linkHighlightingStyle,
                            skipLinksInText)
                } else {
                    quotedText = status.quoted_text_unescaped
                }
                if (quotedDisplayEnd != -1 && quotedDisplayEnd <= quotedText.length) {
                    quotedTextView.text = quotedText.subSequence(0, quotedDisplayEnd)
                } else {
                    quotedTextView.text = quotedText
                }

                if (quotedTextView.length() == 0) {
                    // No text
                    quotedTextView.visibility = View.GONE
                } else {
                    quotedTextView.visibility = View.VISIBLE
                }

                if (status.quoted_user_color != 0) {
                    quotedView.drawStart(status.quoted_user_color)
                } else {
                    quotedView.drawStart(ThemeUtils.getColorFromAttribute(context, R.attr.quoteIndicatorBackgroundColor, 0))
                }

                if (status.quoted_media?.isNotEmpty() ?: false) {

                    if (!adapter.sensitiveContentEnabled && status.is_possibly_sensitive) {
                        // Sensitive content, show label instead of media view
                        quotedMediaPreview.visibility = View.GONE
                        quotedMediaLabel.visibility = View.VISIBLE
                    } else if (!adapter.mediaPreviewEnabled) {
                        // Media preview disabled, just show label
                        quotedMediaPreview.visibility = View.GONE
                        quotedMediaLabel.visibility = View.VISIBLE
                    } else {
                        // Show media
                        quotedMediaPreview.visibility = View.VISIBLE
                        quotedMediaLabel.visibility = View.GONE

                        quotedMediaPreview.displayMedia(status.quoted_media, loader, status.account_key, -1,
                                null, null)
                    }
                } else {
                    // No media, hide all related views
                    quotedMediaPreview.visibility = View.GONE
                    quotedMediaLabel.visibility = View.GONE
                }
            } else {
                quotedNameView.visibility = View.GONE
                quotedTextView.visibility = View.VISIBLE

                // Not available
                val string = SpannableString.valueOf(context.getString(R.string.status_not_available_text))
                string.setSpan(ForegroundColorSpan(ThemeUtils.getColorFromAttribute(context,
                        android.R.attr.textColorTertiary, textView.currentTextColor)), 0,
                        string.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                quotedTextView.text = string

                quotedView.drawStart(ThemeUtils.getColorFromAttribute(context, R.attr.quoteIndicatorBackgroundColor, 0))
            }

            itemContent.drawStart(status.user_color)
        } else {
            quotedView.visibility = View.GONE

            if (status.is_retweet) {
                val retweetUserColor = status.retweet_user_color
                val userColor = status.user_color
                if (retweetUserColor == 0) {
                    itemContent.drawStart(userColor)
                } else if (userColor == 0) {
                    itemContent.drawStart(retweetUserColor)
                } else {
                    itemContent.drawStart(retweetUserColor, userColor)
                }
            } else {
                itemContent.drawStart(status.user_color)
            }
        }

        if (status.is_retweet) {
            timeView.setTime(status.retweet_timestamp)
        } else {
            timeView.setTime(status.timestamp)
        }
        nameView.setName(UserColorNameManager.decideNickname(status.user_nickname, status.user_name))
        nameView.setScreenName("@${status.user_screen_name}")

        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            loader.displayProfileImage(profileImageView, status)

            profileTypeView.setImageResource(getUserTypeIconRes(status.user_is_verified, status.user_is_protected))
            profileTypeView.visibility = View.VISIBLE
        } else {
            profileImageView.visibility = View.GONE
            loader.cancelDisplayTask(profileImageView)

            profileTypeView.setImageDrawable(null)
            profileTypeView.visibility = View.GONE
        }

        if (adapter.showAccountsColor) {
            itemContent.drawEnd(status.account_color)
        } else {
            itemContent.drawEnd()
        }

        if (status.media?.isNotEmpty() ?: false) {

            if (!adapter.sensitiveContentEnabled && status.is_possibly_sensitive) {
                // Sensitive content, show label instead of media view
                mediaLabel.visibility = View.VISIBLE
                mediaPreview.visibility = View.GONE
            } else if (!adapter.mediaPreviewEnabled) {
                // Media preview disabled, just show label
                mediaLabel.visibility = View.VISIBLE
                mediaPreview.visibility = View.GONE
            } else {
                // Show media
                mediaLabel.visibility = View.GONE
                mediaPreview.visibility = View.VISIBLE

                mediaPreview.displayMedia(status.media, loader, status.account_key, -1, this,
                        adapter.mediaLoadingHandler)
            }
        } else {
            // No media, hide all related views
            mediaLabel.visibility = View.GONE
            mediaPreview.visibility = View.GONE
        }



        var displayEnd = -1
        if (status.extras.display_text_range != null) {
            displayEnd = status.extras.display_text_range!![1]
        }

        val text: CharSequence
        if (adapter.linkHighlightingStyle != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            text = SpannableStringBuilder.valueOf(status.text_unescaped)
            ParcelableStatusUtils.applySpans(text as Spannable, status.spans)
            linkify.applyAllLinks(text, status.account_key, layoutPosition.toLong(),
                    status.is_possibly_sensitive, adapter.linkHighlightingStyle,
                    skipLinksInText)
        } else {
            text = status.text_unescaped
        }

        if (displayEnd != -1 && displayEnd <= text.length) {
            textView.text = text.subSequence(0, displayEnd)
        } else {
            textView.text = text
        }
        if (textView.length() == 0) {
            // No text
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
        }

        if (replyCount > 0) {
            replyCountView.text = UnitConvertUtils.calculateProperCount(replyCount)
            replyCountView.visibility = View.VISIBLE
        } else {
            replyCountView.text = null
            replyCountView.visibility = View.GONE
        }

        if (twitter.isDestroyingStatus(status.account_key, status.my_retweet_id)) {
            retweetIcon.isActivated = false
            retweetCount = Math.max(0, status.retweet_count - 1)
        } else {
            val creatingRetweet = twitter.isCreatingRetweet(status.account_key, status.id)
            retweetIcon.isActivated = creatingRetweet || status.retweeted ||
                    Utils.isMyRetweet(status.account_key, status.retweeted_by_user_key,
                            status.my_retweet_id)
            retweetCount = status.retweet_count + if (creatingRetweet) 1 else 0
        }

        if (retweetCount > 0) {
            retweetCountView.text = UnitConvertUtils.calculateProperCount(retweetCount)
            retweetCountView.visibility = View.VISIBLE
        } else {
            retweetCountView.text = null
            retweetCountView.visibility = View.GONE
        }
        if (twitter.isDestroyingFavorite(status.account_key, status.id)) {
            favoriteIcon.isActivated = false
            favoriteCount = Math.max(0, status.favorite_count - 1)
        } else {
            val creatingFavorite = twitter.isCreatingFavorite(status.account_key, status.id)
            favoriteIcon.isActivated = creatingFavorite || status.is_favorite
            favoriteCount = status.favorite_count + if (creatingFavorite) 1 else 0
        }
        if (favoriteCount > 0) {
            favoriteCountView.text = UnitConvertUtils.calculateProperCount(favoriteCount)
            favoriteCountView.visibility = View.VISIBLE
        } else {
            favoriteCountView.text = null
            favoriteCountView.visibility = View.GONE
        }
        if (shouldDisplayExtraType) {
            displayExtraTypeIcon(status.card_name, status.media, status.location,
                    status.place_full_name, status.is_possibly_sensitive)
        } else {
            extraTypeView.visibility = View.GONE
        }

        nameView.updateText(formatter)
        quotedNameView.updateText(formatter)

    }

    override fun onMediaClick(view: View, media: ParcelableMedia, accountKey: UserKey, extraId: Long) {
        statusClickListener?.onMediaClick(this, view, media, layoutPosition)
    }


    fun setOnClickListeners() {
        setStatusClickListener(adapter.statusClickListener)
    }

    override fun setStatusClickListener(listener: IStatusViewHolder.StatusClickListener?) {
        statusClickListener = listener
        itemContent.setOnClickListener(eventListener)
        itemContent.setOnLongClickListener(eventListener)

        itemMenu.setOnClickListener(eventListener)
        profileImageView.setOnClickListener(eventListener)
        replyButton.setOnClickListener(eventListener)
        retweetButton.setOnClickListener(eventListener)
        favoriteButton.setOnClickListener(eventListener)

        mediaLabel.setOnClickListener(eventListener)
    }


    override fun setTextSize(textSize: Float) {
        nameView.setPrimaryTextSize(textSize)
        quotedNameView.setPrimaryTextSize(textSize)
        textView.textSize = textSize
        quotedTextView.textSize = textSize
        nameView.setSecondaryTextSize(textSize * 0.85f)
        quotedNameView.setSecondaryTextSize(textSize * 0.85f)
        timeView.textSize = textSize * 0.85f
        statusInfoLabel.textSize = textSize * 0.75f

        mediaLabelTextView.textSize = textSize * 0.95f

        replyCountView.textSize = textSize
        retweetCountView.textSize = textSize
        favoriteCountView.textSize = textSize
    }

    fun setupViewOptions() {
        setTextSize(adapter.textSize)
        mediaPreview.setStyle(adapter.mediaPreviewStyle)
        quotedMediaPreview.setStyle(adapter.mediaPreviewStyle)
        //        profileImageView.setStyle(adapter.getProfileImageStyle());

        val nameFirst = adapter.nameFirst
        nameView.setNameFirst(nameFirst)
        quotedNameView.setNameFirst(nameFirst)

        val favIcon: Int
        val favStyle: Int
        val favColor: Int
        val context = adapter.context
        if (adapter.useStarsForLikes) {
            favIcon = R.drawable.ic_action_star
            favStyle = LikeAnimationDrawable.Style.FAVORITE
            favColor = ContextCompat.getColor(context, R.color.highlight_favorite)
        } else {
            favIcon = R.drawable.ic_action_heart
            favStyle = LikeAnimationDrawable.Style.LIKE
            favColor = ContextCompat.getColor(context, R.color.highlight_like)
        }
        val icon = ContextCompat.getDrawable(context, favIcon)
        val drawable = LikeAnimationDrawable(icon,
                favoriteCountView.textColors.defaultColor, favColor, favStyle)
        drawable.mutate()
        favoriteIcon.setImageDrawable(drawable)
        timeView.setShowAbsoluteTime(adapter.isShowAbsoluteTime)

        favoriteIcon.activatedColor = favColor
    }

    override fun playLikeAnimation(listener: LikeAnimationDrawable.OnLikedListener) {
        var handled = false
        val drawable = favoriteIcon.drawable
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

    private fun showCardActions() {
        adapter.showCardActions(layoutPosition)
    }

    private fun hideTempCardActions(): Boolean {
        adapter.showCardActions(RecyclerView.NO_POSITION)
        return !adapter.isCardActionsShown(RecyclerView.NO_POSITION)
    }

    private fun displayExtraTypeIcon(cardName: String?, media: Array<ParcelableMedia?>?,
                                     location: ParcelableLocation?, placeFullName: String?,
                                     sensitive: Boolean) {
        if (TwitterCardUtils.CARD_NAME_AUDIO == cardName) {
            extraTypeView.setImageResource(if (sensitive) R.drawable.ic_action_warning else R.drawable.ic_action_music)
            extraTypeView.visibility = View.VISIBLE
        } else if (TwitterCardUtils.CARD_NAME_ANIMATED_GIF == cardName) {
            extraTypeView.setImageResource(if (sensitive) R.drawable.ic_action_warning else R.drawable.ic_action_movie)
            extraTypeView.visibility = View.VISIBLE
        } else if (TwitterCardUtils.CARD_NAME_PLAYER == cardName) {
            extraTypeView.setImageResource(if (sensitive) R.drawable.ic_action_warning else R.drawable.ic_action_play_circle)
            extraTypeView.visibility = View.VISIBLE
        } else if (media?.isNotEmpty() ?: false) {
            if (hasVideo(media)) {
                extraTypeView.setImageResource(if (sensitive) R.drawable.ic_action_warning else R.drawable.ic_action_movie)
            } else {
                extraTypeView.setImageResource(if (sensitive) R.drawable.ic_action_warning else R.drawable.ic_action_gallery)
            }
            extraTypeView.visibility = View.VISIBLE
        } else if (ParcelableLocationUtils.isValidLocation(location) || !TextUtils.isEmpty(placeFullName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_location)
            extraTypeView.visibility = View.VISIBLE
        } else {
            extraTypeView.visibility = View.GONE
        }
    }

    private fun hasVideo(media: Array<ParcelableMedia?>?): Boolean {
        if (media == null) return false
        for (item in media) {
            if (item == null) continue
            when (item.type) {
                ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF, ParcelableMedia.Type.EXTERNAL_PLAYER -> return true
            }
        }
        return false
    }

    internal class EventListener(holder: StatusViewHolder) : OnClickListener, OnLongClickListener {

        val holderRef: WeakReference<StatusViewHolder>

        init {
            this.holderRef = WeakReference(holder)
        }

        override fun onClick(v: View) {
            val holder = holderRef.get() ?: return
            val listener = holder.statusClickListener ?: return
            val position = holder.layoutPosition
            when (v) {
                holder.itemContent -> {
                    listener.onStatusClick(holder, position)
                }
                holder.itemMenu -> {
                    listener.onItemMenuClick(holder, v, position)
                }
                holder.profileImageView -> {
                    listener.onUserProfileClick(holder, position)
                }
                holder.replyButton -> {
                    listener.onItemActionClick(holder, R.id.reply, position)
                }
                holder.retweetButton -> {
                    listener.onItemActionClick(holder, R.id.retweet, position)
                }
                holder.favoriteButton -> {
                    listener.onItemActionClick(holder, R.id.favorite, position)
                }
                holder.mediaLabel -> {
                    val firstMedia = holder.adapter.getStatus(position)?.media?.firstOrNull() ?: return
                    listener.onMediaClick(holder, v, firstMedia, position)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            val holder = holderRef.get() ?: return false
            val listener = holder.statusClickListener ?: return false
            val position = holder.layoutPosition
            when (v) {
                holder.itemContent -> {
                    if (!holder.isCardActionsShown) {
                        holder.showCardActions()
                        return true
                    } else if (holder.hideTempCardActions()) {
                        return true
                    }
                    return listener.onStatusLongClick(holder, position)
                }
            }
            return false
        }
    }


}
