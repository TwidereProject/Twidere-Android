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
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class UserListViewHolder extends CardViewHolder {

	public final ImageView profile_image;
	public final TextView name, description, created_by, members_count, subscribers_count;
	private float text_size;
	public int position;

	public UserListViewHolder(final View view) {
		super(view);
		profile_image = (ImageView) findViewById(R.id.profile_image);
		name = (TextView) findViewById(R.id.name);
		description = (TextView) findViewById(R.id.description);
		created_by = (TextView) findViewById(R.id.created_by);
		members_count = (TextView) findViewById(R.id.members_count);
		subscribers_count = (TextView) findViewById(R.id.subscribers_count);
	}

	public void setTextSize(final float text_size) {
		if (this.text_size == text_size) return;
		this.text_size = text_size;
		description.setTextSize(text_size);
		name.setTextSize(text_size * 1.05f);
		created_by.setTextSize(text_size * 0.65f);
	}

}
