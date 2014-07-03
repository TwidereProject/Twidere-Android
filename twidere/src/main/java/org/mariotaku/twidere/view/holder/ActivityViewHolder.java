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
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class ActivityViewHolder extends StatusViewHolder {

	public final ImageView activity_profile_image_1, activity_profile_image_2, activity_profile_image_3,
			activity_profile_image_4, activity_profile_image_5;
	public final ImageView[] activity_profile_images;
	public final ViewGroup activity_profile_images_container;
	public final TextView activity_profile_image_more_number;
	public final View divider;

	public ActivityViewHolder(final View view) {
		super(view);
		divider = findViewById(R.id.divider);
		activity_profile_images_container = (ViewGroup) findViewById(R.id.activity_profile_image_container);
		activity_profile_image_1 = (ImageView) findViewById(R.id.activity_profile_image_1);
		activity_profile_image_2 = (ImageView) findViewById(R.id.activity_profile_image_2);
		activity_profile_image_3 = (ImageView) findViewById(R.id.activity_profile_image_3);
		activity_profile_image_4 = (ImageView) findViewById(R.id.activity_profile_image_4);
		activity_profile_image_5 = (ImageView) findViewById(R.id.activity_profile_image_5);
		activity_profile_image_more_number = (TextView) findViewById(R.id.activity_profile_image_more_number);
		activity_profile_images = new ImageView[] { activity_profile_image_1, activity_profile_image_2,
				activity_profile_image_3, activity_profile_image_4, activity_profile_image_5 };
	}

	@Override
	public boolean setTextSize(final float text_size) {
		if (super.setTextSize(text_size)) return false;
		activity_profile_image_more_number.setTextSize(text_size);
		return true;
	}

}
