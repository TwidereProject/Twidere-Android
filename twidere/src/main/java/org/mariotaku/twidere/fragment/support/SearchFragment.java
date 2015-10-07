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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarOffsetListener;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
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
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.TabPagerIndicator;

public class SearchFragment extends BaseSupportFragment implements RefreshScrollTopInterface,
        SupportFragmentCallback, SystemWindowsInsetsCallback, ControlBarOffsetListener,
        OnPageChangeListener, KeyboardShortcutCallback {

    private ViewPager mViewPager;
    private View mPagerWindowOverlay;
    private TabPagerIndicator mPagerIndicator;

    private SupportTabsAdapter mPagerAdapter;

    private int mControlBarOffsetPixels;
    private int mControlBarHeight;

    public long getAccountId() {
        return getArguments().getLong(EXTRA_ACCOUNT_ID);
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

    public String getQuery() {
        return getArguments().getString(EXTRA_QUERY);
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
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_NAVIGATION_PREVIOUS_TAB: {
                    final int previous = mViewPager.getCurrentItem() - 1;
                    if (previous >= 0 && previous < mPagerAdapter.getCount()) {
                        mViewPager.setCurrentItem(previous, true);
                    }
                    return true;
                }
                case ACTION_NAVIGATION_NEXT_TAB: {
                    final int next = mViewPager.getCurrentItem() + 1;
                    if (next >= 0 && next < mPagerAdapter.getCount()) {
                        mViewPager.setCurrentItem(next, true);
                    }
                    return true;
                }
            }
        }
        return handler.handleKey(getActivity(), null, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true;
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_PREVIOUS_TAB.equals(action) || ACTION_NAVIGATION_NEXT_TAB.equals(action);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IControlBarActivity) {
            ((IControlBarActivity) context).registerControlBarOffsetListener(this);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_pages, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        final FragmentActivity activity = getActivity();
        mPagerAdapter = new SupportTabsAdapter(activity, getChildFragmentManager(), null, 1);
        mPagerAdapter.addTab(StatusesSearchFragment.class, args, getString(R.string.statuses), R.drawable.ic_action_twitter, 0, null);
        mPagerAdapter.addTab(SearchUsersFragment.class, args, getString(R.string.users), R.drawable.ic_action_user, 1, null);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);
        mPagerIndicator.setOnPageChangeListener(this);
        ThemeUtils.initPagerIndicatorAsActionBarTab(activity, mPagerIndicator, mPagerWindowOverlay);
        ThemeUtils.setCompatToolbarOverlay(activity, new EmptyDrawable());
        ThemeUtils.setCompatContentViewOverlay(activity, new EmptyDrawable());
        ThemeUtils.setWindowOverlayViewOverlay(activity, new EmptyDrawable());

        if (activity instanceof IThemedActivity) {
            final String backgroundOption = ((IThemedActivity) activity).getCurrentThemeBackgroundOption();
            final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
            final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(activity)) : 0xFF;
            mPagerIndicator.setAlpha(actionBarAlpha / 255f);
        }
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

    @Override
    public void onDetach() {
        final FragmentActivity activity = getActivity();
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).unregisterControlBarOffsetListener(this);
        }
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isDetached() || getActivity() == null) return;
        final MenuItem item = menu.findItem(R.id.compose);
        item.setTitle(getString(R.string.tweet_hashtag, getQuery()));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                final Bundle args = getArguments();
                if (twitter != null && args != null) {
                    twitter.createSavedSearchAsync(getAccountId(), getQuery());
                }
                return true;
            }
            case R.id.compose: {
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
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerWindowOverlay = view.findViewById(R.id.pager_window_overlay);
        mPagerIndicator = (TabPagerIndicator) view.findViewById(R.id.view_pager_tabs);
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

    private int getControlBarHeight() {
        final FragmentActivity activity = getActivity();
        final int controlBarHeight;
        if (activity instanceof LinkHandlerActivity) {
            controlBarHeight = ((LinkHandlerActivity) activity).getControlBarHeight();
        } else {
            controlBarHeight = mControlBarHeight;
        }
        if (controlBarHeight == 0) {
            return ThemeUtils.getActionBarHeight(activity);
        }
        return controlBarHeight;
    }

    private Fragment getKeyboardShortcutRecipient() {
        return getCurrentVisibleFragment();
    }

    private boolean handleFragmentKeyboardShortcutRepeat(KeyboardShortcutsHandler handler, int keyCode,
                                                         int repeatCount, @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState);
        }
        return false;
    }

    private boolean handleFragmentKeyboardShortcutSingle(KeyboardShortcutsHandler handler, int keyCode,
                                                         @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState);
        }
        return false;
    }

    private boolean isFragmentKeyboardShortcutHandled(KeyboardShortcutsHandler handler, int keyCode,
                                                      @NonNull KeyEvent event, int metaState) {
        final Fragment fragment = getKeyboardShortcutRecipient();
        if (fragment instanceof KeyboardShortcutCallback) {
            return ((KeyboardShortcutCallback) fragment).isKeyboardShortcutHandled(handler, keyCode,
                    event, metaState);
        }
        return false;
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
}
