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

import android.content.res.ColorStateList
import android.database.Cursor
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import org.mariotaku.messagebubbleview.library.MessageBubbleView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessageConversationAdapter
import org.mariotaku.twidere.model.ParcelableDirectMessageCursorIndices
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.TwidereColorUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.CardMediaContainer

open class MessageViewHolder(
        protected val adapter: MessageConversationAdapter,
        itemView: View
) : ViewHolder(itemView) {

    val mediaContainer: CardMediaContainer
    val textView: TextView
    val time: TextView

    private val messageContent: MessageBubbleView

    private val textColorPrimary: Int
    private val textColorPrimaryInverse: Int
    private val textColorSecondary: Int
    private val textColorSecondaryInverse: Int


    init {
        val context = itemView.context
        val a = context.obtainStyledAttributes(R.styleable.MessageViewHolder)
        textColorPrimary = a.getColor(R.styleable.MessageViewHolder_android_textColorPrimary, 0)
        textColorPrimaryInverse = a.getColor(R.styleable.MessageViewHolder_android_textColorPrimaryInverse, 0)
        textColorSecondary = a.getColor(R.styleable.MessageViewHolder_android_textColorSecondary, 0)
        textColorSecondaryInverse = a.getColor(R.styleable.MessageViewHolder_android_textColorSecondaryInverse, 0)
        a.recycle()
        messageContent = itemView.findViewById(R.id.messageContent) as MessageBubbleView
        messageContent.setOnLongClickListener {
            itemView.parent.showContextMenuForChild(itemView)
            true
        }
        messageContent.setOnClickListener { itemView.parent.showContextMenuForChild(itemView) }
        textView = itemView.findViewById(R.id.text) as TextView
        time = itemView.findViewById(R.id.time) as TextView
        mediaContainer = itemView.findViewById(R.id.media_preview_container) as CardMediaContainer
        mediaContainer.setStyle(adapter.mediaPreviewStyle)
    }

    open fun displayMessage(cursor: Cursor, indices: ParcelableDirectMessageCursorIndices) {
        val context = adapter.context
        val linkify = adapter.linkify
        val loader = adapter.mediaLoader

        val accountKey = UserKey.valueOf(cursor.getString(indices.account_key))
        val timestamp = cursor.getLong(indices.timestamp)
        val media = JsonSerializer.parseArray(cursor.getString(indices.media),
                ParcelableMedia::class.java)
        val spans = JsonSerializer.parseArray(cursor.getString(indices.spans),
                SpanItem::class.java)
        val text = SpannableStringBuilder.valueOf(cursor.getString(indices.text_unescaped))
        ParcelableStatusUtils.applySpans(text, spans)
        // Detect entity support
        linkify.applyAllLinks(text, accountKey, false, true)
        textView.text = text
        time.text = Utils.formatToLongTimeString(context, timestamp)
        mediaContainer.visibility = if (media != null && media.isNotEmpty()) View.VISIBLE else View.GONE
        mediaContainer.displayMedia(loader, media, accountKey, adapter.onMediaClickListener, adapter.mediaLoadingHandler, layoutPosition.toLong(), true
        )
    }

    fun setMessageColor(color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        messageContent.bubbleColor = colorStateList
        val textLuminancePrimary = TwidereColorUtils.getYIQLuminance(textColorPrimary)
        val textPrimaryDark: Int
        val textPrimaryLight: Int
        val textSecondaryDark: Int
        val textSecondaryLight: Int
        if (textLuminancePrimary < 128) {
            textPrimaryDark = textColorPrimary
            textPrimaryLight = textColorPrimaryInverse
            textSecondaryDark = textColorSecondary
            textSecondaryLight = textColorSecondaryInverse
        } else {
            textPrimaryDark = textColorPrimaryInverse
            textPrimaryLight = textColorPrimary
            textSecondaryDark = textColorSecondaryInverse
            textSecondaryLight = textColorSecondary
        }
        val textContrastPrimary = TwidereColorUtils.getContrastYIQ(color,
                ThemeUtils.ACCENT_COLOR_THRESHOLD, textPrimaryDark, textPrimaryLight)
        val textContrastSecondary = TwidereColorUtils.getContrastYIQ(color,
                ThemeUtils.ACCENT_COLOR_THRESHOLD, textSecondaryDark, textSecondaryLight)
        textView.setTextColor(textContrastPrimary)
        textView.setLinkTextColor(textContrastSecondary)
        time.setTextColor(textContrastSecondary)
    }

    fun setTextSize(textSize: Float) {
        textView.textSize = textSize
        time.textSize = textSize * 0.75f
    }

}
