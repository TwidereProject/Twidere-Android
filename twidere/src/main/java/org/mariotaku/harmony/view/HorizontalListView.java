package org.mariotaku.harmony.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HorizontalListView extends ListView {

	public HorizontalListView(final Context context) {
		this(context, null);
	}

	public HorizontalListView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public HorizontalListView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setPivotX(0);
		setPivotY(0);
		setRotation(90);
		setRotationY(180);
	}

	@Override
	public ListAdapter getAdapter() {
		final ListAdapter adapter = super.getAdapter();
		return adapter instanceof HorizontalWrapperAdapter ? ((HorizontalWrapperAdapter) adapter).getWrapped()
				: adapter;
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int w = MeasureSpec.getSize(widthMeasureSpec), h = MeasureSpec.getSize(heightMeasureSpec);
		final int wSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY), hSpec = MeasureSpec.makeMeasureSpec(w,
				MeasureSpec.EXACTLY);
		setMeasuredDimension(h, w);
		super.onMeasure(wSpec, hSpec);
	}

	@Override
	public void setAdapter(final ListAdapter adapter) {
		super.setAdapter(new HorizontalWrapperAdapter(adapter));
	}
}
