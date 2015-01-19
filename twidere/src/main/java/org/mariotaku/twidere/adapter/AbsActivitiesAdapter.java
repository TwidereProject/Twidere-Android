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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
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
import org.mariotaku.twidere.fragment.support.UserFragment;
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
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.StatusClickListener;

/**
 * Created by mariotaku on 15/1/3.
 */
public abstract class AbsActivitiesAdapter<Data> extends Adapter<ViewHolder> implements Constants,
        IActivitiesAdapter<Data>, StatusClickListener {

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
    private final boolean mCompactCards;
    private final boolean mDisplayMediaPreview;
    private final boolean mNameFirst;
    private final boolean mNicknameOnly;
    private boolean mLoadMoreIndicatorEnabled;
    private ActivityAdapterListener mActivityAdapterListener;

    protected AbsActivitiesAdapter(final Context context, boolean compact) {
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
        mCompactCards = compact;
        mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mDisplayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
        mNameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
        mNicknameOnly = preferences.getBoolean(KEY_NICKNAME_ONLY, false);
    }

    public abstract ParcelableActivity getActivity(int position);

    public abstract int getActivityCount();

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

    public boolean isNameFirst() {
        return mNameFirst;
    }

    public boolean isNicknameOnly() {
        return mNicknameOnly;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_status_compact, parent, false);
                    final View itemContent = view.findViewById(R.id.item_content);
                    itemContent.setBackgroundColor(mCardBackgroundColor);
                } else {
                    view = mInflater.inflate(R.layout.card_item_status, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    cardView.setCardBackgroundColor(mCardBackgroundColor);
                }
                final StatusViewHolder holder = new StatusViewHolder(view);
                holder.setTextSize(getTextSize());
                holder.setStatusClickListener(this);
                return holder;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_activity_summary_compact, parent, false);
                } else {
                    view = mInflater.inflate(R.layout.card_item_activity_summary, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    cardView.setCardBackgroundColor(mCardBackgroundColor);
                }
                final ActivityTitleSummaryViewHolder holder = new ActivityTitleSummaryViewHolder(this, view);
                holder.setTextSize(getTextSize());
                return holder;
            }
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
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
                statusViewHolder.displayStatus(getContext(), getImageLoader(), getImageLoadingHandler(),
                        getTwitterWrapper(), isMediaPreviewDisplayed(), false, false, isNameFirst(),
                        isNicknameOnly(), getProfileImageStyle(), getMediaPreviewStyle(), status, null);
                break;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                bindTitleSummaryViewHolder((ActivityTitleSummaryViewHolder) holder, position);
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
        if (position == getActivityCount()) {
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
            case ParcelableActivity.ACTION_RETWEET:
            case ParcelableActivity.ACTION_FAVORITED_RETWEET:
            case ParcelableActivity.ACTION_RETWEETED_RETWEET:
            case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
                return ITEM_VIEW_TYPE_TITLE_SUMMARY;
            }
        }
        return ITEM_VIEW_TYPE_STUB;
    }

    public final int getItemCount() {
        return getActivityCount() + (mLoadMoreIndicatorEnabled ? 1 : 0);
    }

    @Override
    public void onGapClick(ViewHolder holder, int position) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onGapClick((GapViewHolder) holder, position);
        }
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {

    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {

    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
        final ParcelableActivity activity = getActivity(position);
        final ParcelableStatus status;
        if (activity.action == ParcelableActivity.ACTION_MENTION) {
            status = activity.target_object_statuses[0];
        } else {
            status = activity.target_statuses[0];
        }
        final View profileImageView = holder.getProfileImageView();
        final View profileTypeView = holder.getProfileTypeView();
        if (context instanceof FragmentActivity) {
            final Bundle options = Utils.makeSceneTransitionOption((FragmentActivity) context,
                    new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                    new Pair<>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, options);
        } else {
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, null);
        }
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        final ParcelableActivity activity = getActivity(position);
        final ParcelableStatus status;
        if (activity.action == ParcelableActivity.ACTION_MENTION) {
            status = activity.target_object_statuses[0];
        } else {
            status = activity.target_statuses[0];
        }
        Utils.openStatus(getContext(), status, null);
    }

    public void setListener(ActivityAdapterListener listener) {
        mActivityAdapterListener = listener;
    }

    public void setLoadMoreIndicatorEnabled(boolean enabled) {
        if (mLoadMoreIndicatorEnabled == enabled) return;
        mLoadMoreIndicatorEnabled = enabled;
        notifyDataSetChanged();
    }

    protected abstract void bindTitleSummaryViewHolder(ActivityTitleSummaryViewHolder holder, int position);

    protected abstract int getActivityAction(int position);

    private boolean isMediaPreviewDisplayed() {
        return mDisplayMediaPreview;
    }


    public static interface ActivityAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

    }

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
