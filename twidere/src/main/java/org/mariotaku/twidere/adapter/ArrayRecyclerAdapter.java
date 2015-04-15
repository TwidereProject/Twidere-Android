package org.mariotaku.twidere.adapter;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mariotaku on 14/10/27.
 */
public abstract class ArrayRecyclerAdapter<T, H extends ViewHolder> extends Adapter<H> {

    protected final ArrayList<T> mData = new ArrayList<>();

    @Override
    public final void onBindViewHolder(H holder, int position) {
        onBindViewHolder(holder, position, getItem(position));
    }

    public abstract void onBindViewHolder(H holder, int position, T item);


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

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public T getItem(final int position) {
        return mData.get(position);
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

    public void sort(final Comparator<? super T> comparator) {
        Collections.sort(mData, comparator);
        notifyDataSetChanged();
    }
}
