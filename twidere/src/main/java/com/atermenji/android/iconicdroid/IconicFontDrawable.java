/*
 * Copyright (C) 2013 Artur Termenji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atermenji.android.iconicdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.atermenji.android.iconicdroid.icon.Icon;

/**
 * A custom {@link Drawable} which can display icons from icon fonts.
 */
public class IconicFontDrawable extends Drawable {

	private final Context mContext;

	private final Paint mIconPaint;
	private final Paint mContourPaint;

	private final Rect mPaddingBounds;
	private final RectF mPathBounds;

	private final Path mPath;

	private int mIconPadding;
	private int mContourWidth;

	private int mIntrinsicWidth;
	private int mIntrinsicHeight;

	private boolean mDrawContour;

	private Icon mIcon;
	private char[] mIconUtfChars;

	public IconicFontDrawable(final Context context) {
		mContext = context.getApplicationContext();

		mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mContourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mContourPaint.setStyle(Paint.Style.STROKE);

		mPath = new Path();

		mPathBounds = new RectF();
		mPaddingBounds = new Rect();
	}

	public IconicFontDrawable(final Context context, final Icon icon) {
		this(context);
		updateIcon(icon);
	}

	@Override
	public void draw(final Canvas canvas) {
		if (mIcon != null) {
			final Rect viewBounds = getBounds();

			updatePaddingBounds(viewBounds);
			updateTextSize(viewBounds);
			offsetIcon(viewBounds);

			mPath.close();

			if (mDrawContour) {
				canvas.drawPath(mPath, mContourPaint);
			}

			canvas.drawPath(mPath, mIconPaint);

		}
	}

	/**
	 * Enable/disable contour drawing.
	 * 
	 * @param drawContour
	 */
	public void drawContour(final boolean drawContour) {
		mDrawContour = drawContour;

		if (mDrawContour) {
			mIconPadding += mContourWidth;
		} else {
			mIconPadding -= mContourWidth;
		}

		invalidateSelf();
	}

	@Override
	public int getIntrinsicHeight() {
		return mIntrinsicHeight;
	}

	@Override
	public int getIntrinsicWidth() {
		return mIntrinsicWidth;
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(final int alpha) {
		mIconPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(final ColorFilter cf) {
		mIconPaint.setColorFilter(cf);
	}

	/**
	 * Set contour params for the {@link Icon}. You should call
	 * {@link #drawContour(boolean)} method to enable contour.
	 * 
	 * @param contourColor
	 * @param contourWidth
	 */
	public void setContour(final int contourColor, final int contourWidth) {
		setContourColor(contourColor);
		setContourWidth(contourWidth);
		invalidateSelf();
	}

	/**
	 * Set contour color for the {@link Icon}. You should call
	 * {@link #drawContour(boolean)} method to enable contour.
	 * 
	 * @param contourColor
	 */
	public void setContourColor(final int contourColor) {
		mContourPaint.setColor(contourColor);
		invalidateSelf();
	}

	/**
	 * Set contour width for the {@link Icon}. You should call
	 * {@link #drawContour(boolean)} method to enable contour.
	 * 
	 * @param contourWidth
	 */
	public void setContourWidth(final int contourWidth) {
		mContourWidth = contourWidth;
		mContourPaint.setStrokeWidth(mContourWidth);
		invalidateSelf();
	}

	/**
	 * Loads and draws given {@link Icon}.
	 * 
	 * @param icon
	 */
	public void setIcon(final Icon icon) {
		updateIcon(icon);
		invalidateSelf();
	}

	/**
	 * Set a color for the {@link Icon}.
	 * 
	 * @param color
	 */
	public void setIconColor(final int color) {
		mIconPaint.setColor(color);
		invalidateSelf();
	}

	/**
	 * Set a padding for the {@link Icon}.
	 * 
	 * @param iconPadding
	 */
	public void setIconPadding(final int iconPadding) {
		mIconPadding = iconPadding;
		if (mDrawContour) {
			mIconPadding += mContourWidth;
		}

		invalidateSelf();
	}

	/**
	 * Set intrinsic height, which is used by several controls.
	 * 
	 * @param intrinsicHeight
	 */
	public void setIntrinsicHeight(final int intrinsicHeight) {
		mIntrinsicHeight = intrinsicHeight;
	}

	/**
	 * Set intrinsic width, which is used by several controls.
	 * 
	 * @param intrinsicWidth
	 */
	public void setIntrinsicWidth(final int intrinsicWidth) {
		mIntrinsicWidth = intrinsicWidth;
	}

	private void offsetIcon(final Rect viewBounds) {
		// final float startX = viewBounds.centerX() - mPathBounds.width() / 2;
		// final float offsetX = startX - mPathBounds.left;
		// final float startY = viewBounds.centerY() - mPathBounds.height() / 2;
		// final float offsetY = startY - mPathBounds.top;

		final FontMetrics metrics = mIconPaint.getFontMetrics();
		final float textSize = mIconPaint.getTextSize();
		final float offsetX = viewBounds.centerX() - textSize / 2;
		final float offsetY = viewBounds.centerY() - (metrics.ascent + metrics.descent) / 2
				+ (metrics.bottom - metrics.descent);

		mPath.offset(offsetX, offsetY);
	}

	private void updateIcon(final Icon icon) {
		mIcon = icon;
		mIconUtfChars = Character.toChars(icon.getIconUtfValue());
		mIconPaint.setTypeface(mIcon.getIconicTypeface().getTypeface(mContext));
	}

	private void updatePaddingBounds(final Rect viewBounds) {
		if (mIconPadding >= 0 && !(mIconPadding * 2 > viewBounds.width()) && !(mIconPadding * 2 > viewBounds.height())) {
			mPaddingBounds.set(viewBounds.left + mIconPadding, viewBounds.top + mIconPadding, viewBounds.right
					- mIconPadding, viewBounds.bottom - mIconPadding);
		}
	}

	private void updateTextSize(final Rect viewBounds) {
		final float textSize = viewBounds.height();
		mIconPaint.setTextSize(textSize);
		mIconPaint.getTextPath(mIconUtfChars, 0, mIconUtfChars.length, 0, 0, mPath);
		mPath.computeBounds(mPathBounds, true);

		// final float deltaWidth = mPaddingBounds.width() /
		// mPathBounds.width();
		// final float deltaHeight = mPaddingBounds.height() /
		// mPathBounds.height();
		// final float delta = deltaWidth < deltaHeight ? deltaWidth :
		// deltaHeight;
		// textSize *= delta;
		//
		// mIconPaint.setTextSize(textSize);

		// mIconPaint.getTextPath(mIconUtfChars, 0, mIconUtfChars.length, 0,
		// viewBounds.height(), mPath);
		// mPath.computeBounds(mPathBounds, true);
	}
}
