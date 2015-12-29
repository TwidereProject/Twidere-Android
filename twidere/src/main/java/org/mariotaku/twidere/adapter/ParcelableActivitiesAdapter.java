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

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ParcelableActivitiesAdapter extends AbsActivitiesAdapter<List<ParcelableActivity>> {

    private List<ParcelableActivity> mData;
    private final boolean mIsByFriends;

    public ParcelableActivitiesAdapter(Context context, boolean compact, boolean byFriends) {
        super(context, compact);
        mIsByFriends = byFriends;
    }

    @Override
    public boolean isGapItem(int position) {
        return getActivity(position).is_gap && position != getActivityCount() - 1;
    }


    @Override
    public String getActivityAction(int position) {
        if (mData instanceof ObjectCursor) {
            final ParcelableActivityCursorIndices indices = (ParcelableActivityCursorIndices) ((ObjectCursor) mData).getIndices();
            return ((ObjectCursor) mData).getCursor(position).getString(indices.action);
        }
        return mData.get(position).action;
    }

    @Override
    public long getTimestamp(int position) {
        if (mData instanceof ObjectCursor) {
            final ParcelableActivityCursorIndices indices = (ParcelableActivityCursorIndices) ((ObjectCursor) mData).getIndices();
            return ((ObjectCursor) mData).getCursor(position).getLong(indices.timestamp);
        }
        return mData.get(position).timestamp;
    }

    @Override
    public ParcelableActivity getActivity(int position) {
        if (position == getActivityCount()) return null;
        return mData.get(position);
    }

    @Override
    public int getActivityCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    protected void onSetData(List<ParcelableActivity> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    protected void bindTitleSummaryViewHolder(ActivityTitleSummaryViewHolder holder, int position) {
        holder.displayActivity(getActivity(position), mIsByFriends);
    }

    @Override
    public List<ParcelableActivity> getData() {
        return mData;
    }


}
