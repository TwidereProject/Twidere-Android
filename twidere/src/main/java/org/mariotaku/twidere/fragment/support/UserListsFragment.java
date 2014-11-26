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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.LinePageIndicator;

public class UserListsFragment extends BaseSupportFragment implements OnPageChangeListener,
        RefreshScrollTopInterface, SupportFragmentCallback {

    private ExtendedViewPager mViewPager;

    private SupportTabsAdapter mAdapter;
    private LinePageIndicator mPagerIndicator;

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
        mAdapter.addTab(UserListsListFragment.class, args, getString(R.string.lists),
                R.drawable.ic_action_twitter, 0);
        mAdapter.addTab(UserListMembershipsListFragment.class, args,
                getString(R.string.lists_following_user), R.drawable.ic_action_user, 1);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(2);
        mPagerIndicator.setSelectedColor(ThemeUtils.getThemeColor(activity));
        mPagerIndicator.setViewPager(mViewPager);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onDetachFragment(final Fragment fragment) {

    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
    }

    @Override
    public void onPageSelected(final int position) {
    }

    @Override
    public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
        if (isVisibleToUser) {
            mCurrentVisibleFragment = fragment;
        }
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ExtendedViewPager) view.findViewById(R.id.search_pager);
        mPagerIndicator = (LinePageIndicator) view.findViewById(R.id.search_pager_indicator);
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
    public boolean triggerRefresh(final int position) {
        return false;
    }

}
