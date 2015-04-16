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
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

public class ParcelableUsersAdapter extends AbsUsersAdapter<List<ParcelableUser>> {

    private List<ParcelableUser> mData;


    public ParcelableUsersAdapter(Context context, boolean compact) {
        super(context, compact);
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
    protected void bindStatus(UserViewHolder holder, int position) {
        holder.displayUser(getUser(position));
    }

    @Override
    public int getItemCount() {
        return getUsersCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
    }

    @Override
    public ParcelableUser getUser(int position) {
        if (position == getUsersCount()) return null;
        return mData.get(position);
    }

    @Override
    public long getUserId(int position) {
        if (position == getUsersCount()) return -1;
        return mData.get(position).id;
    }

    @Override
    public int getUsersCount() {
        if (mData == null) return 0;
        return mData.size();
    }
}
