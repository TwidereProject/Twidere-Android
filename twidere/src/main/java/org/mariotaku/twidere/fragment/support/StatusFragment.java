/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.ParcelableStatusLoader;
import org.mariotaku.twidere.loader.support.StatusRepliesLoader;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.task.TwidereAsyncTask.Status;
import org.mariotaku.twidere.text.method.StatusContentMovementMethod;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.MediaPreviewUtils.OnMediaClickListener;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CircularImageView;
import org.mariotaku.twidere.view.StatusTextView;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;

/**
 * Created by mariotaku on 14/12/5.
 */
public class StatusFragment extends BaseSupportFragment
        implements LoaderCallbacks<SingleResponse<ParcelableStatus>>, OnMediaClickListener {

    private static final int LOADER_ID_DETAIL_STATUS = 1;
    private static final int LOADER_ID_STATUS_REPLIES = 2;


    private RecyclerView mRecyclerView;
    private StatusAdapter mStatusAdapter;
    private boolean mRepliesLoaderInitialized;

    private LoadConversationTask mLoadConversationTask;

    private LoaderCallbacks<List<ParcelableStatus>> mRepliesLoaderCallback = new LoaderCallbacks<List<ParcelableStatus>>() {
        @Override
        public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
            final long maxId = args.getLong(EXTRA_MAX_ID, -1);
            final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
            final StatusRepliesLoader loader = new StatusRepliesLoader(getActivity(), accountId,
                    screenName, statusId, maxId, sinceId, null, null, 0);
            loader.setComparator(ParcelableStatus.REVERSE_ID_COMPARATOR);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
            setReplies(data);
        }

        @Override
        public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {

        }
    };
    private LinearLayoutManager mLayoutManager;

    @Override
    public Loader<SingleResponse<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
        final Bundle fragmentArgs = getArguments();
        final long accountId = fragmentArgs.getLong(EXTRA_ACCOUNT_ID, -1);
        final long statusId = fragmentArgs.getLong(EXTRA_STATUS_ID, -1);
        return new ParcelableStatusLoader(getActivity(), false, fragmentArgs, accountId, statusId);
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableStatus>> loader,
                               final SingleResponse<ParcelableStatus> data) {
        if (data.hasData()) {
            final ParcelableStatus status = data.getData();
            mStatusAdapter.setConversation(null);
            mStatusAdapter.setReplies(null);
            mStatusAdapter.setStatus(status);
            mLayoutManager.scrollToPositionWithOffset(1, 0);
            loadReplies(status);
            loadConversation(status);
        } else {
            //TODO show errors
        }
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media) {

    }

    private void loadConversation(ParcelableStatus status) {
        if (mLoadConversationTask != null && mLoadConversationTask.getStatus() == Status.RUNNING) {
            mLoadConversationTask.cancel(true);
        }
        mLoadConversationTask = new LoadConversationTask(this);
        mLoadConversationTask.executeTask(status);
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
//        mRecyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        getView().setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        final boolean compact = Utils.isCompactCards(context);
        mLayoutManager = new MyLinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (compact) {
            mRecyclerView.addItemDecoration(new DividerItemDecoration(context, mLayoutManager.getOrientation()));
        }
        mLayoutManager.setRecycleChildrenOnDetach(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setClipToPadding(false);
        mStatusAdapter = new StatusAdapter(this, compact);
        mRecyclerView.setAdapter(mStatusAdapter);

        getLoaderManager().initLoader(LOADER_ID_DETAIL_STATUS, getArguments(), this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    private void loadReplies(ParcelableStatus status) {
        if (status == null) return;
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, status.account_id);
        args.putLong(EXTRA_STATUS_ID, status.id);
        args.putString(EXTRA_SCREEN_NAME, status.user_screen_name);
        if (mRepliesLoaderInitialized) {
            getLoaderManager().restartLoader(LOADER_ID_STATUS_REPLIES, args, mRepliesLoaderCallback);
            return;
        }
        getLoaderManager().initLoader(LOADER_ID_STATUS_REPLIES, args, mRepliesLoaderCallback);
        mRepliesLoaderInitialized = true;
    }

    private static class StatusAdapter extends Adapter<ViewHolder> implements IStatusesAdapter<List<ParcelableStatus>> {

        private static final int VIEW_TYPE_DETAIL_STATUS = 0;
        private static final int VIEW_TYPE_LIST_STATUS = 1;
        private static final int VIEW_TYPE_CONVERSATION_LOAD_INDICATOR = 2;
        private static final int VIEW_TYPE_REPLIES_LOAD_INDICATOR = 3;
        private static final int VIEW_TYPE_SPACE = 4;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ImageLoaderWrapper mImageLoader;

        private final boolean mNameFirst, mNicknameOnly;
        private final int mCardLayoutResource;
        private final StatusFragment mFragment;

        private ParcelableStatus mStatus;

        private List<ParcelableStatus> mConversation, mReplies;
        private boolean mDetailMediaExpanded;

        public StatusAdapter(StatusFragment fragment, boolean compact) {
            final Context context = fragment.getActivity();
            final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);
            mFragment = fragment;
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
            mNameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
            mNicknameOnly = preferences.getBoolean(KEY_NICKNAME_ONLY, true);
            if (compact) {
                mCardLayoutResource = R.layout.card_item_status_compat;
            } else {
                mCardLayoutResource = R.layout.card_item_status;
            }
        }

        public int findPositionById(long itemId) {
            for (int i = 0, j = getItemCount(); i < j; i++) {
                if (getItemId(i) == itemId) return i;
            }
            return -1;
        }

        public Context getContext() {
            return mContext;
        }

        @Override
        public ImageLoadingHandler getImageLoadingHandler() {
            return null;
        }

        @Override
        public ParcelableStatus getStatus(int position) {
            final int conversationCount = getConversationCount();
            if (position == getItemCount() - 1) {
                return null;
            } else if (position < conversationCount) {
                return mConversation.get(position);
            } else if (position > conversationCount) {
                return mReplies.get(position - conversationCount - 1);
            } else {
                return mStatus;
            }
        }

        @Override
        public int getStatusCount() {
            return getConversationCount() + 1 + getRepliesCount() + 1;
        }

        public boolean isDetailMediaExpanded() {
            return mDetailMediaExpanded;
        }

        @Override
        public boolean isGapItem(int position) {
            return false;
        }

        @Override
        public void onGapClick(ViewHolder holder, int position) {

        }

        @Override
        public void onItemActionClick(ViewHolder holder, int id, int position) {

        }

        @Override
        public void onItemMenuClick(ViewHolder holder, int position) {

        }

        public void onLoadMediaClick(DetailStatusViewHolder holder, ParcelableStatus status, int position) {
            mFragment.onLoadMediaClick(holder, status, position);
        }

        @Override
        public void onStatusClick(StatusViewHolder holder, int position) {

        }

        @Override
        public void onUserProfileClick(StatusViewHolder holder, int position) {

        }

        @Override
        public void setData(List<ParcelableStatus> data) {

        }

        public ImageLoaderWrapper getImageLoader() {
            return mImageLoader;
        }

        public boolean isNameFirst() {
            return mNameFirst;
        }

        public boolean isNicknameOnly() {
            return mNicknameOnly;
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_DETAIL_STATUS: {
                    final View view = mInflater.inflate(R.layout.header_status, parent, false);
                    return new DetailStatusViewHolder(this, view);
                }
                case VIEW_TYPE_LIST_STATUS: {
                    final View view = mInflater.inflate(mCardLayoutResource, parent, false);
                    return new StatusViewHolder(this, view);
                }
                case VIEW_TYPE_CONVERSATION_LOAD_INDICATOR:
                case VIEW_TYPE_REPLIES_LOAD_INDICATOR: {
                    final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent,
                            false);
                    return new LoadIndicatorViewHolder(view);
                }
                case VIEW_TYPE_SPACE: {
                    return new SpaceViewHolder(new Space(mContext));
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            final int conversationCount = getConversationCount();
            if (position == getItemCount() - 1) {
                return 4;
            } else if (position < conversationCount) {
                return mConversation != null ? mConversation.get(position).id : 2;
            } else if (position > conversationCount) {
                return mReplies != null ? mReplies.get(position - conversationCount - 1).id : 3;
            } else {
                return mStatus != null ? mStatus.id : 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            final int conversationCount = getConversationCount();
            if (position == getItemCount() - 1) {
                return VIEW_TYPE_SPACE;
            } else if (position < conversationCount) {
                return mConversation != null ? VIEW_TYPE_LIST_STATUS : VIEW_TYPE_CONVERSATION_LOAD_INDICATOR;
            } else if (position > conversationCount) {
                return mReplies != null ? VIEW_TYPE_LIST_STATUS : VIEW_TYPE_REPLIES_LOAD_INDICATOR;
            } else {
                return VIEW_TYPE_DETAIL_STATUS;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_DETAIL_STATUS: {
                    final ParcelableStatus status = getStatus(position);
                    final DetailStatusViewHolder detailHolder = (DetailStatusViewHolder) holder;
                    detailHolder.showStatus(status);
                    break;
                }
                case VIEW_TYPE_LIST_STATUS: {
                    final ParcelableStatus status = getStatus(position);
                    final StatusViewHolder statusHolder = (StatusViewHolder) holder;
                    statusHolder.displayStatus(status);
                    break;
                }
            }
        }

        public void setConversation(List<ParcelableStatus> conversation) {
            mConversation = conversation;
            notifyDataSetChanged();
        }

        public void setDetailMediaExpanded(boolean expanded) {
            mDetailMediaExpanded = expanded;
            notifyDataSetChanged();
        }

        private int getConversationCount() {
            return mConversation != null ? mConversation.size() : 1;
        }

        private int getRepliesCount() {
            return mReplies != null ? mReplies.size() : 1;
        }

        public void setReplies(List<ParcelableStatus> replies) {
            mReplies = replies;
            notifyDataSetChanged();
        }

        public void setStatus(ParcelableStatus status) {
            mStatus = status;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return getStatusCount();
        }
    }

    private void onLoadMediaClick(DetailStatusViewHolder holder, ParcelableStatus status, int position) {
        mStatusAdapter.setDetailMediaExpanded(true);
    }


    static class LoadConversationTask extends TwidereAsyncTask<ParcelableStatus, ParcelableStatus,
            ListResponse<ParcelableStatus>> {

        final Context context;
        final StatusFragment fragment;

        LoadConversationTask(final StatusFragment fragment) {
            context = fragment.getActivity();
            this.fragment = fragment;
        }

        @Override
        protected ListResponse<ParcelableStatus> doInBackground(final ParcelableStatus... params) {
            final ArrayList<ParcelableStatus> list = new ArrayList<>();
            try {
                ParcelableStatus status = params[0];
                final long account_id = status.account_id;
                while (status.in_reply_to_status_id > 0 && !isCancelled()) {
                    status = findStatus(context, account_id, status.in_reply_to_status_id);
                    publishProgress(status);
                    list.add(0, status);
                }
            } catch (final TwitterException e) {
                return ListResponse.getListInstance(e);
            }
            return ListResponse.getListInstance(list);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onProgressUpdate(ParcelableStatus... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final ListResponse<ParcelableStatus> data) {
            if (data.hasData()) {
                fragment.setConversation(data.getData());
            } else {
                showErrorMessage(context, context.getString(R.string.action_getting_status), data.getException(), true);
            }
        }

    }

    private void setReplies(List<ParcelableStatus> data) {
        if (mLayoutManager.getChildCount() != 0) {
            final long itemId = mStatusAdapter.getItemId(mLayoutManager.findFirstVisibleItemPosition());
            final int top = mLayoutManager.getChildAt(0).getTop();
            mStatusAdapter.setReplies(data);
            final int position = mStatusAdapter.findPositionById(itemId);
            mLayoutManager.scrollToPositionWithOffset(position, top);
        } else {
            mStatusAdapter.setReplies(data);
        }
    }

    private void setConversation(List<ParcelableStatus> data) {
        if (mLayoutManager.getChildCount() != 0) {
            final long itemId = mStatusAdapter.getItemId(mLayoutManager.findFirstVisibleItemPosition());
            final int top = mLayoutManager.getChildAt(0).getTop();
            mStatusAdapter.setConversation(data);
            final int position = mStatusAdapter.findPositionById(itemId);
            mLayoutManager.scrollToPositionWithOffset(position, top);
        } else {
            mStatusAdapter.setConversation(data);
        }
    }

    private static class SpaceViewHolder extends ViewHolder {

        public SpaceViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DetailStatusViewHolder extends ViewHolder implements OnClickListener, OnMediaClickListener {

        private final StatusAdapter adapter;

        private final CardView cardView;
        private final MenuBar menuBar;
        private final TextView nameView, screenNameView;
        private final StatusTextView textView;
        private final CircularImageView profileImageView;
        private final ImageView profileTypeView;
        private final TextView timeSourceView;
        private final TextView replyRetweetStatusView;
        private final View repliesContainer, retweetsContainer, favoritesContainer;
        private final TextView repliesCountView, retweetsCountView, favoritesCountView;
        private final View cardContent, progressContainer;
        private final View loadMedia;
        private final LinearLayout mediaPreviewGrid;

        public DetailStatusViewHolder(StatusAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            cardView = (CardView) itemView.findViewById(R.id.card);
            cardContent = itemView.findViewById(R.id.card_content);
            progressContainer = itemView.findViewById(R.id.progress_container);
            menuBar = (MenuBar) itemView.findViewById(R.id.menu_bar);
            nameView = (TextView) itemView.findViewById(R.id.name);
            screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
            textView = (StatusTextView) itemView.findViewById(R.id.text);
            profileImageView = (CircularImageView) itemView.findViewById(R.id.profile_image);
            profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
            timeSourceView = (TextView) itemView.findViewById(R.id.time_source);
            replyRetweetStatusView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
            repliesContainer = itemView.findViewById(R.id.replies_container);
            retweetsContainer = itemView.findViewById(R.id.retweets_container);
            favoritesContainer = itemView.findViewById(R.id.favorites_container);
            repliesCountView = (TextView) itemView.findViewById(R.id.replies_count);
            retweetsCountView = (TextView) itemView.findViewById(R.id.retweets_count);
            favoritesCountView = (TextView) itemView.findViewById(R.id.favorites_count);
            loadMedia = itemView.findViewById(R.id.load_media);
            mediaPreviewGrid = (LinearLayout) itemView.findViewById(R.id.media_grid);

            setIsRecyclable(false);
            initViews();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.load_media: {
                    final ParcelableStatus status = adapter.getStatus(getPosition());
                    adapter.onLoadMediaClick(this, status, getPosition());
                    break;
                }
            }
        }

        @Override
        public void onMediaClick(View view, ParcelableMedia media) {
            adapter.mFragment.onMediaClick(view, media);
        }

        private void initViews() {
            menuBar.inflate(R.menu.menu_status);
            loadMedia.setOnClickListener(this);
        }

        public void showStatus(ParcelableStatus status) {
            if (status == null) return;
            progressContainer.setVisibility(View.GONE);
            final Context context = adapter.getContext();
            final Resources resources = context.getResources();
            final ImageLoaderWrapper loader = adapter.getImageLoader();
            final boolean nameFirst = adapter.isNameFirst();
            final boolean nicknameOnly = adapter.isNicknameOnly();
            final String nickname = getUserNickname(context, status.user_id, true);
            if (TextUtils.isEmpty(nickname)) {
                nameView.setText(status.user_name);
            } else if (nicknameOnly) {
                nameView.setText(nickname);
            } else {
                nameView.setText(context.getString(R.string.name_with_nickname, status.user_name,
                        nickname));
            }
            screenNameView.setText("@" + status.user_screen_name);

            textView.setText(Html.fromHtml(status.text_html));
            final TwidereLinkify linkify = new TwidereLinkify(new OnLinkClickHandler(context, null));
            linkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
            linkify.applyAllLinks(textView, status.account_id, status.is_possibly_sensitive);
            ThemeUtils.applyParagraphSpacing(textView, 1.1f);

            textView.setMovementMethod(StatusContentMovementMethod.getInstance());
//            textView.setCustomSelectionActionModeCallback(this);


            final String timeString = formatToLongTimeString(context, status.timestamp);
            final String sourceHtml = status.source;
            if (!isEmpty(timeString) && !isEmpty(sourceHtml)) {
                timeSourceView.setText(Html.fromHtml(context.getString(R.string.time_source,
                        timeString, sourceHtml)));
            } else if (isEmpty(timeString) && !isEmpty(sourceHtml)) {
                timeSourceView.setText(Html.fromHtml(context.getString(R.string.source,
                        sourceHtml)));
            } else if (!isEmpty(timeString) && isEmpty(sourceHtml)) {
                timeSourceView.setText(timeString);
            }
            timeSourceView.setMovementMethod(LinkMovementMethod.getInstance());


            retweetsContainer.setVisibility(!status.user_is_protected ? View.VISIBLE : View.GONE);
            repliesContainer.setVisibility(status.reply_count < 0 ? View.GONE : View.VISIBLE);
            final Locale locale = context.getResources().getConfiguration().locale;
            repliesCountView.setText(getLocalizedNumber(locale, status.reply_count));
            retweetsCountView.setText(getLocalizedNumber(locale, status.retweet_count));
            favoritesCountView.setText(getLocalizedNumber(locale, status.favorite_count));

            loader.displayProfileImage(profileImageView, status.user_profile_image_url);

            final int typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
            if (typeIconRes != 0) {
                profileTypeView.setImageResource(typeIconRes);
                profileTypeView.setVisibility(View.VISIBLE);
            } else {
                profileTypeView.setImageDrawable(null);
                profileTypeView.setVisibility(View.GONE);
            }

            if (adapter.isDetailMediaExpanded()) {
                loadMedia.setVisibility(View.GONE);
                mediaPreviewGrid.setVisibility(View.VISIBLE);
                mediaPreviewGrid.removeAllViews();
                if (status.media != null) {
                    final int maxColumns = resources.getInteger(R.integer.grid_column_image_preview);
                    MediaPreviewUtils.addToLinearLayout(mediaPreviewGrid, loader, status.media,
                            maxColumns, this);
                }
            } else {
                loadMedia.setVisibility(View.VISIBLE);
                mediaPreviewGrid.setVisibility(View.GONE);
                mediaPreviewGrid.removeAllViews();
            }
//            setMenuForStatus(context, menuBar.getMenu(), status);
            menuBar.show();
        }
    }

    private class MyLinearLayoutManager extends LinearLayoutManager {

        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public int getDecoratedMeasuredHeight(View child) {
            final int height = super.getDecoratedMeasuredHeight(child);
            int heightBeforeSpace = 0;
            if (getItemViewType(child) == StatusAdapter.VIEW_TYPE_SPACE) {
                for (int i = 0, j = getChildCount(); i < j; i++) {
                    final View childToMeasure = getChildAt(i);
                    final LayoutParams paramsToMeasure = (LayoutParams) childToMeasure.getLayoutParams();
                    final int typeToMeasure = getItemViewType(childToMeasure);
                    if (typeToMeasure == StatusAdapter.VIEW_TYPE_DETAIL_STATUS || heightBeforeSpace != 0) {
                        heightBeforeSpace += super.getDecoratedMeasuredHeight(childToMeasure)
                                + paramsToMeasure.topMargin + paramsToMeasure.bottomMargin;
                    }
                    if (typeToMeasure == StatusAdapter.VIEW_TYPE_REPLIES_LOAD_INDICATOR) {
                        break;
                    }
                }
                if (heightBeforeSpace != 0) {
                    final int spaceHeight = mRecyclerView.getMeasuredHeight() - heightBeforeSpace;
                    return Math.max(0, spaceHeight);
                }
            }
            return height;
        }

    }
}
