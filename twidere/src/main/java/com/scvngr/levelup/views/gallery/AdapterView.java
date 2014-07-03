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

package com.scvngr.levelup.views.gallery;

// CHECKSTYLE:OFF
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

// CHECKSTYLE:ON

/**
 * An AdapterView is a view whose children are determined by an {@link Adapter}.
 * 
 * <p>
 * See {@link ListView}, {@link GridView}, {@link Spinner} and {@link Gallery}
 * for commonly used subclasses of AdapterView.
 */
// CHECKSTYLE:OFF android comment
public abstract class AdapterView<T extends Adapter> extends ViewGroup {
	// CHECKSTYLE:ON
	/**
	 * The item view type returned by {@link Adapter#getItemViewType(int)} when
	 * the adapter does not want the item's view recycled.
	 */
	public static final int ITEM_VIEW_TYPE_IGNORE = -1;

	/**
	 * The item view type returned by {@link Adapter#getItemViewType(int)} when
	 * the item is a header or footer.
	 */
	public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;

	/**
	 * The position of the first child displayed.
	 */
	@ViewDebug.ExportedProperty
	int mFirstPosition = 0;

	/**
	 * The offset in pixels from the top of the AdapterView to the top of the
	 * view to select during the next layout.
	 */
	int mSpecificTop;

	/**
	 * Position from which to start looking for mSyncRowId.
	 */
	int mSyncPosition;

	/**
	 * Row id to look for when data has changed.
	 */
	long mSyncRowId = INVALID_ROW_ID;

	/**
	 * Height of the view when mSyncPosition and mSyncRowId where set.
	 */
	long mSyncHeight;

	/**
	 * True if we need to sync to mSyncRowId.
	 */
	boolean mNeedSync = false;

	/**
	 * Indicates whether to sync based on the selection or position. Possible
	 * values are {@link #SYNC_SELECTED_POSITION} or
	 * {@link #SYNC_FIRST_POSITION}.
	 */
	int mSyncMode;

	/**
	 * Our height after the last layout.
	 */
	private int mLayoutHeight;

	/**
	 * Sync based on the selected child.
	 */
	static final int SYNC_SELECTED_POSITION = 0;

	/**
	 * Sync based on the first child displayed.
	 */
	static final int SYNC_FIRST_POSITION = 1;

	/**
	 * Maximum amount of time to spend in {@link #findSyncPosition()}.
	 */
	static final int SYNC_MAX_DURATION_MILLIS = 100;

	/**
	 * Indicates that this view is currently being laid out.
	 */
	boolean mInLayout = false;

	/**
	 * The listener that receives notifications when an item is selected.
	 */
	OnItemSelectedListener mOnItemSelectedListener;

	/**
	 * The listener that receives notifications when an item is clicked.
	 */
	OnItemClickListener mOnItemClickListener;

	/**
	 * The listener that receives notifications when an item is long clicked.
	 */
	OnItemLongClickListener mOnItemLongClickListener;

	/**
	 * True if the data has changed since the last layout.
	 */
	boolean mDataChanged;

	/**
	 * The position within the adapter's data set of the item to select during
	 * the next layout.
	 */
	@ViewDebug.ExportedProperty
	int mNextSelectedPosition = INVALID_POSITION;

	/**
	 * The item id of the item to select during the next layout.
	 */
	long mNextSelectedRowId = INVALID_ROW_ID;

	/**
	 * The position within the adapter's data set of the currently selected
	 * item.
	 */
	@ViewDebug.ExportedProperty
	int mSelectedPosition = INVALID_POSITION;

	/**
	 * The item id of the currently selected item.
	 */
	long mSelectedRowId = INVALID_ROW_ID;

	/**
	 * View to show if there are no items to show.
	 */
	View mEmptyView;

	/**
	 * The number of items in the current adapter.
	 */
	@ViewDebug.ExportedProperty
	int mItemCount;

	/**
	 * The number of items in the adapter before a data changed event occured.
	 */
	int mOldItemCount;

