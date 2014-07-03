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

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class DirectMessageConversationViewHolder extends CardViewHolder {

	public final ImageView incoming_profile_image, outgoing_profile_image;
	public final View incoming_message_container, outgoing_message_container, incoming_profile_image_container,
			outgoing_profile_image_container;
	public final TextView incoming_text, incoming_time, outgoing_text, outgoing_time;
	public final View incoming_item_menu, outgoing_item_menu;

	public final ImageView incoming_image_preview, outgoing_image_preview;
	public final ViewGroup incoming_image_preview_container, outgoing_image_preview_container;
	public final ProgressBar incoming_image_preview_progress, outgoing_image_preview_progress;

	private float text_size;

	public DirectMessageConversationViewHolder(final View view) {
		super(view);
		incoming_message_container = findViewById(R.id.incoming_message_container);
		outgoing_message_container = findViewById(R.id.outgoing_message_container);
		incoming_profile_image_container = findViewById(R.id.incoming_profile_image_container);
		outgoing_profile_image_container = findViewById(R.id.outgoing_profile_image_container);
		incoming_profile_image = (ImageView) findViewById(R.id.incoming_profile_image);
		outgoing_profile_image = (ImageView) findViewById(R.id.outgoing_profile_image);
		incoming_text = (TextView) findViewById(R.id.incoming_text);
		incoming_time = (TextView) findViewById(R.id.incoming_time);
		outgoing_text = (TextView) findViewById(R.id.outgoing_text);
		outgoing_time = (TextView) findViewById(R.id.outgoing_time);
		incoming_item_menu = findViewById(R.id.incoming_item_menu);
		outgoing_item_menu = findViewById(R.id.outgoing_item_menu);
		outgoing_image_preview = (ImageView) findViewById(R.id.outgoing_image_preview);
		outgoing_image_preview_progress = (ProgressBar) findViewById(R.id.outgoing_image_preview_progress);
		outgoing_image_preview_container = (ViewGroup) findViewById(R.id.outgoing_image_preview_container);
		incoming_image_preview = (ImageView) findViewById(R.id.incoming_image_preview);
		incoming_image_preview_progress = (ProgressBar) findViewById(R.id.incoming_image_preview_progress);
		incoming_image_preview_container = (ViewGroup) findViewById(R.id.incoming_image_preview_container);
	}

	public void setTextSize(final float text_size) {
		if (this.text_size != text_size) {
			this.text_size = text_size;
			incoming_text.setTextSize(text_size);
			incoming_time.setTextSize(text_size * 0.75f);
			outgoing_text.setTextSize(text_size);
			outgoing_time.setTextSize(text_size * 0.75f);
		}
	}
}
