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

import static android.support.v4.app.ListFragmentTrojan.INTERNAL_EMPTY_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_LIST_CONTAINER_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_PROGRESS_CONTAINER_ID;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.refreshnow.widget.RefreshNowConfig;
import org.mariotaku.refreshnow.widget.RefreshNowListView;
import org.mariotaku.refreshnow.widget.RefreshNowProgressIndicator;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.util.ThemeUtils;

public abstract class BasePullToRefreshListFragment extends BaseSupportListFragment implements
		IBasePullToRefreshFragment, OnTouchListener {

	@Override
	public RefreshNowListView getListView() {
		return (RefreshNowListView) super.getListView();
	}

	@Override
	public RefreshMode getRefreshMode() {
		if (getView() == null) return RefreshMode.NONE;
		return getListView().getRefreshMode();
	}

	@Override
	public boolean isRefreshing() {
		if (getView() == null) return false;
		return getListView().isRefreshing();
	}

	/**
	 * Provide default implementation to return a simple list view. Subclasses
	 * can override to replace with their own layout. If doing so, the returned
	 * view hierarchy <em>must</em> have a ListView whose id is
	 * {@link android.R.id#list android.R.id.list} and can optionally have a
	 * sibling view id {@link android.R.id#empty android.R.id.empty} that is to
	 * be shown when the list is empty.
	 * <p>
	 * If you are overriding this method with your own custom content, consider
	 * including the standard layout {@link android.R.layout#list_content} in
	 * your layout file, so that you continue to retain all of the standard
	 * behavior of ListFragment. In particular, this is currently the only way
	 * to have the built-in indeterminant progress state be shown.
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final Context context = getActivity();

		final FrameLayout root = new FrameLayout(context);

		// ------------------------------------------------------------------

		final LinearLayout pframe = new LinearLayout(context);
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		pframe.setOrientation(LinearLayout.VERTICAL);
		pframe.setVisibility(View.GONE);
		pframe.setGravity(Gravity.CENTER);

		final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		pframe.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		root.addView(pframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		// ------------------------------------------------------------------

		final FrameLayout lframe = new FrameLayout(context);
		lframe.setId(INTERNAL_LIST_CONTAINER_ID);

		final TextView tv = new TextView(getActivity());
		tv.setId(INTERNAL_EMPTY_ID);
		tv.setGravity(Gravity.CENTER);
		lframe.addView(tv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		final RefreshNowListView lv = new RefreshNowListView(getActivity());
		lv.setId(android.R.id.list);
		lv.setOverScrollMode(View.OVER_SCROLL_NEVER);
		lv.setDrawSelectorOnTop(false);
		lv.setOnRefreshListener(this);
		lv.setConfig(ThemeUtils.buildRefreshNowConfig(context));
		lv.setOnTouchListener(this);
		lframe.addView(lv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		final RefreshNowProgressIndicator indicator = new RefreshNowProgressIndicator(context);
		indicator.setConfig(ThemeUtils.buildRefreshIndicatorConfig(context));
		final int indicatorHeight = Math.round(3 * getResources().getDisplayMetrics().density);
		lframe.addView(indicator, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorHeight,
				Gravity.TOP));

		lv.setRefreshIndicatorView(indicator);

		root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		return root;
	}

	@Override
	public void onRefreshComplete() {

	}

	@Override
	public final void onRefreshStart(final RefreshMode mode) {
		if (mode.hasStart()) {
			onRefreshFromStart();
		} else if (mode.hasEnd()) {
			onRefreshFromEnd();
		}
	}

	@Override
	public final boolean onTouch(final View v, final MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				onListTouched();
				break;
			}
		}
		return false;
	}

	@Override
	public void setConfig(final RefreshNowConfig config) {
		if (getView() == null) return;
		getListView().setConfig(config);
	}

	@Override
	public void setOnRefreshListener(final OnRefreshListener listener) {

	}

	@Override
	public void setRefreshComplete() {
		if (getView() == null) return;
		getListView().setRefreshComplete();
	}

	@Override
	public void setRefreshIndicatorView(final View view) {
		if (getView() == null) return;
		getListView().setRefreshIndicatorView(view);
	}

	@Override
	public void setRefreshing(final boolean refresh) {
		if (getView() == null) return;
		getListView().setRefreshing(refresh);
	}

	@Override
	public void setRefreshMode(final RefreshMode mode) {
		if (getView() == null) return;
		getListView().setRefreshMode(mode);
	}

	@Override
	public boolean triggerRefresh() {
		onRefreshFromStart();
		setRefreshing(true);
		return true;
	}

	protected void onListTouched() {

	}

}
