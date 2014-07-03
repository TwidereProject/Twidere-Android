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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ActionBarHomeAsUpIndicator extends ImageView {

	public ActionBarHomeAsUpIndicator(final Context context) {
		this(context, null);
	}

	public ActionBarHomeAsUpIndicator(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarHomeAsUpIndicator(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.homeAsUpIndicator });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		setImageDrawable(d);
		setScaleType(ScaleType.CENTER);
	}

}
