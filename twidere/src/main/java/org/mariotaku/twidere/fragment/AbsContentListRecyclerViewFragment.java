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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;

/**
 * Comment, blah, blah, blah.
 * Created by mariotaku on 15/4/16.
 */
public abstract class AbsContentListRecyclerViewFragment<A extends LoadMoreSupportAdapter>
        extends AbsContentRecyclerViewFragment<A, LinearLayoutManager> {

    @Nullable
    protected RecyclerView.ItemDecoration createItemDecoration(Context context,
                                                               RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        return new DividerItemDecoration(context, layoutManager.getOrientation());
    }


    @Override
    public void setLoadMoreIndicatorPosition(@IndicatorPosition int position) {
        RecyclerView.ItemDecoration decor = getItemDecoration();
        if (decor != null) {
            if (decor instanceof DividerItemDecoration) {
                ((DividerItemDecoration) decor).setDecorationStart((position & IndicatorPosition.START) != 0 ? 1 : 0);
                ((DividerItemDecoration) decor).setDecorationEndOffset((position & IndicatorPosition.END) != 0 ? 1 : 0);
            }
        }
        super.setLoadMoreIndicatorPosition(position);
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
    public boolean isReachingEnd() {
        return getLayoutManager().findLastCompletelyVisibleItemPosition() >= getLayoutManager().getItemCount() - 1;
    }

    @Override
    public boolean isReachingStart() {
        return getLayoutManager().findFirstCompletelyVisibleItemPosition() <= 0;
    }

}
