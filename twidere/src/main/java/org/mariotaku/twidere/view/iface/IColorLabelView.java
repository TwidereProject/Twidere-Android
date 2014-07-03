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

package org.mariotaku.twidere.view.iface;

import static org.mariotaku.twidere.util.Utils.isRTL;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.R;

public interface IColorLabelView {

	public static final float LABEL_WIDTH = 3.5f;

	public void drawBackground(final int color);

	public void drawBottom(final int... colors);

	public void drawEnd(final int... colors);

	public void drawLabel(final int[] start, final int[] end, int[] top, int[] bottom, final int background);

	public void drawStart(final int... colors);

	public void drawTop(final int... colors);

	public boolean isPaddingsIgnored();

	public void setIgnorePaddings(final boolean ignorePaddings);

	public void setVisibility(int visibility);

	public static final class Helper {

		private final View mView;

		private final Paint mPaint = new Paint();
		private final float mDensity;
		private final boolean mIsRTL;

		private int mBackgroundColor;
		private int[] mStartColors, mEndColors, mTopColors, mBottomColors;

		private boolean mIgnorePadding;

		public Helper(final View view, final Context context, final AttributeSet attrs, final int defStyle) {
			mView = view;
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Twidere);
			mIgnorePadding = a.getBoolean(R.styleable.Twidere_ignorePadding, false);
			a.recycle();
			final Resources res = context.getResources();
			mDensity = res.getDisplayMetrics().density;
			mIsRTL = isRTL(context);
		}

		public void dispatchDrawBackground(final Canvas canvas) {
			final int left = mIgnorePadding ? 0 : mView.getPaddingLeft();
			final int top = mIgnorePadding ? 0 : mView.getPaddingTop();
			final int right = mIgnorePadding ? mView.getWidth() : mView.getWidth() - mView.getPaddingRight();
			final int bottom = mIgnorePadding ? mView.getHeight() : mView.getHeight() - mView.getPaddingBottom();
			mPaint.setColor(mBackgroundColor);
			canvas.drawRect(left, top, right, bottom, mPaint);
		}

		public void dispatchDrawLabels(final Canvas canvas) {
			final int left = mIgnorePadding ? 0 : mView.getPaddingLeft();
			final int top = mIgnorePadding ? 0 : mView.getPaddingTop();
			final int right = mIgnorePadding ? mView.getWidth() : mView.getWidth() - mView.getPaddingRight();
			final int bottom = mIgnorePadding ? mView.getHeight() : mView.getHeight() - mView.getPaddingBottom();
			final int labelWidth = Math.round(mDensity * LABEL_WIDTH);
			final int[] leftColors = mIsRTL ? mEndColors : mStartColors;
			final int[] rightColors = mIsRTL ? mStartColors : mEndColors;
			drawColorsHorizontally(canvas, mTopColors, left, top, right - left, labelWidth);
			drawColorsHorizontally(canvas, mBottomColors, left, bottom - labelWidth, right - left, labelWidth);
			drawColorsVertically(canvas, leftColors, left, top, labelWidth, bottom - top);
			drawColorsVertically(canvas, rightColors, right - labelWidth, top, labelWidth, bottom - top);
		}

		public void drawBackground(final int color) {
			drawLabel(mStartColors, mEndColors, mTopColors, mBottomColors, color);
		}

		public void drawBottom(final int[] colors) {
			drawLabel(mStartColors, mEndColors, mTopColors, colors, mBackgroundColor);
		}

		public void drawEnd(final int[] colors) {
			drawLabel(mStartColors, colors, mTopColors, mBottomColors, mBackgroundColor);
		}

		public void drawLabel(final int[] start, final int[] end, final int[] top, final int[] bottom,
				final int background) {
			mStartColors = start;
			mEndColors = end;
			mTopColors = top;
			mBottomColors = bottom;
			mBackgroundColor = background;
			mView.invalidate();
		}

		public void drawStart(final int[] colors) {
			drawLabel(colors, mEndColors, mTopColors, mBottomColors, mBackgroundColor);
		}

		public void drawTop(final int[] colors) {
			drawLabel(mStartColors, mEndColors, colors, mBottomColors, mBackgroundColor);
		}

		public boolean isPaddingsIgnored() {
			return mIgnorePadding;
		}

		public void setIgnorePaddings(final boolean ignorePaddings) {
			mIgnorePadding = ignorePaddings;
			mView.invalidate();
		}

		private void drawColorsHorizontally(final Canvas canvas, final int[] colors, final int left, final int top,
				final int width, final int height) {
			if (colors == null || colors.length == 0) return;
			for (int i = 0, len = colors.length; i < len; i++) {
				mPaint.setColor(colors[i]);
				final float colorLeft = left + i * (width / len);
				final float colorRight = left + (i + 1) * (width / len);
				canvas.drawRect(colorLeft, top, colorRight, top + height, mPaint);
			}
		}

		private void drawColorsVertically(final Canvas canvas, final int[] colors, final int left, final int top,
				final int width, final int height) {
			if (colors == null || colors.length == 0) return;
			for (int i = 0, len = colors.length; i < len; i++) {
				mPaint.setColor(colors[i]);
				final float colorTop = top + i * (height / len);
				final float colorBottom = top + (i + 1) * (height / len);
				canvas.drawRect(left, colorTop, left + width, colorBottom, mPaint);
			}
		}

	}
}
