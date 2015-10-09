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

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter;

/**
 * Created by mariotaku on 15/4/16.
 */
public abstract class LoadMoreSupportAdapter<VH extends ViewHolder> extends BaseRecyclerViewAdapter<VH>
        implements ILoadMoreSupportAdapter {

    private boolean mLoadMoreSupported;
    private boolean mLoadMoreIndicatorVisible;

    public LoadMoreSupportAdapter(Context context) {
        super(context);
    }

    @Override
    public final boolean isLoadMoreIndicatorVisible() {
        return mLoadMoreIndicatorVisible;
    }

    @Override
    public final void setLoadMoreIndicatorVisible(boolean enabled) {
        if (mLoadMoreIndicatorVisible == enabled) return;
        mLoadMoreIndicatorVisible = enabled && mLoadMoreSupported;
        notifyDataSetChanged();
    }

    @Override
    public final boolean isLoadMoreSupported() {
        return mLoadMoreSupported;
    }

    @Override
    public final void setLoadMoreSupported(boolean supported) {
        mLoadMoreSupported = supported;
        if (!supported) {
            mLoadMoreIndicatorVisible = false;
        }
        notifyDataSetChanged();
    }

}
