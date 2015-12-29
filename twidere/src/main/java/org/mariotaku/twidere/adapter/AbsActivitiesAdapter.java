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
import android.support.v4.widget.Space;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.collections.primitives.ArrayLongList;
import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.fragment.support.CursorActivitiesFragment;
import org.mariotaku.twidere.fragment.support.UserFragment;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.DummyStatusHolderAdapter;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

/**
 * Created by mariotaku on 15/1/3.
 */
public abstract class AbsActivitiesAdapter<Data> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IActivitiesAdapter<Data>, IStatusViewHolder.StatusClickListener,
        ActivityTitleSummaryViewHolder.ActivityClickListener {

    public static final int ITEM_VIEW_TYPE_STUB = 0;
    public static final int ITEM_VIEW_TYPE_GAP = 1;
    public static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;
    public static final int ITEM_VIEW_TYPE_TITLE_SUMMARY = 3;
    public static final int ITEM_VIEW_TYPE_STATUS = 4;
    public static final int ITEM_VIEW_TYPE_EMPTY = 5;

    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mLoadingHandler;
    private final int mCardBackgroundColor;
    private final boolean mCompactCards;
    private final DummyStatusHolderAdapter mStatusAdapterDelegate;
    private ActivityAdapterListener mActivityAdapterListener;

    private long[] mFilteredUserIds;

    protected AbsActivitiesAdapter(final Context context, boolean compact) {
        super(context);
        mStatusAdapterDelegate = new DummyStatusHolderAdapter(context, new TwidereLinkify(new OnLinkClickHandler(context, null)));
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mCompactCards = compact;
        mStatusAdapterDelegate.updateOptions();
    }

    @Override
    public abstract ParcelableActivity getActivity(int position);

    @Override
    public abstract int getActivityCount();

    public abstract Data getData();

    @Override
    public final void setData(Data data) {
        if (data instanceof CursorActivitiesFragment.CursorActivitiesLoader.ActivityCursor) {
            mFilteredUserIds = ((CursorActivitiesFragment.CursorActivitiesLoader.ActivityCursor) data).getFilteredUserIds();
        }
        onSetData(data);
    }

    protected abstract void onSetData(Data data);

    @Override
    public MediaLoadingHandler getMediaLoadingHandler() {
        return mLoadingHandler;
    }

    @Override
    public int getProfileImageStyle() {
        return mStatusAdapterDelegate.getProfileImageStyle();
    }

    @Override
    public int getMediaPreviewStyle() {
        return mStatusAdapterDelegate.getMediaPreviewStyle();
    }

    @Override
    public float getTextSize() {
        return mStatusAdapterDelegate.getTextSize();
    }

    public int getLinkHighlightingStyle() {
        return mStatusAdapterDelegate.getLinkHighlightingStyle();
    }

    public boolean isNameFirst() {
        return mStatusAdapterDelegate.isNameFirst();
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mStatusAdapterDelegate.isProfileImageEnabled();
    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onStatusClick(holder, position);
        }
    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onMediaClick(holder, view, media, statusPosition);
        }
    }

    @Override
    public boolean shouldUseStarsForLikes() {
        return mStatusAdapterDelegate.shouldUseStarsForLikes();
    }

    @Override
    public void onUserProfileClick(IStatusViewHolder holder, int position) {
        final Context context = getContext();
        final ParcelableActivity activity = getActivity(position);
        final ParcelableStatus status = ParcelableActivity.getActivityStatus(activity);
        assert status != null;
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
                holder.setupViewOptions();
                holder.setStatusClickListener(this);
                return holder;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                final View view;
//                if (mCompactCards) {
                view = mInflater.inflate(R.layout.card_item_activity_summary_compact, parent, false);
//                } else {
//                    view = mInflater.inflate(R.layout.card_item_activity_summary, parent, false);
//                    final CardView cardView = (CardView) view.findViewById(R.id.card);
//                    cardView.setCardBackgroundColor(mCardBackgroundColor);
//                }
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
            case ITEM_VIEW_TYPE_STUB: {
                final View view = mInflater.inflate(R.layout.list_item_two_line, parent, false);
                return new StubViewHolder(view);
            }
            case ITEM_VIEW_TYPE_EMPTY: {
                final View view = new Space(getContext());
                return new ViewHolder(view) {
                };
            }
        }
        throw new UnsupportedOperationException("Unsupported viewType " + viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_VIEW_TYPE_STATUS: {
                final ParcelableActivity activity = getActivity(position);
                final ParcelableStatus status = ParcelableActivity.getActivityStatus(activity);
                assert status != null;
                final IStatusViewHolder statusViewHolder = (IStatusViewHolder) holder;
                statusViewHolder.displayStatus(status, true, true);
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
    public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        final ParcelableActivity activity = getActivity(position);
        if (position == getActivityCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        final String action = getActivityAction(position);
        if (Activity.Action.MENTION.literal.equals(action)) {
            if (ArrayUtils.isEmpty(activity.target_object_statuses)) {
                return ITEM_VIEW_TYPE_STUB;
            }
            return ITEM_VIEW_TYPE_STATUS;
        } else if (Activity.Action.REPLY.literal.equals(action)) {
            if (ArrayUtils.isEmpty(activity.target_statuses)) {
                return ITEM_VIEW_TYPE_STUB;
            }
            return ITEM_VIEW_TYPE_STATUS;
        } else if (Activity.Action.QUOTE.literal.equals(action)) {
            if (ArrayUtils.isEmpty(activity.target_statuses)) {
                return ITEM_VIEW_TYPE_STUB;
            }
            return ITEM_VIEW_TYPE_STATUS;
        } else if (Activity.Action.FOLLOW.literal.equals(action) || Activity.Action.FAVORITE.literal.equals(action)
                || Activity.Action.RETWEET.literal.equals(action) || Activity.Action.FAVORITED_RETWEET.literal.equals(action)
                || Activity.Action.RETWEETED_RETWEET.literal.equals(action) || Activity.Action.RETWEETED_MENTION.literal.equals(action)
                || Activity.Action.FAVORITED_MENTION.literal.equals(action) || Activity.Action.LIST_CREATED.literal.equals(action)
                || Activity.Action.LIST_MEMBER_ADDED.literal.equals(action)) {
            if (activity.filtered_source_ids == null) {
                if (!ArrayUtils.isEmpty(mFilteredUserIds)) {
                    ArrayLongList list = new ArrayLongList();
                    for (long id : ArrayUtils.nullToEmpty(activity.source_ids)) {
                        if (!ArrayUtils.contains(mFilteredUserIds, id)) {
                            list.add(id);
                        }
                    }
                    activity.filtered_source_ids = list.toArray();
                } else {
                    activity.filtered_source_ids = activity.source_ids;
                }
            }
            if (ArrayUtils.isEmpty(activity.filtered_source_ids)) {
                return ITEM_VIEW_TYPE_EMPTY;
            }
            return ITEM_VIEW_TYPE_TITLE_SUMMARY;
        }
        return ITEM_VIEW_TYPE_STUB;
    }

    @Override
    public final int getItemCount() {
        return getActivityCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
    }

    @Override
    public final void onGapClick(ViewHolder holder, int position) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onGapClick((GapViewHolder) holder, position);
        }
    }

    @Override
    public final void onItemActionClick(ViewHolder holder, int id, int position) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onStatusActionClick(((IStatusViewHolder) holder), id, position);
        }
    }

    @Override
    public final void onItemMenuClick(ViewHolder holder, View menuView, int position) {
        if (mActivityAdapterListener != null) {
            mActivityAdapterListener.onStatusMenuClick((StatusViewHolder) holder, menuView, position);
        }
    }

    public void setListener(ActivityAdapterListener listener) {
        mActivityAdapterListener = listener;
    }

    protected abstract void bindTitleSummaryViewHolder(ActivityTitleSummaryViewHolder holder, int position);

    public abstract String getActivityAction(int position);

    public abstract long getTimestamp(int position);

    @Override
    public boolean isMediaPreviewEnabled() {
        return mStatusAdapterDelegate.isMediaPreviewEnabled();
    }

    @Override
    public void onActivityClick(ActivityTitleSummaryViewHolder holder, int position) {
        if (mActivityAdapterListener == null) return;
        mActivityAdapterListener.onActivityClick(holder, position);
    }


    @Override
    public boolean shouldShowAccountsColor() {
        return mStatusAdapterDelegate.shouldShowAccountsColor();
    }

    public void setShowAccountsColor(boolean showAccountsColor) {
        mStatusAdapterDelegate.setShouldShowAccountsColor(showAccountsColor);
        notifyDataSetChanged();
    }

    public boolean isActivity(int position) {
        return position < getActivityCount();
    }


    public interface ActivityAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onActivityClick(ActivityTitleSummaryViewHolder holder, int position);

        void onStatusActionClick(IStatusViewHolder holder, int id, int position);

        void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position);

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusClick(IStatusViewHolder holder, int position);

    }

    private static class StubViewHolder extends ViewHolder {

        private final TextView text1, text2;

        public StubViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

        public void displayActivity(ParcelableActivity activity) {
            text1.setText(text1.getResources().getString(R.string.unsupported_activity_action_title,
                    activity.action));
            text2.setText(R.string.unsupported_activity_action_summary);
        }
    }


}
