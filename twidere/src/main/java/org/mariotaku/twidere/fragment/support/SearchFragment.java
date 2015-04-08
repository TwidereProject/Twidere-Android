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

package org.mariotaku.twidere.fragment.support;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarOffsetListener;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.TabPagerIndicator;

public class SearchFragment extends BaseSupportFragment implements RefreshScrollTopInterface,
        SupportFragmentCallback, SystemWindowsInsetsCallback, ControlBarOffsetListener,
        OnPageChangeListener {

    private ViewPager mViewPager;
    private View mPagerWindowOverlay;

    private SupportTabsAdapter mAdapter;
    private TabPagerIndicator mPagerIndicator;

    private Fragment mCurrentVisibleFragment;
    private int mControlBarOffsetPixels;
    private int mControlBarHeight;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).registerControlBarOffsetListener(this);
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

    private void updateTabOffset() {
        final int controlBarHeight = getControlBarHeight();
        final int translationY = controlBarHeight - mControlBarOffsetPixels;
        final FragmentActivity activity = getActivity();
        if (activity instanceof LinkHandlerActivity) {
            final View view = activity.getWindow().findViewById(android.support.v7.appcompat.R.id.action_bar);
            if (view != null && controlBarHeight != 0) {
                view.setAlpha(translationY / (float) controlBarHeight);
            }
        }
        mPagerIndicator.setTranslationY(translationY);
        mPagerWindowOverlay.setTranslationY(translationY);
    }

    private int getControlBarHeight() {
        final FragmentActivity activity = getActivity();
        final int controlBarHeight;
        if (activity instanceof LinkHandlerActivity) {
            controlBarHeight = ((LinkHandlerActivity) activity).getControlBarHeight();
        } else {
            controlBarHeight = mControlBarHeight;
        }
        if (controlBarHeight == 0) {
            return Utils.getActionBarHeight(activity);
        }
        return controlBarHeight;
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View view = getView();
        if (view != null) {
            final int top = Utils.getInsetsTopWithoutActionBarHeight(getActivity(), insets.top);
            view.setPadding(insets.left, top, insets.right, insets.bottom);
        }
        updateTabOffset();
    }

    @Override
    public Fragment getCurrentVisibleFragment() {
        return mCurrentVisibleFragment;
    }

    public void hideIndicator() {
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        final FragmentActivity activity = getActivity();
        mAdapter = new SupportTabsAdapter(activity, getChildFragmentManager(), null, 1);
        mAdapter.addTab(StatusesSearchFragment.class, args, getString(R.string.statuses), R.drawable.ic_action_twitter, 0, null);
        mAdapter.addTab(SearchUsersFragment.class, args, getString(R.string.users), R.drawable.ic_action_user, 1, null);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);
        mPagerIndicator.setOnPageChangeListener(this);
        ThemeUtils.initPagerIndicatorAsActionBarTab(activity, mPagerIndicator);
        ThemeUtils.setCompatToolbarOverlay(activity, new EmptyDrawable());
        if (savedInstanceState == null && args != null && args.containsKey(EXTRA_QUERY)) {
            final String query = args.getString(EXTRA_QUERY);
            final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                    RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            final ContentResolver cr = getContentResolver();
            final ContentValues values = new ContentValues();
            values.put(SearchHistory.QUERY, query);
            cr.insert(SearchHistory.CONTENT_URI, values);
            if (activity instanceof LinkHandlerActivity) {
                final ActionBar ab = activity.getActionBar();
                if (ab != null) {
                    ab.setSubtitle(query);
                }
            }
        }
        updateTabOffset();
    }

    public String getQuery() {
        return getArguments().getString(EXTRA_QUERY);
    }

    public long getAccountId() {
        return getArguments().getLong(EXTRA_ACCOUNT_ID);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_pages, container, false);
    }

    @Override
    public void onDetachFragment(final Fragment fragment) {

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE: {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                final Bundle args = getArguments();
                if (twitter != null && args != null) {
                    twitter.createSavedSearchAsync(getAccountId(), getQuery());
                }
                return true;
            }
            case MENU_COMPOSE: {
                final Intent intent = new Intent(getActivity(), ComposeActivity.class);
                intent.setAction(INTENT_ACTION_COMPOSE);
                intent.putExtra(Intent.EXTRA_TEXT, String.format("#%s ", getQuery()));
                intent.putExtra(EXTRA_ACCOUNT_ID, getAccountId());
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem item = menu.findItem(MENU_COMPOSE);
        item.setTitle(getString(R.string.tweet_hashtag, getQuery()));
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
        mPagerWindowOverlay = view.findViewById(R.id.pager_window_overlay);
        mPagerIndicator = (TabPagerIndicator) view.findViewById(R.id.view_pager_tabs);
    }

    @Override
    public boolean scrollToStart() {
        if (!(mCurrentVisibleFragment instanceof RefreshScrollTopInterface)) return false;
        ((RefreshScrollTopInterface) mCurrentVisibleFragment).scrollToStart();
        return true;
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
}
