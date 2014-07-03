package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;

import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.refreshnow.widget.RefreshNowConfig;
import org.mariotaku.refreshnow.widget.iface.IRefreshNowView;

public class RefreshNowStaggeredGridView extends StaggeredGridView implements IRefreshNowView {

	private final Helper mHelper;

	public RefreshNowStaggeredGridView(final Context context) {
		this(context, null);
	}

	public RefreshNowStaggeredGridView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RefreshNowStaggeredGridView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mHelper = new Helper(this, context, attrs, defStyle);
	}

	@Override
	public RefreshMode getRefreshMode() {
		return mHelper.getRefreshMode();
	}

	@Override
	public boolean isRefreshing() {
		return mHelper.isRefreshing();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		return super.onTouchEvent(mHelper.processOnTouchEvent(ev));
	}

	@Override
	public void setConfig(final RefreshNowConfig config) {
		mHelper.setConfig(config);
	}

	@Override
	public void setFriction(final float friction) {
		super.setFriction(friction);
		mHelper.setFriction(friction);
	}

	@Override
	public void setOnRefreshListener(final OnRefreshListener listener) {
		mHelper.setOnRefreshListener(listener);
	}

	@Override
	public void setRefreshComplete() {
		mHelper.setRefreshComplete();
	}

	@Override
	public void setRefreshIndicatorView(final View view) {
		mHelper.setRefreshIndicatorView(view);
	}

	@Override
	public void setRefreshing(final boolean refreshing) {
		mHelper.setRefreshing(refreshing);
	}

	@Override
	public void setRefreshMode(final RefreshMode mode) {
		mHelper.setRefreshMode(mode);
	}

	@Override
	protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		mHelper.dispatchOnScrollChanged(l, t, oldl, oldt);
	}

	@Override
	protected boolean overScrollBy(final int deltaX, final int deltaY, final int scrollX, final int scrollY,
			final int scrollRangeX, final int scrollRangeY, final int maxOverScrollX, final int maxOverScrollY,
			final boolean isTouchEvent) {
		return true;
	}

}