package org.mariotaku.harmony.view;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public final class HorizontalWrapperAdapter implements ListAdapter {

	private final ListAdapter mAdapter;

	HorizontalWrapperAdapter(final ListAdapter adapter) {
		mAdapter = adapter;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mAdapter.areAllItemsEnabled();
	}

	@Override
	public int getCount() {
		return mAdapter.getCount();
	}

	@Override
	public Object getItem(final int position) {
		return mAdapter.getItem(position);
	}

	@Override
	public long getItemId(final int position) {
		return mAdapter.getItemId(position);
	}

	@Override
	public int getItemViewType(final int position) {
		return mAdapter.getItemViewType(position);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = mAdapter.getView(position, convertView, parent);
		if (convertView == null) {
			view.setPivotX(0);
			view.setPivotX(0);
			view.setRotation(view.getRotation() + 90);
			view.setRotationY(view.getRotationY() + 180);
		}
		return view;
	}

	@Override
	public int getViewTypeCount() {
		return mAdapter.getViewTypeCount();
	}

	public ListAdapter getWrapped() {
		return mAdapter;
	}

	@Override
	public boolean hasStableIds() {
		return mAdapter.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return mAdapter.isEmpty();
	}

	@Override
	public boolean isEnabled(final int position) {
		return mAdapter.isEnabled(position);
	}

	@Override
	public void registerDataSetObserver(final DataSetObserver observer) {
		mAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(final DataSetObserver observer) {
		mAdapter.unregisterDataSetObserver(observer);
	}

}