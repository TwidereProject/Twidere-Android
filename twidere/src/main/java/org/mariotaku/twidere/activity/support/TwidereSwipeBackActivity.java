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

package org.mariotaku.twidere.activity.support;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout.OnSwipeBackScrollListener;

import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Registered")
public class TwidereSwipeBackActivity extends BaseSupportThemedSwipeBackActivity implements SupportFragmentCallback,
		OnSwipeBackScrollListener {

	private final List<Fragment> mAttachedFragments = new ArrayList<Fragment>();
	private Fragment mCurrentVisibleFragment;

	@Override
	public void finish() {
		super.finish();
		ThemeUtils.overrideActivityCloseAnimation(this);
	}

	@Override
	public Fragment getCurrentVisibleFragment() {
		if (mCurrentVisibleFragment != null) return mCurrentVisibleFragment;
		for (final Fragment f : mAttachedFragments) {
			if (f.getUserVisibleHint()) return f;
		}
		return null;
	}

	@Override
	public void onAttachFragment(final Fragment fragment) {
		super.onAttachFragment(fragment);
		mAttachedFragments.add(fragment);
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {
		mAttachedFragments.remove(fragment);
	}

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
	}

	@Override
	public void onSwipeBackScroll(final float percent) {
	}

	@Override
	public boolean triggerRefresh(final int position) {
		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		ThemeUtils.overrideActivityOpenAnimation(this);
		super.onCreate(savedInstanceState);
		final SwipeBackLayout swipeBack = getSwipeBackLayout();
		swipeBack.setOnSwipeBackScrollListener(this);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

}
