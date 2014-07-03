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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArrayAdapter<T> extends BaseAdapter {

	private final LayoutInflater mInflater;
	private final int mLayoutRes;
	private int mDropDownLayoutRes;

	protected final ArrayList<T> mData = new ArrayList<T>();
	private final Context mContext;

	public ArrayAdapter(final Context context, final int layoutRes) {
		this(context, layoutRes, null);
	}

	public ArrayAdapter(final Context context, final int layoutRes, final Collection<? extends T> collection) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mLayoutRes = layoutRes;
		if (collection != null) {
			addAll(collection);
		}
	}

	public void add(final T item) {
		if (item == null) return;
		mData.add(item);
		notifyDataSetChanged();
	}

	public void addAll(final Collection<? extends T> collection) {
		mData.addAll(collection);
		notifyDataSetChanged();
	}

	public void clear() {
		mData.clear();
		notifyDataSetChanged();
	}

	public T findItem(final long id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	public int findItemPosition(final long id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItemId(i) == id) return i;
		}
		return -1;
	}

	public List<T> getAsList() {
		return Collections.unmodifiableList(mData);
	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		final int layoutRes = mDropDownLayoutRes > 0 ? mDropDownLayoutRes : mLayoutRes;
		return convertView != null ? convertView : mInflater.inflate(layoutRes, parent, false);
	}

	@Override
	public T getItem(final int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		return convertView != null ? convertView : mInflater.inflate(mLayoutRes, parent, false);
	}

	public boolean remove(final int position) {
		final boolean ret = mData.remove(position) != null;
		notifyDataSetChanged();
		return ret;
	}

	public void removeAll(final List<T> collection) {
		mData.removeAll(collection);
		notifyDataSetChanged();
	}

	public void setDropDownViewResource(final int layoutRes) {
		mDropDownLayoutRes = layoutRes;
	}

	public void sort(final Comparator<? super T> comparator) {
		Collections.sort(mData, comparator);
		notifyDataSetChanged();
	}

}
