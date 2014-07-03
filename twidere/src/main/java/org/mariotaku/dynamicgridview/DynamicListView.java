/*
 * Copyright (C) 2013 The Android Open Source Project
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

package org.mariotaku.dynamicgridview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * The dynamic listview is an extension of listview that supports cell dragging
 * and swapping.
 * 
 * This layout is in charge of positioning the hover cell in the correct
 * location on the screen in response to user touch events. It uses the position
 * of the hover cell to determine when two cells should be swapped. If two cells
 * should be swapped, all the corresponding data set and layout changes are
 * handled here.
 * 
 * If no cell is selected, all the touch events are passed down to the listview
 * and behave normally. If one of the items in the listview experiences a long
 * press event, the contents of its current visible state are captured as a
 * bitmap and its visibility is set to INVISIBLE. A hover cell is then created
 * and added to this layout as an overlaying BitmapDrawable above the listview.
 * Once the hover cell is translated some distance to signify an item swap, a
 * data set change accompanied by animation takes place. When the user releases
 * the hover cell, it animates into its corresponding position in the listview.
 * 
 * When the hover cell is either above or below the bounds of the listview, this
 * listview also scrolls on its own so as to reveal additional content.
 */
public class DynamicListView extends ListView {

	private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
	private final int MOVE_DURATION = 150;
	private final int LINE_THICKNESS = 15;

	private int mLastEventY = -1;

	private int mDownY = -1;
	private int mDownX = -1;

	private int mTotalOffset = 0;

	private boolean mCellIsMobile = false;
	private boolean mIsMobileScrolling = false;
	private int mSmoothScrollAmountAtEdge = 0;

	private final int INVALID_ID = -1;
	private long mAboveItemId = INVALID_ID;
	private long mMobileItemId = INVALID_ID;
	private long mBelowItemId = INVALID_ID;

	private BitmapDrawable mHoverCell;
	private Rect mHoverCellCurrentBounds;
	private Rect mHoverCellOriginalBounds;

