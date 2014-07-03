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
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.mariotaku.twidere.util.ThemeUtils;

public class ListMenuOverflowButton extends ImageView {

	private final int mHighlightColor;
	private final Rect mRect;
	private boolean mIsDown;

	public ListMenuOverflowButton(final Context context) {
		this(context, null);
	}

	public ListMenuOverflowButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ListMenuOverflowButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.CENTER_INSIDE);
		mHighlightColor = isInEditMode() ? 0 : ThemeUtils.getUserThemeColor(context);
		mRect = new Rect();
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.src });
		if (!a.hasValue(0)) {
			setImageDrawable(ThemeUtils.getListMenuOverflowButtonDrawable(context));
		}
		a.recycle();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mRect.set(getLeft(), getTop(), getRight(), getBottom());
				mIsDown = true;
				updateColorFilter();
				break;
			case MotionEvent.ACTION_MOVE:
				if (mRect.contains(getLeft() + (int) e.getX(), getTop() + (int) e.getY())) {
					break;
				}
				if (mIsDown) {
					mIsDown = false;
					updateColorFilter();
				}
				break;
			default:
				mIsDown = false;
				updateColorFilter();
				break;
		}
		return super.onTouchEvent(e);
	}

	private void updateColorFilter() {
		if (mIsDown && isClickable() && isEnabled()) {
			setColorFilter(mHighlightColor, Mode.SRC_ATOP);
		} else {
			clearColorFilter();
		}
	}
}
