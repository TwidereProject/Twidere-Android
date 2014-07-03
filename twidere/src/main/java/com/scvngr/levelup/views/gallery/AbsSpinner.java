package com.scvngr.levelup.views.gallery;

/*
 * Modified from the Android Source code. The example for how to do so was viewed here:
 * http://www.inter-fuser.com/2010/01/android-coverflow-widget.html
 *
 * Copyright (C) 2006 The Android Open Source Project
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
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.SpinnerAdapter;

/**
 * An abstract base class for spinner widgets. SDK users will probably not need
 * to use this class.
 * 
 * CHECKSTYLE:OFF Android Attr
 * 
 * @attr ref android.R.styleable#AbsSpinner_entries CHECKSTYLE:ON
 */
public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {

	SpinnerAdapter mAdapter;

	int mHeightMeasureSpec;
	int mWidthMeasureSpec;
	boolean mBlockLayoutRequests;
	int mSelectionLeftPadding = 0;
	int mSelectionTopPadding = 0;
	int mSelectionRightPadding = 0;
	int mSelectionBottomPadding = 0;
	Rect mSpinnerPadding = new Rect();
	View mSelectedView = null;
	Interpolator mInterpolator;

	RecycleBin mRecycler = new RecycleBin();
	private DataSetObserver mDataSetObserver;

	/*
	 * Temporary frame to hold a child View's frame rectangle.
	 */
	private Rect mTouchFrame;

	/**
	 * Constructor.
	 * 
	 * @param context the context to inflate into
	 */
	public AbsSpinner(final Context context) {
		super(context);
		initAbsSpinner();
	}

	/**
	 * Constructor for xml inflation.
	 * 
	 * @param context the context to inflate into
	 * @param attrs the xml attrs
	 */
	public AbsSpinner(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Constructor for xml inflation.
	 * 
	 * @param context the context to inflate into
	 * @param attrs the xml attrs
	 * @param defStyle the default style resource
	 */
	public AbsSpinner(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		initAbsSpinner();
	}

	@Override
	public final SpinnerAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public final int getCount() {
		return mItemCount;
	}

	@Override
	public final View getSelectedView() {
		if (mItemCount > 0 && mSelectedPosition >= 0)
			return getChildAt(mSelectedPosition - mFirstPosition);
		else
			return null;
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		final SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.selectedId >= 0) {
			mDataChanged = true;
			mNeedSync = true;
			mSyncRowId = ss.selectedId;
			mSyncPosition = ss.position;
			mSyncMode = SYNC_SELECTED_POSITION;
			requestLayout();
		}
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState ss = new SavedState(superState);
		ss.selectedId = getSelectedItemId();
		if (ss.selectedId >= 0) {
			ss.position = getSelectedItemPosition();
		} else {
			ss.position = INVALID_POSITION;
		}
		return ss;
	}

	/**
	 * Maps a point to a position in the list.
	 * 
	 * @param x X in local coordinate
	 * @param y Y in local coordinate
	 * @return The position of the item which contains the specified point, or
	 *         {@link #INVALID_POSITION} if the point does not intersect an
	 *         item.
	 */
	public final int pointToPosition(final int x, final int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}

		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) return mFirstPosition + i;
			}
		}
		return INVALID_POSITION;
	}

	/**
	 * Override to prevent spamming ourselves with layout requests as we place
	 * views.
	 * 
	 * @see android.view.View#requestLayout()
	 */
	@Override
	public final void requestLayout() {
		if (!mBlockLayoutRequests) {
			super.requestLayout();
		}
	}

	/**
	 * The Adapter is used to provide the data which backs this Spinner. It also
	 * provides methods to transform spinner items based on their position
	 * relative to the selected item.
	 * 
	 * @param adapter The SpinnerAdapter to use for this Spinner
	 */
	// CHECKSTYLE:OFF unmodified
	@Override
	public void setAdapter(final SpinnerAdapter adapter) {
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
			resetList();
		}

		mAdapter = adapter;

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			checkFocus();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			final int position = mItemCount > 0 ? 0 : INVALID_POSITION;

			setSelectedPositionInt(position);
			setNextSelectedPositionInt(position);

			if (mItemCount == 0) {
				// Nothing selected
				checkSelectionChanged();
			}

		} else {
			checkFocus();
			resetList();
			// Nothing selected
			checkSelectionChanged();
		}

		requestLayout();
	}

	// CHECKSTYLE:ON

	@Override
	public final void setSelection(final int position) {
		setNextSelectedPositionInt(position);
		requestLayout();
		invalidate();
	}

	/**
	 * Jump directly to a specific item in the adapter data.
	 * 
	 * @param position the position to select
	 * @param animate if true, animate the selection
	 */
	public final void setSelection(final int position, final boolean animate) {
		// Animate only if requested position is already on screen somewhere
		final boolean shouldAnimate = animate && mFirstPosition <= position
				&& position <= mFirstPosition + getChildCount() - 1;
		setSelectionInt(position, shouldAnimate);
	}

	// CHECKSTYLE:OFF overridden in gallery
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		// CHECKSTYLE:ON
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	/**
	 * @see android.view.View#measure(int, int)
	 * 
	 *      Figure out the dimensions of this Spinner. The width comes from the
	 *      widthMeasureSpec as Spinnners can't have their width set to
	 *      UNSPECIFIED. The height is based on the height of the selected item
	 *      plus padding.
	 */
	// CHECKSTYLE:OFF unmodified
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize;
		int heightSize;

		mSpinnerPadding.left = getPaddingLeft() > mSelectionLeftPadding ? getPaddingLeft() : mSelectionLeftPadding;
		mSpinnerPadding.top = getPaddingTop() > mSelectionTopPadding ? getPaddingTop() : mSelectionTopPadding;
		mSpinnerPadding.right = getPaddingRight() > mSelectionRightPadding ? getPaddingRight() : mSelectionRightPadding;
		mSpinnerPadding.bottom = getPaddingBottom() > mSelectionBottomPadding ? getPaddingBottom()
				: mSelectionBottomPadding;

		if (mDataChanged) {
			handleDataChanged();
		}

		int preferredHeight = 0;
		int preferredWidth = 0;
		boolean needsMeasuring = true;

		final int selectedPosition = getSelectedItemPosition();
		if (selectedPosition >= 0 && mAdapter != null) {
			// Try looking in the recycler. (Maybe we were measured once
			// already)
			View view = mRecycler.get(selectedPosition);
			if (view == null) {
				// Make a new one
				view = mAdapter.getView(selectedPosition, null, this);
			}

			if (view != null) {
				// Put in recycler for re-measuring and/or layout
				mRecycler.put(selectedPosition, view);
			}

			if (view != null) {
				if (view.getLayoutParams() == null) {
					mBlockLayoutRequests = true;
					view.setLayoutParams(generateDefaultLayoutParams());
					mBlockLayoutRequests = false;
				}
				measureChild(view, widthMeasureSpec, heightMeasureSpec);

				preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
				preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;

				needsMeasuring = false;
			}
		}

		if (needsMeasuring) {
			// No views -- just use padding
			preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
			if (widthMode == MeasureSpec.UNSPECIFIED) {
				preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
			}
		}

		preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
		preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());

		heightSize = resolveSize(preferredHeight, heightMeasureSpec);
		widthSize = resolveSize(preferredWidth, widthMeasureSpec);

		setMeasuredDimension(widthSize, heightSize);
		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	// CHECKSTYLE:ON

	/**
	 * Common code for different constructor flavors.
	 */
	private void initAbsSpinner() {
		setFocusable(true);
		setWillNotDraw(false);
	}

	/**
	 * Gets the height of the child view passed.
	 * 
	 * @param child the child to get the height of
	 * @return the child's measured height
	 */
	final int getChildHeight(final View child) {
		return child.getMeasuredHeight();
	}

	/**
	 * Gets the width of the child view passed.
	 * 
	 * @param child the child to get the width of
	 * @return the child's measure width
	 */
	final int getChildWidth(final View child) {
		return child.getMeasuredWidth();
	}

	@Override
	final void handleDataChanged() {
		/*
		 * FIXME -- this is called from both measure and layout. This is
		 * harmless right now, but we don't want to do redundant work if this
		 * gets more complicated
		 */
		super.handleDataChanged();
	}

	// CHECKSTYLE:OFF unmodified
	abstract void layout(int delta, boolean animate);

	// CHECKSTYLE:ON

	/**
	 * Recycle all child views.
	 */
	final void recycleAllViews() {
		final int childCount = getChildCount();
		final AbsSpinner.RecycleBin recycleBin = mRecycler;

		// All views go in recycler
		for (int i = 0; i < childCount; i++) {
			final View v = getChildAt(i);
			final int index = mFirstPosition + i;
			recycleBin.put(index, v);
		}
	}

	/**
	 * Clear out all children from the list.
	 */
	final void resetList() {
		mDataChanged = false;
		mNeedSync = false;

		removeAllViewsInLayout();
		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		setSelectedPositionInt(INVALID_POSITION);
		setNextSelectedPositionInt(INVALID_POSITION);
		invalidate();
	}

	/**
	 * Makes the item at the supplied position selected.
	 * 
	 * @param position Position to select
	 * @param animate Should the transition be animated
	 * 
	 */
	final void setSelectionInt(final int position, final boolean animate) {
		if (position != mOldSelectedPosition) {
			mBlockLayoutRequests = true;
			final int delta = position - mSelectedPosition;
			setNextSelectedPositionInt(position);
			layout(delta, animate);
			mBlockLayoutRequests = false;
		}
	}

	class RecycleBin {
		private final SparseArray<View> mScrapHeap = new SparseArray<View>();

		public void put(final int position, final View v) {
			mScrapHeap.put(position, v);
		}

		void clear() {
			final SparseArray<View> scrapHeap = mScrapHeap;
			final int count = scrapHeap.size();
			for (int i = 0; i < count; i++) {
				final View view = scrapHeap.valueAt(i);
				if (view != null) {
					removeDetachedView(view, true);
				}
			}
			scrapHeap.clear();
		}

		View get(final int position) {
			// System.out.print("Looking for " + position);
			final View result = mScrapHeap.get(position);
			if (result != null) {
				// System.out.println(" HIT");
				mScrapHeap.delete(position);
			} else {
				// System.out.println(" MISS");
			}
			return result;
		}

		View peek(final int position) {
			// System.out.print("Looking for " + position);
			return mScrapHeap.get(position);
		}
	}

	// CHECKSTYLE:ON

	// CHECKSTYLE:OFF unmodified
	static class SavedState extends BaseSavedState {
		long selectedId;
		int position;

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

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(final Parcel in) {
			super(in);
			selectedId = in.readLong();
			position = in.readInt();
		}

		/**
		 * Constructor called from {@link AbsSpinner#onSaveInstanceState()}
		 */
		SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public String toString() {
			return "AbsSpinner.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId="
					+ selectedId + " position=" + position + "}";
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);
			out.writeLong(selectedId);
			out.writeInt(position);
		}
	}
}
