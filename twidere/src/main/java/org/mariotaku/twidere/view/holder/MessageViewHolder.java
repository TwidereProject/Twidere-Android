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

package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.method.ArrowKeyMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.messagebubbleview.library.MessageBubbleView;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.MessageConversationAdapter;
import org.mariotaku.twidere.model.ParcelableDirectMessage.CursorIndices;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.StatusActionModeCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereColorUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.CardMediaContainer.OnMediaClickListener;

public class MessageViewHolder extends ViewHolder implements OnMediaClickListener {

    public final CardMediaContainer mediaContainer;
    public final TextView textView, time;

    private final MessageBubbleView messageContent;
    protected final MessageConversationAdapter adapter;

    private final int textColorPrimary, textColorPrimaryInverse, textColorSecondary, textColorSecondaryInverse;
    private final StatusActionModeCallback callback;


    public MessageViewHolder(final MessageConversationAdapter adapter, final View itemView) {
        super(itemView);
        this.adapter = adapter;
        final Context context = itemView.getContext();
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary,
                android.R.attr.textColorPrimaryInverse, android.R.attr.textColorSecondary,
                android.R.attr.textColorSecondaryInverse});
        textColorPrimary = a.getColor(0, 0);
        textColorPrimaryInverse = a.getColor(1, 0);
        textColorSecondary = a.getColor(2, 0);
        textColorSecondaryInverse = a.getColor(3, 0);
        a.recycle();
        messageContent = (MessageBubbleView) itemView.findViewById(R.id.message_content);
        textView = (TextView) itemView.findViewById(R.id.text);
        time = (TextView) itemView.findViewById(R.id.time);
        mediaContainer = (CardMediaContainer) itemView.findViewById(R.id.media_preview_container);
        mediaContainer.setStyle(adapter.getMediaPreviewStyle());
        callback = new StatusActionModeCallback(textView, adapter.getContext());
    }

    public void displayMessage(Cursor cursor, CursorIndices indices) {
        final Context context = adapter.getContext();
        final TwidereLinkify linkify = adapter.getLinkify();
        final MediaLoaderWrapper loader = adapter.getMediaLoader();

        final long accountId = cursor.getLong(indices.account_id);
        final long timestamp = cursor.getLong(indices.message_timestamp);
        final ParcelableMedia[] media = ParcelableMedia.fromSerializedJson(cursor.getString(indices.media));
        textView.setText(Html.fromHtml(cursor.getString(indices.text)));
        linkify.applyAllLinks(textView, accountId, false);
        time.setText(Utils.formatToLongTimeString(context, timestamp));
        mediaContainer.setVisibility(media != null && media.length > 0 ? View.VISIBLE : View.GONE);
        mediaContainer.displayMedia(media, loader, accountId, true, this, adapter.getMediaLoadingHandler());

        textView.setTextIsSelectable(true);
        textView.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        textView.setCustomSelectionActionModeCallback(callback);
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, long accountId) {
        final Bundle options = Utils.createMediaViewerActivityOption(view);
        Utils.openMedia(adapter.getContext(), adapter.getDirectMessage(getAdapterPosition()), media, options);
    }

    public void setMessageColor(int color) {
        final ColorStateList colorStateList = ColorStateList.valueOf(color);
        messageContent.setBubbleColor(colorStateList);
        final int textLuminancePrimary = TwidereColorUtils.getYIQLuminance(textColorPrimary);
        final int textPrimaryDark, textPrimaryLight, textSecondaryDark, textSecondaryLight;
        if (textLuminancePrimary < 128) {
            textPrimaryDark = textColorPrimary;
            textPrimaryLight = textColorPrimaryInverse;
            textSecondaryDark = textColorSecondary;
            textSecondaryLight = textColorSecondaryInverse;
        } else {
            textPrimaryDark = textColorPrimaryInverse;
            textPrimaryLight = textColorPrimary;
            textSecondaryDark = textColorSecondaryInverse;
            textSecondaryLight = textColorSecondary;
        }
        final int textContrastPrimary = TwidereColorUtils.getContrastYIQ(color,
                ThemeUtils.ACCENT_COLOR_THRESHOLD, textPrimaryDark, textPrimaryLight);
        final int textContrastSecondary = TwidereColorUtils.getContrastYIQ(color,
                ThemeUtils.ACCENT_COLOR_THRESHOLD, textSecondaryDark, textSecondaryLight);
        textView.setTextColor(textContrastPrimary);
        textView.setLinkTextColor(textContrastSecondary);
        time.setTextColor(textContrastSecondary);
    }

    public void setTextSize(final float textSize) {
        textView.setTextSize(textSize);
        time.setTextSize(textSize * 0.75f);
    }

}
