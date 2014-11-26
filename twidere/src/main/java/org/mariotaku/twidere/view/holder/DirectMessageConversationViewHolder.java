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

import android.graphics.LightingColorFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.messagebubbleview.library.MessageBubbleView;
import org.mariotaku.twidere.R;

public class DirectMessageConversationViewHolder extends CardViewHolder {

    public final TextView text, time;

    public final ImageView media_preview;
    public final ViewGroup media_preview_container;
    public final ProgressBar media_preview_progress;
    private final MessageBubbleView message_content;

    private float text_size;

    public DirectMessageConversationViewHolder(final View view) {
        super(view);
        message_content = (MessageBubbleView) findViewById(R.id.message_content);
        text = (TextView) findViewById(R.id.text);
        time = (TextView) findViewById(R.id.time);
        media_preview = (ImageView) findViewById(R.id.media_preview);
        media_preview_progress = (ProgressBar) findViewById(R.id.media_preview_progress);
        media_preview_container = (ViewGroup) findViewById(R.id.media_preview_container);
    }

    public void setTextSize(final float text_size) {
        if (this.text_size != text_size) {
            this.text_size = text_size;
            text.setTextSize(text_size);
            time.setTextSize(text_size * 0.75f);
        }
    }

    public void setOutgoing(boolean isOutgoing) {
        message_content.setCaretPosition(isOutgoing ? MessageBubbleView.BOTTOM_RIGHT : MessageBubbleView.TOP_LEFT);
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) message_content.getLayoutParams();
        lp.gravity = isOutgoing ? Gravity.RIGHT : Gravity.LEFT;
        message_content.setLayoutParams(lp);
        if (isOutgoing) {
            message_content.setBubbleColorFilter(new LightingColorFilter(0xFFC0FFC4, 0x00102015));
        } else {
            message_content.clearBubbleColorFilter();
        }
    }
}
