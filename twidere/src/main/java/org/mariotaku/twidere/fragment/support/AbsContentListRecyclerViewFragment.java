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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;

/**
 * Comment, blah, blah, blah.
 * Created by mariotaku on 15/4/16.
 */
public abstract class AbsContentListRecyclerViewFragment<A extends LoadMoreSupportAdapter>
        extends AbsContentRecyclerViewFragment<A, LinearLayoutManager> {

    private DividerItemDecoration mItemDecoration;

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {
        if (compact) {
            mItemDecoration = new DividerItemDecoration(context, getLayoutManager().getOrientation());
            getRecyclerView().addItemDecoration(mItemDecoration);
        }
    }

    @Override
    public void setLoadMoreIndicatorVisible(boolean visible) {
        if (mItemDecoration != null) {
            mItemDecoration.setDecorationEndOffset(visible ? 1 : 0);
        }
        super.setLoadMoreIndicatorVisible(visible);
    }

    @Override
    protected void scrollToPositionWithOffset(int position, int offset) {
        getLayoutManager().scrollToPositionWithOffset(0, 0);
    }

    @NonNull
    @Override
    protected LinearLayoutManager onCreateLayoutManager(Context context) {
        return new FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public int[] findLastVisibleItemPositions() {
        return new int[]{getLayoutManager().findLastVisibleItemPosition()};
    }

    @Override
    public int[] findFirstVisibleItemPositions() {
        return new int[]{getLayoutManager().findFirstVisibleItemPosition()};
    }

    @Override
    public int getItemCount() {
        return getLayoutManager().getItemCount();
    }
}