	/**
	 * Represents an invalid position. All valid positions are in the range 0 to
	 * 1 less than the number of items in the current adapter.
	 */
	public static final int INVALID_POSITION = -1;

	/**
	 * Represents an empty or invalid row id.
	 */
	public static final long INVALID_ROW_ID = Long.MIN_VALUE;

	/**
	 * The last selected position we used when notifying.
	 */
	int mOldSelectedPosition = INVALID_POSITION;

	/**
	 * The id of the last selected position we used when notifying.
	 */
	long mOldSelectedRowId = INVALID_ROW_ID;

	/**
	 * Indicates what focusable state is requested when calling setFocusable().
	 * In addition to this, this view has other criteria for actually
	 * determining the focusable state (such as whether its empty or the text
	 * filter is shown).
	 * 
	 * @see #setFocusable(boolean)
	 * @see #checkFocus()
	 */
	private boolean mDesiredFocusableState;
	private boolean mDesiredFocusableInTouchModeState;

	private SelectionNotifier mSelectionNotifier;
	/**
	 * When set to true, calls to requestLayout() will not propagate up the
	 * parent hierarchy. This is used to layout the children during a layout
	 * pass.
	 */
	boolean mBlockLayoutRequests = false;

	/**
	 * Constructor.
	 * 
	 * @param context the context to inflate with
	 */
	public AdapterView(final Context context) {
		super(context);
	}

