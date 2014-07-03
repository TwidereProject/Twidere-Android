/*
 * Copyright (C) 2010 Daniel Nilsson
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

package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * This drawable that draws a simple white and gray chessboard pattern. It's
 * pattern you will often see as a background behind a partly transparent image
 * in many applications.
 * 
 * @author Daniel Nilsson
 */
public class AlphaPatternDrawable extends Drawable {

	private final int mAlphaPatternSize;

	private int mNumRectanglesHorizontal;
	private int mNumRectanglesVertical;

	private final Rect mRect = new Rect(), mBounds = new Rect();
	private final Paint mPaint = new Paint();

	public AlphaPatternDrawable(final int alphaPatternSize) {
		mAlphaPatternSize = alphaPatternSize;
	}

	@Override
	public void draw(final Canvas canvas) {

		boolean verticalStartWhite = true;
		for (int i = 0; i <= mNumRectanglesVertical; i++) {
			boolean horizontalStartWhite = verticalStartWhite;
			for (int j = 0; j <= mNumRectanglesHorizontal; j++) {
				mRect.setEmpty();
				mRect.top = i * mAlphaPatternSize + mBounds.top;
				mRect.left = j * mAlphaPatternSize + mBounds.left;
				mRect.bottom = Math.min(mRect.top + mAlphaPatternSize, mBounds.bottom);
				mRect.right = Math.min(mRect.left + mAlphaPatternSize, mBounds.right);

				mPaint.setColor(horizontalStartWhite ? Color.WHITE : Color.GRAY);
				canvas.drawRect(mRect, mPaint);

				horizontalStartWhite = !horizontalStartWhite;
			}
			verticalStartWhite = !verticalStartWhite;
		}
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(final int alpha) {

	}

	@Override
	public void setColorFilter(final ColorFilter cf) {

	}

	@Override
	protected void onBoundsChange(final Rect bounds) {
		super.onBoundsChange(bounds);
		mBounds.set(bounds);
		final int height = bounds.height();
		final int width = bounds.width();
		mNumRectanglesHorizontal = (int) Math.ceil(width / mAlphaPatternSize);
		mNumRectanglesVertical = (int) Math.ceil(height / mAlphaPatternSize);
		invalidateSelf();
	}

}
