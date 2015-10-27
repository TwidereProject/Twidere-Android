/*
 *                 Twidere - Twitter client for Android
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

import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

/**
 * Created by mariotaku on 15/10/26.
 */
public abstract class AbsParcelableStatusesAdapter extends AbsStatusesAdapter<List<ParcelableStatus>> {
    private List<ParcelableStatus> mData;

    public AbsParcelableStatusesAdapter(Context context, boolean compact) {
        super(context, compact);
        setHasStableIds(true);
    }

    @Override
    public boolean isGapItem(int position) {
        return getStatus(position).is_gap && position != getStatusesCount() - 1;
    }

    @Override
    public ParcelableStatus getStatus(int position) {
        if (position == getStatusesCount()) return null;
        return mData.get(position);
    }

    @Override
    public int getStatusesCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        if (position == getStatusesCount()) return position;
        return mData.get(position).hashCode();
    }

    @Override
    public long getStatusId(int position) {
        if (position == getStatusesCount()) return -1;
        return mData.get(position).id;
    }

    @Override
    public void setData(List<ParcelableStatus> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public List<ParcelableStatus> getData() {
        return mData;
    }
}
