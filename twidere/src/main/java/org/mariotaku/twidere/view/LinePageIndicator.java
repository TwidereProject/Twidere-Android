/*
 * Copyright (C) 2012 Jake Wharton
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
package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.iface.PagerIndicator;

/**
 * Draws a line for each page. The current page line is colored differently than
 * the unselected page lines.
 */
public class LinePageIndicator extends View implements PagerIndicator {
	private static final int INVALID_POINTER = -1;

	private final Paint mPaintUnselected = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;
	private int mCurrentPage;
	private boolean mCentered;
	private float mLineWidth;
	private float mGapWidth;

	private int mTouchSlop;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mIsDragging;

	public LinePageIndicator(final Context context) {
		this(context, null);
	}

	public LinePageIndicator(final Context context, final AttributeSet attrs) {
		super(context, attrs, 0);
		if (isInEditMode()) return;

		final Resources res = getResources();

		// Load defaults from resources
		final int defaultSelectedColor = res.getColor(R.color.default_line_indicator_selected_color);
		final int defaultUnselectedColor = res.getColor(R.color.default_line_indicator_unselected_color);
		final float defaultLineWidth = res.getDimension(R.dimen.default_line_indicator_line_width);
		final float defaultGapWidth = res.getDimension(R.dimen.default_line_indicator_gap_width);
		final float defaultStrokeWidth = res.getDimension(R.dimen.default_line_indicator_stroke_width);
		final boolean defaultCentered = res.getBoolean(R.bool.default_line_indicator_centered);

		// Retrieve styles attributes
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinePageIndicator, 0, 0);

		mCentered = a.getBoolean(R.styleable.LinePageIndicator_centered, defaultCentered);
		mLineWidth = a.getDimension(R.styleable.LinePageIndicator_lineWidth, defaultLineWidth);
		mGapWidth = a.getDimension(R.styleable.LinePageIndicator_gapWidth, defaultGapWidth);
		setStrokeWidth(a.getDimension(R.styleable.LinePageIndicator_strokeWidth, defaultStrokeWidth));
		mPaintUnselected.setColor(a.getColor(R.styleable.LinePageIndicator_unselectedColor, defaultUnselectedColor));
		mPaintSelected.setColor(a.getColor(R.styleable.LinePageIndicator_selectedColor, defaultSelectedColor));

		final Drawable background = a.getDrawable(R.styleable.LinePageIndicator_android_background);
		if (background != null) {
			ViewAccessor.setBackground(this, background);
		}

