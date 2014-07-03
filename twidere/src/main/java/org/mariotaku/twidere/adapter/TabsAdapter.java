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

package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable;
import static org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.view.TabPageIndicator;
import org.mariotaku.twidere.view.TabPageIndicator.TabListener;
import org.mariotaku.twidere.view.TabPageIndicator.TabProvider;

import java.util.ArrayList;
import java.util.Collection;

public class TabsAdapter extends FragmentStatePagerAdapter implements TabProvider, TabListener, Constants {

	private final ArrayList<TabSpec> mTabs = new ArrayList<TabSpec>();

	private final Context mContext;
	private final TabPageIndicator mIndicator;

	public TabsAdapter(final Context context, final FragmentManager fm, final TabPageIndicator indicator) {
		super(fm);
		mContext = context;
		mIndicator = indicator;
		clear();
	}

	public void addTab(final Class<? extends Fragment> cls, final Bundle args, final CharSequence title,
			final Integer icon, final int position) {
		addTab(new TabSpec(title, icon, cls, args, position));
	}

	public void addTab(final TabSpec spec) {
		mTabs.add(spec);
		notifyDataSetChanged();
	}

	public void addTabs(final Collection<? extends TabSpec> specs) {
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

	public TabSpec getTab(final int position) {
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
	}

	@Override
	public void onPageSelected(final int position) {
		if (mIndicator == null) return;
		announceForAccessibilityCompat(mContext, mIndicator, getPageTitle(position), getClass());
	}

	@Override
	public boolean onTabLongClick(final int position) {
		return true;
	}

}
