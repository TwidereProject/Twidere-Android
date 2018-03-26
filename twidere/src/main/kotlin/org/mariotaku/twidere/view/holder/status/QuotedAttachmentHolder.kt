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

import android.view.View
import kotlinx.android.synthetic.main.layout_content_item_attachment_quote.view.*
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class QuotedAttachmentHolder(adapter: IStatusesAdapter, view: View) : StatusViewHolder.AttachmentHolder(adapter, view) {
    private val quotedNameView = view.quotedName

    private val quotedTextView = view.quotedText
    private val quotedMediaLabel = view.quotedMediaLabel
    private val quotedMediaPreview = view.quotedMediaPreview
    override fun setupViewOptions() {
        quotedMediaPreview.style = adapter.mediaPreviewStyle

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

    override fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {
        when (v) {
            view -> {
                listener.onQuotedStatusClick(holder, position)
            }
        }
    }

}