		a.recycle();

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
	}

	public float getGapWidth() {
		return mGapWidth;
	}

	public float getLineWidth() {
		return mLineWidth;
	}

	public int getSelectedColor() {
		return mPaintSelected.getColor();
	}

	public float getStrokeWidth() {
		return mPaintSelected.getStrokeWidth();
	}

	public int getUnselectedColor() {
		return mPaintUnselected.getColor();
	}

	public boolean isCentered() {
		return mCentered;
	}

	@Override
	public void notifyDataSetChanged() {
		invalidate();
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageScrollStateChanged(final int state) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageSelected(final int position) {
		mCurrentPage = position;
		invalidate();

		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPage = savedState.currentPage;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState savedState = new SavedState(superState);
		savedState.currentPage = mCurrentPage;
		return savedState;
	}

	@Override
	public boolean onTouchEvent(final android.view.MotionEvent ev) {
		if (super.onTouchEvent(ev)) return true;
		if (mViewPager == null || mViewPager.getAdapter().getCount() == 0) return false;

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
				mLastMotionX = ev.getX();
				break;

			case MotionEvent.ACTION_MOVE: {
				final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final float deltaX = x - mLastMotionX;

				if (!mIsDragging) {
					if (Math.abs(deltaX) > mTouchSlop) {
						mIsDragging = true;
					}
				}

				if (mIsDragging) {
					mLastMotionX = x;
					if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
						mViewPager.fakeDragBy(deltaX);
					}
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (!mIsDragging) {
					final int count = mViewPager.getAdapter().getCount();
					final int width = getWidth();
					final float halfWidth = width / 2f;
					final float sixthWidth = width / 6f;

					if (mCurrentPage > 0 && ev.getX() < halfWidth - sixthWidth) {
						if (action != MotionEvent.ACTION_CANCEL) {
							mViewPager.setCurrentItem(mCurrentPage - 1);
						}
						return true;
					} else if (mCurrentPage < count - 1 && ev.getX() > halfWidth + sixthWidth) {
						if (action != MotionEvent.ACTION_CANCEL) {
							mViewPager.setCurrentItem(mCurrentPage + 1);
						}
						return true;
					}
				}

				mIsDragging = false;
				mActivePointerId = INVALID_POINTER;
				if (mViewPager.isFakeDragging()) {
					mViewPager.endFakeDrag();
				}
				break;

			case MotionEventCompat.ACTION_POINTER_DOWN: {
				final int index = MotionEventCompat.getActionIndex(ev);
				mLastMotionX = MotionEventCompat.getX(ev, index);
				mActivePointerId = MotionEventCompat.getPointerId(ev, index);
				break;
			}

			case MotionEventCompat.ACTION_POINTER_UP:
				final int pointerIndex = MotionEventCompat.getActionIndex(ev);
				final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
				if (pointerId == mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
				}
				mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
				break;
		}

		return true;
	}

	public void setCentered(final boolean centered) {
		mCentered = centered;
		invalidate();
	}

	@Override
	public void setCurrentItem(final int item) {
		if (mViewPager == null) throw new IllegalStateException("ViewPager has not been bound.");
		mViewPager.setCurrentItem(item);
		mCurrentPage = item;
		invalidate();
	}

	public void setGapWidth(final float gapWidth) {
		mGapWidth = gapWidth;
		invalidate();
	}

	public void setLineWidth(final float lineWidth) {
		mLineWidth = lineWidth;
		invalidate();
	}

	@Override
	public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	public void setSelectedColor(final int selectedColor) {
		mPaintSelected.setColor(selectedColor);
		invalidate();
	}

	public void setStrokeWidth(final float lineHeight) {
		mPaintSelected.setStrokeWidth(lineHeight);
		mPaintUnselected.setStrokeWidth(lineHeight);
		invalidate();
	}

	public void setUnselectedColor(final int unselectedColor) {
		mPaintUnselected.setColor(unselectedColor);
		invalidate();
	}

	@Override
	public void setViewPager(final ViewPager viewPager) {
		if (mViewPager == viewPager) return;
		if (mViewPager != null) {
			// Clear us from the old pager.
			mViewPager.setOnPageChangeListener(null);
		}
		if (viewPager.getAdapter() == null)
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		mViewPager = viewPager;
		mViewPager.setOnPageChangeListener(this);
		invalidate();
	}

	@Override
	public void setViewPager(final ViewPager view, final int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		if (mViewPager == null) return;
		final int count = mViewPager.getAdapter().getCount();
		if (count == 0) return;

		if (mCurrentPage >= count) {
			setCurrentItem(count - 1);
			return;
		}

		final float lineWidthAndGap = mLineWidth + mGapWidth;
		final float indicatorWidth = count * lineWidthAndGap - mGapWidth;
		final float paddingTop = getPaddingTop();
		final float paddingLeft = getPaddingLeft();
		final float paddingRight = getPaddingRight();

		final float verticalOffset = paddingTop + (getHeight() - paddingTop - getPaddingBottom()) / 2.0f;
		float horizontalOffset = paddingLeft;
		if (mCentered) {
			horizontalOffset += (getWidth() - paddingLeft - paddingRight) / 2.0f - indicatorWidth / 2.0f;
		}

		// Draw stroked circles
		for (int i = 0; i < count; i++) {
			final float dx1 = horizontalOffset + i * lineWidthAndGap;
			final float dx2 = dx1 + mLineWidth;
			canvas.drawLine(dx1, verticalOffset, dx2, verticalOffset, i == mCurrentPage ? mPaintSelected
					: mPaintUnselected);
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(final int measureSpec) {
		float result;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the height
			result = mPaintSelected.getStrokeWidth() + getPaddingTop() + getPaddingBottom();
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return (int) Math.ceil(result);
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(final int measureSpec) {
		float result;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY || mViewPager == null) {
			// We were told how big to be
			result = specSize;
		} else {
			// Calculate the width according the views count
			final int count = mViewPager.getAdapter().getCount();
			result = getPaddingLeft() + getPaddingRight() + count * mLineWidth + (count - 1) * mGapWidth;
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return (int) Math.ceil(result);
	}

	static class SavedState extends BaseSavedState {
		int currentPage;

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		private SavedState(final Parcel in) {
			super(in);
			currentPage = in.readInt();
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPage);
		}
	}
}