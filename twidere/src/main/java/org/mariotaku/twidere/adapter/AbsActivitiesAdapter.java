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
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 15/1/3.
 */
public abstract class AbsActivitiesAdapter<Data> extends Adapter<ViewHolder> implements Constants,
        IActivitiesAdapter<Data> {

    private static final int ITEM_VIEW_TYPE_STUB = 0;
    private static final int ITEM_VIEW_TYPE_GAP = 1;
    private static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;
    private static final int ITEM_VIEW_TYPE_TITLE_SUMMARY = 3;
    private static final int ITEM_VIEW_TYPE_STATUS = 4;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ImageLoaderWrapper mImageLoader;
    private final ImageLoadingHandler mLoadingHandler;
    private final AsyncTwitterWrapper mTwitterWrapper;
    private final int mCardBackgroundColor;
    private final int mTextSize;
    private final int mProfileImageStyle, mMediaPreviewStyle;
    private boolean mLoadMoreIndicatorEnabled;

    protected AbsActivitiesAdapter(Context context) {
        mContext = context;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context);
        mInflater = LayoutInflater.from(context);
        mImageLoader = app.getImageLoaderWrapper();
        mLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
        mTwitterWrapper = app.getTwitterWrapper();
        final SharedPreferencesWrapper preferences = SharedPreferencesWrapper.getInstance(context,
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mTextSize = preferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
    }

    public abstract ParcelableActivity getActivity(int position);

    public abstract int getActivityCount();

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {

    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {

    }

    public abstract Data getData();

    public abstract void setData(Data data);

    @Override
    public ImageLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public ImageLoadingHandler getImageLoadingHandler() {
        return mLoadingHandler;
    }

    @Override
    public int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    @Override
    public AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    public boolean hasLoadMoreIndicator() {
        return mLoadMoreIndicatorEnabled;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                final View view = mInflater.inflate(R.layout.card_item_status_compat, parent, false);
                final StatusViewHolder holder = new StatusViewHolder(view);
                holder.setTextSize(getTextSize());
                return holder;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                final View view = mInflater.inflate(R.layout.list_item_activity_about_me_status, parent, false);
                final ActivityTitleSummaryViewHolder holder = new ActivityTitleSummaryViewHolder(this, view);
                holder.setTextSize(getTextSize());
                return holder;
            }
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
            default: {
                final View view = mInflater.inflate(R.layout.list_item_two_line, parent, false);
                return new StubViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_VIEW_TYPE_STATUS: {
                final ParcelableActivity activity = getActivity(position);
                final ParcelableStatus status;
                if (activity.action == ParcelableActivity.ACTION_MENTION) {
                    status = activity.target_object_statuses[0];
                } else {
                    status = activity.target_statuses[0];
                }
                final StatusViewHolder statusViewHolder = (StatusViewHolder) holder;
                statusViewHolder.displayStatus(getContext(), getImageLoader(),
                        getImageLoadingHandler(), getTwitterWrapper(),
                        getProfileImageStyle(), getMediaPreviewStyle(), status, null);
                break;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                ((ActivityTitleSummaryViewHolder) holder).displayActivity(getActivity(position));
                break;
            }
            case ITEM_VIEW_TYPE_STUB: {
                ((StubViewHolder) holder).displayActivity(getActivity(position));
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        switch (getActivityAction(position)) {
            case ParcelableActivity.ACTION_MENTION:
            case ParcelableActivity.ACTION_REPLY: {
                return ITEM_VIEW_TYPE_STATUS;
            }
            case ParcelableActivity.ACTION_FOLLOW:
            case ParcelableActivity.ACTION_FAVORITE:
            case ParcelableActivity.ACTION_RETWEET: {
                return ITEM_VIEW_TYPE_TITLE_SUMMARY;
            }
        }
        return ITEM_VIEW_TYPE_STUB;
    }

    public final int getItemCount() {
        return getActivityCount() + (mLoadMoreIndicatorEnabled ? 1 : 0);
    }

    public void setLoadMoreIndicatorEnabled(boolean enabled) {
        if (mLoadMoreIndicatorEnabled == enabled) return;
        mLoadMoreIndicatorEnabled = enabled;
        notifyDataSetChanged();
    }

    protected abstract int getActivityAction(int position);

    private static class StubViewHolder extends ViewHolder {

        private final TextView text1, text2;

        public StubViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

        public void displayActivity(ParcelableActivity activity) {
            text1.setText(String.valueOf(activity.action));
            text2.setText(activity.toString());
        }
    }

}
