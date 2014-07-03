package com.scvngr.levelup.views.gallery;

/*
 * Based on the android GalleryView source. Modified to allow the view to snap to the
 * left rather than the center. The biggest changes in this class are marked with comments
 * with "Note:".
 *
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

/**
 * A view that shows items in a center-locked, horizontally scrolling list.
 * <p>
 * The default values for the Gallery assume you will be using
 * {@link android.R.styleable#Theme_GalleryItemBackground} as the background for
 * each View given to the Gallery from the Adapter. If you are not doing this,
 * you may need to adjust some Gallery properties, such as the spacing.
 * <p>
 * Views given to the Gallery should use {@link Gallery.LayoutParams} as their
 * layout parameters type.
 * 
 * <p>
 * See the <a href="{@docRoot}
 * resources/tutorials/views/hello-Gallery.html">Gallery tutorial</a>.
 * </p>
 * 
 * CHECKSTYLE:OFF Android comments
 * 
 * @attr ref android.R.styleable#Gallery_animationDuration
 * @attr ref android.R.styleable#Gallery_spacing
 * @attr ref android.R.styleable#Gallery_gravity CHECKSTYLE:ON
 */
public final class Gallery extends AbsSpinner implements GestureDetector.OnGestureListener {

	/**
	 * Duration in milliseconds from the start of a scroll during which we're
	 * unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 */
	// CHECKSTYLE:OFF not a magic number
	private int mAnimationDuration = 400;
	// CHECKSTYLE:ON

	/**
	 * The alpha of items that are not selected.
	 */
	private float mUnselectedAlpha;

	/**
	 * Left most edge of a child seen so far during layout.
	 */
	private int mLeftMost;

	/**
	 * Right most edge of a child seen so far during layout.
	 */
	private int mRightMost;

	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private final GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement.
	 */
	private final FlingRunnable mFlingRunnable = new FlingRunnable();

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false
	 * in the future. It will also trigger a selection changed.
	 */
	private final Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
		@Override
		public void run() {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * The currently selected item's child.
	 */
	private View mSelectedChild;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener.
	 */
	private boolean mSuppressSelectionChanged;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key
	 * down. This is checked before we action on the "invoke" key up, and is
	 * subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	/**
	 * If true, mFirstPosition is the position of the rightmost child, and the
	 * children are ordered right to left.
	 */
	private boolean mIsRtl = true;

	private boolean mScrollToChildAfterItemClickEnabled;
	private boolean mRightSpacingEnabled;

	/**
	 * Constructor.
	 * 
	 * @param context the context to inflate into
	 */
	public Gallery(final Context context) {
		this(context, null);
	}

	/**
	 * Constructor for XML inflation.
	 * 
	 * @param context the context to inflate with
	 * @param attrs the xml attrs
	 */
	public Gallery(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.galleryStyle);
	}

