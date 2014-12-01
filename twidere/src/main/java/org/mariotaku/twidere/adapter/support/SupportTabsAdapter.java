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

package org.mariotaku.twidere.adapter.support;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.view.iface.PagerIndicator;
import org.mariotaku.twidere.view.iface.PagerIndicator.TabListener;
import org.mariotaku.twidere.view.iface.PagerIndicator.TabProvider;

import java.util.ArrayList;
import java.util.Collection;

import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable;
import static org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat;

public class SupportTabsAdapter extends SupportFixedFragmentStatePagerAdapter implements TabProvider, TabListener,
        Constants {

    private final ArrayList<SupportTabSpec> mTabs = new ArrayList<SupportTabSpec>();

    private final Context mContext;
    private final PagerIndicator mIndicator;

    private final int mColumns;

    public SupportTabsAdapter(final Context context, final FragmentManager fm) {
        this(context, fm, null);
    }

    public SupportTabsAdapter(final Context context, final FragmentManager fm, final PagerIndicator indicator) {
        this(context, fm, indicator, 1);
    }

    public SupportTabsAdapter(final Context context, final FragmentManager fm, final PagerIndicator indicator,
                              final int columns) {
        super(fm);
        mContext = context;
        mIndicator = indicator;
        mColumns = columns;
        clear();
    }

    public void addTab(final Class<? extends Fragment> cls, final Bundle args, final String name, final Integer icon,
                       final int position) {
        addTab(new SupportTabSpec(name, icon, cls, args, position));
    }

    public void addTab(final SupportTabSpec spec) {
        mTabs.add(spec);
        notifyDataSetChanged();
    }

    public void addTabs(final Collection<? extends SupportTabSpec> specs) {
        mTabs.addAll(specs);
        notifyDataSetChanged();
    }

    public void clear() {
        mTabs.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment fragment = Fragment.instantiate(mContext, mTabs.get(position).cls.getName());
        fragment.setArguments(mTabs.get(position).args);
        return fragment;
    }

    @Override
    public Drawable getPageIcon(final int position) {
        return getTabIconDrawable(mContext, mTabs.get(position).icon);
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mTabs.get(position).name;
    }

    @Override
    public float getPageWidth(final int position) {
        return 1.0f / mColumns;
    }

    public SupportTabSpec getTab(final int position) {
        return position >= 0 && position < mTabs.size() ? mTabs.get(position) : null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mIndicator != null) {
            mIndicator.notifyDataSetChanged();
        }
    }

    @Override
    public void onPageReselected(final int position) {
        if (!(mContext instanceof SupportFragmentCallback)) return;
        final Fragment f = ((SupportFragmentCallback) mContext).getCurrentVisibleFragment();
        if (f instanceof RefreshScrollTopInterface) {
            ((RefreshScrollTopInterface) f).scrollToStart();
        }
    }

    @Override
    public void onPageSelected(final int position) {
        if (mIndicator == null) return;
        announceForAccessibilityCompat(mContext, (View) mIndicator, getPageTitle(position), getClass());
    }

    @Override
    public boolean onTabLongClick(final int position) {
        if (!(mContext instanceof SupportFragmentCallback)) return false;
        if (((SupportFragmentCallback) mContext).triggerRefresh(position)) return true;
        final Fragment f = ((SupportFragmentCallback) mContext).getCurrentVisibleFragment();
        if (f instanceof RefreshScrollTopInterface)
            return ((RefreshScrollTopInterface) f).triggerRefresh();
        return false;
    }
}
