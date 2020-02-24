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

package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public class PaddingDrawable extends Drawable {

	private final Drawable mDrawable;
	private final int mPaddingTop;
	private final int mPaddingLeft;
	private final int mPaddingBottom;
	private final int mPaddingRight;

	public PaddingDrawable(final Drawable drawable, final int padding) {
		this(drawable, padding, padding, padding, padding);
	}

	public PaddingDrawable(final Drawable drawable, final int paddingTop, final int paddingLeft,
			final int paddingRight, final int paddingBottom) {
		mDrawable = drawable;
		mPaddingTop = paddingTop;
		mPaddingLeft = paddingLeft;
		mPaddingBottom = paddingBottom;
		mPaddingRight = paddingRight;
	}

	@Override
	public void draw(@NonNull final Canvas canvas) {
		mDrawable.draw(canvas);
	}

	@Override
	public int getIntrinsicHeight() {
		return mDrawable.getIntrinsicHeight() + mPaddingTop + mPaddingBottom;
	}

	@Override
	public int getIntrinsicWidth() {
		return mDrawable.getIntrinsicWidth() + mPaddingLeft + mPaddingRight;
	}

	@Override
	public int getOpacity() {
		return mDrawable.getOpacity();
	}

	@Override
	public void setAlpha(final int alpha) {
		mDrawable.setAlpha(alpha);
	}

	@Override
	public void setBounds(final int left, final int top, final int right, final int bottom) {
		mDrawable.setBounds(left + mPaddingLeft, top + mPaddingTop, right - mPaddingRight, bottom - mPaddingBottom);
	}

	@Override
	public void setBounds(@NonNull final Rect bounds) {
		final int left = bounds.left + mPaddingLeft;
		final int top = bounds.top + mPaddingTop;
		final int right = bounds.right - mPaddingRight;
		final int bottom = bounds.bottom - mPaddingBottom;
		mDrawable.setBounds(left, top, right, bottom);
	}

	@Override
	public void setColorFilter(final ColorFilter cf) {
		mDrawable.setColorFilter(cf);
	}

}