	/**
	 * Constructor for xml inflation.
	 * 
	 * @param context the context to inflate with
	 * @param attrs the xml attrs
	 */
	public AdapterView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor for xml inflation.
	 * 
	 * @param context the contex to inflate with
	 * @param attrs the xml attrs
	 * @param defStyle the default style resource
	 */
	public AdapterView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void addView(final View child) {
		throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param index Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void addView(final View child, final int index) {
		throw new UnsupportedOperationException("addView(View, int) is " + "not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param index Ignored.
	 * @param params Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void addView(final View child, final int index, final LayoutParams params) {
		throw new UnsupportedOperationException("addView(View, int, LayoutParams) " + "is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param params Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void addView(final View child, final LayoutParams params) {
		throw new UnsupportedOperationException("addView(View, LayoutParams) " + "is not supported in AdapterView");
	}

	/**
	 * Returns the adapter currently associated with this widget.
	 * 
	 * @return The adapter used to provide this view's content.
	 */
	public abstract T getAdapter();

	/**
	 * @return The number of items owned by the Adapter associated with this
	 *         AdapterView. (This is the number of data items, which may be
	 *         larger than the number of visible view.)
	 */
	// CHECKSTYLE:OFF overridden in subclass
	// @ViewDebug.CapturedViewProperty
	public int getCount() {
		return mItemCount;
	}

	// CHECKSTYLE:ON

	/**
	 * When the current adapter is empty, the AdapterView can display a special
	 * view call the empty view. The empty view is used to provide feedback to
	 * the user that no data is available in this AdapterView.
	 * 
	 * @return The view to show if the adapter is empty.
	 */
	public final View getEmptyView() {
		return mEmptyView;
	}

	/**
	 * Returns the position within the adapter's data set for the first item
	 * displayed on screen.
	 * 
	 * @return The position within the adapter's data set
	 */
	public final int getFirstVisiblePosition() {
		return mFirstPosition;
	}

	/**
	 * Gets the data associated with the specified position in the list.
	 * 
	 * @param position Which data to get
	 * @return The data associated with the specified position in the list
	 */
	// CHECKSTYLE:OFF unmodified
	public Object getItemAtPosition(final int position) {
		final T adapter = getAdapter();
		return adapter == null || position < 0 ? null : adapter.getItem(position);
	}

	public long getItemIdAtPosition(final int position) {
		final T adapter = getAdapter();
		return adapter == null || position < 0 ? INVALID_ROW_ID : adapter.getItemId(position);
	}

	// CHECKSTYLE:ON

	/**
	 * Returns the position within the adapter's data set for the last item
	 * displayed on screen.
	 * 
	 * @return The position within the adapter's data set
	 */
	public final int getLastVisiblePosition() {
		return mFirstPosition + getChildCount() - 1;
	}

	/**
	 * @return The callback to be invoked with an item in this AdapterView has
	 *         been clicked, or null id no callback has been set.
	 */
	public final OnItemClickListener getOnItemClickListener() {
		return mOnItemClickListener;
	}

	/**
	 * @return The callback to be invoked with an item in this AdapterView has
	 *         been clicked and held, or null id no callback as been set.
	 */
	public final OnItemLongClickListener getOnItemLongClickListener() {
		return mOnItemLongClickListener;
	}

	/**
	 * Gets the OnItemSelected listener.
	 * 
	 * @return the {@link OnItemSelectedListener}
	 */
	public final OnItemSelectedListener getOnItemSelectedListener() {
		return mOnItemSelectedListener;
	}

	/**
	 * Get the position within the adapter's data set for the view, where view
	 * is a an adapter item or a descendant of an adapter item.
	 * 
	 * @param view an adapter item, or a descendant of an adapter item. This
	 *            must be visible in this AdapterView at the time of the call.
	 * @return the position within the adapter's data set of the view, or
	 *         {@link #INVALID_POSITION} if the view does not correspond to a
	 *         list item (or it is not currently visible).
	 */
	public final int getPositionForView(final View view) {
		View listItem = view;
		try {
			View v;
			// CHECKSTYLE:OFF unmodified
			while (!(v = (View) listItem.getParent()).equals(this)) {
				// CHECKSTYLE:ON
				listItem = v;
			}
		} catch (final ClassCastException e) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (getChildAt(i).equals(listItem)) return mFirstPosition + i;
		}

		// Child not found!
		return INVALID_POSITION;
	}

	/**
	 * @return The data corresponding to the currently selected item, or null if
	 *         there is nothing selected.
	 */
	public final Object getSelectedItem() {
		final T adapter = getAdapter();
		final int selection = getSelectedItemPosition();
		if (adapter != null && adapter.getCount() > 0 && selection >= 0)
			return adapter.getItem(selection);
		else
			return null;
	}

	/**
	 * @return The id corresponding to the currently selected item, or
	 *         {@link #INVALID_ROW_ID} if nothing is selected.
	 */
	// @ViewDebug.CapturedViewProperty
	public final long getSelectedItemId() {
		return mNextSelectedRowId;
	}

	/**
	 * Return the position of the currently selected item within the adapter's
	 * data set.
	 * 
	 * @return int Position (starting at 0), or {@link #INVALID_POSITION} if
	 *         there is nothing selected.
	 */
	// @ViewDebug.CapturedViewProperty
	public final int getSelectedItemPosition() {
		return mNextSelectedPosition;
	}

	/**
	 * @return The view corresponding to the currently selected item, or null if
	 *         nothing is selected
	 */
	public abstract View getSelectedView();

	/**
	 * Call the OnItemClickListener, if it is defined.
	 * 
	 * @param view The view within the AdapterView that was clicked.
	 * @param position The position of the view in the adapter.
	 * @param id The row id of the item that was clicked.
	 * @return True if there was an assigned OnItemClickListener that was
	 *         called, false otherwise is returned.
	 */
	public final boolean performItemClick(final View view, final int position, final long id) {
		if (mOnItemClickListener != null) {
			playSoundEffect(SoundEffectConstants.CLICK);
			mOnItemClickListener.onItemClick(this, view, position, id);
			return true;
		}

		return false;
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void removeAllViews() {
		throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void removeView(final View child) {
		throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param index Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public final void removeViewAt(final int index) {
		throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
	}

	/**
	 * Sets the adapter that provides the data and the views to represent the
	 * data in this widget.
	 * 
	 * @param adapter The adapter to use to create this view's content.
	 */
	public abstract void setAdapter(T adapter);

	/**
	 * Sets the view to show if the adapter is empty.
	 * 
	 * @param emptyView the view to show when the adapter is empty
	 */
	public final void setEmptyView(final View emptyView) {
		mEmptyView = emptyView;

		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.isEmpty();
		updateEmptyStatus(empty);
	}

	@Override
	public final void setFocusable(final boolean focusable) {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;

		mDesiredFocusableState = focusable;
		if (!focusable) {
			mDesiredFocusableInTouchModeState = false;
		}

		super.setFocusable(focusable && (!empty || isInFilterMode()));
	}

	@Override
	public final void setFocusableInTouchMode(final boolean focusable) {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;

		mDesiredFocusableInTouchModeState = focusable;
		if (focusable) {
			mDesiredFocusableState = true;
		}

		super.setFocusableInTouchMode(focusable && (!empty || isInFilterMode()));
	}

	@Override
	public final void setOnClickListener(final OnClickListener l) {
		throw new RuntimeException("Don't call setOnClickListener for an AdapterView. "
				+ "You probably want setOnItemClickListener instead");
	}

	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been clicked.
	 * 
	 * @param listener The callback that will be invoked.
	 */
	public final void setOnItemClickListener(final OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been clicked and held.
	 * 
	 * @param listener The callback that will run
	 */
	public final void setOnItemLongClickListener(final OnItemLongClickListener listener) {
		if (!isLongClickable()) {
			setLongClickable(true);
		}
		mOnItemLongClickListener = listener;
	}

	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been selected.
	 * 
	 * @param listener The callback that will run
	 */
	public final void setOnItemSelectedListener(final OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
	}

	/**
	 * Sets the currently selected item.
	 * 
	 * @param position Index (starting at 0) of the data item to be selected.
	 */
	public abstract void setSelection(int position);

	@Override
	protected final boolean canAnimate() {
		return super.canAnimate() && mItemCount > 0;
	}

	/**
	 * Override to prevent thawing of any views created by the adapter.
	 * 
	 * @param container the container of parcelables to use to restore the state
	 */
	@Override
	protected final void dispatchRestoreInstanceState(final SparseArray<Parcelable> container) {
		dispatchThawSelfOnly(container);
	}

	/**
	 * Override to prevent freezing of any views created by the adapter.
	 * 
	 * @param container the container of parcelables to use to save the state.
	 */
	@Override
	protected final void dispatchSaveInstanceState(final SparseArray<Parcelable> container) {
		dispatchFreezeSelfOnly(container);
	}

	// CHECKSTYLE:OFF
	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mLayoutHeight = getHeight();
	}

	// CHECKSTYLE:ON

	/**
	 * Fire the onselected listener for the chosen item.
	 */
	private void fireOnSelected() {
		if (mOnItemSelectedListener == null) return;

		final int selection = this.getSelectedItemPosition();
		if (selection >= 0) {
			final View v = getSelectedView();
			mOnItemSelectedListener.onItemSelected(this, v, selection, getAdapter().getItemId(selection));
		} else {
			mOnItemSelectedListener.onNothingSelected(this);
		}
	}

	/**
	 * Update the status of the list based on the empty parameter. If empty is
	 * true and we have an empty view, display it. In all the other cases, make
	 * sure that the listview is VISIBLE and that the empty view is GONE (if
	 * it's not null).
	 * 
	 */
	// CHECKSTYLE:OFF unmodifed
	@SuppressLint("WrongCall")
	private void updateEmptyStatus(boolean empty) {
		if (isInFilterMode()) {
			empty = false;
		}

		if (empty) {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.VISIBLE);
				setVisibility(View.GONE);
			} else {
				// If the caller just removed our empty view, make sure the list
				// view is visible
				setVisibility(View.VISIBLE);
			}

			// We are now GONE, so pending layouts will not be dispatched.
			// Force one here to make sure that the state of the list matches
			// the state of the adapter.
			if (mDataChanged) {
				this.onLayout(false, getLeft(), getTop(), getRight(), getBottom());
			}
		} else {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.GONE);
			}
			setVisibility(View.VISIBLE);
		}
	}

	// CHECKSTYLE:ON

	// CHECKSTYLE:OFF unmodified
	void checkFocus() {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;
		final boolean focusable = !empty || isInFilterMode();
		// The order in which we set focusable in touch mode/focusable may
		// matter
		// for the client, see View.setFocusableInTouchMode() comments for more
		// details
		super.setFocusableInTouchMode(focusable && mDesiredFocusableInTouchModeState);
		super.setFocusable(focusable && mDesiredFocusableState);
		if (mEmptyView != null) {
			updateEmptyStatus(adapter == null || adapter.isEmpty());
		}
	}

	// CHECKSTYLE:ON

	void checkSelectionChanged() {
		if (mSelectedPosition != mOldSelectedPosition || mSelectedRowId != mOldSelectedRowId) {
			selectionChanged();
			mOldSelectedPosition = mSelectedPosition;
			mOldSelectedRowId = mSelectedRowId;
		}
	}

	// CHECKSTYLE:ON

	/**
	 * Searches the adapter for a position matching mSyncRowId. The search
	 * starts at mSyncPosition and then alternates between moving up and moving
	 * down until 1) we find the right position, or 2) we run out of time, or 3)
	 * we have looked at every position
	 * 
	 * @return Position of the row that matches mSyncRowId, or
	 *         {@link #INVALID_POSITION} if it can't be found
	 */
	final int findSyncPosition() {
		final int count = mItemCount;

		if (count == 0) return INVALID_POSITION;

		final long idToMatch = mSyncRowId;
		int seed = mSyncPosition;

		// If there isn't a selection don't hunt for it
		if (idToMatch == INVALID_ROW_ID) return INVALID_POSITION;

		// Pin seed to reasonable values
		seed = Math.max(0, seed);
		seed = Math.min(count - 1, seed);

		final long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

		long rowId;

		// First position scanned so far
		int first = seed;

		// Last position scanned so far
		int last = seed;

		// True if we should move down on the next iteration
		boolean next = false;

		// True when we have looked at the first item in the data
		boolean hitFirst;

		// True when we have looked at the last item in the data
		boolean hitLast;

		// Get the item ID locally (instead of getItemIdAtPosition), so we need
		// the adapter
		final T adapter = getAdapter();
		if (adapter == null) return INVALID_POSITION;

		while (SystemClock.uptimeMillis() <= endTime) {
			rowId = adapter.getItemId(seed);
			if (rowId == idToMatch) // Found it!
				return seed;

			hitLast = last == count - 1;
			hitFirst = first == 0;

			if (hitLast && hitFirst) {
				// Looked at everything
				break;
			}

			if (hitFirst || next && !hitLast) {
				// Either we hit the top, or we are trying to move down
				last++;
				seed = last;
				// Try going up next time
				next = false;
			} else if (hitLast || !next && !hitFirst) {
				// Either we hit the bottom, or we are trying to move up
				first--;
				seed = first;
				// Try going down next time
				next = true;
			}

		}

		return INVALID_POSITION;
	}

	// CHECKSTYLE:OFF
	void handleDataChanged() {
		final int count = mItemCount;
		boolean found = false;

		if (count > 0) {

			int newPos;

			// Find the row we are supposed to sync to
			if (mNeedSync) {
				// Update this first, since setNextSelectedPositionInt inspects
				// it
				mNeedSync = false;

				// See if we can find a position in the new data with the same
				// id as the old selection
				newPos = findSyncPosition();
				if (newPos >= 0) {
					// Verify that new selection is selectable
					final int selectablePos = lookForSelectablePosition(newPos, true);
					if (selectablePos == newPos) {
						// Same row id is selected
						setNextSelectedPositionInt(newPos);
						found = true;
					}
				}
			}
			if (!found) {
				// Try to use the same position if we can't find matching data
				newPos = getSelectedItemPosition();

				// Pin position to the available range
				if (newPos >= count) {
					newPos = count - 1;
				}
				if (newPos < 0) {
					newPos = 0;
				}

				// Make sure we select something selectable -- first look down
				int selectablePos = lookForSelectablePosition(newPos, true);
				if (selectablePos < 0) {
					// Looking down didn't work -- try looking up
					selectablePos = lookForSelectablePosition(newPos, false);
				}
				if (selectablePos >= 0) {
					setNextSelectedPositionInt(selectablePos);
					checkSelectionChanged();
					found = true;
				}
			}
		}
		if (!found) {
			// Nothing is selected
			mSelectedPosition = INVALID_POSITION;
			mSelectedRowId = INVALID_ROW_ID;
			mNextSelectedPosition = INVALID_POSITION;
			mNextSelectedRowId = INVALID_ROW_ID;
			mNeedSync = false;
			checkSelectionChanged();
		}
	}

	/**
	 * Indicates whether this view is in filter mode. Filter mode can for
	 * instance be enabled by a user when typing on the keyboard.
	 * 
	 * @return True if the view is in filter mode, false otherwise.
	 */
	final boolean isInFilterMode() {
		return false;
	}

	/**
	 * Find a position that can be selected (i.e., is not a separator).
	 * 
	 * @param position The starting position to look at.
	 * @param lookDown Whether to look down for other positions.
	 * @return The next selectable position starting at position and then
	 *         searching either up or down. Returns {@link #INVALID_POSITION} if
	 *         nothing can be found.
	 */
	final int lookForSelectablePosition(final int position, final boolean lookDown) {
		return position;
	}

	/**
	 * Remember enough information to restore the screen state when the data has
	 * changed.
	 * 
	 */
	final void rememberSyncState() {
		if (getChildCount() > 0) {
			mNeedSync = true;
			mSyncHeight = mLayoutHeight;
			if (mSelectedPosition >= 0) {
				// Sync the selection state
				final View v = getChildAt(mSelectedPosition - mFirstPosition);
				mSyncRowId = mNextSelectedRowId;
				mSyncPosition = mNextSelectedPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_SELECTED_POSITION;
			} else {
				// Sync the based on the offset of the first view
				final View v = getChildAt(0);
				final T adapter = getAdapter();
				if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
					mSyncRowId = adapter.getItemId(mFirstPosition);
				} else {
					mSyncRowId = NO_ID;
				}
				mSyncPosition = mFirstPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_FIRST_POSITION;
			}
		}
	}

	void selectionChanged() {
		if (mOnItemSelectedListener != null) {
			if (mInLayout || mBlockLayoutRequests) {
				// If we are in a layout traversal, defer notification
				// by posting. This ensures that the view tree is
				// in a consistent state and is able to accomodate
				// new layout or invalidate requests.
				if (mSelectionNotifier == null) {
					mSelectionNotifier = new SelectionNotifier();
				}
				mSelectionNotifier.post(mSelectionNotifier);
			} else {
				fireOnSelected();
			}
		}
	}

	// CHECKSTYLE:ON

	/**
	 * Utility to keep mNextSelectedPosition and mNextSelectedRowId in sync.
	 * 
	 * @param position Intended value for mSelectedPosition the next time we go
	 *            through layout
	 */
	// CHECKSTYLE:OFF overridden in subclass
	void setNextSelectedPositionInt(final int position) {
		mNextSelectedPosition = position;
		mNextSelectedRowId = getItemIdAtPosition(position);
		// If we are trying to sync to the selection, update that too
		if (mNeedSync && mSyncMode == SYNC_SELECTED_POSITION && position >= 0) {
			mSyncPosition = position;
			mSyncRowId = mNextSelectedRowId;
		}
	}

	// CHECKSTYLE:ON

	/**
	 * Utility to keep mSelectedPosition and mSelectedRowId in sync.
	 * 
	 * @param position Our current position
	 */
	// CHECKSTYLE:OFF overridden in gallery
	void setSelectedPositionInt(final int position) {
		mSelectedPosition = position;
		mSelectedRowId = getItemIdAtPosition(position);
	}

	// CHECKSTYLE:ON

	/**
	 * Extra menu information provided to the
	 * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo) }
	 * callback when a context menu is brought up for this AdapterView.
	 * 
	 */
	public static class AdapterContextMenuInfo implements ContextMenu.ContextMenuInfo {

		/**
		 * The child view for which the context menu is being displayed. This
		 * will be one of the children of this AdapterView.
		 */
		public View targetView;

		/**
		 * The position in the adapter for which the context menu is being
		 * displayed.
		 */
		public int position;

		/**
		 * The row id of the item for which the context menu is being displayed.
		 */
		public long id;

		// CHECKSTYLE:OFF not modified
		public AdapterContextMenuInfo(final View targetView, final int position, final long id) {
			this.targetView = targetView;
			this.position = position;
			this.id = id;
		}
		// CHECKSTYLE:ON
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * AdapterView has been clicked.
	 */
	public interface OnItemClickListener {

		/**
		 * Callback method to be invoked when an item in this AdapterView has
		 * been clicked.
		 * <p>
		 * Implementers can call getItemAtPosition(position) if they need to
		 * access the data associated with the selected item.
		 * 
		 * @param parent The AdapterView where the click happened.
		 * @param view The view within the AdapterView that was clicked (this
		 *            will be a view provided by the adapter)
		 * @param position The position of the view in the adapter.
		 * @param id The row id of the item that was clicked.
		 */
		void onItemClick(AdapterView<?> parent, View view, int position, long id);
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * view has been clicked and held.
	 */
	public interface OnItemLongClickListener {
		/**
		 * Callback method to be invoked when an item in this view has been
		 * clicked and held.
		 * 
		 * Implementers can call getItemAtPosition(position) if they need to
		 * access the data associated with the selected item.
		 * 
		 * @param parent The AbsListView where the click happened
		 * @param view The view within the AbsListView that was clicked
		 * @param position The position of the view in the list
		 * @param id The row id of the item that was clicked
		 * 
		 * @return true if the callback consumed the long click, false otherwise
		 */
		boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * view has been selected.
	 */
	public interface OnItemSelectedListener {
		/**
		 * Callback method to be invoked when an item in this view has been
		 * selected.
		 * 
		 * Impelmenters can call getItemAtPosition(position) if they need to
		 * access the data associated with the selected item.
		 * 
		 * @param parent The AdapterView where the selection happened
		 * @param view The view within the AdapterView that was clicked
		 * @param position The position of the view in the adapter
		 * @param id The row id of the item that is selected
		 */
		void onItemSelected(AdapterView<?> parent, View view, int position, long id);

		/**
		 * Callback method to be invoked when the selection disappears from this
		 * view. The selection can disappear for instance when touch is
		 * activated or when the adapter becomes empty.
		 * 
		 * @param parent The AdapterView that now contains no selected item.
		 */
		void onNothingSelected(AdapterView<?> parent);
	}

	// CHECKSTYLE:OFF unmodified
	private class SelectionNotifier extends Handler implements Runnable {
		@Override
		public void run() {
			if (mDataChanged) {
				// Data has changed between when this SelectionNotifier
				// was posted and now. We need to wait until the AdapterView
				// has been synched to the new data.
				post(this);
			} else {
				fireOnSelected();
			}
		}
	}

	// CHECKSTYLE:OFF unmodified
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		public void clearSavedState() {
			mInstanceState = null;
		}

		@Override
		public void onChanged() {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = getAdapter().getCount();

			// Detect the case where a cursor that was previously invalidated
			// has
			// been repopulated with new data.
			if (AdapterView.this.getAdapter().hasStableIds() && mInstanceState != null && mOldItemCount == 0
					&& mItemCount > 0) {
				onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			} else {
				rememberSyncState();
			}
			checkFocus();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			mDataChanged = true;

			if (AdapterView.this.getAdapter().hasStableIds()) {
				// Remember the current state for the case where our hosting
				// activity is being
				// stopped and later restarted
				mInstanceState = onSaveInstanceState();
			}

			// Data is invalid so we should reset our state
			mOldItemCount = mItemCount;
			mItemCount = 0;
			mSelectedPosition = INVALID_POSITION;
			mSelectedRowId = INVALID_ROW_ID;
			mNextSelectedPosition = INVALID_POSITION;
			mNextSelectedRowId = INVALID_ROW_ID;
			mNeedSync = false;
			checkSelectionChanged();

			checkFocus();
			requestLayout();
		}
	}
	// CHECKSTYLE:ON
}
