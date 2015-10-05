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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.loader.support.ScheduledStatusesLoader;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 15/7/10.
 */
public class ScheduledStatusesFragment extends AbsContentRecyclerViewFragment<ScheduledStatusesFragment.ScheduledStatusesAdapter>
        implements LoaderManager.LoaderCallbacks<List<ScheduledStatus>> {

    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }

    @NonNull
    @Override
    protected ScheduledStatusesAdapter onCreateAdapter(Context context, boolean compact) {
        return new ScheduledStatusesAdapter(getActivity());
    }

    @Override
    public Loader<List<ScheduledStatus>> onCreateLoader(int id, Bundle args) {
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final ScheduledStatus.State[] states = {ScheduledStatus.State.SCHEDULED, ScheduledStatus.State.FAILED};
        return new ScheduledStatusesLoader(getActivity(), accountId, sinceId, maxId, states, null);
    }

    @Override
    public void onLoadFinished(Loader<List<ScheduledStatus>> loader, List<ScheduledStatus> data) {
        getAdapter().setData(data);
        showContent();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        final Bundle loaderArgs = new Bundle();
        loaderArgs.putLong(EXTRA_ACCOUNT_ID, args.getLong(EXTRA_ACCOUNT_ID));
        getLoaderManager().initLoader(0, loaderArgs, this);
        showProgress();
    }

    @Override
    public void onLoaderReset(Loader<List<ScheduledStatus>> loader) {
        getAdapter().setData(null);
    }

    public static class ScheduledStatusesAdapter extends LoadMoreSupportAdapter<RecyclerView.ViewHolder> {
        public static final int ITEM_VIEW_TYPE_SCHEDULED_STATUS = 2;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private List<ScheduledStatus> mData;

        public ScheduledStatusesAdapter(Context context) {
            super(context);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            setLoadMoreSupported(false);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_SCHEDULED_STATUS: {
                    return new ScheduledStatusViewHolder(mInflater.inflate(R.layout.list_item_scheduled_status, parent, false));
                }
                case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                    return new LoadIndicatorViewHolder(mInflater.inflate(R.layout.card_item_load_indicator, parent, false));
                }
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public int getItemViewType(int position) {
            return ITEM_VIEW_TYPE_SCHEDULED_STATUS;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case ITEM_VIEW_TYPE_SCHEDULED_STATUS: {
                    ((ScheduledStatusViewHolder) holder).displayScheduledStatus(mData.get(position));
                    break;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mData == null) return 0;
            return mData.size();
        }

        public void setData(List<ScheduledStatus> data) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    private static final class ScheduledStatusViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ScheduledStatusViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

        public void displayScheduledStatus(ScheduledStatus status) {
            textView.setText(status.getText());
        }
    }

}
