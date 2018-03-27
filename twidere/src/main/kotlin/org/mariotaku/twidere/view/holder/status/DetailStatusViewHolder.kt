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

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.support.annotation.UiThread
import android.support.v4.view.ViewCompat
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_item_status_count_label.view.*
import kotlinx.android.synthetic.main.header_status.view.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.model.twitter.TranslationResult
import org.mariotaku.twidere.Constants.MENU_GROUP_STATUS_SHARE
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.StatusDetailsAdapter
import org.mariotaku.twidere.annotation.ProfileImageSize
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.data.status.StatusActivitySummaryLiveData
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.fragment.status.StatusFragment
import org.mariotaku.twidere.fragment.timeline.AbsTimelineFragment
import org.mariotaku.twidere.menu.FavoriteItemProvider
import org.mariotaku.twidere.menu.RetweetItemProvider
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.UserColorNameManager.Companion
import org.mariotaku.twidere.util.twitter.card.StatusCardViewFactory
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.TimelineContentTextView
import java.util.*

class DetailStatusViewHolder(
        private val adapter: StatusDetailsAdapter,
        itemView: View
) : RecyclerView.ViewHolder(itemView), View.OnClickListener, ActionMenuView.OnMenuItemClickListener {

    private val linkClickHandler: StatusLinkClickHandler
    private val linkify: TwidereLinkify

    private val profileTypeView = itemView.profileType
    private val nameView = itemView.name
    private val summaryView = itemView.summary
    private val textView = itemView.text
    private val locationView = itemView.locationView
    private val retweetedByView = itemView.retweetedBy
    private val translateResultView = itemView.translateResult
    private val translateChangeLanguageView = itemView.translateChangeLanguage
    private val translateContainer = itemView.translateContainer
    private val translateLabelView = itemView.translateLabel
    private val quotedTextView: TimelineContentTextView = itemView.quotedText
    private val quotedNameView = itemView.quotedName

    init {
        this.linkClickHandler = DetailStatusLinkClickHandler(adapter.context,
                adapter.multiSelectManager, adapter, PreferencesSingleton.get(adapter.context))
        this.linkify = TwidereLinkify(linkClickHandler)

        initViews()
    }

    @UiThread
    fun displayStatus(account: AccountDetails?, status: ParcelableStatus?,
            statusActivity: StatusActivitySummaryLiveData.StatusActivity?, translation: TranslationResult?) {
        if (account == null || status == null) return
        val fragment = adapter.fragment
        val context = adapter.context
        val formatter = adapter.bidiFormatter
        val colorNameManager = UserColorNameManager.get(adapter.context)

        linkClickHandler.status = status

        if (status.retweet_id != null) {
            val retweetedBy = colorNameManager.getDisplayName(status.retweeted_by_user_key!!,
                    status.retweeted_by_user_name!!, status.retweeted_by_user_acct!!)
            retweetedByView.spannable = context.getString(R.string.name_retweeted, retweetedBy)
            retweetedByView.visibility = View.VISIBLE
        } else {
            retweetedByView.spannable = null
            retweetedByView.visibility = View.GONE
        }

        itemView.profileContainer.drawEnd(status.account_color)

        val layoutPosition = layoutPosition
        val skipLinksInText = status.extras?.support_entities == true

        val quoted = status.quoted
        if (status.is_quote && quoted != null) {

            itemView.quotedView.visibility = View.VISIBLE

            val quoteContentAvailable = quoted.text_plain != null && quoted.text_unescaped != null

            if (quoteContentAvailable) {
                quotedNameView.visibility = View.VISIBLE
                quotedTextView.visibility = View.VISIBLE

                quotedNameView.name = colorNameManager.getUserNickname(quoted.user_key!!,
                        quoted.user_name)
                quotedNameView.screenName = "@${quoted.user_acct}"
                quotedNameView.updateText(formatter)


                val quotedDisplayEnd = status.extras?.quoted_display_text_range?.getOrNull(1) ?: -1
                val quotedText = SpannableStringBuilder.valueOf(quoted.text_unescaped)
                quoted.spans?.applyTo(quotedText, status.extras?.emojis,
                        adapter.requestManager, quotedTextView)
                linkify.applyAllLinks(quotedText, status.account_key, layoutPosition.toLong(),
                        status.is_possibly_sensitive, skipLinksInText)
                if (quotedDisplayEnd != -1 && quotedDisplayEnd <= quotedText.length) {
                    quotedTextView.spannable = quotedText.subSequence(0, quotedDisplayEnd)
                } else {
                    quotedTextView.spannable = quotedText
                }
                quotedTextView.hideIfEmpty()

                val quotedUserColor = colorNameManager.getUserColor(quoted.user_key!!)
                if (quotedUserColor != 0) {
                    itemView.quotedView.drawStart(quotedUserColor)
                } else {
                    itemView.quotedView.drawStart(ThemeUtils.getColorFromAttribute(context,
                            R.attr.quoteIndicatorBackgroundColor))
                }

                val quotedMedia = quoted.media

                if (quotedMedia?.isEmpty() != false) {
                    itemView.quotedMediaLabel.visibility = View.GONE
                    itemView.quotedMediaPreview.visibility = View.GONE
                } else if (adapter.isDetailMediaExpanded) {
                    itemView.quotedMediaLabel.visibility = View.GONE
                    itemView.quotedMediaPreview.visibility = View.VISIBLE
                    itemView.quotedMediaPreview.displayMedia(adapter.requestManager,
                            media = quotedMedia, accountKey = status.account_key,
                            mediaClickListener = adapter.fragment)
                } else {
                    itemView.quotedMediaLabel.visibility = View.VISIBLE
                    itemView.quotedMediaPreview.visibility = View.GONE
                }
            } else {
                quotedNameView.visibility = View.GONE
                quotedTextView.visibility = View.VISIBLE
                itemView.quotedMediaLabel.visibility = View.GONE
                itemView.quotedMediaPreview.visibility = View.GONE

                // Not available
                val string = SpannableString.valueOf(context.getString(R.string.label_status_not_available))
                string.setSpan(ForegroundColorSpan(ThemeUtils.getColorFromAttribute(context,
                        android.R.attr.textColorTertiary, textView.currentTextColor)), 0,
                        string.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                quotedTextView.spannable = string

                itemView.quotedView.drawStart(ThemeUtils.getColorFromAttribute(context,
                        R.attr.quoteIndicatorBackgroundColor))
            }
        } else {
            itemView.quotedView.visibility = View.GONE
        }

        itemView.profileContainer.drawStart(colorNameManager.getUserColor(status.user_key))

        val timestamp: Long

        if (status.is_retweet) {
            timestamp = status.retweet_timestamp
        } else {
            timestamp = status.timestamp
        }

        nameView.name = colorNameManager.getUserNickname(status.user_key, status.user_name)
        nameView.screenName = "@${status.user_acct}"
        nameView.updateText(formatter)

        adapter.requestManager.loadProfileImage(context, status, adapter.profileImageStyle,
                itemView.profileImage.cornerRadius, itemView.profileImage.cornerRadiusRatio,
                size = ProfileImageSize.ORIGINAL).into(itemView.profileImage)

        val typeIconRes = Utils.getUserTypeIconRes(status.user_is_verified, status.user_is_protected)
        val typeDescriptionRes = Utils.getUserTypeDescriptionRes(status.user_is_verified, status.user_is_protected)


        if (typeIconRes != 0 && typeDescriptionRes != 0) {
            profileTypeView.setImageResource(typeIconRes)
            profileTypeView.contentDescription = context.getString(typeDescriptionRes)
            profileTypeView.visibility = View.VISIBLE
        } else {
            profileTypeView.setImageDrawable(null)
            profileTypeView.contentDescription = null
            profileTypeView.visibility = View.GONE
        }

        val timeString = Utils.formatToLongTimeString(context, timestamp)?.takeIf(String::isNotEmpty)
        val source = status.source?.takeIf(String::isNotEmpty)
        itemView.timeSource.spannable = when {
            timeString != null && source != null -> {
                HtmlSpanBuilder.fromHtml(context.getString(R.string.status_format_time_source,
                        timeString, source))
            }
            source != null -> HtmlSpanBuilder.fromHtml(source)
            timeString != null -> timeString
            else -> null
        }
        itemView.timeSource.movementMethod = LinkMovementMethod.getInstance()

        summaryView.spannable = status.extras?.summary_text
        summaryView.hideIfEmpty()

        val displayEnd = status.extras?.display_text_range?.getOrNull(1) ?: -1
        val text = SpannableStringBuilder.valueOf(status.text_unescaped).apply {
            status.spans?.applyTo(this, status.extras?.emojis, adapter.requestManager, textView)
            linkify.applyAllLinks(this, status.account_key, layoutPosition.toLong(),
                    status.is_possibly_sensitive, skipLinksInText)
        }

        if (displayEnd != -1 && displayEnd <= text.length) {
            val displayText = text.subSequence(0, displayEnd)
            if (!TextUtils.equals(textView.text, displayText)) {
                textView.spannable = displayText
            }
        } else {
            if (!TextUtils.equals(textView.text, text)) {
                textView.spannable = text
            }
        }
        textView.hideIfEmpty()

        val location = status.location
        val placeFullName = status.place_full_name

        if (placeFullName != null && placeFullName.isNotEmpty()) {
            locationView.visibility = View.VISIBLE
            locationView.spannable = placeFullName
            locationView.isClickable = location != null && location.isValid
        } else if (location != null && location.isValid) {
            locationView.visibility = View.VISIBLE
            locationView.setText(R.string.action_view_map)
            locationView.isClickable = true
        } else {
            locationView.visibility = View.GONE
            locationView.spannable = null
        }

        val interactUsersAdapter = itemView.countsUsers.adapter as CountsUsersAdapter
        if (statusActivity != null) {
            updateStatusActivity(statusActivity)
        } else {
            interactUsersAdapter.setUsers(null)
            interactUsersAdapter.setCounts(status)
        }

        if (interactUsersAdapter.itemCount > 0) {
            itemView.countsUsers.visibility = View.VISIBLE
            itemView.countsUsersHeightHolder.visibility = View.INVISIBLE
        } else {
            itemView.countsUsers.visibility = View.GONE
            itemView.countsUsersHeightHolder.visibility = View.GONE
        }

        val media = status.attachment?.media

        when {
            media.isNullOrEmpty() -> {
                itemView.mediaPreviewContainer.visibility = View.GONE
                itemView.mediaPreview.visibility = View.GONE
                itemView.mediaPreviewLoad.visibility = View.GONE
                itemView.mediaPreview.displayMedia()
            }
            adapter.isDetailMediaExpanded -> {
                itemView.mediaPreviewContainer.visibility = View.VISIBLE
                itemView.mediaPreview.visibility = View.VISIBLE
                itemView.mediaPreviewLoad.visibility = View.GONE
                itemView.mediaPreview.displayMedia(adapter.requestManager, media = media,
                        accountKey = status.account_key, mediaClickListener = adapter.fragment)
            }
            else -> {
                itemView.mediaPreviewContainer.visibility = View.VISIBLE
                itemView.mediaPreview.visibility = View.GONE
                itemView.mediaPreviewLoad.visibility = View.VISIBLE
                itemView.mediaPreview.displayMedia()
            }
        }

        if (TwitterCardUtils.isCardSupported(status)) {
            val size = TwitterCardUtils.getCardSize(status.attachment?.card!!)

            if (size != null) {
                itemView.twitterCard.setCardSize(size.x, size.y)
            } else {
                itemView.twitterCard.setCardSize(0, 0)
            }
            val vc = StatusCardViewFactory.from(status)
            itemView.twitterCard.viewController = vc
            if (vc != null) {
                itemView.twitterCard.visibility = View.VISIBLE
            } else {
                itemView.twitterCard.visibility = View.GONE
            }

        } else {
            itemView.twitterCard.viewController = null
            itemView.twitterCard.visibility = View.GONE
        }

        MenuUtils.setupForStatus(context, itemView.menuBar.menu, PreferencesSingleton.get(fragment.context!!), colorNameManager,
                status, adapter.statusAccount!!)


        val lang = status.lang
        if (lang != null && lang.isValidLocale && account.isOfficial(context)) {
            translateContainer.visibility = View.VISIBLE
            if (translation != null) {
                val locale = Locale(translation.translatedLang)
                translateLabelView.text = context.getString(R.string.label_translated_to_language,
                        locale.displayLanguage)
                translateResultView.visibility = View.VISIBLE
                translateChangeLanguageView.visibility = View.VISIBLE
                translateResultView.text = translation.text
            } else {
                val locale = Locale(lang)
                translateLabelView.text = context.getString(R.string.label_translate_from_language,
                        locale.displayLanguage)
                translateResultView.visibility = View.GONE
                translateChangeLanguageView.visibility = View.GONE
            }
        } else {
            translateLabelView.setText(R.string.unknown_language)
            translateContainer.visibility = View.GONE
        }

        textView.setTextIsSelectable(true)
        translateResultView.setTextIsSelectable(true)

        textView.movementMethod = LinkMovementMethod.getInstance()
        quotedTextView.movementMethod = null
    }

    override fun onClick(v: View) {
        val status = adapter.getStatus(layoutPosition)
        val fragment = adapter.fragment
        val preferences = PreferencesSingleton.get(fragment.context!!)
        when (v) {
            itemView.mediaPreviewLoad -> {
                if (adapter.sensitiveContentEnabled || !status.is_possibly_sensitive) {
                    adapter.isDetailMediaExpanded = true
                } else {
                    val f = StatusFragment.LoadSensitiveImageConfirmDialogFragment()
                    f.show(fragment.childFragmentManager, "load_sensitive_image_confirm")
                }
            }
            itemView.profileContainer -> {
                IntentUtils.openUserProfile(adapter.context, status.account_key, status.user_key,
                        status.user_screen_name, status.extras?.user_statusnet_profile_url,
                        preferences[newDocumentApiKey], null)
            }
            retweetedByView -> {
                if (status.retweet_id != null) {
                    IntentUtils.openUserProfile(adapter.context, status.account_key,
                            status.retweeted_by_user_key, status.retweeted_by_user_screen_name,
                            null, preferences[newDocumentApiKey], null)
                }
            }
            locationView -> {
                val location = status.location?.takeIf { it.isValid } ?: return
                IntentUtils.openMap(adapter.context, location.latitude, location.longitude)
            }
            itemView.quotedView -> {
                val quotedId = status.quoted?.id ?: return
                IntentUtils.openStatus(adapter.context, status.account_key, quotedId)
            }
            translateLabelView -> {
                fragment.loadTranslation(adapter.status)
            }
            translateChangeLanguageView -> {
                fragment.openTranslationDestinationChooser()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val layoutPosition = layoutPosition
        if (layoutPosition < 0) return false
        val fragment = adapter.fragment
        val status = adapter.getStatus(layoutPosition)
        val activity = fragment.activity!!
        val preferences = PreferencesSingleton.get(fragment.context!!)
        val manager = UserColorNameManager.get(activity)
        return MenuUtils.handleStatusClick(activity, fragment, fragment.childFragmentManager,
                preferences, manager, status, item)
    }

    internal fun updateStatusActivity(activity: StatusActivitySummaryLiveData.StatusActivity) {
        val adapter = itemView.countsUsers.adapter as CountsUsersAdapter
        adapter.setUsers(activity.relatedUsers)
        adapter.setCounts(activity)
    }

    private fun initViews() {
        itemView.menuBar.setOnMenuItemClickListener(this)
        val fragment = adapter.fragment
        val activity = fragment.activity!!
        val inflater = activity.menuInflater
        val menu = itemView.menuBar.menu
        inflater.inflate(R.menu.menu_detail_status, menu)

        val favoriteItem = menu.findItem(R.id.favorite)
        val favoriteProvider = favoriteItem?.supportActionProvider
        if (favoriteProvider is FavoriteItemProvider) {
            val useStar = adapter.useStarsForLikes
            favoriteProvider.icon = if (useStar) R.drawable.ic_action_star else R.drawable.ic_action_heart
            favoriteProvider.tint = if (useStar) R.color.btn_tint_favorite_stateful else R.color.btn_tint_like_stateful
            favoriteProvider.useStar = useStar
            favoriteProvider.longClickListener = {
                val status = adapter.getStatus(layoutPosition)
                val itemId = adapter.getItemId(layoutPosition)
                AbsTimelineFragment.handleActionLongClick(fragment, status, itemId, R.id.favorite)
            }
            favoriteProvider.init(itemView.menuBar, favoriteItem)
        }

        val retweetItem = menu.findItem(R.id.retweet)
        val retweetProvider = retweetItem?.supportActionProvider
        if (retweetProvider is RetweetItemProvider) {
            retweetProvider.longClickListener = {
                val status = adapter.getStatus(layoutPosition)
                val itemId = adapter.getItemId(layoutPosition)
                AbsTimelineFragment.handleActionLongClick(fragment, status, itemId, R.id.retweet)
            }
            retweetProvider.init(itemView.menuBar, retweetItem)
        }

        ThemeUtils.wrapMenuIcon(itemView.menuBar, excludeGroups = *intArrayOf(MENU_GROUP_STATUS_SHARE))
        itemView.mediaPreviewLoad.setOnClickListener(this)
        itemView.profileContainer.setOnClickListener(this)
        retweetedByView.setOnClickListener(this)
        locationView.setOnClickListener(this)
        itemView.quotedView.setOnClickListener(this)
        translateLabelView.setOnClickListener(this)
        translateChangeLanguageView.setOnClickListener(this)

        val textSize = adapter.textSize

        nameView.setPrimaryTextSize(textSize * 1.25f)
        nameView.setSecondaryTextSize(textSize * 0.85f)
        summaryView.textSize = textSize * 1.25f
        textView.textSize = textSize * 1.25f

        quotedNameView.setPrimaryTextSize(textSize * 1.25f)
        quotedNameView.setSecondaryTextSize(textSize * 0.85f)
        quotedTextView.textSize = textSize * 1.25f

        locationView.textSize = textSize * 0.85f
        itemView.timeSource.textSize = textSize * 0.85f
        translateLabelView.textSize = textSize * 0.85f
        translateChangeLanguageView.textSize = textSize * 0.85f
        translateResultView.textSize = textSize * 1.05f

        itemView.countsUsersHeightHolder.countItem.apply {
            setPrimaryTextSize(textSize * 1.25f)
            setSecondaryTextSize(textSize * 0.85f)
            updateTextAppearance()
            primaryText = "255"
            secondaryText = "XXXX"
            updateText()
        }

        nameView.updateTextAppearance()
        quotedNameView.updateTextAppearance()

        nameView.nameFirst = adapter.nameFirst
        quotedNameView.nameFirst = adapter.nameFirst

        itemView.mediaPreview.style = adapter.mediaPreviewStyle
        itemView.quotedMediaPreview.style = adapter.mediaPreviewStyle

        itemView.text.customSelectionActionModeCallback = StatusActionModeCallback(itemView.text, activity)
        itemView.profileImage.style = adapter.profileImageStyle

        val layoutManager = LinearLayoutManager(adapter.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        itemView.countsUsers.layoutManager = layoutManager

        val countsUsersAdapter = CountsUsersAdapter(fragment, adapter)
        itemView.countsUsers.adapter = countsUsersAdapter
        val resources = activity.resources
        itemView.countsUsers.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelOffset(R.dimen.element_spacing_normal)))

        // Apply font families
        nameView.applyFontFamily(adapter.lightFont)
        summaryView.applyFontFamily(adapter.lightFont)
        textView.applyFontFamily(adapter.lightFont)
        quotedNameView.applyFontFamily(adapter.lightFont)
        quotedTextView.applyFontFamily(adapter.lightFont)
        itemView.locationView.applyFontFamily(adapter.lightFont)
        translateLabelView.applyFontFamily(adapter.lightFont)
        translateResultView.applyFontFamily(adapter.lightFont)
    }


    private class CountsUsersAdapter(
            private val fragment: StatusFragment,
            private val statusAdapter: StatusDetailsAdapter
    ) : BaseRecyclerViewAdapter<RecyclerView.ViewHolder>(statusAdapter.context, Glide.with(fragment)) {

        private val inflater = LayoutInflater.from(statusAdapter.context)

        private var counts: List<LabeledCount>? = null
        private var users: List<ParcelableUser>? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ITEM_VIEW_TYPE_USER -> {
                    (holder as ProfileImageViewHolder).displayUser(getUser(position)!!)
                }
                ITEM_VIEW_TYPE_COUNT -> {
                    (holder as CountViewHolder).displayCount(getCount(position)!!)
                }
            }
        }

        private fun getCount(position: Int): LabeledCount? {
            if (counts == null) return null
            if (position < countItemsCount) {
                return counts!![position]
            }
            return null
        }

        override fun getItemCount(): Int {
            return countItemsCount + usersCount
        }


        override fun getItemViewType(position: Int): Int {
            val countItemsCount = countItemsCount
            if (position < countItemsCount) {
                return ITEM_VIEW_TYPE_COUNT
            }
            return ITEM_VIEW_TYPE_USER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                ITEM_VIEW_TYPE_USER -> return ProfileImageViewHolder(this, inflater.inflate(R.layout.adapter_item_status_interact_user, parent, false))
                ITEM_VIEW_TYPE_COUNT -> return CountViewHolder(this, inflater.inflate(R.layout.adapter_item_status_count_label, parent, false))
            }
            throw UnsupportedOperationException("Unsupported viewType " + viewType)
        }

        fun setUsers(users: List<ParcelableUser>?) {
            this.users = users
            notifyDataSetChanged()
        }

        fun setCounts(activity: StatusActivitySummaryLiveData.StatusActivity?) {
            if (activity != null) {
                val counts = ArrayList<LabeledCount>()
                val replyCount = activity.replyCount
                if (replyCount > 0) {
                    counts.add(LabeledCount(KEY_REPLY_COUNT, replyCount))
                }
                val retweetCount = activity.retweetCount
                if (retweetCount > 0) {
                    counts.add(LabeledCount(KEY_RETWEET_COUNT, retweetCount))
                }
                val favoriteCount = activity.favoriteCount
                if (favoriteCount > 0) {
                    counts.add(LabeledCount(KEY_FAVORITE_COUNT, favoriteCount))
                }
                this.counts = counts
            } else {
                counts = null
            }
            notifyDataSetChanged()
        }

        fun setCounts(status: ParcelableStatus?) {
            if (status != null) {
                val counts = ArrayList<LabeledCount>()
                if (status.reply_count > 0) {
                    counts.add(LabeledCount(KEY_REPLY_COUNT, status.reply_count))
                }
                if (status.retweet_count > 0) {
                    counts.add(LabeledCount(KEY_RETWEET_COUNT, status.retweet_count))
                }
                if (status.favorite_count > 0) {
                    counts.add(LabeledCount(KEY_FAVORITE_COUNT, status.favorite_count))
                }
                this.counts = counts
            } else {
                counts = null
            }
            notifyDataSetChanged()
        }

        val countItemsCount: Int
            get() {
                if (counts == null) return 0
                return counts!!.size
            }

        private val usersCount: Int
            get() {
                if (users == null) return 0
                return users!!.size
            }

        private fun notifyItemClick(position: Int) {
            when (getItemViewType(position)) {
                ITEM_VIEW_TYPE_COUNT -> {
                    val count = getCount(position)
                    val status = statusAdapter.status
                    if (count == null || status == null) return
                    when (count.type) {
                        KEY_RETWEET_COUNT -> {
                            IntentUtils.openStatusRetweeters(context, status.account_key,
                                    status.originalId)
                        }
                        KEY_FAVORITE_COUNT -> {
                            IntentUtils.openStatusFavoriters(context, status.account_key,
                                    status.originalId)
                        }
                    }
                }
                ITEM_VIEW_TYPE_USER -> {
                    fragment.onUserClick(getUser(position)!!)
                }
            }
        }

        private fun getUser(position: Int): ParcelableUser? {
            val countItemsCount = countItemsCount
            if (users == null || position < countItemsCount) return null
            return users!![position - countItemsCount]
        }


        internal class ProfileImageViewHolder(private val adapter: CountsUsersAdapter, itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            private val profileImageView = itemView.findViewById<ProfileImageView>(R.id.profileImage)

            init {
                itemView.setOnClickListener(this)
            }

            fun displayUser(item: ParcelableUser) {
                val context = adapter.context
                val requestManager = adapter.requestManager
                requestManager.loadProfileImage(context, item, adapter.profileImageStyle,
                        profileImageView.cornerRadius, profileImageView.cornerRadiusRatio,
                        adapter.profileImageSize).into(profileImageView)
            }

            override fun onClick(v: View) {
                adapter.notifyItemClick(layoutPosition)
            }
        }

        internal class CountViewHolder(
                private val adapter: CountsUsersAdapter,
                itemView: View
        ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            private val labelView = itemView.countItem

            init {
                itemView.setOnClickListener(this)
                val textSize = adapter.textSize
                labelView.setPrimaryTextSize(textSize * 1.25f)
                labelView.setSecondaryTextSize(textSize * 0.85f)
                labelView.updateTextAppearance()
            }

            override fun onClick(v: View) {
                adapter.notifyItemClick(layoutPosition)
            }

            fun displayCount(count: LabeledCount) {
                val label = when (count.type) {
                    KEY_REPLY_COUNT -> {
                        adapter.context.getString(R.string.replies)
                    }
                    KEY_RETWEET_COUNT -> {
                        adapter.context.getString(R.string.count_label_retweets)
                    }
                    KEY_FAVORITE_COUNT -> {
                        adapter.context.getString(R.string.title_favorites)
                    }
                    else -> {
                        throw UnsupportedOperationException("Unsupported type " + count.type)
                    }
                }
                labelView.primaryText = count.count.toString(Locale.getDefault())
                labelView.secondaryText = label

                labelView.updateText(adapter.bidiFormatter)
            }
        }

        internal class LabeledCount(var type: Int, var count: Long)

        companion object {
            private val ITEM_VIEW_TYPE_USER = 1
            private val ITEM_VIEW_TYPE_COUNT = 2

            private val KEY_REPLY_COUNT = 1
            private val KEY_RETWEET_COUNT = 2
            private val KEY_FAVORITE_COUNT = 3
        }
    }

    private class DetailStatusLinkClickHandler(
            context: Context,
            manager: MultiSelectManager,
            private val adapter: StatusDetailsAdapter,
            preferences: SharedPreferences
    ) : StatusLinkClickHandler(context, manager, preferences) {

        override fun onLinkClick(link: String, orig: String?, accountKey: UserKey?,
                extraId: Long, type: Int, sensitive: Boolean, start: Int, end: Int): Boolean {
            val position = extraId.toInt()
            val current = getCurrentMedia(link, position)
            if (current != null && !current.open_browser) {
                expandOrOpenMedia(current)
                return true
            }
            return super.onLinkClick(link, orig, accountKey, extraId, type, sensitive, start, end)
        }

        private fun expandOrOpenMedia(current: ParcelableMedia) {
            if (adapter.isDetailMediaExpanded) {
                IntentUtils.openMedia(adapter.context, adapter.status!!, current,
                        preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
                return
            }
            adapter.isDetailMediaExpanded = true
        }

        override fun isMedia(link: String, extraId: Long): Boolean {
            val current = getCurrentMedia(link, extraId.toInt())
            return current != null && !current.open_browser
        }

        private fun getCurrentMedia(link: String, extraId: Int): ParcelableMedia? {
            val status = adapter.getStatus(extraId)
            val media = ParcelableMediaUtils.getAllMedia(status)
            return findByLink(media, link)
        }
    }

    private class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            if (ViewCompat.getLayoutDirection(parent) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                outRect.set(spacing, 0, 0, 0)
            } else {
                outRect.set(0, 0, spacing, 0)
            }
        }
    }

    companion object {

        const val REQUEST_FAVORITE_SELECT_ACCOUNT = 101
        const val REQUEST_RETWEET_SELECT_ACCOUNT = 102

        private val String.isValidLocale: Boolean
            get() = !isEmpty() && this != "und"
    }
}