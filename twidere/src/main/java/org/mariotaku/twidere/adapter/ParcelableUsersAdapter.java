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
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

public class ParcelableUsersAdapter extends AbsUsersAdapter<List<ParcelableUser>> {

    private List<ParcelableUser> mData;


    public ParcelableUsersAdapter(Context context) {
        super(context);
    }

    @Override
    public List<ParcelableUser> getData() {
        return mData;
    }


    @Override
    public void setData(List<ParcelableUser> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    protected void bindUser(UserViewHolder holder, int position) {
        holder.displayUser(getUser(position));
    }

    @Override
    public int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = getUserCount();
        if ((position & IndicatorPosition.START) != 0) {
            count++;
        }
        if ((position & IndicatorPosition.END) != 0) {
            count++;
        }
        return count;
    }

    @Override
    public ParcelableUser getUser(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return null;
        return mData.get(dataPosition);
    }

    public int getUserStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }

    @Override
    public long getUserId(int position) {
        if (position == getUserCount()) return -1;
        return mData.get(position).key.getId();
    }

    @Override
    public int getUserCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public boolean removeUserAt(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return false;
        mData.remove(dataPosition);
        notifyItemRemoved(adapterPosition);
        return true;
    }

    public int findPosition(UserKey accountKey, long userId) {
        if (mData == null) return RecyclerView.NO_POSITION;
        for (int i = getUserStartIndex(), j = i + getUserCount(); i < j; i++) {
            final ParcelableUser user = mData.get(i);
            if (user.account_key.equals(accountKey) && user.key.getId() == userId) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

}
