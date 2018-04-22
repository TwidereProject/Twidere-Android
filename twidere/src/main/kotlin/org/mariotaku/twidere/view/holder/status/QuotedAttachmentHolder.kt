/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import kotlinx.android.synthetic.main.layout_content_item_attachment_quote.view.*
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.user_acct
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.singleton.BidiFormatterSingleton
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import org.mariotaku.twidere.view.iface.IColorLabelView

class QuotedAttachmentHolder(parent: StatusViewHolder, view: ConstraintLayout) : StatusViewHolder.AttachmentHolder(parent, view) {

    private val quotedNameView = view.quotedName
    private val quotedTextView = view.quotedText
    private val quotedMediaLabel = view.quotedMediaLabel
    private val quotedMediaPreview = view.quotedMediaPreview

    private var linkHighlightingStyle: Int = TwidereLinkify.VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH

    override fun setupViewOptions(adapter: IStatusesAdapter) {
        quotedMediaPreview.style = adapter.mediaPreviewStyle
        linkHighlightingStyle = adapter.linkHighlightingStyle

        quotedNameView.nameFirst = adapter.nameFirst

        quotedNameView.applyFontFamily(adapter.lightFont)
        quotedTextView.applyFontFamily(adapter.lightFont)
        quotedMediaLabel.applyFontFamily(adapter.lightFont)
    }

    override fun setTextSize(textSize: Float) {
        quotedTextView.textSize = textSize
        quotedNameView.setPrimaryTextSize(textSize)
        quotedNameView.setSecondaryTextSize(textSize * 0.85f)

        quotedMediaLabel.textSize = textSize * 0.95f

        quotedNameView.updateTextAppearance()
    }

    override fun display(status: ParcelableStatus) {
        val context = view.context
        val colorNameManager = UserColorNameManager.get(context)

        val quoted = status.attachment!!.quoted!!
        val quoteContentAvailable = quoted.text_plain != null && quoted.text_unescaped != null
        val isFanfouStatus = status.account_key.host == USER_TYPE_FANFOU_COM
        if (quoteContentAvailable && !isFanfouStatus) {
            val quotedUserKey = quoted.user_key!!
            quotedNameView.name = colorNameManager.getUserNickname(quotedUserKey,
                    quoted.user_name)
            quotedNameView.screenName = "@${quoted.user_acct}"

            val quotedDisplayEnd = status.extras?.quoted_display_text_range?.getOrNull(1) ?: -1
            val quotedText: CharSequence
            if (linkHighlightingStyle != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                quotedText = SpannableStringBuilder.valueOf(quoted.text_unescaped)
                quoted.spans?.applyTo(quotedText, status.extras?.emojis
                )
//                linkify.applyAllLinks(quotedText, status.account_key, parent.layoutPosition.toLong(),
//                        status.is_possibly_sensitive, adapter.linkHighlightingStyle,
//                        skipLinksInText)
            } else {
                quotedText = quoted.text_unescaped
            }
            if (quotedDisplayEnd != -1 && quotedDisplayEnd <= quotedText.length) {
                quotedTextView.spannable = quotedText.subSequence(0, quotedDisplayEnd)
            } else {
                quotedTextView.spannable = quotedText
            }
            quotedTextView.hideIfEmpty()

            val quotedUserColor = colorNameManager.getUserColor(quotedUserKey)
//            if (quotedUserColor != 0) {
//                quotedView.drawStart(quotedUserColor)
//            } else {
//                quotedView.drawStart(ThemeUtils.getColorFromAttribute(context,
//                        R.attr.quoteIndicatorBackgroundColor))
//            }

//            displayQuotedMedia(requestManager, status)

            quotedNameView.updateText(BidiFormatterSingleton.get())
        } else {
            quotedNameView.visibility = View.GONE
            quotedTextView.visibility = View.VISIBLE

            if (quoteContentAvailable) {
//                displayQuotedMedia(requestManager, status)
            } else {
                quotedMediaPreview.visibility = View.GONE
                quotedMediaLabel.visibility = View.GONE
            }

            quotedTextView.spannable = if (!quoteContentAvailable) {
                // Display 'not available' label
                SpannableString.valueOf(context.getString(R.string.label_status_not_available)).apply {
                    setSpan(ForegroundColorSpan(ThemeUtils.getColorFromAttribute(context,
                            android.R.attr.textColorTertiary, quotedTextView.currentTextColor)), 0,
                            length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                // Display 'original status' label
                context.getString(R.string.label_original_status)
            }

//            quotedView.drawStart(ThemeUtils.getColorFromAttribute(context,
//                    R.attr.quoteIndicatorBackgroundColor))
        }

        (view as? IColorLabelView)?.drawStart(colorNameManager.getUserColor(status.user_key))
    }

    override fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {
        when (v) {
            view -> {
                listener.onQuotedStatusClick(holder, position)
            }
        }
    }

}
