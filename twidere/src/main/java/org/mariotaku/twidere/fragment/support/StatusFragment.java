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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
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
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.text.method.StatusContentMovementMethod;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler;
import org.mariotaku.twidere.util.StatusLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.Utils.OnMediaClickListener;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.StatusTextView;
import org.mariotaku.twidere.view.TwitterCardContainer;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;
import edu.tsinghua.spice.Utilies.TypeMappingUtil;
import twitter4j.TwitterException;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.UserColorNameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;

/**
 * Created by mariotaku on 14/12/5.
 */
public class StatusFragment extends BaseSupportFragment implements LoaderCallbacks<SingleResponse<ParcelableStatus>>,
        OnMediaClickListener, StatusAdapterListener {

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
            final Pair<Long, Integer> readPosition = saveReadPosition();
            setReplies(data);
            restoreReadPosition(readPosition);
        }

        @Override
        public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {

        }
    };

    private void restoreReadPosition(@Nullable Pair<Long, Integer> position) {
        if (position == null) return;
        final int adapterPosition = mStatusAdapter.findPositionById(position.first);
        if (adapterPosition == RecyclerView.NO_POSITION) return;
        mLayoutManager.scrollToPositionWithOffset(adapterPosition, position.second);
    }

    @Nullable
    private Pair<Long, Integer> saveReadPosition() {
        final int position = mLayoutManager.findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) return null;
        long itemId = mStatusAdapter.getItemId(position);
        final View positionView;
        if (itemId == StatusAdapter.VIEW_TYPE_CONVERSATION_LOAD_INDICATOR) {
            // Should be next item
            positionView = mLayoutManager.findViewByPosition(position + 1);
            itemId = mStatusAdapter.getItemId(position + 1);
        } else {
            positionView = mLayoutManager.findViewByPosition(position);
        }
        return new Pair<>(itemId, positionView != null ? positionView.getTop() : -1);
    }

    private PopupMenu mPopupMenu;
    private ParcelableStatus mSelectedStatus;
    private OnMenuItemClickListener mOnStatusMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final ParcelableStatus status = mSelectedStatus;
            if (status == null) return false;
            return Utils.handleMenuItemClick(getActivity(), StatusFragment.this,
                    getFragmentManager(), getTwitterWrapper(), status, item);
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
        Utils.setNdefPushMessageCallback(getActivity(), new CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableStatus status = getStatus();
                if (status == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(LinkCreator.getTwitterStatusLink(status.user_screen_name, status.id)),
                });
            }
        });
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
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position) {
        final ParcelableStatus status = mStatusAdapter.getStatus(position);
        if (status == null) return;
        Utils.openMedia(getActivity(), status, media);
        SpiceProfilingUtil.log(getActivity(),
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.preview_url + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mStatusAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
        SpiceProfilingUtil.profile(getActivity(), status.account_id,
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.preview_url + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mStatusAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
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
    public boolean onStatusLongClick(StatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onStatusMenuClick(StatusViewHolder holder, View menuView, int position) {
        //TODO show status menu
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        final PopupMenu popupMenu = new PopupMenu(mStatusAdapter.getContext(), menuView,
                Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
        popupMenu.setOnMenuItemClickListener(mOnStatusMenuItemClickListener);
        popupMenu.inflate(R.menu.action_status);
        final ParcelableStatus status = mStatusAdapter.getStatus(position);
        setMenuForStatus(mStatusAdapter.getContext(), popupMenu.getMenu(), status);
        popupMenu.show();
        mPopupMenu = popupMenu;
        mSelectedStatus = status;
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
        Utils.openMediaDirectly(getActivity(), accountId, status, media, status.media);
        //spice
        SpiceProfilingUtil.log(getActivity(),
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.preview_url + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mStatusAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
        SpiceProfilingUtil.profile(getActivity(), status.account_id,
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.preview_url + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mStatusAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
        //end
    }

    private void addConversation(ParcelableStatus status, int position) {
        mStatusAdapter.addConversation(status, position);
    }

    private DividerItemDecoration getItemDecoration() {
        return mItemDecoration;
    }

    private ParcelableStatus getStatus() {
        return mStatusAdapter.getStatus();
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
            setState(STATE_LOADED);
        } else {
            //TODO show errors
            setState(STATE_ERROR);
        }
    }

    private void loadConversation(ParcelableStatus status) {
        if (AsyncTaskUtils.isTaskRunning(mLoadConversationTask)) {
            mLoadConversationTask.cancel(true);
        }
        mLoadConversationTask = new LoadConversationTask(this);
        AsyncTaskUtils.executeTask(mLoadConversationTask, status);
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
        if (status.media == null) {
            SpiceProfilingUtil.profile(getActivity(), status.account_id,
                    status.id + ",Words," + status.account_id + "," + status.user_id + "," + status.reply_count
                            + "," + status.retweet_count + "," + status.favorite_count
                            + "," + status.text_plain.length() + "," + status.timestamp);
            SpiceProfilingUtil.log(getActivity(), status.id + ",Words," + status.account_id + "," + status.user_id
                    + "," + status.reply_count + "," + status.retweet_count + "," + status.favorite_count
                    + "," + status.text_plain.length() + "," + status.timestamp);
        } else {
            for (final ParcelableMedia spiceMedia : status.media) {
                if (spiceMedia.type == ParcelableMedia.TYPE_IMAGE) {
                    SpiceProfilingUtil.profile(getActivity(), status.account_id,
                            status.id + ",PreviewM," + status.account_id + "," + status.user_id
                                    + "," + status.reply_count + "," + status.retweet_count + "," + status.favorite_count
                                    + "," + status.text_plain.length() + "," + TypeMappingUtil.getMediaType(spiceMedia.type)
                                    + "," + spiceMedia.media_url + "," + spiceMedia.width + "x" + spiceMedia.height
                                    + "," + status.timestamp);
                    SpiceProfilingUtil.log(getActivity(),
                            status.id + ",PreviewM," + status.account_id + "," + status.user_id
                                    + "," + status.reply_count + "," + status.retweet_count + "," + status.favorite_count
                                    + "," + status.text_plain.length() + "," + TypeMappingUtil.getMediaType(spiceMedia.type)
                                    + "," + spiceMedia.media_url + "," + spiceMedia.width + "x" + spiceMedia.height
                                    + "," + status.timestamp);
                } else {
                    SpiceProfilingUtil.profile(getActivity(), status.account_id,
                            status.id + ",PreviewO," + status.account_id + "," + status.user_id
                                    + "," + status.reply_count + "," + status.retweet_count + "," + status.favorite_count
                                    + "," + status.text_plain.length() + "," + TypeMappingUtil.getMediaType(spiceMedia.type)
                                    + "," + spiceMedia.preview_url + "," + spiceMedia.media_url + "," + status.timestamp);
                    SpiceProfilingUtil.log(getActivity(),
                            status.id + ",PreviewO," + status.account_id + "," + status.user_id
                                    + "," + status.reply_count + "," + status.retweet_count + "," + status.favorite_count
                                    + "," + status.text_plain.length() + "," + TypeMappingUtil.getMediaType(spiceMedia.type)
                                    + "," + spiceMedia.preview_url + "," + spiceMedia.media_url + "," + status.timestamp);
                }
            }
        }
        //end
    }

    private void setConversation(List<ParcelableStatus> data) {
        final Pair<Long, Integer> readPosition = saveReadPosition();
        mStatusAdapter.setConversation(data);
        restoreReadPosition(readPosition);
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

    private static class DetailStatusViewHolder extends ViewHolder implements OnClickListener,
            ActionMenuView.OnMenuItemClickListener {

        private final StatusAdapter adapter;

        private final CardView cardView;

        private final ActionMenuView menuBar;
        private final TextView nameView, screenNameView;
        private final StatusTextView textView;
        private final TextView quoteTextView;
        private final TextView quotedNameView, quotedScreenNameView;
        private final ShapedImageView profileImageView;
        private final ImageView profileTypeView;
        private final TextView timeSourceView;
        private final TextView retweetedByView;
        private final View repliesContainer, retweetsContainer, favoritesContainer;
        private final TextView repliesCountView, retweetsCountView, favoritesCountView;

        private final ColorLabelRelativeLayout profileContainer;
        private final View retweetedByContainer;
        private final View mediaPreviewContainer;
        private final View mediaPreviewLoad;
        private final CardMediaContainer mediaPreview;

        private final View quotedNameContainer;
        private final ForegroundColorView quoteIndicator;

        private final TextView locationView;
        private final TwitterCardContainer twitterCard;
        private final StatusLinkClickHandler linkClickHandler;
        private final TwidereLinkify linkify;

        public DetailStatusViewHolder(StatusAdapter adapter, View itemView) {
            super(itemView);
            this.linkClickHandler = new StatusLinkClickHandler(adapter.getContext(), null);
            this.linkify = new TwidereLinkify(linkClickHandler, false);
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
            mediaPreviewContainer = itemView.findViewById(R.id.media_preview_container);
            mediaPreviewLoad = itemView.findViewById(R.id.media_preview_load);
            mediaPreview = (CardMediaContainer) itemView.findViewById(R.id.media_preview);
            locationView = (TextView) itemView.findViewById(R.id.location_view);
            profileContainer = (ColorLabelRelativeLayout) itemView.findViewById(R.id.profile_container);
            twitterCard = (TwitterCardContainer) itemView.findViewById(R.id.twitter_card);

            quoteTextView = (TextView) itemView.findViewById(R.id.quote_text);
            quotedNameView = (TextView) itemView.findViewById(R.id.quoted_name);
            quotedScreenNameView = (TextView) itemView.findViewById(R.id.quoted_screen_name);
            quotedNameContainer = itemView.findViewById(R.id.quoted_name_container);
            quoteIndicator = (ForegroundColorView) itemView.findViewById(R.id.quote_indicator);

            setIsRecyclable(false);
            initViews();
        }

        public void displayStatus(ParcelableStatus status) {
            if (status == null) return;
            final StatusFragment fragment = adapter.getFragment();
            final Context context = adapter.getContext();
            final Resources resources = context.getResources();
            final MediaLoaderWrapper loader = adapter.getMediaLoader();
            final boolean nameFirst = adapter.isNameFirst();

            linkClickHandler.setStatus(status);

            if (status.retweet_id > 0) {
                final String retweetedBy = UserColorNameUtils.getDisplayName(context, status.retweeted_by_id,
                        status.retweeted_by_name, status.retweeted_by_screen_name, nameFirst);
                retweetedByView.setText(context.getString(R.string.name_retweeted, retweetedBy));
                retweetedByContainer.setVisibility(View.VISIBLE);
            } else {
                retweetedByView.setText(null);
                retweetedByContainer.setVisibility(View.GONE);
            }

            profileContainer.drawEnd(Utils.getAccountColor(context, status.account_id));

            final int typeIconRes, typeDescriptionRes;
            final long timestamp;
            final String source;
            if (status.is_quote) {
                quotedNameView.setText(getUserNickname(context, status.user_id, status.user_name, true));
                quotedScreenNameView.setText("@" + status.user_screen_name);

                nameView.setText(getUserNickname(context, status.quoted_by_user_id, status.quoted_by_user_name, true));
                screenNameView.setText("@" + status.quoted_by_user_screen_name);

                final int idx = status.quote_text_unescaped.lastIndexOf(" twitter.com");
                final Spanned quote_text = Html.fromHtml(status.quote_text_html);
                quoteTextView.setText(idx > 0 ? quote_text.subSequence(0, idx) : quote_text);
                linkify.applyAllLinks(quoteTextView, status.account_id, getLayoutPosition(),
                        status.is_possibly_sensitive, adapter.getLinkHighlightingStyle());

                loader.displayProfileImage(profileImageView, status.quoted_by_user_profile_image);

                quotedNameContainer.setVisibility(View.VISIBLE);
                quoteTextView.setVisibility(View.VISIBLE);
                quoteIndicator.setVisibility(View.VISIBLE);

                quoteIndicator.setColor(UserColorNameUtils.getUserColor(context, status.user_id));

                profileContainer.drawStart(UserColorNameUtils.getUserColor(context, status.quoted_by_user_id));

                typeIconRes = getUserTypeIconRes(status.quoted_by_user_is_verified, status.quoted_by_user_is_protected);
                typeDescriptionRes = Utils.getUserTypeDescriptionRes(status.quoted_by_user_is_verified, status.quoted_by_user_is_protected);

                timestamp = status.quote_timestamp;
                source = status.quote_source;

            } else {
                nameView.setText(getUserNickname(context, status.user_id, status.user_name, true));
                screenNameView.setText("@" + status.user_screen_name);

                loader.displayProfileImage(profileImageView, status.user_profile_image_url);

                quotedNameContainer.setVisibility(View.GONE);
                quoteTextView.setVisibility(View.GONE);
                quoteIndicator.setVisibility(View.GONE);

                profileContainer.drawStart(UserColorNameUtils.getUserColor(context, status.user_id));

                typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
                typeDescriptionRes = Utils.getUserTypeDescriptionRes(status.user_is_verified, status.user_is_protected);

                timestamp = status.timestamp;
                source = status.source;
            }

            if (typeIconRes != 0 && typeDescriptionRes != 0) {
                profileTypeView.setImageResource(typeIconRes);
                profileTypeView.setContentDescription(context.getString(typeDescriptionRes));
                profileTypeView.setVisibility(View.VISIBLE);
            } else {
                profileTypeView.setImageDrawable(null);
                profileTypeView.setContentDescription(null);
                profileTypeView.setVisibility(View.GONE);
            }

            final String timeString = formatToLongTimeString(context, timestamp);
            if (!isEmpty(timeString) && !isEmpty(source)) {
                timeSourceView.setText(Html.fromHtml(context.getString(R.string.time_source, timeString, source)));
            } else if (isEmpty(timeString) && !isEmpty(source)) {
                timeSourceView.setText(Html.fromHtml(context.getString(R.string.source, source)));
            } else if (!isEmpty(timeString) && isEmpty(source)) {
                timeSourceView.setText(timeString);
            }
            timeSourceView.setMovementMethod(null);


            textView.setText(Html.fromHtml(status.text_html));
            linkify.applyAllLinks(textView, status.account_id, getAdapterPosition(), status.is_possibly_sensitive);
            ThemeUtils.applyParagraphSpacing(textView, 1.1f);

            if (!TextUtils.isEmpty(status.place_full_name)) {
                locationView.setVisibility(View.VISIBLE);
                locationView.setText(status.place_full_name);
                locationView.setClickable(ParcelableLocation.isValidLocation(status.location));
            } else if (ParcelableLocation.isValidLocation(status.location)) {
                locationView.setVisibility(View.VISIBLE);
                locationView.setText(R.string.view_map);
                locationView.setClickable(true);
            } else {
                locationView.setVisibility(View.GONE);
                locationView.setText(null);
            }

            retweetsContainer.setVisibility(!status.user_is_protected ? View.VISIBLE : View.GONE);
            repliesContainer.setVisibility(status.reply_count < 0 ? View.GONE : View.VISIBLE);
            final Locale locale = context.getResources().getConfiguration().locale;
            repliesCountView.setText(getLocalizedNumber(locale, status.reply_count));
            retweetsCountView.setText(getLocalizedNumber(locale, status.retweet_count));
            favoritesCountView.setText(getLocalizedNumber(locale, status.favorite_count));


            if (status.media == null) {
                mediaPreviewContainer.setVisibility(View.GONE);
                mediaPreview.setVisibility(View.GONE);
                mediaPreviewLoad.setVisibility(View.GONE);
                mediaPreview.displayMedia();
            } else if (adapter.isDetailMediaExpanded()) {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreview.setVisibility(View.VISIBLE);
                mediaPreviewLoad.setVisibility(View.GONE);
                mediaPreview.displayMedia(status.media, loader, status.account_id,
                        adapter.getFragment(), adapter.getMediaLoadingHandler());
            } else {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreview.setVisibility(View.GONE);
                mediaPreviewLoad.setVisibility(View.VISIBLE);
                mediaPreview.displayMedia();
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
                if (cardFragment != null) {
                    final FragmentManager fm = fragment.getChildFragmentManager();
                    final FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.twitter_card, cardFragment);
                    ft.commit();
                } else {
                    twitterCard.setVisibility(View.GONE);
                }
            } else {
                twitterCard.setVisibility(View.GONE);
            }

            Utils.setMenuForStatus(context, menuBar.getMenu(), status, adapter.getStatusAccount());
        }

        @Override
        public void onClick(View v) {
            final ParcelableStatus status = adapter.getStatus(getAdapterPosition());
            final StatusFragment fragment = adapter.getFragment();
            switch (v.getId()) {
                case R.id.media_preview_load: {
                    if (adapter.isSensitiveContentEnabled() || !status.is_possibly_sensitive) {
                        adapter.setDetailMediaExpanded(true);
                    } else {
                        final LoadSensitiveImageConfirmDialogFragment f = new LoadSensitiveImageConfirmDialogFragment();
                        f.show(fragment.getChildFragmentManager(), "load_sensitive_image_confirm");
                    }
                    break;
                }
                case R.id.profile_container: {
                    final FragmentActivity activity = fragment.getActivity();
                    final Bundle activityOption = Utils.makeSceneTransitionOption(activity,
                            new Pair<View, String>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                            new Pair<View, String>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
                    openUserProfile(activity, status.account_id, status.user_id, status.user_screen_name,
                            activityOption);
                    break;
                }
                case R.id.retweets_container: {
                    final FragmentActivity activity = fragment.getActivity();
                    Utils.openStatusRetweeters(activity, status.account_id, status.id);
                    break;
                }
                case R.id.retweeted_by_container: {
                    if (status.retweet_id > 0) {
                        Utils.openUserProfile(adapter.getContext(), status.account_id, status.retweeted_by_id,
                                status.retweeted_by_screen_name, null);
                    }
                    break;
                }
                case R.id.location_view: {
                    final ParcelableLocation location = status.location;
                    if (!ParcelableLocation.isValidLocation(location)) return;
                    Utils.openMap(adapter.getContext(), location.latitude, location.longitude);
                    break;
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final StatusFragment fragment = adapter.getFragment();
            final ParcelableStatus status = adapter.getStatus(getAdapterPosition());
            if (status == null || fragment == null) return false;
            final AsyncTwitterWrapper twitter = fragment.getTwitterWrapper();
            final FragmentActivity activity = fragment.getActivity();
            final FragmentManager fm = fragment.getFragmentManager();
            if (item.getItemId() == MENU_RETWEET) {
                RetweetQuoteDialogFragment.show(fm, status);
                return true;
            }
            return Utils.handleMenuItemClick(activity, fragment, fm, twitter, status, item);
        }

        private void initViews() {
//            menuBar.setOnMenuItemClickListener(this);
            menuBar.setOnMenuItemClickListener(this);
            final StatusFragment fragment = adapter.getFragment();
            final FragmentActivity activity = fragment.getActivity();
            final MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.menu_status, menuBar.getMenu());
            ThemeUtils.wrapMenuIcon(menuBar, MENU_GROUP_STATUS_SHARE);
            mediaPreviewLoad.setOnClickListener(this);
            profileContainer.setOnClickListener(this);
            retweetsContainer.setOnClickListener(this);
            retweetedByContainer.setOnClickListener(this);
            locationView.setOnClickListener(this);

            final float defaultTextSize = adapter.getTextSize();
            nameView.setTextSize(defaultTextSize * 1.25f);
            quotedNameView.setTextSize(defaultTextSize * 1.25f);
            textView.setTextSize(defaultTextSize * 1.25f);
            quoteTextView.setTextSize(defaultTextSize * 1.25f);
            screenNameView.setTextSize(defaultTextSize * 0.85f);
            quotedScreenNameView.setTextSize(defaultTextSize * 0.85f);
            locationView.setTextSize(defaultTextSize * 0.85f);
            timeSourceView.setTextSize(defaultTextSize * 0.85f);

            mediaPreview.setStyle(adapter.getMediaPreviewStyle());

            textView.setMovementMethod(StatusContentMovementMethod.getInstance());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                quoteTextView.setCustomSelectionActionModeCallback(new StatusActionModeCallback(quoteTextView, fragment, activity));
                textView.setCustomSelectionActionModeCallback(new StatusActionModeCallback(textView, fragment, activity));
//            }
        }


        private static class StatusActionModeCallback implements Callback {
            private final TextView textView;
            private final StatusFragment fragment;
            private final FragmentActivity activity;

            public StatusActionModeCallback(TextView textView, StatusFragment fragment, FragmentActivity activity) {
                this.textView = textView;
                this.fragment = fragment;
                this.activity = activity;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                final FragmentActivity activity = fragment.getActivity();
                if (activity instanceof IThemedActivity) {
                    final int themeRes = ((IThemedActivity) activity).getCurrentThemeResourceId();
                    final int accentColor = ((IThemedActivity) activity).getCurrentThemeColor();
                    ThemeUtils.applySupportActionModeBackground(mode, fragment.getActivity(), themeRes, accentColor, true);
                }
                mode.getMenuInflater().inflate(R.menu.action_status_text_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                final int start = textView.getSelectionStart(), end = textView.getSelectionEnd();
                final SpannableString string = SpannableString.valueOf(textView.getText());
                final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
                final boolean avail = spans.length == 1 && URLUtil.isValidUrl(spans[0].getURL());
                MenuUtils.setMenuItemAvailability(menu, android.R.id.copyUrl, avail);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.copyUrl: {
                        final int start = textView.getSelectionStart(), end = textView.getSelectionEnd();
                        final SpannableString string = SpannableString.valueOf(textView.getText());
                        final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
                        if (spans.length != 1) return true;
                        ClipboardUtils.setText(activity, spans[0].getURL());
                        mode.finish();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        }

    }

    public static final class LoadSensitiveImageConfirmDialogFragment extends BaseSupportDialogFragment implements
            DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Fragment f = getParentFragment();
                    if (f instanceof StatusFragment) {
                        final StatusAdapter adapter = ((StatusFragment) f).getAdapter();
                        adapter.setDetailMediaExpanded(true);
                    }
                    break;
                }
            }

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(R.string.sensitive_content_warning);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }
    }

    private StatusAdapter getAdapter() {
        return mStatusAdapter;
    }

    static class LoadConversationTask extends AsyncTask<ParcelableStatus, ParcelableStatus,
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
        protected void onPostExecute(final ListResponse<ParcelableStatus> data) {
            if (data.hasData()) {
                fragment.setConversation(data.getData());
            } else {
                showErrorMessage(context, context.getString(R.string.action_getting_status), data.getException(), true);
            }
        }

        @Override
        protected void onProgressUpdate(ParcelableStatus... values) {
            for (ParcelableStatus status : values) {
//                fragment.addConversation(status, 0);
            }
        }

        @Override
        protected void onCancelled() {
        }

    }

    private static class SpaceViewHolder extends ViewHolder {

        public SpaceViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {

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
        private final MediaLoaderWrapper mImageLoader;
        private final MediaLoadingHandler mMediaLoadingHandler;
        private final TwidereLinkify mTwidereLinkify;

        private final boolean mNameFirst;
        private final int mCardLayoutResource;
        private final int mTextSize;
        private final int mCardBackgroundColor;
        private final boolean mIsCompact;
        private final int mProfileImageStyle;
        private final int mMediaPreviewStyle;
        private final int mLinkHighligingStyle;
        private final boolean mDisplayMediaPreview;
        private final boolean mDisplayProfileImage;
        private final boolean mSensitiveContentEnabled;
        private final boolean mHideCardActions;

        private boolean mLoadMoreSupported;
        private boolean mLoadMoreIndicatorVisible;

        private ParcelableStatus mStatus;
        private ParcelableCredentials mStatusAccount;
        private List<ParcelableStatus> mConversation, mReplies;
        private boolean mDetailMediaExpanded;
        private StatusAdapterListener mStatusAdapterListener;
        private DetailStatusViewHolder mCachedHolder;

        public StatusAdapter(StatusFragment fragment, boolean compact) {
            final Context context = fragment.getActivity();
            final Resources res = context.getResources();
            final SharedPreferencesWrapper preferences = SharedPreferencesWrapper.getInstance(context,
                    SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            mFragment = fragment;
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = TwidereApplication.getInstance(context).getMediaLoaderWrapper();
            mMediaLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
            mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context);
            mNameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
            mTextSize = preferences.getInt(KEY_TEXT_SIZE, res.getInteger(R.integer.default_text_size));
            mProfileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
            mLinkHighligingStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
            mIsCompact = compact;
            mDisplayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
            mDisplayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
            mSensitiveContentEnabled = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
            mHideCardActions = preferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
            if (compact) {
                mCardLayoutResource = R.layout.card_item_status_compact;
            } else {
                mCardLayoutResource = R.layout.card_item_status;
            }
            mTwidereLinkify = new TwidereLinkify(new StatusAdapterLinkClickHandler<>(this));
        }

        public void addConversation(ParcelableStatus status, int position) {
            if (mConversation == null) {
                mConversation = new ArrayList<>();
            }
            mConversation.add(position, status);
            notifyDataSetChanged();
            updateItemDecoration();
        }

        public int findPositionById(long itemId) {
            for (int i = 0, j = getItemCount(); i < j; i++) {
                if (getItemId(i) == itemId) return i;
            }
            return RecyclerView.NO_POSITION;
        }

        public StatusFragment getFragment() {
            return mFragment;
        }

        public MediaLoaderWrapper getMediaLoader() {
            return mImageLoader;
        }

        public Context getContext() {
            return mContext;
        }

        @Override
        public MediaLoadingHandler getMediaLoadingHandler() {
            return mMediaLoadingHandler;
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
            return mFragment.getTwitterWrapper();
        }

        public float getTextSize() {
            return mTextSize;
        }

        @Override
        public boolean isLoadMoreIndicatorVisible() {
            return mLoadMoreIndicatorVisible;
        }

        @Override
        public boolean isLoadMoreSupported() {
            return mLoadMoreSupported;
        }

        @Override
        public void setLoadMoreSupported(boolean supported) {
            mLoadMoreSupported = supported;
            if (!supported) {
                mLoadMoreIndicatorVisible = false;
            }
            notifyDataSetChanged();
        }

        @Override
        public void setLoadMoreIndicatorVisible(boolean enabled) {
            if (mLoadMoreIndicatorVisible == enabled) return;
            mLoadMoreIndicatorVisible = enabled && mLoadMoreSupported;
            updateItemDecoration();
            notifyDataSetChanged();
        }

        @Override
        public ParcelableStatus getStatus(int position) {
            final int conversationCount = getConversationCount();
            if (position == getItemCount() - 1) {
                return null;
            } else if (position < conversationCount) {
                return mConversation != null ? mConversation.get(position) : null;
            } else if (position > conversationCount) {
                return mReplies != null ? mReplies.get(position - conversationCount - 1) : null;
            } else {
                return mStatus;
            }
        }

        @Override
        public int getStatusesCount() {
            return getConversationCount() + 1 + getRepliesCount() + 1;
        }

        @Override
        public long getStatusId(int position) {
            final ParcelableStatus status = getStatus(position);
            return status != null ? status.hashCode() : position;
        }

        @Override
        public TwidereLinkify getTwidereLinkify() {
            return mTwidereLinkify;
        }

        @Override
        public boolean isMediaPreviewEnabled() {
            return mDisplayMediaPreview;
        }

        @Override
        public int getLinkHighlightingStyle() {
            return mLinkHighligingStyle;
        }

        public boolean isNameFirst() {
            return mNameFirst;
        }

        @Override
        public boolean isSensitiveContentEnabled() {
            return mSensitiveContentEnabled;
        }

        @Override
        public boolean isCardActionsHidden() {
            return mHideCardActions;
        }

        @Override
        public void setData(List<ParcelableStatus> data) {

        }

        @Override
        public boolean shouldShowAccountsColor() {
            return false;
        }

        public ParcelableStatus getStatus() {
            return mStatus;
        }

        public ParcelableCredentials getStatusAccount() {
            return mStatusAccount;
        }

        public boolean isDetailMediaExpanded() {
            if (mDetailMediaExpanded) return true;
            if (mDisplayMediaPreview) {
                final ParcelableStatus status = mStatus;
                return status != null && (mSensitiveContentEnabled || !status.is_possibly_sensitive);
            }
            return false;
        }

        public void setDetailMediaExpanded(boolean expanded) {
            mDetailMediaExpanded = expanded;
            notifyDataSetChanged();
            updateItemDecoration();
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

        @Override
        public boolean isProfileImageEnabled() {
            return mDisplayProfileImage;
        }

        @Override
        public final void onStatusClick(StatusViewHolder holder, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onStatusClick(holder, position);
            }
        }

        @Override
        public boolean onStatusLongClick(StatusViewHolder holder, int position) {
            return false;
        }

        @Override
        public void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position) {
            if (mStatusAdapterListener != null) {
                mStatusAdapterListener.onMediaClick(holder, media, position);
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
                    holder.setupViewOptions();
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
                    detailHolder.displayStatus(status);
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
                return VIEW_TYPE_SPACE;
            } else if (position < conversationCount) {
                return mConversation != null ? mConversation.get(position).id : VIEW_TYPE_CONVERSATION_LOAD_INDICATOR;
            } else if (position > conversationCount) {
                return mReplies != null ? mReplies.get(position - conversationCount - 1).id : VIEW_TYPE_REPLIES_LOAD_INDICATOR;
            } else {
                return mStatus != null ? mStatus.id : VIEW_TYPE_DETAIL_STATUS;
            }
        }

        @Override
        public int getItemCount() {
            return getStatusesCount();
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (mCachedHolder == holder) {
                mCachedHolder = null;
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (holder instanceof DetailStatusViewHolder) {
                mCachedHolder = (DetailStatusViewHolder) holder;
            }
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

        private int getStatusPosition() {
            return getConversationCount();
        }

        private void updateItemDecoration() {
            final DividerItemDecoration decoration = mFragment.getItemDecoration();
            decoration.setDecorationStart(0);
            if (isLoadMoreIndicatorVisible()) {
                decoration.setDecorationEndOffset(3);
            } else {
                decoration.setDecorationEndOffset(mReplies != null && mReplies.size() > 0 ? 1 : 2);
            }
            mFragment.mRecyclerView.invalidateItemDecorations();
        }
    }

    private static class StatusListLinearLayoutManager extends FixedLinearLayoutManager {

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
