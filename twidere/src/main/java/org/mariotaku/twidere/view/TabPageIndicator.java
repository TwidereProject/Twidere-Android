/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 * Copyright (C) 2012 Mariotaku Lee
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
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.iface.PagerIndicator;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class TabPageIndicator extends HorizontalScrollView implements PagerIndicator {

	private Runnable mTabSelector;
	private int mCurrentItem;
	private TabProvider mTabProvider;
	private TabListener mTabListener;

	private final LinearLayout mTabLayout;
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mPageListener;

	private final LayoutInflater mInflater;

	private final boolean mShouldApplyColorFilterToTabIcons;
	private final int mTabIconColor;

	int mMaxTabWidth;

	private int mSelectedTabIndex;
	private final int mTabHighlightColor;
	private boolean mSwitchingEnabled = true;

	private boolean mDisplayLabel, mDisplayIcon = true;
	private final OnClickListener mTabClickListener = new OnClickListener() {

		@Override
		public void onClick(final View view) {
			if (!mSwitchingEnabled) return;
			final TabView tabView = (TabView) view;
			if (mCurrentItem == tabView.getIndex() && mTabListener != null) {
				mTabListener.onPageReselected(mCurrentItem);
			}
			mCurrentItem = tabView.getIndex();
			setCurrentItem(mCurrentItem);
		}
	};

	private final OnLongClickListener mTabLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(final View view) {
			if (!mSwitchingEnabled) return false;
			final TabView tabView = (TabView) view;
			return mTabListener != null && mTabListener.onTabLongClick(tabView.getIndex());
		}
	};

	public TabPageIndicator(final Context context) {
		this(context, null);
	}

	public TabPageIndicator(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setHorizontalScrollBarEnabled(false);
		mShouldApplyColorFilterToTabIcons = ThemeUtils.shouldApplyColorFilterToActionIcons(context);
		mTabHighlightColor = isInEditMode() ? 0 : ThemeUtils.getUserThemeColor(context);
		mTabIconColor = isInEditMode() ? 0 : ThemeUtils.getActionIconColor(context);
		mInflater = LayoutInflater.from(context);
		mTabLayout = new LinearLayout(context);
		mTabLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		addView(mTabLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

	public int getTabCount() {
		return mTabLayout.getChildCount();
	}

	public View getTabItem(final int position) {
		return mTabLayout.getChildAt(position);
	}

	public ViewGroup getTabLayout() {
		return mTabLayout;
	}

	@Override
	public void notifyDataSetChanged() {
		if (mTabLayout == null || mViewPager == null) return;
		mTabLayout.removeAllViews();
		final PagerAdapter adapter = mViewPager.getAdapter();
		mTabProvider = adapter instanceof TabProvider ? (TabProvider) adapter : null;
		mTabListener = adapter instanceof TabListener ? (TabListener) adapter : null;
		if (mTabProvider == null) return;
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final CharSequence title = mTabProvider.getPageTitle(i);
			final Drawable icon = mTabProvider.getPageIcon(i);
			if (title != null && icon != null) {
				addTab(title, icon, i);
			} else if (title == null && icon != null) {
				addTab(null, icon, i);
			} else if (title != null && icon == null) {
				addTab(title, null, i);
			}
		}
		if (mSelectedTabIndex > count) {
			mSelectedTabIndex = count - 1;
		}
		setCurrentItem(mSelectedTabIndex);
		requestLayout();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mTabSelector != null) {
			// Re-post the selector we saved
			post(mTabSelector);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
	}

	@Override
	public boolean onGenericMotionEvent(final MotionEvent event) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) return false;
		if (mTabProvider == null) return false;
		if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_SCROLL: {
					final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
					if (vscroll < 0) {
						if (mCurrentItem + 1 < mTabProvider.getCount()) {
							setCurrentItem(mCurrentItem + 1);
						}
					} else if (vscroll > 0) {
						if (mCurrentItem - 1 >= 0) {
							setCurrentItem(mCurrentItem - 1);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		setFillViewport(lockedExpanded);

		final int childCount = mTabLayout.getChildCount();
		if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
			if (childCount > 2) {
				mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
			} else {
				mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
			}
		} else {
			mMaxTabWidth = -1;
		}

		final int oldWidth = getMeasuredWidth();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int newWidth = getMeasuredWidth();

		if (lockedExpanded && oldWidth != newWidth) {
			// Recenter the tab display if we're at a new (scrollable) size.
			setCurrentItem(mSelectedTabIndex);
		}
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		if (mPageListener != null) {
			mPageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageScrollStateChanged(final int state) {
		if (mPageListener != null) {
			mPageListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageSelected(final int position) {
		setCurrentItem(position);
		if (mTabListener != null) {
			mTabListener.onPageSelected(position);
		}
		if (mPageListener != null) {
			mPageListener.onPageSelected(position);

		}
	}

	@Override
	public void setCurrentItem(final int item) {
		if (mViewPager == null) return;
		// throw new IllegalStateException("ViewPager has not been bound.");
		mCurrentItem = item;
		mViewPager.setCurrentItem(item);
		mSelectedTabIndex = item;
		final int tabCount = mTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			final View child = mTabLayout.getChildAt(i);
			final boolean isSelected = i == item;
			child.setSelected(isSelected);
			if (isSelected) {
				animateToTab(item);
			}
		}
	}

	public void setDisplayIcon(final boolean display) {
		mDisplayIcon = display;
		notifyDataSetChanged();
	}

	public void setDisplayLabel(final boolean display) {
		mDisplayLabel = display;
		notifyDataSetChanged();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		setAlpha(enabled ? 1 : 0.5f);
	}

	@Override
	public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
		mPageListener = listener;
	}

	public void setSwitchingEnabled(final boolean enabled) {
		if (!(mViewPager instanceof ViewPager))
			throw new IllegalStateException(
					"This method should only called when your ViewPager instance is ExtendedViewPager");
		mSwitchingEnabled = enabled;
	}

	@Override
	public void setViewPager(final ViewPager pager) {
		final PagerAdapter adapter = pager.getAdapter();
		if (adapter == null) return;
		// throw new IllegalStateException("ViewPager has not been bound.");
		if (!(adapter instanceof TabProvider))
			throw new IllegalStateException(
					"ViewPager adapter must implement TitleProvider to be used with TitlePageIndicator.");
		mViewPager = pager;
		pager.setOnPageChangeListener(this);
		notifyDataSetChanged();
	}

	@Override
	public void setViewPager(final ViewPager pager, final int initialPosition) {
		setViewPager(pager);
		setCurrentItem(initialPosition);
	}

	private void addTab(final CharSequence label, final Drawable icon, final int index) {
		// Workaround for not being able to pass a defStyle on pre-3.0
		final TabView tabView = (TabView) mInflater.inflate(R.layout.tab_item_vpi, null);
		tabView.init(this, mDisplayLabel ? label : null, mDisplayIcon ? icon : null, index);
		tabView.setFocusable(true);
		tabView.setOnClickListener(mTabClickListener);
		tabView.setOnLongClickListener(mTabLongClickListener);
		tabView.setContentDescription(label);
		mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
		if (ThemeUtils.shouldApplyColorFilter(getContext())) {
			ThemeUtils.applyBackground(tabView, mTabHighlightColor);
		}
	}

	private void animateToTab(final int position) {
		final View tabView = mTabLayout.getChildAt(position);
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
		mTabSelector = new Runnable() {

			@Override
			public void run() {
				final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
				smoothScrollTo(scrollPos, 0);
				mTabSelector = null;
			}
		};
		post(mTabSelector);
	}

	private int getTabIconColor() {
		return mTabIconColor;
	}

	private boolean shouldApplyColorFilterToTabIcons() {
		return mShouldApplyColorFilterToTabIcons;
	}

	public interface TabListener {

		public void onPageReselected(int position);

		public void onPageSelected(int position);

		public boolean onTabLongClick(int position);
	}

	/**
	 * A TitleProvider provides the title to display according to a view.
	 */
	public interface TabProvider {

		public int getCount();

		/**
		 * Returns the icon of the view at position
		 * 
		 * @param position
		 * @return
		 */
		public Drawable getPageIcon(int position);

		/**
		 * Returns the title of the view at position
		 * 
		 * @param position
		 * @return
		 */
		public CharSequence getPageTitle(int position);

		public float getPageWidth(int position);
	}

	public static class TabView extends FrameLayout {

		private TabPageIndicator mParent;
		private int mIndex;

		public TabView(final Context context, final AttributeSet attrs) {
			super(context, attrs);
		}

		public int getIndex() {
			return mIndex;
		}

		public void init(final TabPageIndicator parent, final CharSequence label, final Drawable icon, final int index) {
			if (isInEditMode()) return;
			mParent = parent;
			mIndex = index;

			final ImageView imageView = (ImageView) findViewById(R.id.tab_item_icon);
			imageView.setVisibility(icon != null ? View.VISIBLE : View.GONE);
			imageView.setImageDrawable(icon);
			if (parent.shouldApplyColorFilterToTabIcons()) {
				imageView.setColorFilter(parent.getTabIconColor(), Mode.SRC_ATOP);
			}
			final TextView textView = (TextView) findViewById(R.id.tab_item_title);
			textView.setVisibility(TextUtils.isEmpty(label) ? View.GONE : View.VISIBLE);
			textView.setText(label);
		}

		public void init(final TabPageIndicator parent, final CharSequence text, final int index) {
			init(parent, text, null, index);
		}

		public void init(final TabPageIndicator parent, final Drawable icon, final int index) {
			init(parent, null, icon, index);
		}

		@Override
		public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			// Re-measure if we went beyond our maximum size.
			if (!isInEditMode() && mParent.mMaxTabWidth > 0 && getMeasuredWidth() > mParent.mMaxTabWidth) {
				super.onMeasure(MeasureSpec.makeMeasureSpec(mParent.mMaxTabWidth, MeasureSpec.EXACTLY),
						heightMeasureSpec);
			}
		}
	}
}
