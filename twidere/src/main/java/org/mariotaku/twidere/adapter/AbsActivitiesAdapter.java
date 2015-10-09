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
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.fragment.support.UserFragment;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.DummyStatusHolderAdapter;
import org.mariotaku.twidere.view.holder.StatusViewHolder.StatusClickListener;

/**
 * Created by mariotaku on 15/1/3.
 */
public abstract class AbsActivitiesAdapter<Data> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IActivitiesAdapter<Data>, StatusClickListener, OnLinkClickListener, ActivityTitleSummaryViewHolder.ActivityClickListener {

    private static final int ITEM_VIEW_TYPE_STUB = 0;
    private static final int ITEM_VIEW_TYPE_GAP = 1;
    private static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;
    private static final int ITEM_VIEW_TYPE_TITLE_SUMMARY = 3;
    private static final int ITEM_VIEW_TYPE_STATUS = 4;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mLoadingHandler;
    private final int mCardBackgroundColor;
    private final int mTextSize;
    private final int mProfileImageStyle, mMediaPreviewStyle, mLinkHighlightingStyle;
    private final boolean mCompactCards;
    private final boolean mDisplayMediaPreview;
    private final boolean mNameFirst;
    private final boolean mDisplayProfileImage;
    private final TwidereLinkify mLinkify;
    private final DummyStatusHolderAdapter mStatusAdapterDelegate;
    private ActivityAdapterListener mActivityAdapterListener;

    protected AbsActivitiesAdapter(final Context context, boolean compact) {
        super(context);
        mContext = context;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mCompactCards = compact;
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mLinkHighlightingStyle = Utils.getLinkHighlightingStyleInt(mPreferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mDisplayMediaPreview = mPreferences.getBoolean(KEY_MEDIA_PREVIEW, false);
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mLinkify = new TwidereLinkify(this);
        mStatusAdapterDelegate = new DummyStatusHolderAdapter(context);
    }

    @Override
    public abstract ParcelableActivity getActivity(int position);

    @Override
    public abstract int getActivityCount();

    public abstract Data getData();

    @Override
    public abstract void setData(Data data);

    @NonNull
    @Override
    public MediaLoaderWrapper getMediaLoader() {
        return mMediaLoader;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public MediaLoadingHandler getMediaLoadingHandler() {
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

    @NonNull
    @Override
    public AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    public int getLinkHighlightingStyle() {
        return mLinkHighlightingStyle;
    }

    public TwidereLinkify getLinkify() {
        return mLinkify;
    }

    public boolean isNameFirst() {
        return mNameFirst;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        final ParcelableActivity activity = getActivity(position);
        final ParcelableStatus status;
        if (activity.action == Activity.ACTION_MENTION) {
            status = activity.target_object_statuses[0];
        } else {
            status = activity.target_statuses[0];
        }
        Utils.openStatus(getContext(), status, null);
    }

    @Override
    public void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position) {

    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
        final ParcelableActivity activity = getActivity(position);
        final ParcelableStatus status;
        if (activity.action == Activity.ACTION_MENTION) {
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
                final StatusViewHolder holder = new StatusViewHolder(mStatusAdapterDelegate, view);
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
                holder.setOnClickListeners();
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
                if (activity.action == Activity.ACTION_MENTION) {
                    status = activity.target_object_statuses[0];
                } else {
                    status = activity.target_statuses[0];
                }
                final StatusViewHolder statusViewHolder = (StatusViewHolder) holder;
                statusViewHolder.displayStatus(status, null, true, true);
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
    public boolean onStatusLongClick(StatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getActivityCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        switch (getActivityAction(position)) {
            case Activity.ACTION_MENTION:
            case Activity.ACTION_REPLY:
            case Activity.ACTION_QUOTE: {
                return ITEM_VIEW_TYPE_STATUS;
            }
            case Activity.ACTION_FOLLOW:
            case Activity.ACTION_FAVORITE:
            case Activity.ACTION_RETWEET:
            case Activity.ACTION_FAVORITED_RETWEET:
            case Activity.ACTION_RETWEETED_RETWEET:
            case Activity.ACTION_RETWEETED_MENTION:
            case Activity.ACTION_FAVORITED_MENTION:
            case Activity.ACTION_LIST_CREATED:
            case Activity.ACTION_LIST_MEMBER_ADDED: {
                return ITEM_VIEW_TYPE_TITLE_SUMMARY;
            }
        }
        return ITEM_VIEW_TYPE_STUB;
    }

    @Override
    public final int getItemCount() {
        return getActivityCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
    }

    @Override
    public boolean isGapItem(int position) {
        return false;
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
    public void onLinkClick(String link, String orig, long accountId, long extraId, int type, boolean sensitive, int start, int end) {

    }

    @NonNull
    @Override
    public UserColorNameManager getUserColorNameManager() {
        return mUserColorNameManager;
    }

    public void setListener(ActivityAdapterListener listener) {
        mActivityAdapterListener = listener;
    }

    protected abstract void bindTitleSummaryViewHolder(ActivityTitleSummaryViewHolder holder, int position);

    protected abstract int getActivityAction(int position);

    private boolean isMediaPreviewEnabled() {
        return mDisplayMediaPreview;
    }

    @Override
    public void onActivityClick(ActivityTitleSummaryViewHolder holder, int position) {
        if (mActivityAdapterListener == null) return;
        mActivityAdapterListener.onActivityClick(holder, position);
    }

    public interface ActivityAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onActivityClick(ActivityTitleSummaryViewHolder holder, int position);
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
