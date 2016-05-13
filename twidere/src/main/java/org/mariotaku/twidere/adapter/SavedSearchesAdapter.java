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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SavedSearch;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 15/4/29.
 */
public class SavedSearchesAdapter extends BaseAdapter {

    private ResponseList<SavedSearch> mData;
    private final LayoutInflater mInflater;

    public SavedSearchesAdapter(final Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public SavedSearch findItem(final long id) {
        for (int i = 0, count = getCount(); i < count; i++) {
            if (id != -1 && id == getItemId(i)) return getItem(i);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public SavedSearch getItem(final int position) {
        return mData != null ? mData.get(position) : null;
    }

    @Override
    public long getItemId(final int position) {
        return mData != null ? mData.get(position).getId() : -1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = convertView != null ? convertView : mInflater.inflate(
                android.R.layout.simple_list_item_1, null);
        final TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(getItem(position).getName());
        return view;
    }

    public void setData(final ResponseList<SavedSearch> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public boolean removeItem(UserKey accountId, long searchId) {
        if (mData == null) return false;
        for (int i = 0, mDataSize = mData.size(); i < mDataSize; i++) {
            SavedSearch search = mData.get(i);
            if (search.getId() == searchId) {
                mData.remove(i);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }
}
