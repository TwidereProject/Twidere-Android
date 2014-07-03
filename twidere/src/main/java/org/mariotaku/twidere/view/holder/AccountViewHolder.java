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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

public class AccountViewHolder {

	public final ImageView profile_image;
	public final TextView name, screen_name;
	public final CheckBox checkbox;
	private final ColorLabelRelativeLayout content;
	private final View default_indicator;

	public AccountViewHolder(final View view) {
		content = (ColorLabelRelativeLayout) view;
		name = (TextView) view.findViewById(android.R.id.text1);
		screen_name = (TextView) view.findViewById(android.R.id.text2);
		profile_image = (ImageView) view.findViewById(android.R.id.icon);
		default_indicator = view.findViewById(R.id.default_indicator);
		checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
	}

	public void setAccountColor(final int color) {
		content.drawEnd(color);
	}

	public void setIsDefault(final boolean is_default) {
		default_indicator.setVisibility(is_default ? View.VISIBLE : View.GONE);
	}
}
