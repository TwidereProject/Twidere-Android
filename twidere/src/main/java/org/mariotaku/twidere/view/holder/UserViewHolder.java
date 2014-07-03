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

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class UserViewHolder extends CardViewHolder {

	public final ImageView profile_image;
	public final TextView name, screen_name, description, location, url, statuses_count, followers_count,
			friends_count;
	private boolean account_color_enabled;
	private float text_size;
	public int position;

	public UserViewHolder(final View view) {
		super(view);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		name = (TextView) findViewById(R.id.name);
		screen_name = (TextView) findViewById(R.id.screen_name);
		description = (TextView) findViewById(R.id.description);
		location = (TextView) findViewById(R.id.location);
		url = (TextView) findViewById(R.id.url);
		statuses_count = (TextView) findViewById(R.id.statuses_count);
		followers_count = (TextView) findViewById(R.id.followers_count);
		friends_count = (TextView) findViewById(R.id.friends_count);
	}

	public void setAccountColor(final int color) {
		content.drawEnd(account_color_enabled ? color : Color.TRANSPARENT);
	}

	public void setAccountColorEnabled(final boolean enabled) {
		account_color_enabled = enabled;
		if (!account_color_enabled) {
			content.drawEnd(Color.TRANSPARENT);
		}
	}

	public void setHighlightColor(final int color) {
		content.drawBackground(color);
	}

	public void setTextSize(final float text_size) {
		if (this.text_size == text_size) return;
		this.text_size = text_size;
		description.setTextSize(text_size);
		name.setTextSize(text_size);
		screen_name.setTextSize(text_size * 0.75f);
		location.setTextSize(text_size);
		url.setTextSize(text_size);
		statuses_count.setTextSize(text_size);
		followers_count.setTextSize(text_size);
		friends_count.setTextSize(text_size);
	}

	public void setUserColor(final int color) {
		content.drawStart(color);
	}

}
