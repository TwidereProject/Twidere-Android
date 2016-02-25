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
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusCursorIndices;

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
        return getStatus(position).is_gap && position != getStatusCount() - 1;
    }

    @Override
    public ParcelableStatus getStatus(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getStatusCount()) return null;
        return mData.get(dataPosition);
    }

    @Override
    public int getStatusCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public long getItemId(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getStatusCount()) return adapterPosition;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor(dataPosition);
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            final long account_id = cursor.getLong(indices.account_id);
            final long id = cursor.getLong(indices.id);
            return ParcelableStatus.calculateHashCode(account_id, id);
        }
        return System.identityHashCode(mData.get(dataPosition));
    }

    @Override
    public long getStatusId(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getStatusCount()) return RecyclerView.NO_ID;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor(dataPosition);
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return cursor.getLong(indices.id);
        }
        return mData.get(dataPosition).id;
    }

    @Override
    public long getAccountId(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getStatusCount()) return RecyclerView.NO_ID;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor(dataPosition);
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return cursor.getLong(indices.account_id);
        }
        return mData.get(dataPosition).account_id;
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
