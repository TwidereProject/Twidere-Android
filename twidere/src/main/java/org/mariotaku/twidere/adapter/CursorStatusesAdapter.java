/*
 * Twidere - Twitter client for Android
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
import android.database.Cursor;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatus.CursorIndices;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public class CursorStatusesAdapter extends AbsStatusesAdapter<Cursor> {

    private Cursor mCursor;
    private CursorIndices mIndices;

    public CursorStatusesAdapter(Context context, boolean compact) {
        super(context, compact);
        setHasStableIds(true);
    }

    @Override
    public boolean isGapItem(int position) {
        final Cursor c = mCursor;
        return c != null && c.moveToPosition(position) && c.getInt(mIndices.is_gap) == 1;
    }


    @Override
    public long getItemId(int position) {
        if (position == getStatusCount()) return -1;
        final Cursor c = mCursor;
        if (c != null && !c.isClosed() && c.moveToPosition(position)) {
            final long account_id = c.getLong(mIndices.account_id);
            final long id = c.getLong(mIndices.status_id);
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (account_id ^ account_id >>> 32);
            result = prime * result + (int) (id ^ id >>> 32);
            return result;
        }
        return -1;
    }

    @Override
    protected void bindStatus(StatusViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.displayStatus(mCursor, mIndices, isShowInReplyTo());
    }

    @Override
    public ParcelableStatus getStatus(int position) {
        if (hasLoadMoreIndicator() && position == getStatusCount() - 1) return null;
        final Cursor c = mCursor;
        if (c != null && !c.isClosed() && c.moveToPosition(position)) {
            return new ParcelableStatus(c, mIndices);
        }
        return null;
    }

    @Override
    public int getStatusCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public long getStatusId(int position) {
        if (position == getStatusCount()) return position;
        final Cursor c = mCursor;
        if (c != null && !c.isClosed() && c.moveToPosition(position)) {
            return c.getLong(mIndices.status_id);
        }
        return position;
    }

    @Override
    public void setData(Cursor data) {
        mCursor = data;
        mIndices = data != null ? new CursorIndices(data) : null;
        notifyDataSetChanged();
    }

    @Override
    public Cursor getData() {
        return mCursor;
    }
}
