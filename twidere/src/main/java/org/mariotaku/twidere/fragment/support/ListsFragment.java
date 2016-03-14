/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.LinkHandlerActivity;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.TabPagerIndicator;

public class ListsFragment extends BaseSupportFragment implements RefreshScrollTopInterface,
        SupportFragmentCallback, SystemWindowsInsetsCallback,
        IControlBarActivity.ControlBarOffsetListener, LinkHandlerActivity.HideUiOnScroll, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private TabPagerIndicator mPagerIndicator;
    private View mPagerOverlay;

    private SupportTabsAdapter mPagerAdapter;
    private int mControlBarHeight;
    private int mControlBarOffsetPixels;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        final FragmentActivity activity = getActivity();
        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager(), null, 1);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(this);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);

        mPagerAdapter.addTab(UserListsFragment.class, args, getString(R.string.follows), null, 0, null);
        mPagerAdapter.addTab(UserListMembershipsFragment.class, args, getString(R.string.belongs_to), 0, 1, null);

        ThemeUtils.initPagerIndicatorAsActionBarTab(activity, mPagerIndicator, mPagerOverlay);
        ThemeUtils.setCompatToolbarOverlay(activity, new EmptyDrawable());
        ThemeUtils.setCompatContentViewOverlay(activity, new EmptyDrawable());
        ThemeUtils.setWindowOverlayViewOverlay(activity, new EmptyDrawable());

        if (activity instanceof IThemedActivity) {
            final String backgroundOption = ((IThemedActivity) activity).getCurrentThemeBackgroundOption();
            final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
            final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(activity)) : 0xFF;
            mPagerIndicator.setAlpha(actionBarAlpha / 255f);
        }
        updateTabOffset();
    }

    @Override
    public void onDestroy() {
        mViewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IControlBarActivity) {
            ((IControlBarActivity) context).registerControlBarOffsetListener(this);
        }
    }

    @Override
    public void onDetach() {
        final FragmentActivity activity = getActivity();
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).unregisterControlBarOffsetListener(this);
        }
        super.onDetach();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_pages, container, false);
    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) view.findViewById(R.id.view_pager_tabs);
        mPagerOverlay = view.findViewById(R.id.pager_window_overlay);
    }

    @Override
    public boolean scrollToStart() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (!(fragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) fragment).scrollToStart();
        return true;
    }

    @Override
    public boolean triggerRefresh() {
        final Fragment fragment = getCurrentVisibleFragment();
        if (!(fragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) fragment).triggerRefresh();
        return true;
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        final int currentItem = mViewPager.getCurrentItem();
        if (currentItem < 0 || currentItem >= mPagerAdapter.getCount()) return null;
        return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentItem);
    }

    @Override
    public boolean triggerRefresh(final int position) {
        return false;
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view != null) {
            final int top = Utils.getInsetsTopWithoutActionBarHeight(getActivity(), insets.top);
            view.setPadding(insets.left, top, insets.right, insets.bottom);
        }
        updateTabOffset();
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mPagerIndicator == null) return false;
        final FragmentActivity activity = getActivity();
        if (activity instanceof LinkHandlerActivity) {
            ((LinkHandlerActivity) activity).getSystemWindowsInsets(insets);
            insets.top = mPagerIndicator.getHeight() + getControlBarHeight();
            return true;
        }
        return false;
    }

    @Override
    public void onControlBarOffsetChanged(IControlBarActivity activity, float offset) {
        mControlBarHeight = activity.getControlBarHeight();
        mControlBarOffsetPixels = Math.round(mControlBarHeight * (1 - offset));
        updateTabOffset();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        final FragmentActivity activity = getActivity();
        if (activity instanceof LinkHandlerActivity) {
            ((LinkHandlerActivity) activity).setControlBarVisibleAnimate(true);
        }
    }

    private int getControlBarHeight() {
        return ThemeUtils.getControlBarHeight(getActivity(), mControlBarHeight);
    }

    private void updateTabOffset() {
        ThemeUtils.updateControlBarUi(getActivity(), getControlBarHeight(), mControlBarOffsetPixels,
                mPagerIndicator, mPagerOverlay);
    }
}
