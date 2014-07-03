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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ColorPreviewDrawable extends AlphaPatternDrawable {

	private final int mColor;
	private final Paint mPaint;
	private final float[] mPoints;

	public ColorPreviewDrawable(final int alphaPatternSize, final int color) {
		super(alphaPatternSize);
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2.0f);
		mColor = color;
		mPoints = new float[16];
	}

	@Override
	public void draw(final Canvas canvas) {
		super.draw(canvas);
		canvas.drawColor(mColor);
		canvas.drawLines(mPoints, mPaint);
	}

	@Override
	protected void onBoundsChange(final Rect bounds) {
		super.onBoundsChange(bounds);
		mPoints[0] = bounds.top;
		mPoints[1] = bounds.top;
		mPoints[2] = bounds.right;
		mPoints[3] = bounds.top;
		mPoints[4] = bounds.top;
		mPoints[5] = bounds.top;
		mPoints[6] = bounds.top;
		mPoints[7] = bounds.bottom;
		mPoints[8] = bounds.right;
		mPoints[9] = bounds.top;
		mPoints[10] = bounds.right;
		mPoints[11] = bounds.bottom;
		mPoints[12] = bounds.top;
		mPoints[13] = bounds.bottom;
		mPoints[14] = bounds.right;
		mPoints[15] = bounds.bottom;
	}
}
