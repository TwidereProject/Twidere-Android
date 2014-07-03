package me.imid.swipebacklayout.lib.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

@SuppressLint("Registered")
public class SwipeBackActivity extends FragmentActivity implements SwipeBackActivityBase {

	private SwipeBackActivityHelper mSwipebackHelper;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v == null && mSwipebackHelper != null) return mSwipebackHelper.findViewById(id);
		return v;
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipebackHelper.getSwipeBackLayout();
	}

	@Override
	public void scrollToFinishActivity() {
		getSwipeBackLayout().scrollToFinishActivity();
	}

	@Override
	public void setSwipeBackEnable(final boolean enable) {
		getSwipeBackLayout().setEnableGesture(enable);
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
