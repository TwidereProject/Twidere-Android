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
import android.view.View;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

@SuppressLint("Registered")
public class BaseSupportThemedSwipeBackActivity extends BaseSupportActivity implements SwipeBackActivityBase {

	private SwipeBackActivityHelper mSwipebackHelper;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v == null && mSwipebackHelper != null) return mSwipebackHelper.findViewById(id);
		return v;
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		if (mSwipebackHelper == null) return null;
		return mSwipebackHelper.getSwipeBackLayout();
	}

	public boolean isSwiping() {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		return swipeBackLayout != null && swipeBackLayout.isSwiping();
	}

	@Override
	public void scrollToFinishActivity() {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		if (swipeBackLayout == null) return;
		swipeBackLayout.scrollToFinishActivity();
	}

	@Override
	public void setSwipeBackEnable(final boolean enable) {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		if (swipeBackLayout == null) return;
		swipeBackLayout.setEnableGesture(enable);
	}

	public void setSwipeListener(final SwipeListener listener) {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		if (swipeBackLayout == null) return;
		swipeBackLayout.setSwipeListener(listener);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSwipebackHelper = new SwipeBackActivityHelper(this);
		mSwipebackHelper.onActivtyCreate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSwipebackHelper.onDestroy();
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSwipebackHelper.onPostCreate();
	}
}