	/**
	 * Constructor for XML inflation.
	 * 
	 * @param context the context to inflate with
	 * @param attrs the XML attrs
	 * @param defStyle the default style resource id
	 */
	public Gallery(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);
		setScrollAfterItemClickEnabled(true);
		setScrollRightSpacingEnabled(true);
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		// Gallery steals all key events
		return event.dispatch(this, null, null);
	}

	@Override
	public void dispatchSetSelected(final boolean selected) {
		/*
		 * We don't want to pass the selected state given from its parent to its
		 * children since this widget itself has a selected state to give to its
		 * children.
		 */
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	public boolean onDown(final MotionEvent e) {

		// Kill any existing fling/scroll
		mFlingRunnable.stop(false);

		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
			mDownTouchView.setPressed(true);
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	@Override
	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {

		if (!mShouldCallbackDuringFling) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks(mDisableSuppressSelectionChangedRunnable);

			// This will get reset once we scroll into slots
			if (!mSuppressSelectionChanged) {
				mSuppressSelectionChanged = true;
			}
		}

		// Fling the Gallery!
		mFlingRunnable.startUsingVelocity((int) -velocityX);

		return true;
	}

	/**
	 * Handles left, right, and clicking.
	 * 
	 * @see android.view.View#onKeyDown
	 */
	// CHECKSTYLE:OFF unmodified
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (movePrevious()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
				}
				return true;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (moveNext()) {
					playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
				}
				return true;

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				mReceivedInvokeKeyDown = true;
				// fallthrough to default handling
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER: {

				if (mReceivedInvokeKeyDown) {
					if (mItemCount > 0) {

						dispatchPress(mSelectedChild);
						postDelayed(new Runnable() {
							@Override
							public void run() {
								dispatchUnpress();
							}
						}, ViewConfiguration.getPressedStateDuration());

						final int selectedIndex = mSelectedPosition - mFirstPosition;
						performItemClick(getChildAt(selectedIndex), mSelectedPosition,
								mAdapter.getItemId(mSelectedPosition));
					}
				}

				// Clear the flag
				mReceivedInvokeKeyDown = false;

				return true;
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onLongPress(final MotionEvent e) {

		if (mDownTouchPosition < 0) return;

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		final long id = getItemIdAtPosition(mDownTouchPosition);
		dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
	}

	@Override
	public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {

		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent(true);

		/*
		 * As the user scrolls, we want to callback selection changes so
		 * related- info on the screen is up-to-date with the Gallery's
		 * selection
		 */
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged) {
					mSuppressSelectionChanged = true;
				}

				postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
			}
		} else {
			if (mSuppressSelectionChanged) {
				mSuppressSelectionChanged = false;
			}
		}

		// Track the motion
		trackMotionScroll(-1 * (int) distanceX);

		mIsFirstScroll = false;
		return true;
	}

	@Override
	public void onShowPress(final MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(final MotionEvent e) {

		if (mDownTouchPosition >= 0) {
			if (mScrollToChildAfterItemClickEnabled) {
				// An item tap should make it selected, so scroll to this child.
				scrollToChild(mDownTouchPosition - mFirstPosition);
			}

			// Also pass the click so the client knows, if it wants to.
			if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
				performItemClick(mDownTouchView, mDownTouchPosition, mAdapter.getItemId(mDownTouchPosition));
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {

		if (!isEnabled()) return false;

		// Give everything to the gesture detector
		final boolean retValue = mGestureDetector.onTouchEvent(event);

		final int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			// Helper method for lifted finger
			onUp();
		} else if (action == MotionEvent.ACTION_CANCEL) {
			onCancel();
		}

		return retValue;
	}

	/**
	 * Sets how long the transition animation should run when a child view
	 * changes position. Only relevant if animation is turned on.
	 * 
	 * @param animationDurationMillis The duration of the transition, in
	 *            milliseconds.
	 * 
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration(final int animationDurationMillis) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Whether or not to callback on any {@link #getOnItemSelectedListener()}
	 * while the items are being flinged. If false, only the final selected item
	 * will cause the callback. If true, all items between the first and the
	 * final will cause callbacks.
	 * 
	 * @param shouldCallback Whether or not to callback on the listener while
	 *            the items are being flinged.
	 */
	public void setCallbackDuringFling(final boolean shouldCallback) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked.
	 * If false, the item will become selected (and re-centered). If true, the
	 * {@link #getOnItemClickListener()} will get the callback.
	 * 
	 * @param shouldCallback Whether or not to callback on the listener when a
	 *            item that is not selected is clicked.
	 * @hide
	 */
	public void setCallbackOnUnselectedItemClick(final boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Describes how the child views are aligned.
	 * 
	 * @param gravity the gravity to set
	 * 
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity(final int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayout();
		}
	}

	public void setScrollAfterItemClickEnabled(final boolean enabled) {
		mScrollToChildAfterItemClickEnabled = enabled;
	}

	public void setScrollRightSpacingEnabled(final boolean enabled) {
		mRightSpacingEnabled = enabled;
	}

	/**
	 * Sets the spacing between items in a Gallery.
	 * 
	 * @param spacing The spacing in pixels between items in the Gallery
	 * 
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing(final int spacing) {
		mSpacing = spacing;
	}

	/**
	 * Sets the alpha of items that are not selected in the Gallery.
	 * 
	 * @param unselectedAlpha the alpha for the items that are not selected.
	 * 
	 * @attr ref android.R.styleable#Gallery_unselectedAlpha
	 */
	public void setUnselectedAlpha(final float unselectedAlpha) {
		mUnselectedAlpha = unselectedAlpha;
	}

	@Override
	public boolean showContextMenu() {

		if (isPressed() && mSelectedPosition >= 0) {
			final int index = mSelectedPosition - mFirstPosition;
			final View v = getChildAt(index);
			return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
		}

		return false;
	}

	@Override
	public boolean showContextMenuForChild(final View originalView) {

		final int longPressPosition = getPositionForView(originalView);
		if (longPressPosition < 0) return false;

		final long longPressId = mAdapter.getItemId(longPressPosition);
		return dispatchLongPress(originalView, longPressPosition, longPressId);
	}

	@Override
	protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected int computeHorizontalScrollExtent() {
		// Only 1 item is considered to be selected
		return 1;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		// Current scroll position is the same as the selected position
		return mSelectedPosition;
	}

	@Override
	protected int computeHorizontalScrollRange() {
		// Scroll range is the same as the item count
		return mItemCount;
	}

	@Override
	protected void dispatchSetPressed(final boolean pressed) {

		// Show the pressed state on the selected child
		if (mSelectedChild != null) {
			mSelectedChild.setPressed(pressed);
		}
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		/*
		 * Gallery expects Gallery.LayoutParams.
		 */
		return new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	// CHECKSTYLE:ON

	@Override
	protected int getChildDrawingOrder(final int childCount, final int i) {
		final int selectedIndex = mSelectedPosition - mFirstPosition;

		// Just to be safe
		if (selectedIndex < 0) return i;

		if (i == childCount - 1)
			// Draw the selected child last
			return selectedIndex;
		else if (i >= selectedIndex) // Move the children after the selected
										// child earlier one
			return i + 1;
		else
			// Keep the children before the selected child the same
			return i;
	}

	// CHECKSTYLE:OFF unchanged method
	@Override
	protected boolean getChildStaticTransformation(final View child, final Transformation t) {

		t.clear();
		t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

		return true;
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

	@Override
	protected void onFocusChanged(final boolean gainFocus, final int direction, final Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		/*
		 * The Gallery shows focus by focusing the selected item. So, give focus
		 * to our selected item instead. We steal keys from our selected item
		 * elsewhere.
		 */
		if (gainFocus && mSelectedChild != null) {
			mSelectedChild.requestFocus(direction);
			mSelectedChild.setSelected(true);
		}

	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		super.onLayout(changed, l, t, r, b);

		/*
		 * Remember that we are in layout to prevent more layout request from
		 * being generated.
		 */
		mInLayout = true;
		layout(0, false);
		mInLayout = false;
	}

	/**
	 * Figure out vertical placement based on mGravity.
	 * 
	 * @param child Child to place
	 * @param duringLayout if this is being called during layout
	 * @return Where the top of the child should be
	 */
	private int calculateTop(final View child, final boolean duringLayout) {
		// CHECKSTYLE:OFF unmodified
		final int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		final int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

		int childTop = 0;

		switch (mGravity) {
			case Gravity.TOP:
				childTop = mSpinnerPadding.top;
				break;
			case Gravity.CENTER_VERTICAL:
				final int availableSpace = myHeight - mSpinnerPadding.bottom - mSpinnerPadding.top - childHeight;
				childTop = mSpinnerPadding.top + availableSpace / 2;
				break;
			case Gravity.BOTTOM:
				childTop = myHeight - mSpinnerPadding.bottom - childHeight;
				break;
		}
		return childTop;
		// CHECKSTYLE:ON
	}

	// CHECKSTYLE:ON

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 * 
	 * @param toLeft Whether to detach children to the left of the Gallery, or
	 *            to the right.
	 */
	private void detachOffScreenChildren(final boolean toLeft) {
		final int numChildren = getChildCount();
		final int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toLeft) {
			final int galleryLeft = getPaddingLeft();
			for (int i = 0; i < numChildren; i++) {
				// CHECKSTYLE:OFF unmodified
				final int n = mIsRtl ? numChildren - 1 - i : i;
				// CHECKSTYLE:ON
				final View child = getChildAt(n);
				if (child.getRight() >= galleryLeft) {
					break;
				} else {
					start = n;
					count++;
					mRecycler.put(firstPosition + n, child);
				}
			}
			if (!mIsRtl) {
				start = 0;
			}
		} else {
			final int galleryRight = getWidth() - getPaddingRight();
			for (int i = numChildren - 1; i >= 0; i--) {
				// CHECKSTYLE:OFF unmodified
				final int n = mIsRtl ? numChildren - 1 - i : i;
				// CHECKSTYLE:ON
				final View child = getChildAt(n);
				if (child.getLeft() <= galleryRight) {
					break;
				} else {
					start = n;
					count++;
					mRecycler.put(firstPosition + n, child);
				}
			}
			if (mIsRtl) {
				start = 0;
			}
		}

		detachViewsFromParent(start, count);

		if (toLeft != mIsRtl) {
			mFirstPosition += count;
		}
	}

	// CHECKSTYLE:OFF unmodified
	private boolean dispatchLongPress(final View view, final int position, final long id) {
		boolean handled = false;

		if (mOnItemLongClickListener != null) {
			handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView, mDownTouchPosition, id);
		}

		if (!handled) {
			mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
			handled = super.showContextMenuForChild(this);
		}

		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}

		return handled;
	}

	// CHECKSTYLE:ON

	// CHECKSTYLE:OFF unmodified
	private void dispatchPress(final View child) {

		if (child != null) {
			child.setPressed(true);
		}

		setPressed(true);
	}

	private void dispatchUnpress() {

		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).setPressed(false);
		}

		setPressed(false);
	}

	// CHECKSTYLE:OFF unmodified
	private void fillToGalleryLeft() {
		if (mIsRtl) {
			fillToGalleryLeftRtl();
		} else {
			fillToGalleryLeftLtr();
		}
	}

	private void fillToGalleryLeftLtr() {
		final int itemSpacing = mSpacing;
		final int galleryLeft = getPaddingLeft();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		} else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition >= 0) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	private void fillToGalleryLeftRtl() {
		final int itemSpacing = mSpacing;
		final int galleryLeft = getPaddingLeft();
		final int numChildren = getChildCount();

		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		} else {
			// No children available!
			mFirstPosition = curPosition = mItemCount - 1;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition < mItemCount) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition++;
		}
	}

	// CHECKSTYLE:ON

	// Unused methods from GestureDetector.OnGestureListener below

	private void fillToGalleryRight() {
		if (mIsRtl) {
			fillToGalleryRightRtl();
		} else {
			fillToGalleryRightLtr();
		}
	}

	// Unused methods from GestureDetector.OnGestureListener above

	private void fillToGalleryRightLtr() {
		final int itemSpacing = mSpacing;
		final int galleryRight = getRight() - getLeft() - getPaddingRight();
		final int numChildren = getChildCount();
		final int numItems = mItemCount;

		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		} else {
			mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < galleryRight && curPosition < numItems) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	private void fillToGalleryRightRtl() {
		final int itemSpacing = mSpacing;
		final int GalleryRight = getRight() - getLeft() - getPaddingRight();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		} else {
			curPosition = 0;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < GalleryRight && curPosition >= 0) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition--;
		}
	}

	/**
	 * Modified from getCenterOfGallery. This now gets the left of the view
	 * (plus padding).
	 * 
	 * @return The left of this Gallery.
	 */
	private int getGalleryLockPoint() {
		// Lock left of the view
		return getPaddingLeft();
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by
	 * getting a new one from the adapter. If we are animating, make sure there
	 * is enough information in the view's layout parameters to animate from the
	 * old to new positions.
	 * 
	 * @param position Position in the Gallery for the view to obtain
	 * @param offset Offset from the selected position
	 * @param x X-coordinate indicating where this view should be placed. This
	 *            will either be the left or right edge of the view, depending
	 *            on the fromLeft parameter
	 * @param fromLeft Are we positioning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 * @return A view that has been added to the Gallery
	 */
	private View makeAndAddView(final int position, final int offset, final int x, final boolean fromLeft) {

		View child;
		if (!mDataChanged) {
			child = mRecycler.get(position);
			if (child != null) {
				// Can reuse an existing view
				final int childLeft = child.getLeft();

				// Remember left and right edges of where views have been placed
				mRightMost = Math.max(mRightMost, childLeft + child.getMeasuredWidth());
				mLeftMost = Math.min(mLeftMost, childLeft);

				// Position the view
				setUpChild(child, offset, x, fromLeft);

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView(position, null, this);

		// Position the view
		setUpChild(child, offset, x, fromLeft);

		return child;
	}

	// CHECKSTYLE:ON

	/**
	 * Offset the horizontal location of all children of this view by the
	 * specified number of pixels.
	 * 
	 * @param offset the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight(final int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).offsetLeftAndRight(offset);
		}
	}

	// CHECKSTYLE:OFF unmodified
	private void onFinishedMovement() {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		invalidate();
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center
	 * is the Gallery's center).
	 * 
	 * Note: modifed to snap the left of a view rather than the center
	 */
	private void scrollIntoSlots() {
		if (getChildCount() == 0 || mSelectedChild == null) return;

		final int selectedCenter = getLeftOfView(mSelectedChild);
		final int targetCenter = getGalleryLockPoint();

		final int scrollAmount = targetCenter - selectedCenter;
		if (scrollAmount != 0) {
			mFlingRunnable.startUsingDistance(scrollAmount);
		} else {
			onFinishedMovement();
		}
	}

	/**
	 * Scroll to the child at the position passed.
	 * 
	 * @param childPosition the child's position
	 * @return if scrolling to the child is starting, false otherwise
	 */
	private boolean scrollToChild(final int childPosition) {
		final View child = getChildAt(childPosition);

		if (child != null) {
			final int distance = getGalleryLockPoint() - getLeftOfView(child);
			mFlingRunnable.startUsingDistance(distance);
			return true;
		}

		return false;
	}

	/**
	 * Looks for the child that is closest to the lock point of the gallery and
	 * sets it as the selected child.
	 * 
	 * Note: modified from setSelectionToCenterChild to now select the the
	 * selected view to the child closest to the gallery's "lock point" (it's
	 * left).
	 */
	private void setSelectionToChildClosestToLockPoint() {
		if (mSelectedChild == null) return;

		final int lockPoint = getGalleryLockPoint();

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for (int i = getChildCount() - 1; i >= 0; i--) {
			final View child = getChildAt(i);

			/*
			 * Note: Since we are locking on the left edge of the scroller, we
			 * want to put more emphasis on the closest edge calculations
			 * because the left edge being on the left of the screen puts
			 * unnecessary weight on the currently selected item.
			 */
			if (child.getLeft() == lockPoint && child.getRight() >= lockPoint) {
				// This child is in the lock point
				newSelectedChildIndex = i;
				break;
			}

			final int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - lockPoint),
					Math.abs(child.getRight() - lockPoint));
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		final int newPos = mFirstPosition + newSelectedChildIndex;

		if (newPos != mSelectedPosition) {
			setSelectedPositionInt(newPos);
			setNextSelectedPositionInt(newPos);
			checkSelectionChanged();
		}
	}

	// CHECKSTYLE:ON

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its
	 * layout parameters.
	 * 
	 * @param child The view to position
	 * @param offset Offset from the selected position
	 * @param x X-coordinate indicating where this view should be placed. This
	 *            will either be the left or right edge of the view, depending
	 *            on the fromLeft parameter
	 * @param fromLeft Are we positioning views based on the left edge? (i.e.,
	 *            building from left to right)?
	 */
	private void setUpChild(final View child, final int offset, final int x, final boolean fromLeft) {

		/*
		 * Respect layout params that are already in the view. Otherwise make
		 * some up...
		 */
		Gallery.LayoutParams lp = child.getLayoutParams();
		if (lp == null) {
			lp = generateDefaultLayoutParams();
		}

		// CHECKSTYLE:OFF unmodified
		addViewInLayout(child, fromLeft != mIsRtl ? -1 : 0, lp);
		// CHECKSTYLE:ON

		child.setSelected(offset == 0);

		// Get measure specs
		final int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec, mSpinnerPadding.top
				+ mSpinnerPadding.bottom, lp.height);
		final int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, mSpinnerPadding.left
				+ mSpinnerPadding.right, lp.width);

		// Measure child
		child.measure(childWidthSpec, childHeightSpec);

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		final int childTop = calculateTop(child, true);
		final int childBottom = childTop + child.getMeasuredHeight();

		final int width = child.getMeasuredWidth();
		if (fromLeft) {
			childLeft = x;
			childRight = childLeft + width;
		} else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout(childLeft, childTop, childRight, childBottom);
	}

	// CHECKSTYLE:OFF unmodified
	private void updateSelectedItemMetadata() {

		final View oldSelectedChild = mSelectedChild;

		final View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
		if (child == null) return;

		child.setSelected(true);
		child.setFocusable(true);

		if (hasFocus()) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if (oldSelectedChild != null && oldSelectedChild != child) {

			// Make sure its drawable state doesn't contain 'selected'
			oldSelectedChild.setSelected(false);

			/*
			 * Make sure it is not focusable anymore, since otherwise arrow keys
			 * can make this one be focused
			 */
			oldSelectedChild.setFocusable(false);
		}

	}

	/**
	 * Gets the limit of the amount to scroll the view. Note: Modified from the
	 * android source to snap to the left of the view rather than the center.
	 * 
	 * @param motionToLeft if the motion is to the left of the view
	 * @param deltaX the change in x
	 * @return the clamped value to scroll
	 */
	// TODO
	int getLimitedMotionScrollAmount(final boolean motionToLeft, final int deltaX) {
		// CHECKSTYLE:OFF unmodifed code
		final int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
		// CHECKSTYLE:ON
		final View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

		if (extremeChild == null) return deltaX;

		final int extremeChildLeft = getLeftOfView(extremeChild);
		final int galleryLockPoint = getGalleryLockPoint();

		if (motionToLeft) {
			if (!mRightSpacingEnabled && extremeChild.getRight() <= getRight()) return 0;
			// The extreme child is past his boundary point!
			if (extremeChildLeft <= galleryLockPoint) return 0;
		} else {
			// The extreme child is past his boundary point!
			if (extremeChildLeft >= galleryLockPoint) return 0;
		}

		final int centerDifference = galleryLockPoint - extremeChildLeft;

		// CHECKSTYLE:OFF unmodifed return
		return motionToLeft ? Math.max(centerDifference, deltaX) : Math.min(centerDifference, deltaX);
		// CHECKSTYLE:ON
	}

	/**
	 * Creates and positions all views for this Gallery.
	 * <p>
	 * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
	 * care of repositioning, adding, and removing children.
	 * 
	 * Note: modified to offset to the gallery lock point rather than based on
	 * the # of children.
	 * 
	 * @param delta Change in the selected position. +1 means the selection is
	 *            moving to the right, so views are scrolling to the left. -1
	 *            means the selection is moving to the left.
	 * @param animate if true, animate the view
	 */
	@Override
	void layout(final int delta, final boolean animate) {

		mIsRtl = false;

		if (mDataChanged) {
			handleDataChanged();
		}

		// Handle an empty Gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		// Update to the new selected position.
		if (mNextSelectedPosition >= 0) {
			setSelectedPositionInt(mNextSelectedPosition);
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		detachAllViewsFromParent();

		/*
		 * These will be used to give initial positions to views entering the
		 * Gallery as we scroll
		 */
		mRightMost = 0;
		mLeftMost = 0;

		// Make selected view and move it to the lock point

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		final View sel = makeAndAddView(mSelectedPosition, 0, 0, true);

		// Put the selected child at the lockPoint
		sel.offsetLeftAndRight(getGalleryLockPoint());

		fillToGalleryRight();
		fillToGalleryLeft();

		// Flush any cached views that did not get reused above
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt(mSelectedPosition);

		updateSelectedItemMetadata();
	}

	// CHECKSTYLE:ON

	boolean moveNext() {
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChild(mSelectedPosition - mFirstPosition + 1);
			return true;
		} else
			return false;
	}

	boolean movePrevious() {
		if (mItemCount > 0 && mSelectedPosition > 0) {
			/**
			 * used to be scrollToChild(mSelectedPosition - mFirstPosition - 1);
			 * Found that this did not work with trackball/keyboard navigation.
			 */
			setSelection(mSelectedPosition - 1);
			return true;
		} else
			return false;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	void onCancel() {
		onUp();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	void onUp() {

		if (mFlingRunnable.mScroller.isFinished()) {
			scrollIntoSlots();
		}

		dispatchUnpress();
	}

	@Override
	void selectionChanged() {
		if (!mSuppressSelectionChanged) {
			super.selectionChanged();
		}
	}

	@Override
	void setSelectedPositionInt(final int position) {
		super.setSelectedPositionInt(position);

		// Updates any metadata we keep about the selected item.
		updateSelectedItemMetadata();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as
	 * selected).
	 * 
	 * @param deltaX Change in X from the previous event.
	 */
	void trackMotionScroll(final int deltaX) {

		if (getChildCount() == 0) return;

		final boolean toLeft = deltaX < 0;

		final int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
		if (limitedDeltaX != deltaX) {
			// The above call returned a limited amount, so stop any
			// scrolls/flings
			mFlingRunnable.endFling(false);
			onFinishedMovement();
		}

		offsetChildrenLeftAndRight(limitedDeltaX);

		detachOffScreenChildren(toLeft);

		if (toLeft) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		} else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}

		// Clear unused views
		mRecycler.clear();

		setSelectionToChildClosestToLockPoint();

		onScrollChanged(0, 0, 0, 0); // Dummy values, View's implementation does
										// not use these.

		invalidate();
	}

	/**
	 * Modified from getCenterOfView to get the left of a view instead.
	 * 
	 * @param view the view to get the left of
	 * @return The center of the given view.
	 */
	private static int getLeftOfView(final View view) {
		return view.getLeft();
	}

	/**
	 * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}. A
	 * FlingRunnable will keep re-posting itself until the fling is done.
	 */
	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll.
		 */
		private final Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling.
		 */
		private int mLastFlingX;

		/**
		 * Constructor.
		 */
		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		@Override
		public void run() {

			if (mItemCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			final boolean more = scroller.computeScrollOffset();
			final int x = scroller.getCurrX();

			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the top)
			int delta = mLastFlingX - x;

			// Pretend that each frame of a fling scroll is a touch scroll
			if (delta > 0) {
				// Moving towards the left. Use leftmost view as
				// mDownTouchPosition
				mDownTouchPosition = mIsRtl ? mFirstPosition + getChildCount() - 1 : mFirstPosition;

				// Don't fling more than 1 screen
				delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
			} else {
				// Moving towards the right. Use rightmost view as
				// mDownTouchPosition
				mDownTouchPosition = mIsRtl ? mFirstPosition : mFirstPosition + getChildCount() - 1;

				// Don't fling more than 1 screen
				delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
			}

			trackMotionScroll(delta);

			if (more && !mShouldStopFling) {
				mLastFlingX = x;
				post(this);
			} else {
				endFling(true);
			}
		}

		public void startUsingDistance(final int distance) {
			if (distance == 0) return;

			startCommon();

			mLastFlingX = 0;
			mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
			post(this);
		}

		public void startUsingVelocity(final int initialVelocity) {
			if (initialVelocity == 0) return;

			startCommon();

			final int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			post(this);
		}

		public void stop(final boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		private void endFling(final boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			mScroller.forceFinished(true);

			if (scrollIntoSlots) {
				scrollIntoSlots();
			}
		}

		// CHECKSTYLE:OFF unmodified
		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

	}
}
