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

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.TabPagerIndicator;

public class UserListsFragment extends BaseSupportFragment implements RefreshScrollTopInterface,
        SupportFragmentCallback, SystemWindowsInsetsCallback {

    private ViewPager mViewPager;

    private SupportTabsAdapter mAdapter;
    private TabPagerIndicator mPagerIndicator;

    private Fragment mCurrentVisibleFragment;

    @Override
    public Fragment getCurrentVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    public void hideIndicator() {
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        final FragmentActivity activity = getActivity();
        mAdapter = new SupportTabsAdapter(activity, getChildFragmentManager(), null, 1);
        mAdapter.addTab(UserListsListFragment.class, args, getString(R.string.follows), null, 0, null);
        mAdapter.addTab(UserListMembershipsListFragment.class, args, getString(R.string.belongs_to), 0, 1, null);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);
        ThemeUtils.initPagerIndicatorAsActionBarTab(activity, mPagerIndicator);
        ThemeUtils.setCompatToolbarOverlay(activity, new EmptyDrawable());
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_pages, container, false);
    }

    @Override
    public void onDetachFragment(final Fragment fragment) {

    }

    @Override
    public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
        if (isVisibleToUser) {
            mCurrentVisibleFragment = fragment;
        }
    }

    @Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) view.findViewById(R.id.view_pager_tabs);
    }

    @Override
    public boolean scrollToStart() {
        if (!(mCurrentVisibleFragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) mCurrentVisibleFragment).scrollToStart();
        return true;
    }

    public void showIndicator() {
    }

    @Override
    public boolean triggerRefresh() {
        if (!(mCurrentVisibleFragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) mCurrentVisibleFragment).triggerRefresh();
        return true;
    }


    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View view = getView();
        if (view == null) {
            return;
        }
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public boolean triggerRefresh(final int position) {
        return false;
    }

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return false;
    }
}
