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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.apache.http.protocol.HTTP;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter.StatusAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.loader.support.ParcelableStatusLoader;
import org.mariotaku.twidere.loader.support.StatusRepliesLoader;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccount.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.task.TwidereAsyncTask.Status;
import org.mariotaku.twidere.text.method.StatusContentMovementMethod;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.StatisticUtils;
import org.mariotaku.twidere.util.StatusLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.Utils.OnMediaClickListener;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.StatusTextView;
import org.mariotaku.twidere.view.TwitterCardContainer;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;
import edu.tsinghua.spice.Utilies.TypeMapingUtil;
import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static android.text.TextUtils.substring;
import static org.mariotaku.twidere.util.UserColorNameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showOkMessage;

/**
 * Created by mariotaku on 14/12/5.
 */
public class StatusFragment extends BaseSupportFragment
        implements LoaderCallbacks<SingleResponse<ParcelableStatus>>, OnMediaClickListener, StatusAdapterListener {

    private static final int LOADER_ID_DETAIL_STATUS = 1;
    private static final int LOADER_ID_STATUS_REPLIES = 2;
    private static final int STATE_LOADED = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_ERROR = 3;
    private RecyclerView mRecyclerView;
    private StatusAdapter mStatusAdapter;
    private boolean mRepliesLoaderInitialized;
    private LoadConversationTask mLoadConversationTask;
    private LinearLayoutManager mLayoutManager;
    private View mStatusContent;
    private View mProgressContainer;
    private View mErrorContainer;
    private DividerItemDecoration mItemDecoration;

    private LoaderCallbacks<List<ParcelableStatus>> mRepliesLoaderCallback = new LoaderCallbacks<List<ParcelableStatus>>() {
        @Override
        public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
            final long maxId = args.getLong(EXTRA_MAX_ID, -1);
            final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);

            final StatusRepliesLoader loader = new StatusRepliesLoader(getActivity(), accountId,
                    screenName, statusId, maxId, sinceId, null, null, 0, true);
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

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                final ParcelableStatus status = mStatusAdapter.getStatus();
                if (status == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
                    setUserColor(getActivity(), status.user_id, color);
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    clearUserColor(getActivity(), status.user_id);
                }
                break;
            }
            case REQUEST_SELECT_ACCOUNT: {
                final ParcelableStatus status = mStatusAdapter.getStatus();
                if (status == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final long accountId = data.getLongExtra(EXTRA_ID, -1);
                    openStatus(getActivity(), accountId, status.id);
                }
                break;
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        final boolean compact = Utils.isCompactCards(context);
        mLayoutManager = new StatusListLinearLayoutManager(context, mRecyclerView);
        mItemDecoration = new DividerItemDecoration(context, mLayoutManager.getOrientation());
        if (compact) {
            mRecyclerView.addItemDecoration(mItemDecoration);
        }
        mLayoutManager.setRecycleChildrenOnDetach(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setClipToPadding(false);
        mStatusAdapter = new StatusAdapter(this, compact);
        mStatusAdapter.setEventListener(this);
        mRecyclerView.setAdapter(mStatusAdapter);

        setState(STATE_LOADING);

        getLoaderManager().initLoader(LOADER_ID_DETAIL_STATUS, getArguments(), this);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onStatusActionClick(StatusViewHolder holder, int id, int position) {
        final ParcelableStatus status = mStatusAdapter.getStatus(position);
        if (status == null) return;
        switch (id) {
            case R.id.reply_count: {
                final Context context = getActivity();
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_REPLY);
                intent.setPackage(context.getPackageName());
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
            }
            case R.id.retweet_count: {
                RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case R.id.favorite_count: {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (twitter == null) return;
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
        }
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        openStatus(getActivity(), mStatusAdapter.getStatus(position), null);
    }

    @Override
    public void onStatusMenuClick(StatusViewHolder holder, View itemView, int position) {
        //TODO show status menu
    }

    @Override
    public Loader<SingleResponse<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
        final Bundle fragmentArgs = getArguments();
        final long accountId = fragmentArgs.getLong(EXTRA_ACCOUNT_ID, -1);
        final long statusId = fragmentArgs.getLong(EXTRA_STATUS_ID, -1);
        return new ParcelableStatusLoader(getActivity(), false, fragmentArgs, accountId, statusId);
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, long accountId) {
        final ParcelableStatus status = mStatusAdapter.getStatus();
        if (status == null) return;
        Utils.openMediaDirectly(getActivity(), accountId, media, status.media);
        //spice
        SpiceProfilingUtil.log(getActivity(),
                status.id + ",Clicked,"  + accountId + "," + status.user_id  + "," + status.text_plain.length() + "," + media.media_url + "," + TypeMapingUtil.getMediaType(media.type) + "," + status.timestamp );
        SpiceProfilingUtil.profile(getActivity(),accountId,
                status.id + ",Clicked,"  + accountId + "," + status.user_id  + "," + status.text_plain.length() + "," + media.media_url + "," + TypeMapingUtil.getMediaType(media.type) + "," + status.timestamp);
        //end
    }

    @Override
    public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mStatusContent = view.findViewById(R.id.status_content);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mErrorContainer = view.findViewById(R.id.error_retry_container);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableStatus>> loader,
                               final SingleResponse<ParcelableStatus> data) {
        if (data.hasData()) {
            final long itemId = mStatusAdapter.getItemId(mLayoutManager.findFirstVisibleItemPosition());
            final View firstChild = mLayoutManager.getChildAt(0);
            final int top = firstChild != null ? firstChild.getTop() : 0;
            final ParcelableStatus status = data.getData();
            if (mStatusAdapter.setStatus(status)) {
                mLayoutManager.scrollToPositionWithOffset(1, 0);
                mStatusAdapter.setConversation(null);
                mStatusAdapter.setReplies(null);
                loadReplies(status);
                loadConversation(status);
            } else {
                final int position = mStatusAdapter.findPositionById(itemId);
                mLayoutManager.scrollToPositionWithOffset(position, top);
            }
            try {
                StatisticUtils.writeStatusOpen(status, null, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setState(STATE_LOADED);
        } else {
            //TODO show errors
            setState(STATE_ERROR);
        }
    }

    private void loadConversation(ParcelableStatus status) {
        if (mLoadConversationTask != null && mLoadConversationTask.getStatus() == Status.RUNNING) {
            mLoadConversationTask.cancel(true);
        }
        mLoadConversationTask = new LoadConversationTask(this);
        mLoadConversationTask.executeTask(status);
    }

    private void loadReplies(ParcelableStatus status) {
        if (status == null) return;
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, status.account_id);
        args.putLong(EXTRA_STATUS_ID, status.retweet_id > 0 ? status.retweet_id : status.id);
        args.putString(EXTRA_SCREEN_NAME, status.user_screen_name);
        if (mRepliesLoaderInitialized) {
            getLoaderManager().restartLoader(LOADER_ID_STATUS_REPLIES, args, mRepliesLoaderCallback);
            return;
        }
        getLoaderManager().initLoader(LOADER_ID_STATUS_REPLIES, args, mRepliesLoaderCallback);
        mRepliesLoaderInitialized = true;
        //spice
        if (status.media != null) {
            SpiceProfilingUtil.profile(getActivity(), status.account_id,
                    status.id + ",Preview," + status.account_id + "," + status.user_id + "," + status.text_plain.length() + "," + TypeMapingUtil.getMediaType(status.media[0].type) + "," + status.timestamp );
            SpiceProfilingUtil.log(getActivity(),
                    status.id + ",Preview," + status.account_id + "," + status.user_id + "," + status.text_plain.length() + "," + TypeMapingUtil.getMediaType(status.media[0].type) + "," + status.timestamp );
        }   else {
            SpiceProfilingUtil.profile(getActivity(), status.account_id,
                    status.id + ",Words," + status.account_id + "," + status.user_id + "," + status.text_plain.length() + "," + status.timestamp );
            SpiceProfilingUtil.log(getActivity(),status.account_id + ",Words," + status.user_id + "," + status.text_plain.length() + "," + status.timestamp );
        }
        //end

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

    private void setState(int state) {
        mStatusContent.setVisibility(state == STATE_LOADED ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);
        mErrorContainer.setVisibility(state == STATE_ERROR ? View.VISIBLE : View.GONE);
    }

    private static class StatusAdapter extends Adapter<ViewHolder> implements IStatusesAdapter<List<ParcelableStatus>> {

        private static final int VIEW_TYPE_DETAIL_STATUS = 0;
        private static final int VIEW_TYPE_LIST_STATUS = 1;
        private static final int VIEW_TYPE_CONVERSATION_LOAD_INDICATOR = 2;
        private static final int VIEW_TYPE_REPLIES_LOAD_INDICATOR = 3;
        private static final int VIEW_TYPE_SPACE = 4;

        private final Context mContext;
        private final StatusFragment mFragment;
        private final LayoutInflater mInflater;
        private final ImageLoaderWrapper mImageLoader;
        private final ImageLoadingHandler mImageLoadingHandler;

        private final boolean mNameFirst, mNicknameOnly;
        private final int mCardLayoutResource;
        private final int mTextSize;
        private final int mCardBackgroundColor;
        private final boolean mIsCompact;
        private final int mProfileImageStyle;
        private final boolean mDisplayMediaPreview;

        private ParcelableStatus mStatus;
        private ParcelableCredentials mStatusAccount;
        private List<ParcelableStatus> mConversation, mReplies;
        private boolean mDetailMediaExpanded;
        private StatusAdapterListener mStatusAdapterListener;
        private DetailStatusViewHolder mCachedHolder;

        public StatusAdapter(StatusFragment fragment, boolean compact) {
            final Context context = fragment.getActivity();
            final Resources res = context.getResources();
            final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);
            mFragment = fragment;
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
            mImageLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
            mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context);
            mNameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
            mNicknameOnly = preferences.getBoolean(KEY_NICKNAME_ONLY, true);
            mTextSize = preferences.getInt(KEY_TEXT_SIZE, res.getInteger(R.integer.default_text_size));
            mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            mIsCompact = compact;
            mDisplayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
            if (compact) {
                mCardLayoutResource = R.layout.card_item_status_compact;
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

        public StatusFragment getFragment() {
            return mFragment;
        }

        public ImageLoaderWrapper getImageLoader() {
            return mImageLoader;
        }

        public Context getContext() {
            return mContext;
        }

        @Override
        public ImageLoadingHandler getImageLoadingHandler() {
            return mImageLoadingHandler;
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

        @Override
        public long getStatusId(int position) {
            final ParcelableStatus status = getStatus(position);
            return status != null ? status.hashCode() : position;
        }

        @Override
        public boolean isMediaPreviewEnabled() {
            return mDisplayMediaPreview;
        }

        @Override
        public int getProfileImageStyle() {
            return mProfileImageStyle;
        }

        @Override
        public int getMediaPreviewStyle() {
            return 0;
        }

        @Override
        public final void onStatusClick(StatusViewHolder holder, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onStatusClick(holder, position);
            }
        }

        @Override
        public void onUserProfileClick(StatusViewHolder holder, int position) {
            final Context context = getContext();
            final ParcelableStatus status = getStatus(position);
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
        public void setData(List<ParcelableStatus> data) {

        }

        @Override
        public boolean shouldShowAccountsColor() {
            return false;
        }

        @Override
        public AsyncTwitterWrapper getTwitterWrapper() {
            return mFragment.getTwitterWrapper();
        }

        public float getTextSize() {
            return mTextSize;
        }

        public ParcelableStatus getStatus() {
            return mStatus;
        }

        public ParcelableCredentials getStatusAccount() {
            return mStatusAccount;
        }

        public boolean isDetailMediaExpanded() {
            return mDetailMediaExpanded;
        }

        public void setDetailMediaExpanded(boolean expanded) {
            mDetailMediaExpanded = expanded;
            notifyDataSetChanged();
            updateItemDecoration();
        }

        private void updateItemDecoration() {
            final DividerItemDecoration decoration = mFragment.getItemDecoration();
            decoration.setDecorationStart(0);
            if (mReplies != null) {
//                decoration.setDecorationEndOffset(2);
                decoration.setDecorationEnd(getItemCount() - 2);
            } else {
//                decoration.setDecorationEndOffset(3);
                decoration.setDecorationEnd(getItemCount() - 3);
            }
            mFragment.mRecyclerView.invalidateItemDecorations();
        }


        @Override
        public boolean isGapItem(int position) {
            return false;
        }

        @Override
        public final void onGapClick(ViewHolder holder, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onGapClick((GapViewHolder) holder, position);
            }
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
            if (holder instanceof DetailStatusViewHolder) {
                mCachedHolder = (DetailStatusViewHolder) holder;
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (mCachedHolder == holder) {
                mCachedHolder = null;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_DETAIL_STATUS: {
                    if (mCachedHolder != null) return mCachedHolder;
                    final View view;
                    if (mIsCompact) {
                        view = mInflater.inflate(R.layout.header_status_compact, parent, false);
                        final View cardView = view.findViewById(R.id.compact_card);
                        cardView.setBackgroundColor(mCardBackgroundColor);
                    } else {
                        view = mInflater.inflate(R.layout.header_status, parent, false);
                        final CardView cardView = (CardView) view.findViewById(R.id.card);
                        cardView.setCardBackgroundColor(mCardBackgroundColor);
                    }
                    return new DetailStatusViewHolder(this, view);
                }
                case VIEW_TYPE_LIST_STATUS: {
                    final View view = mInflater.inflate(mCardLayoutResource, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    if (cardView != null) {
                        cardView.setCardBackgroundColor(mCardBackgroundColor);
                    }
                    final StatusViewHolder holder = new StatusViewHolder(this, view);
                    holder.setOnClickListeners();
                    return holder;
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
                    // Display 'in reply to' for first item
                    // useful to indicate whether first tweet has reply or not
                    statusHolder.displayStatus(status, position == 0);
                    break;
                }
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
        public int getItemCount() {
            return getStatusCount();
        }

        @Override
        public void onItemActionClick(ViewHolder holder, int id, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onStatusActionClick((StatusViewHolder) holder, id, position);
            }
        }

        @Override
        public void onItemMenuClick(ViewHolder holder, View itemView, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onStatusMenuClick((StatusViewHolder) holder, itemView, position);
            }
        }

        public void setConversation(List<ParcelableStatus> conversation) {
            mConversation = conversation;
            notifyDataSetChanged();
            updateItemDecoration();
        }

        public void setEventListener(StatusAdapterListener listener) {
            mStatusAdapterListener = listener;
        }

        public void setReplies(List<ParcelableStatus> replies) {
            mReplies = replies;
            notifyDataSetChanged();
            updateItemDecoration();
        }

        public boolean setStatus(ParcelableStatus status) {
            final ParcelableStatus old = mStatus;
            mStatus = status;
            if (status != null) {
                mStatusAccount = ParcelableAccount.getCredentials(mContext, status.account_id);
            } else {
                mStatusAccount = null;
            }
            notifyDataSetChanged();
            updateItemDecoration();
            return !CompareUtils.objectEquals(old, status);
        }

        private int getConversationCount() {
            return mConversation != null ? mConversation.size() : 1;
        }

        private int getRepliesCount() {
            return mReplies != null ? mReplies.size() : 1;
        }
    }

    private DividerItemDecoration getItemDecoration() {
        return mItemDecoration;
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {

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
        protected void onPostExecute(final ListResponse<ParcelableStatus> data) {
            if (data.hasData()) {
                fragment.setConversation(data.getData());
            } else {
                showErrorMessage(context, context.getString(R.string.action_getting_status), data.getException(), true);
            }
        }

        @Override
        protected void onProgressUpdate(ParcelableStatus... values) {
            super.onProgressUpdate(values);
        }

    }

    private static class SpaceViewHolder extends ViewHolder {

        public SpaceViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DetailStatusViewHolder extends ViewHolder implements OnClickListener,
            ActionMenuView.OnMenuItemClickListener {

        private final StatusAdapter adapter;

        private final CardView cardView;

        private final ActionMenuView menuBar;
        private final TextView nameView, screenNameView;
        private final StatusTextView textView;
        private final ShapedImageView profileImageView;
        private final ImageView profileTypeView;
        private final TextView timeSourceView;
        private final TextView retweetedByView;
        private final View repliesContainer, retweetsContainer, favoritesContainer;
        private final TextView repliesCountView, retweetsCountView, favoritesCountView;

        private final View profileContainer;
        private final View retweetedByContainer;
        private final View mediaPreviewContainer;
        private final View mediaPreviewLoad;
        private final LinearLayout mediaPreviewGrid;

        private final View locationContainer;
        private final TwitterCardContainer twitterCard;

        public DetailStatusViewHolder(StatusAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            cardView = (CardView) itemView.findViewById(R.id.card);
            menuBar = (ActionMenuView) itemView.findViewById(R.id.menu_bar);
            nameView = (TextView) itemView.findViewById(R.id.name);
            screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
            textView = (StatusTextView) itemView.findViewById(R.id.text);
            profileImageView = (ShapedImageView) itemView.findViewById(R.id.profile_image);
            profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
            timeSourceView = (TextView) itemView.findViewById(R.id.time_source);
            retweetedByView = (TextView) itemView.findViewById(R.id.retweeted_by);
            retweetedByContainer = itemView.findViewById(R.id.retweeted_by_container);
            repliesContainer = itemView.findViewById(R.id.replies_container);
            retweetsContainer = itemView.findViewById(R.id.retweets_container);
            favoritesContainer = itemView.findViewById(R.id.favorites_container);
            repliesCountView = (TextView) itemView.findViewById(R.id.replies_count);
            retweetsCountView = (TextView) itemView.findViewById(R.id.retweets_count);
            favoritesCountView = (TextView) itemView.findViewById(R.id.favorites_count);
            mediaPreviewContainer = itemView.findViewById(R.id.media_preview);
            mediaPreviewLoad = itemView.findViewById(R.id.media_preview_load);
            mediaPreviewGrid = (LinearLayout) itemView.findViewById(R.id.media_preview_grid);
            locationContainer = itemView.findViewById(R.id.location_container);
            profileContainer = itemView.findViewById(R.id.profile_container);
            twitterCard = (TwitterCardContainer) itemView.findViewById(R.id.twitter_card);

            setIsRecyclable(false);
            initViews();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.media_preview_load: {
                    adapter.setDetailMediaExpanded(true);
                    break;
                }
                case R.id.profile_container: {
                    final ParcelableStatus status = adapter.getStatus(getPosition());
                    final Fragment fragment = adapter.getFragment();
                    final FragmentActivity activity = fragment.getActivity();
                    final Bundle activityOption = Utils.makeSceneTransitionOption(activity,
                            new Pair<View, String>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                            new Pair<View, String>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
                    openUserProfile(activity, status.account_id, status.user_id, status.user_screen_name,
                            activityOption);
                    break;
                }
                case R.id.retweets_container: {
                    final ParcelableStatus status = adapter.getStatus(getPosition());
                    final Fragment fragment = adapter.getFragment();
                    final FragmentActivity activity = fragment.getActivity();
                    Utils.openStatusRetweeters(activity, status.account_id, status.id);
                    break;
                }
                case R.id.retweeted_by_container: {
                    final ParcelableStatus status = adapter.getStatus(getPosition());
                    if (status.retweet_id > 0) {
                        Utils.openUserProfile(adapter.getContext(), status.account_id, status.user_id,
                                status.user_screen_name, null);
                    }
                    break;
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final StatusFragment fragment = adapter.getFragment();
            final ParcelableStatus status = adapter.getStatus(getPosition());
            if (status == null || fragment == null) return false;
            final AsyncTwitterWrapper twitter = fragment.getTwitterWrapper();
            final FragmentActivity activity = fragment.getActivity();
            switch (item.getItemId()) {
                case MENU_COPY: {
                    if (ClipboardUtils.setText(activity, status.text_plain)) {
                        showOkMessage(activity, R.string.text_copied, false);
                    }
                    break;
                }
                case MENU_RETWEET: {
                    if (isMyRetweet(status)) {
                        twitter.cancelRetweetAsync(status.account_id, status.id, status.my_retweet_id);
                    } else {
                        twitter.retweetStatusAsync(status.account_id, status.id);
                    }
                    break;
                }
                case MENU_QUOTE: {
                    final Intent intent = new Intent(INTENT_ACTION_QUOTE);
                    intent.putExtra(EXTRA_STATUS, status);
                    fragment.startActivity(intent);
                    break;
                }
                case MENU_REPLY: {
                    final Intent intent = new Intent(INTENT_ACTION_REPLY);
                    intent.putExtra(EXTRA_STATUS, status);
                    fragment.startActivity(intent);
                    break;
                }
                case MENU_FAVORITE: {
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_id, status.id);
                        //spice
                        SpiceProfilingUtil.profile(adapter.getContext(),
                                status.account_id, status.id + ",Unfavor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                        SpiceProfilingUtil.log(adapter.getContext(),status.id + ",Unfavor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                        //end
                    } else {
                        twitter.createFavoriteAsync(status.account_id, status.id);
                        //spice
                        SpiceProfilingUtil.profile(adapter.getContext(),
                                status.account_id, status.id + ",Favor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                        SpiceProfilingUtil.log(adapter.getContext(),status.id + ",Favor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                        //end
                    }
                    break;
                }
                case MENU_DELETE: {
                    DestroyStatusDialogFragment.show(fragment.getFragmentManager(), status);
                    break;
                }
                case MENU_ADD_TO_FILTER: {
                    AddStatusFilterDialogFragment.show(fragment.getFragmentManager(), status);
                    break;
                }
                case MENU_SET_COLOR: {
                    final Intent intent = new Intent(activity, ColorPickerDialogActivity.class);
                    final int color = getUserColor(activity, status.user_id, true);
                    if (color != 0) {
                        intent.putExtra(EXTRA_COLOR, color);
                    }
                    intent.putExtra(EXTRA_CLEAR_BUTTON, color != 0);
                    intent.putExtra(EXTRA_ALPHA_SLIDER, false);
                    fragment.startActivityForResult(intent, REQUEST_SET_COLOR);
                    break;
                }
                case MENU_CLEAR_NICKNAME: {
                    clearUserNickname(activity, status.user_id);
                    adapter.notifyDataSetChanged();
                    break;
                }
                case MENU_SET_NICKNAME: {
                    final String nick = getUserNickname(activity, status.user_id, true);
                    SetUserNicknameDialogFragment.show(fragment.getFragmentManager(), status.user_id, nick);
                    break;
                }
                case MENU_TRANSLATE: {
                    final ParcelableCredentials account
                            = ParcelableAccount.getCredentials(activity, status.account_id);
                    if (Utils.isOfficialCredentials(activity, account)) {
                        StatusTranslateDialogFragment.show(fragment.getFragmentManager(), status);
                    } else {
                        final Resources resources = fragment.getResources();
                        final Locale locale = resources.getConfiguration().locale;
                        try {
                            final String template = "http://translate.google.com/#%s|%s|%s";
                            final String sourceLang = "auto";
                            final String targetLang = URLEncoder.encode(locale.getLanguage(), HTTP.UTF_8);
                            final String text = URLEncoder.encode(status.text_unescaped, HTTP.UTF_8);
                            final Uri uri = Uri.parse(String.format(Locale.ROOT, template, sourceLang, targetLang, text));
                            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            fragment.startActivity(intent);
                        } catch (UnsupportedEncodingException ignore) {

                        }
                    }
                    break;
                }
                case MENU_OPEN_WITH_ACCOUNT: {
                    final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
                    intent.setClass(activity, AccountSelectorActivity.class);
                    intent.putExtra(EXTRA_SINGLE_SELECTION, true);
                    fragment.startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
                    break;
                }
                default: {
                    if (item.getIntent() != null) {
                        try {
                            fragment.startActivity(item.getIntent());
                        } catch (final ActivityNotFoundException e) {
                            Log.w(LOGTAG, e);
                            return false;
                        }
                    }
                    break;
                }
            }
            return true;
        }

        public void showStatus(ParcelableStatus status) {
            if (status == null) return;
            final StatusFragment fragment = adapter.getFragment();
            final Context context = adapter.getContext();
            final Resources resources = context.getResources();
            final ImageLoaderWrapper loader = adapter.getImageLoader();
            final boolean nameFirst = adapter.isNameFirst();
            final boolean nicknameOnly = adapter.isNicknameOnly();

            if (status.retweet_id > 0) {
                final String retweetedBy = UserColorNameUtils.getDisplayName(context, status.retweeted_by_id,
                        status.retweeted_by_name, status.retweeted_by_screen_name, nameFirst, nicknameOnly);
                retweetedByView.setText(context.getString(R.string.name_retweeted, retweetedBy));
                retweetedByContainer.setVisibility(View.VISIBLE);
            } else {
                retweetedByView.setText(null);
                retweetedByContainer.setVisibility(View.GONE);
            }

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
            final StatusLinkClickHandler linkClickHandler = new StatusLinkClickHandler(context, null);
            linkClickHandler.setStatus(status);
            final TwidereLinkify linkify = new TwidereLinkify(linkClickHandler);
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

            if (status.media == null) {
                mediaPreviewContainer.setVisibility(View.GONE);
            } else if (adapter.isDetailMediaExpanded()) {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreviewLoad.setVisibility(View.GONE);
                mediaPreviewGrid.setVisibility(View.VISIBLE);
                mediaPreviewGrid.removeAllViews();
                final int maxColumns = resources.getInteger(R.integer.grid_column_image_preview);
                Utils.addToLinearLayout(mediaPreviewGrid, loader, status.media, status.account_id,
                        maxColumns, adapter.getFragment());
            } else {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreviewLoad.setVisibility(View.VISIBLE);
                mediaPreviewGrid.setVisibility(View.GONE);
                mediaPreviewGrid.removeAllViews();
            }

            if (TwitterCardUtils.isCardSupported(status.card)) {
                final Point size = TwitterCardUtils.getCardSize(status.card);
                twitterCard.setVisibility(View.VISIBLE);
                if (size != null) {
                    twitterCard.setCardSize(size.x, size.y);
                } else {
                    twitterCard.setCardSize(0, 0);
                }
                final Fragment cardFragment = TwitterCardUtils.createCardFragment(status.card);
                final FragmentManager fm = fragment.getChildFragmentManager();
                final FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.twitter_card, cardFragment);
                ft.commit();
            } else {
                twitterCard.setVisibility(View.GONE);
                final FragmentManager fm = fragment.getChildFragmentManager();
//                final FragmentTransaction ft = fm.beginTransaction();
            }

            Utils.setMenuForStatus(context, menuBar.getMenu(), status, adapter.getStatusAccount());
        }

        private void initViews() {
//            menuBar.setOnMenuItemClickListener(this);
            menuBar.setOnMenuItemClickListener(this);
            final FragmentActivity activity = adapter.getFragment().getActivity();
            final MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.menu_status, menuBar.getMenu());
            ThemeUtils.wrapMenuIcon(menuBar, MENU_GROUP_STATUS_SHARE);
            mediaPreviewLoad.setOnClickListener(this);
            profileContainer.setOnClickListener(this);
            retweetsContainer.setOnClickListener(this);
            retweetedByContainer.setOnClickListener(this);

            final float defaultTextSize = adapter.getTextSize();
            nameView.setTextSize(defaultTextSize * 1.25f);
            textView.setTextSize(defaultTextSize * 1.25f);
            screenNameView.setTextSize(defaultTextSize * 0.85f);
            timeSourceView.setTextSize(defaultTextSize * 0.85f);
        }


    }

    private static class StatusListLinearLayoutManager extends LinearLayoutManager {

        private final RecyclerView recyclerView;

        public StatusListLinearLayoutManager(Context context, RecyclerView recyclerView) {
            super(context);
            setOrientation(LinearLayoutManager.VERTICAL);
            this.recyclerView = recyclerView;
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
                    final int spaceHeight = recyclerView.getMeasuredHeight() - heightBeforeSpace;
                    return Math.max(0, spaceHeight);
                }
            }
            return height;
        }

        @Override
        public void setOrientation(int orientation) {
            if (orientation != VERTICAL)
                throw new IllegalArgumentException("Only VERTICAL orientation supported");
            super.setOrientation(orientation);
        }

    }

}
