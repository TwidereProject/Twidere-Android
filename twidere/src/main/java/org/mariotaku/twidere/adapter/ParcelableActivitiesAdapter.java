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
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.mariotaku.twidere.model.ParcelableActivity;

import java.util.List;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ParcelableActivitiesAdapter extends AbsActivitiesAdapter<List<ParcelableActivity>> {

    private List<ParcelableActivity> mData;

    public ParcelableActivitiesAdapter(Context context) {
        super(context);
    }

    @Override
    public boolean isGapItem(int position) {
        return false;
    }


    @Override
    public void onGapClick(ViewHolder holder, int position) {

    }


    @Override
    protected int getActivityAction(int position) {
        return mData.get(position).action;
    }

    @Override
    public ParcelableActivity getActivity(int position) {
        if (hasLoadMoreIndicator() && position == getItemCount() - 1) return null;
        return mData.get(position);
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {

    }

    @Override
    public int getActivityCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public void onItemMenuClick(ViewHolder holder, int position) {

    }

    public void setData(List<ParcelableActivity> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public List<ParcelableActivity> getData() {
        return mData;
    }


}