	private final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private boolean mIsWaitingForScrollFinish = false;
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	/**
	 * Listens for long clicks on any items in the listview. When a cell has
	 * been selected, the hover cell is created and set up.
	 */
	private final AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int pos, final long id) {
			mTotalOffset = 0;

			final int position = pointToPosition(mDownX, mDownY);
			final int itemNum = position - getFirstVisiblePosition();

			final View selectedView = getChildAt(itemNum);
			mMobileItemId = getAdapter().getItemId(position);
			mHoverCell = getAndAddHoverView(selectedView);
			selectedView.setVisibility(INVISIBLE);

			mCellIsMobile = true;

			updateNeighborViewsForID(mMobileItemId);

			return true;
		}
	};

	/**
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its
	 * final location when the user lifts his finger by modifying the
	 * BitmapDrawable's bounds.
	 */
	private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
		@Override
		public Rect evaluate(final float fraction, final Rect startValue, final Rect endValue) {
			return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top,
					endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(
					startValue.bottom, endValue.bottom, fraction));
		}

		public int interpolate(final int start, final int end, final float fraction) {
			return (int) (start + fraction * (end - start));
		}
	};

	/**
	 * This scroll listener is added to the listview in order to handle cell
	 * swapping when the cell is either at the top or bottom edge of the
	 * listview. If the hover cell is at either edge of the listview, the
	 * listview will begin scrolling. As scrolling takes place, the listview
	 * continuously checks if new cells became visible and determines whether
	 * they are potential candidates for a cell swap.
	 */
	private final AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		private int mPreviousFirstVisibleItem = -1;
		private int mPreviousVisibleItemCount = -1;
		private int mCurrentFirstVisibleItem;
		private int mCurrentVisibleItemCount;
		private int mCurrentScrollState;

		/**
		 * Determines if the listview scrolled up enough to reveal a new cell at
		 * the top of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleFirstVisibleCellChange() {
			if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}

		/**
		 * Determines if the listview scrolled down enough to reveal a new cell
		 * at the bottom of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleLastVisibleCellChange() {
			final int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
			final int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
			if (currentLastVisibleItem != previousLastVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}

		@Override
		public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
				final int totalItemCount) {
			mCurrentFirstVisibleItem = firstVisibleItem;
			mCurrentVisibleItemCount = visibleItemCount;

			mPreviousFirstVisibleItem = mPreviousFirstVisibleItem == -1 ? mCurrentFirstVisibleItem
					: mPreviousFirstVisibleItem;
			mPreviousVisibleItemCount = mPreviousVisibleItemCount == -1 ? mCurrentVisibleItemCount
					: mPreviousVisibleItemCount;

			checkAndHandleFirstVisibleCellChange();
			checkAndHandleLastVisibleCellChange();

			mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
			mPreviousVisibleItemCount = mCurrentVisibleItemCount;
		}

		@Override
		public void onScrollStateChanged(final AbsListView view, final int scrollState) {
			mCurrentScrollState = scrollState;
			mScrollState = scrollState;
			isScrollCompleted();
		}

		/**
		 * This method is in charge of invoking 1 of 2 actions. Firstly, if the
		 * listview is in a state of scrolling invoked by the hover cell being
		 * outside the bounds of the listview, then this scrolling event is
		 * continued. Secondly, if the hover cell has already been released,
		 * this invokes the animation for the hover cell to return to its
		 * correct position after the listview has entered an idle scroll state.
		 */
		private void isScrollCompleted() {
			if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
				if (mCellIsMobile && mIsMobileScrolling) {
					handleMobileCellScroll();
				} else if (mIsWaitingForScrollFinish) {
					touchEventsEnded();
				}
			}
		}
	};

	public DynamicListView(final Context context) {
		super(context);
		init(context);
	}

	public DynamicListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DynamicListView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/** Retrieves the position in the list corresponding to itemID */
	public int getPositionForID(final long itemID) {
		final View v = getViewForID(itemID);
		if (v == null)
			return -1;
		else
			return getPositionForView(v);
	}

	/** Retrieves the view in the list corresponding to itemID */
	public View getViewForID(final long itemID) {
		final int firstVisiblePosition = getFirstVisiblePosition();
		final ListAdapter adapter = getAdapter();
		for (int i = 0; i < getChildCount(); i++) {
			final View v = getChildAt(i);
			final int position = firstVisiblePosition + i;
			final long id = adapter.getItemId(position);
			if (id == itemID) return v;
		}
		return null;
	}

	/**
	 * This method is in charge of determining if the hover cell is above or
	 * below the bounds of the listview. If so, the listview does an appropriate
	 * upward or downward smooth scroll so as to reveal new items.
	 */
	public boolean handleMobileCellScroll(final Rect r) {
		final int offset = computeVerticalScrollOffset();
		final int height = getHeight();
		final int extent = computeVerticalScrollExtent();
		final int range = computeVerticalScrollRange();
		final int hoverViewTop = r.top;
		final int hoverHeight = r.height();

		if (hoverViewTop <= 0 && offset > 0) {
			smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		if (hoverViewTop + hoverHeight >= height && offset + extent < range) {
			smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		return false;
	}

	public void init(final Context context) {
		setOnItemLongClickListener(mOnItemLongClickListener);
		setOnScrollListener(mScrollListener);
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mDownX = (int) event.getX();
				mDownY = (int) event.getY();
				mActivePointerId = event.getPointerId(0);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mActivePointerId == INVALID_POINTER_ID) {
					break;
				}

				int pointerIndex = event.findPointerIndex(mActivePointerId);

				mLastEventY = (int) event.getY(pointerIndex);
				final int deltaY = mLastEventY - mDownY;

				if (mCellIsMobile) {
					mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top
							+ deltaY + mTotalOffset);
					mHoverCell.setBounds(mHoverCellCurrentBounds);
					invalidate();

					handleCellSwitch();

					mIsMobileScrolling = false;
					handleMobileCellScroll();

					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				touchEventsEnded();
				break;
			case MotionEvent.ACTION_CANCEL:
				touchEventsCancelled();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				/*
				 * If a multitouch event took place and the original touch
				 * dictating the movement of the hover cell has ended, then the
				 * dragging event ends and the hover cell is animated to its
				 * corresponding position in the listview.
				 */
				pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					touchEventsEnded();
				}
				break;
			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void setAdapter(final ListAdapter adapter) {
		if (!(adapter instanceof DraggableAdapter))
			throw new IllegalArgumentException("Adapter have to implement DraggableAdapter");
		super.setAdapter(adapter);
	}

	/**
	 * dispatchDraw gets invoked when all the child views are about to be drawn.
	 * By overriding this method, the hover cell (BitmapDrawable) can be drawn
	 * over the listview's items whenever the listview is redrawn.
	 */
	@Override
	protected void dispatchDraw(final Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHoverCell != null) {
			mHoverCell.draw(canvas);
		}
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate
	 * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(final View v) {

		final int w = v.getWidth();
		final int h = v.getHeight();
		final int top = v.getTop();
		final int left = v.getLeft();

		final Bitmap b = getBitmapWithBorder(v);

		final BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
		mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

		drawable.setBounds(mHoverCellCurrentBounds);

		return drawable;
	}

	/** Returns a bitmap showing a screenshot of the view passed in. */
	private Bitmap getBitmapFromView(final View v) {
		final Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}

	/** Draws a black border over the screenshot of the view passed in. */
	private Bitmap getBitmapWithBorder(final View v) {
		final Bitmap bitmap = getBitmapFromView(v);
		final Canvas can = new Canvas(bitmap);

		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		final Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(LINE_THICKNESS);
		paint.setColor(Color.BLACK);

		can.drawBitmap(bitmap, 0, 0, null);
		can.drawRect(rect, paint);

		return bitmap;
	}

	/**
	 * This method determines whether the hover cell has been shifted far enough
	 * to invoke a cell swap. If so, then the respective cell swap candidate is
	 * determined and the data set is changed. Upon posting a notification of
	 * the data set change, a layout is invoked to place the cells in the right
	 * place. Using a ViewTreeObserver and a corresponding OnPreDrawListener, we
	 * can offset the cell being swapped to where it previously was and then
	 * animate it to its new position.
	 */
	private void handleCellSwitch() {
		final int deltaY = mLastEventY - mDownY;
		final int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

		final View belowView = getViewForID(mBelowItemId);
		final View mobileView = getViewForID(mMobileItemId);
		final View aboveView = getViewForID(mAboveItemId);

		final boolean isBelow = belowView != null && deltaYTotal > belowView.getTop();
		final boolean isAbove = aboveView != null && deltaYTotal < aboveView.getTop();

		if (isBelow || isAbove) {

			final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
			final View switchView = isBelow ? belowView : aboveView;
			final int originalItem = getPositionForView(mobileView);

			if (switchView == null) {
				updateNeighborViewsForID(mMobileItemId);
				return;
			}

			final int newItemPosition = getPositionForID(switchItemID);
			((DraggableAdapter) getAdapter()).swapElements(originalItem, newItemPosition);

			mDownY = mLastEventY;

			final int switchViewStartTop = switchView.getTop();

			mobileView.setVisibility(View.VISIBLE);
			switchView.setVisibility(View.INVISIBLE);

			updateNeighborViewsForID(mMobileItemId);

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);

					final View switchView = getViewForID(switchItemID);

					mTotalOffset += deltaY;

					final int switchViewNewTop = switchView.getTop();
					final int delta = switchViewStartTop - switchViewNewTop;

					switchView.setTranslationY(delta);

					final ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, View.TRANSLATION_Y, 0);
					animator.setDuration(MOVE_DURATION);
					animator.start();

					return true;
				}
			});
		}
	}

	/**
	 * Determines whether this listview is in a scrolling state invoked by the
	 * fact that the hover cell is out of the bounds of the listview;
	 */
	private void handleMobileCellScroll() {
		mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
	}

	/**
	 * Resets all the appropriate fields to a default state.
	 */
	private void touchEventsCancelled() {
		final View mobileView = getViewForID(mMobileItemId);
		if (mCellIsMobile) {
			mAboveItemId = INVALID_ID;
			mMobileItemId = INVALID_ID;
			mBelowItemId = INVALID_ID;
			mobileView.setVisibility(VISIBLE);
			mHoverCell = null;
			invalidate();
		}
		mCellIsMobile = false;
		mIsMobileScrolling = false;
		mActivePointerId = INVALID_POINTER_ID;
	}

	/**
	 * Resets all the appropriate fields to a default state while also animating
	 * the hover cell back to its correct location.
	 */
	private void touchEventsEnded() {
		final View mobileView = getViewForID(mMobileItemId);
		if (mCellIsMobile || mIsWaitingForScrollFinish) {
			mCellIsMobile = false;
			mIsWaitingForScrollFinish = false;
			mIsMobileScrolling = false;
			mActivePointerId = INVALID_POINTER_ID;

			// If the autoscroller has not completed scrolling, we need to wait
			// for it to
			// finish in order to determine the final location of where the
			// hover cell
			// should be animated to.
			if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				mIsWaitingForScrollFinish = true;
				return;
			}

			mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

			final ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds", sBoundEvaluator,
					mHoverCellCurrentBounds);
			hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(final ValueAnimator valueAnimator) {
					invalidate();
				}
			});
			hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(final Animator animation) {
					mAboveItemId = INVALID_ID;
					mMobileItemId = INVALID_ID;
					mBelowItemId = INVALID_ID;
					mobileView.setVisibility(VISIBLE);
					mHoverCell = null;
					setEnabled(true);
					invalidate();
				}

				@Override
				public void onAnimationStart(final Animator animation) {
					setEnabled(false);
				}
			});
			hoverViewAnimator.start();
		} else {
			touchEventsCancelled();
		}
	}

	/**
	 * Stores a reference to the views above and below the item currently
	 * corresponding to the hover cell. It is important to note that if this
	 * item is either at the top or bottom of the list, mAboveItemId or
	 * mBelowItemId may be invalid.
	 */
	private void updateNeighborViewsForID(final long itemID) {
		final int position = getPositionForID(itemID);
		final ListAdapter adapter = getAdapter();
		mAboveItemId = adapter.getItemId(position - 1);
		mBelowItemId = adapter.getItemId(position + 1);
	}
}