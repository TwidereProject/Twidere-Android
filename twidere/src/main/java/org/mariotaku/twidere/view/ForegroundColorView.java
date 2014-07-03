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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.view.iface.IForegroundView;

public class ForegroundColorView extends View implements IForegroundView {

	private final ForegroundViewHelper mForegroundViewHelper;

	private final Rect mAlphaRect, mColorRect;
	private final Paint mPaint;

	private boolean mAlphaPattern;

	private int mNumRectanglesHorizontal;

	private int mNumRectanglesVertical;

	private final int mAlphaPatternSize;

	public ForegroundColorView(final Context context) {
		this(context, null);
	}

	public ForegroundColorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForegroundColorView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mForegroundViewHelper = new ForegroundViewHelper(this, context, attrs, defStyle);
		mAlphaPatternSize = Math.round(getResources().getDisplayMetrics().density * 4);
		mAlphaRect = new Rect();
		mColorRect = new Rect();
		mPaint = new Paint();
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.color });
		setColor(a.getColor(0, Color.TRANSPARENT));
		a.recycle();
	}

	public int getColor() {
		return mPaint.getColor();
	}

	@Override
	public Drawable getForeground() {
		return mForegroundViewHelper.getForeground();
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		mForegroundViewHelper.jumpDrawablesToCurrentState();
	}

	public void setAlphaPatternEnable(final boolean alphaPattern) {
		if (mAlphaPattern == alphaPattern) return;
		mAlphaPattern = alphaPattern;
		invalidate();
	}

	public void setColor(final int color) {
		mPaint.setColor(color);
		invalidate();
	}

	/**
	 * Supply a Drawable that is to be rendered on top of all of the child views
	 * in the frame layout. Any padding in the Drawable will be taken into
	 * account by ensuring that the children are inset to be placed inside of
	 * the padding area.
	 * 
	 * @param drawable The Drawable to be drawn on top of the children.
	 * 
	 * @attr ref android.R.styleable#FrameLayout_foreground
	 */
	@Override
	public void setForeground(final Drawable drawable) {
		mForegroundViewHelper.setForeground(drawable);
	}

	/**
	 * Describes how the foreground is positioned. Defaults to START and TOP.
	 * 
	 * @param foregroundGravity See {@link android.view.Gravity}
	 * 
	 * @attr ref android.R.styleable#FrameLayout_foregroundGravity
	 */
	@Override
	public void setForegroundGravity(final int foregroundGravity) {
		mForegroundViewHelper.setForegroundGravity(foregroundGravity);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		mForegroundViewHelper.drawableStateChanged();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		drawAlphaPattern(canvas);
		canvas.drawRect(mColorRect, mPaint);
		mForegroundViewHelper.dispatchOnDraw(canvas);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mForegroundViewHelper.dispatchOnLayout(changed, left, top, right, bottom);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mForegroundViewHelper.dispatchOnSizeChanged(w, h, oldw, oldh);
		mColorRect.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
		mNumRectanglesHorizontal = (int) Math.ceil(w / mAlphaPatternSize);
		mNumRectanglesVertical = (int) Math.ceil(h / mAlphaPatternSize);
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || mForegroundViewHelper.verifyDrawable(who);
	}

	private void drawAlphaPattern(final Canvas canvas) {
		if (!mAlphaPattern) return;
		boolean verticalStartWhite = true;
		for (int i = 0; i <= mNumRectanglesVertical; i++) {
			boolean horizontalStartWhite = verticalStartWhite;
			for (int j = 0; j <= mNumRectanglesHorizontal; j++) {
				mAlphaRect.top = i * mAlphaPatternSize + getTop();
				mAlphaRect.left = j * mAlphaPatternSize + getLeft();
				mAlphaRect.bottom = Math.min(mAlphaRect.top + mAlphaPatternSize, getBottom());
				mAlphaRect.right = Math.min(mAlphaRect.left + mAlphaPatternSize, getRight());

				mPaint.setColor(horizontalStartWhite ? Color.WHITE : Color.GRAY);
				canvas.drawRect(mAlphaRect, mPaint);

				horizontalStartWhite = !horizontalStartWhite;
			}
			verticalStartWhite = !verticalStartWhite;
		}
	}

}
