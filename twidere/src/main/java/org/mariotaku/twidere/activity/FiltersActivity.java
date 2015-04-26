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

package org.mariotaku.twidere.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseDialogWhenLargeActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredLinksFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.TabPagerIndicator;

public class FiltersActivity extends BaseDialogWhenLargeActivity {

    private TabPagerIndicator mPagerIndicator;
    private ViewPager mViewPager;

    private SupportTabsAdapter mAdapter;

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        mAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), null, 1);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mPagerIndicator.setViewPager(mViewPager);
        mPagerIndicator.setTabDisplayOption(TabPagerIndicator.LABEL);


        mAdapter.addTab(FilteredUsersFragment.class, null, getString(R.string.users), null, 0, null);
        mAdapter.addTab(FilteredKeywordsFragment.class, null, getString(R.string.keywords), null, 1, null);
        mAdapter.addTab(FilteredSourcesFragment.class, null, getString(R.string.sources), null, 2, null);
        mAdapter.addTab(FilteredLinksFragment.class, null, getString(R.string.links), null, 3, null);


        ThemeUtils.initPagerIndicatorAsActionBarTab(this, mPagerIndicator);
        ThemeUtils.setCompatToolbarOverlay(this, new EmptyDrawable());
        ThemeUtils.setCompatContentViewOverlay(this, new EmptyDrawable());

    }

    @Override
    protected boolean isActionBarOutlineEnabled() {
        return false;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerIndicator = (TabPagerIndicator) findViewById(R.id.view_pager_tabs);
    }


}
