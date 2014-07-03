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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AutoAdjustHeightImageView extends ImageView {

	public AutoAdjustHeightImageView(final Context context) {
		this(context, null);
	}

	public AutoAdjustHeightImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AutoAdjustHeightImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.CENTER_CROP);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final Drawable d = getDrawable();
		final Bitmap b = d instanceof BitmapDrawable ? ((BitmapDrawable) d).getBitmap() : null;

		if (b != null) {
			final int height = Math.round((float) width * (float) b.getHeight() / b.getWidth());
			setMeasuredDimension(width, height);
		} else {
			setMeasuredDimension(width, width);
			// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
