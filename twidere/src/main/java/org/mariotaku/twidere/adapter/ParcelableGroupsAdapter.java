/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.view.holder.GroupViewHolder;

import java.util.List;

public class ParcelableGroupsAdapter extends AbsGroupsAdapter<List<ParcelableGroup>> {

    private List<ParcelableGroup> mData;


    public ParcelableGroupsAdapter(Context context) {
        super(context);
    }

    @Override
    public List<ParcelableGroup> getData() {
        return mData;
    }


    @Override
    public void setData(List<ParcelableGroup> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    protected void bindGroup(GroupViewHolder holder, int position) {
        holder.displayGroup(getGroup(position));
    }

    @Override
    public int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = getGroupsCount();
        if ((position & IndicatorPosition.START) != 0) {
            count++;
        }
        if ((position & IndicatorPosition.END) != 0) {
            count++;
        }
        return count;
    }

    @Override
    public ParcelableGroup getGroup(int position) {
        if (position == getGroupsCount()) return null;
        return mData.get(position);
    }

    @Override
    public long getGroupId(int position) {
        if (position == getGroupsCount()) return -1;
        return mData.get(position).id;
    }

    @Override
    public int getGroupsCount() {
        if (mData == null) return 0;
        return mData.size();
    }
}
