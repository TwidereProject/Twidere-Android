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
import android.support.annotation.Nullable;
import android.support.v4.widget.Space;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.fragment.support.CursorActivitiesFragment;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 15/1/3.
 */
public abstract class AbsActivitiesAdapter<Data> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IActivitiesAdapter<Data> {

    public static final int ITEM_VIEW_TYPE_STUB = 0;
    public static final int ITEM_VIEW_TYPE_GAP = 1;
    public static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 2;
    public static final int ITEM_VIEW_TYPE_TITLE_SUMMARY = 3;
    public static final int ITEM_VIEW_TYPE_STATUS = 4;
    public static final int ITEM_VIEW_TYPE_EMPTY = 5;

    final LayoutInflater mInflater;
    final MediaLoadingHandler mLoadingHandler;
    final int mCardBackgroundColor;
    final boolean mCompactCards;
    final DummyStatusHolderAdapter mStatusAdapterDelegate;
    final EventListener mEventListener;
    ActivityAdapterListener mActivityAdapterListener;

    long[] mFilteredUserIds;
    boolean mFollowingOnly;
    boolean mMentionsOnly;


    protected AbsActivitiesAdapter(final Context context, boolean compact) {
        super(context);
        mStatusAdapterDelegate = new DummyStatusHolderAdapter(context,
                new TwidereLinkify(new OnLinkClickHandler(context, null)), this);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mEventListener = new EventListener(this);
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
    public ActivityClickListener getActivityClickListener() {
        return mEventListener;
    }

    @Nullable
    @Override
    public GapClickListener getGapClickListener() {
        return mEventListener;
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
    public boolean isShowAbsoluteTime() {
        return mStatusAdapterDelegate.isShowAbsoluteTime();
    }

    @Override
    public boolean shouldUseStarsForLikes() {
        return mStatusAdapterDelegate.shouldUseStarsForLikes();
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
                holder.setStatusClickListener(mEventListener);
                return holder;
            }
            case ITEM_VIEW_TYPE_TITLE_SUMMARY: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_activity_summary_compact, parent, false);
                } else {
                    view = mInflater.inflate(R.layout.card_item_activity_summary, parent, false);
                }
                final ActivityTitleSummaryViewHolder holder = new ActivityTitleSummaryViewHolder(this,
                        view, mCompactCards);
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
                return new EmptyViewHolder(new Space(getContext()));
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
    public int getItemViewType(int position) {
        final ParcelableActivity activity = getActivity(position);
        if ((getLoadMoreIndicatorPosition() & IndicatorPosition.START) != 0 && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (position == getActivityCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        final String action = getActivityAction(position);
        if (action == null) throw new NullPointerException();
        switch (action) {
            case Activity.Action.MENTION: {
                if (ArrayUtils.isEmpty(activity.target_object_statuses)) {
                    return ITEM_VIEW_TYPE_STUB;
                }
                if (mFollowingOnly && !activity.status_user_following) return ITEM_VIEW_TYPE_EMPTY;
                return ITEM_VIEW_TYPE_STATUS;
            }
            case Activity.Action.REPLY: {
                if (ArrayUtils.isEmpty(activity.target_statuses)) {
                    return ITEM_VIEW_TYPE_STUB;
                }
                if (mFollowingOnly && !activity.status_user_following) return ITEM_VIEW_TYPE_EMPTY;
                return ITEM_VIEW_TYPE_STATUS;
            }
            case Activity.Action.QUOTE: {
                if (ArrayUtils.isEmpty(activity.target_statuses)) {
                    return ITEM_VIEW_TYPE_STUB;
                }
                if (mFollowingOnly && !activity.status_user_following) return ITEM_VIEW_TYPE_EMPTY;
                return ITEM_VIEW_TYPE_STATUS;
            }
            case Activity.Action.FOLLOW:
            case Activity.Action.FAVORITE:
            case Activity.Action.RETWEET:
            case Activity.Action.FAVORITED_RETWEET:
            case Activity.Action.RETWEETED_RETWEET:
            case Activity.Action.RETWEETED_MENTION:
            case Activity.Action.FAVORITED_MENTION:
            case Activity.Action.LIST_CREATED:
            case Activity.Action.LIST_MEMBER_ADDED: {
                if (mMentionsOnly) return ITEM_VIEW_TYPE_EMPTY;
                ParcelableActivityUtils.initAfterFilteredSourceIds(activity, mFilteredUserIds, mFollowingOnly);
                if (ArrayUtils.isEmpty(activity.after_filtered_source_ids)) {
                    return ITEM_VIEW_TYPE_EMPTY;
                }
                return ITEM_VIEW_TYPE_TITLE_SUMMARY;
            }
        }
        return ITEM_VIEW_TYPE_STUB;
    }

    public void setFollowingOnly(boolean followingOnly) {
        mFollowingOnly = followingOnly;
        notifyDataSetChanged();
    }

    public void setMentionsOnly(boolean mentionsOnly) {
        mMentionsOnly = mentionsOnly;
        notifyDataSetChanged();
    }

    @Override
    public final int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = 0;
        if ((position & IndicatorPosition.START) != 0) {
            count += 1;
        }
        count += getActivityCount();
        if ((position & IndicatorPosition.END) != 0) {
            count += 1;
        }
        return count;
    }


    public void setListener(ActivityAdapterListener listener) {
        mActivityAdapterListener = listener;
    }

    protected abstract void bindTitleSummaryViewHolder(ActivityTitleSummaryViewHolder holder, int position);

    @Nullable
    public abstract String getActivityAction(int position);

    public abstract long getTimestamp(int position);

    @Override
    public boolean isMediaPreviewEnabled() {
        return mStatusAdapterDelegate.isMediaPreviewEnabled();
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

    public int getActivityStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }


    public interface ActivityAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onActivityClick(ActivityTitleSummaryViewHolder holder, int position);

        void onStatusActionClick(IStatusViewHolder holder, int id, int position);

        void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position);

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusClick(IStatusViewHolder holder, int position);

    }

    static class StubViewHolder extends ViewHolder {

        final TextView text1, text2;

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


    static class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    static class EventListener implements IStatusViewHolder.StatusClickListener, GapClickListener,
            ActivityClickListener {

        final WeakReference<AbsActivitiesAdapter<?>> adapterRef;

        EventListener(AbsActivitiesAdapter<?> adapter) {
            adapterRef = new WeakReference<AbsActivitiesAdapter<?>>(adapter);
        }

        @Override
        public final void onGapClick(ViewHolder holder, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener != null) {
                adapter.mActivityAdapterListener.onGapClick((GapViewHolder) holder, position);
            }
        }

        @Override
        public final void onItemActionClick(ViewHolder holder, int id, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener != null) {
                adapter.mActivityAdapterListener.onStatusActionClick(((IStatusViewHolder) holder), id, position);
            }
        }

        @Override
        public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
            return false;
        }

        @Override
        public void onUserProfileClick(IStatusViewHolder holder, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final Context context = adapter.getContext();
            final ParcelableActivity activity = adapter.getActivity(position);
            final ParcelableStatus status = ParcelableActivity.getActivityStatus(activity);
            assert status != null;
            IntentUtils.openUserProfile(context, status.account_id, status.user_id,
                    status.user_screen_name, null, true);
        }

        @Override
        public void onStatusClick(IStatusViewHolder holder, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener != null) {
                adapter.mActivityAdapterListener.onStatusClick(holder, position);
            }
        }

        @Override
        public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener != null) {
                adapter.mActivityAdapterListener.onMediaClick(holder, view, media, statusPosition);
            }
        }

        @Override
        public void onActivityClick(ActivityTitleSummaryViewHolder holder, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener == null) return;
            adapter.mActivityAdapterListener.onActivityClick(holder, position);
        }

        @Override
        public final void onItemMenuClick(ViewHolder holder, View menuView, int position) {
            final AbsActivitiesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            if (adapter.mActivityAdapterListener != null) {
                adapter.mActivityAdapterListener.onStatusMenuClick((StatusViewHolder) holder, menuView, position);
            }
        }
    }
}
