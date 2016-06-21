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

package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerAccessor;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.text.BidiFormatter;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.TranslationResult;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ColorPickerDialogActivity;
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter;
import org.mariotaku.twidere.adapter.LoadMoreSupportAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.ConversationLoader;
import org.mariotaku.twidere.loader.ParcelableStatusLoader;
import org.mariotaku.twidere.menu.FavoriteItemProvider;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices;
import org.mariotaku.twidere.model.ParcelableActivityValuesCreator;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusValuesCreator;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FavoriteTaskEvent;
import org.mariotaku.twidere.model.message.StatusListChangedEvent;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableLocationUtils;
import org.mariotaku.twidere.model.util.ParcelableMediaUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.CheckUtils;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.ContentScrollHandler.ContentListSupport;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.HtmlSpanBuilder;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Nullables;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.util.RecyclerViewScrollHandler;
import org.mariotaku.twidere.util.RecyclerViewUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.StatusActionModeCallback;
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler;
import org.mariotaku.twidere.util.StatusLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.CardMediaContainer.OnMediaClickListener;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ExtendedRecyclerView;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.TwitterCardContainer;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder.StatusClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.MediaEvent;
import edu.tsinghua.hotmobi.model.TimelineType;
import edu.tsinghua.hotmobi.model.TweetEvent;

/**
 * Created by mariotaku on 14/12/5.
 */
public class StatusFragment extends BaseSupportFragment implements LoaderCallbacks<SingleResponse<ParcelableStatus>>,
        OnMediaClickListener, StatusClickListener, KeyboardShortcutCallback, ContentListSupport {

    // Constants
    private static final int LOADER_ID_DETAIL_STATUS = 1;
    private static final int LOADER_ID_STATUS_CONVERSATIONS = 2;
    private static final int LOADER_ID_STATUS_ACTIVITY = 3;
    private static final int STATE_LOADED = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_ERROR = 3;

    // Views
    private View mStatusContent;
    private View mProgressContainer;
    private View mErrorContainer;
    private RecyclerView mRecyclerView;

    private DividerItemDecoration mItemDecoration;

    private StatusAdapter mStatusAdapter;
    private LinearLayoutManager mLayoutManager;

    private LoadTranslationTask mLoadTranslationTask;
    private RecyclerViewNavigationHelper mNavigationHelper;
    private RecyclerViewScrollHandler mScrollListener;

    // Data fields
    private boolean mConversationLoaderInitialized;
    private boolean mActivityLoaderInitialized;
    private boolean mHasMoreConversation = true;
    private TweetEvent mStatusEvent;

    // Listeners
    private LoaderCallbacks<List<ParcelableStatus>> mConversationsLoaderCallback = new LoaderCallbacks<List<ParcelableStatus>>() {
        @Override
        public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
            mStatusAdapter.setRepliesLoading(true);
            mStatusAdapter.setConversationsLoading(true);
            mStatusAdapter.updateItemDecoration();
            final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
            final String maxId = args.getString(EXTRA_MAX_ID);
            final String sinceId = args.getString(EXTRA_SINCE_ID);
            final long maxSortId = args.getLong(EXTRA_MAX_SORT_ID);
            final long sinceSortId = args.getLong(EXTRA_SINCE_SORT_ID);
            final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
            assert status != null;
            final ConversationLoader loader = new ConversationLoader(getActivity(), status, sinceId,
                    maxId, sinceSortId, maxSortId, mStatusAdapter.getData(), true, loadingMore);
            // Setting comparator to null lets statuses sort ascending
            loader.setComparator(null);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
            mStatusAdapter.updateItemDecoration();
            ConversationLoader conversationLoader = (ConversationLoader) loader;
            int supportedPositions = 0;
            if (data != null && !data.isEmpty()) {
                if (conversationLoader.getSinceSortId() < data.get(data.size() - 1).sort_id) {
                    supportedPositions |= IndicatorPosition.END;
                }
                if (data.get(0).in_reply_to_status_id != null) {
                    supportedPositions |= IndicatorPosition.START;
                }
            } else {
                supportedPositions |= IndicatorPosition.END;
                final ParcelableStatus status = getStatus();
                if (status != null && status.in_reply_to_status_id != null) {
                    supportedPositions |= IndicatorPosition.START;
                }
            }
            mStatusAdapter.setLoadMoreSupportedPosition(supportedPositions);
            setConversation(data);
            final boolean canLoadAllReplies = ((ConversationLoader) loader).canLoadAllReplies();
            if (canLoadAllReplies) {
                mStatusAdapter.setReplyError(null);
            } else {
                final SpannableStringBuilder error = SpannableStringBuilder.valueOf(
                        HtmlSpanBuilder.fromHtml(getString(R.string.cant_load_all_replies_message)));
                ClickableSpan dialogSpan = null;
                for (URLSpan span : error.getSpans(0, error.length(), URLSpan.class)) {
                    if ("#dialog".equals(span.getURL())) {
                        dialogSpan = span;
                        break;
                    }
                }
                if (dialogSpan != null) {
                    final int spanStart = error.getSpanStart(dialogSpan), spanEnd = error.getSpanEnd(dialogSpan);
                    error.removeSpan(dialogSpan);
                    error.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            final FragmentActivity activity = getActivity();
                            if (activity == null || activity.isFinishing()) return;
                            MessageDialogFragment.show(activity,
                                    getString(R.string.cant_load_all_replies_explanation),
                                    "cant_load_all_replies_explanation");
                        }
                    }, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                mStatusAdapter.setReplyError(error);
            }
            mStatusAdapter.setConversationsLoading(false);
            mStatusAdapter.setRepliesLoading(false);
        }

        @Override
        public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {

        }
    };
    private LoaderCallbacks<StatusActivity> mStatusActivityLoaderCallback = new LoaderCallbacks<StatusActivity>() {
        @Override
        public Loader<StatusActivity> onCreateLoader(int id, Bundle args) {
            final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
            final String statusId = args.getString(EXTRA_STATUS_ID);
            return new StatusActivitySummaryLoader(getActivity(), accountKey, statusId);
        }

        @Override
        public void onLoadFinished(Loader<StatusActivity> loader, StatusActivity data) {
            mStatusAdapter.updateItemDecoration();
            mStatusAdapter.setStatusActivity(data);
        }

        @Override
        public void onLoaderReset(Loader<StatusActivity> loader) {

        }
    };

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        switch (requestCode) {
            case REQUEST_SET_COLOR: {
                final ParcelableStatus status = mStatusAdapter.getStatus();
                if (status == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
                    mUserColorNameManager.setUserColor(status.user_key, color);
                    status.user_color = color;
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    mUserColorNameManager.clearUserColor(status.user_key);
                    status.user_color = 0;
                }
                final Bundle args = getArguments();
                if (args.containsKey(EXTRA_STATUS)) {
                    args.putParcelable(EXTRA_STATUS, status);
                }
                getLoaderManager().restartLoader(LOADER_ID_DETAIL_STATUS, args, this);
                break;
            }
            case REQUEST_SELECT_ACCOUNT: {
                final ParcelableStatus status = mStatusAdapter.getStatus();
                if (status == null) return;
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return;
                    final UserKey accountKey = data.getParcelableExtra(EXTRA_ACCOUNT_KEY);
                    IntentUtils.openStatus(activity, accountKey, status.id);
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
        setHasOptionsMenu(true);
        final View view = getView();
        assert view != null;
        final Context context = view.getContext();
        Utils.setNdefPushMessageCallback(getActivity(), new CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableStatus status = getStatus();
                if (status == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(LinkCreator.getStatusWebLink(status)),
                });
            }
        });
        mStatusAdapter = new StatusAdapter(this);
        mLayoutManager = new StatusListLinearLayoutManager(context, mRecyclerView);
        mItemDecoration = new StatusDividerItemDecoration(context, mStatusAdapter, mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(mItemDecoration);
        mLayoutManager.setRecycleChildrenOnDetach(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setClipToPadding(false);
        mStatusAdapter.setStatusClickListener(this);
        mRecyclerView.setAdapter(mStatusAdapter);
        registerForContextMenu(mRecyclerView);

        mScrollListener = new RecyclerViewScrollHandler(this,
                new RecyclerViewScrollHandler.RecyclerViewCallback(mRecyclerView));
        mScrollListener.setTouchSlop(ViewConfiguration.get(context).getScaledTouchSlop());

        mNavigationHelper = new RecyclerViewNavigationHelper(mRecyclerView, mLayoutManager,
                mStatusAdapter, null);

        setState(STATE_LOADING);

        getLoaderManager().initLoader(LOADER_ID_DETAIL_STATUS, getArguments(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStatusContent = view.findViewById(R.id.status_content);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mErrorContainer = view.findViewById(R.id.error_container);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {
        final ParcelableStatus status = mStatusAdapter.getStatus(statusPosition);
        if (status == null || media == null) return;
        IntentUtils.openMedia(getActivity(), status, media, null,
                mPreferences.getBoolean(KEY_NEW_DOCUMENT_API));

        MediaEvent event = MediaEvent.create(getActivity(), status, media, TimelineType.DETAILS,
                mStatusAdapter.isMediaPreviewEnabled());
        HotMobiLogger.getInstance(getActivity()).log(status.account_key, event);
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {
        final ParcelableStatus status = mStatusAdapter.getStatus(position);
        AbsStatusesFragment.handleStatusActionClick(getContext(), getFragmentManager(), mTwitterWrapper,
                (StatusViewHolder) holder, status, id);
    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        IntentUtils.openStatus(getActivity(), mStatusAdapter.getStatus(position), null);
    }

    @Override
    public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {
        if (getActivity() == null) return;
        final View view = mLayoutManager.findViewByPosition(position);
        if (view == null) return;
        mRecyclerView.showContextMenuForChild(view);
    }

    @Override
    public void onUserProfileClick(IStatusViewHolder holder, int position) {
        final FragmentActivity activity = getActivity();
        final ParcelableStatus status = mStatusAdapter.getStatus(position);
        IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                status.user_screen_name, null, mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                UserFragment.Referral.TIMELINE_STATUS);
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, UserKey accountKey, long extraId) {
        final ParcelableStatus status = mStatusAdapter.getStatus();
        if (status == null || media == null) return;
        IntentUtils.openMediaDirectly(getActivity(), accountKey, status, media, null,
                mPreferences.getBoolean(KEY_NEW_DOCUMENT_API));
        // BEGIN HotMobi
        MediaEvent event = MediaEvent.create(getActivity(), status, media, TimelineType.OTHER,
                mStatusAdapter.isMediaPreviewEnabled());
        HotMobiLogger.getInstance(getActivity()).log(status.account_key, event);
        // END HotMobi
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, @NonNull final KeyEvent event,
                                                final int metaState) {
        if (!KeyboardShortcutsHandler.isValidForHotkey(keyCode, event)) return false;
        final View focusedChild = RecyclerViewUtils.findRecyclerViewChild(mRecyclerView, mLayoutManager.getFocusedChild());
        final int position;
        if (focusedChild != null && focusedChild.getParent() == mRecyclerView) {
            position = mRecyclerView.getChildLayoutPosition(focusedChild);
        } else {
            return false;
        }
        if (position == -1) return false;
        final ParcelableStatus status = getAdapter().getStatus(position);
        if (status == null) return false;
        String action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
        if (action == null) return false;
        switch (action) {
            case ACTION_STATUS_REPLY: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.putExtra(EXTRA_STATUS, status);
                startActivity(intent);
                return true;
            }
            case ACTION_STATUS_RETWEET: {
                RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                return true;
            }
            case ACTION_STATUS_FAVORITE: {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_key, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_key, status.id);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
        if (action == null) return false;
        switch (action) {
            case ACTION_STATUS_REPLY:
            case ACTION_STATUS_RETWEET:
            case ACTION_STATUS_FAVORITE:
                return true;
        }
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode,
                repeatCount, event, metaState);
    }


    @Override
    public Loader<SingleResponse<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
        final Bundle fragmentArgs = getArguments();
        final UserKey accountKey = fragmentArgs.getParcelable(EXTRA_ACCOUNT_KEY);
        final String statusId = fragmentArgs.getString(EXTRA_STATUS_ID);
        return new ParcelableStatusLoader(getActivity(), false, fragmentArgs, accountKey, statusId);
    }

    @Override
    public void onLoadFinished(final Loader<SingleResponse<ParcelableStatus>> loader,
                               final SingleResponse<ParcelableStatus> data) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        if (data.hasData()) {
            final ReadPosition readPosition = saveReadPosition();
            final ParcelableStatus status = data.getData();
            final Bundle dataExtra = data.getExtras();
            final ParcelableCredentials credentials = dataExtra.getParcelable(EXTRA_ACCOUNT);
            if (mStatusAdapter.setStatus(status, credentials)) {
                Bundle args = getArguments();
                if (args.containsKey(EXTRA_STATUS)) {
                    args.putParcelable(EXTRA_STATUS, status);
                }
                mStatusAdapter.setLoadMoreSupportedPosition(IndicatorPosition.BOTH);
                mStatusAdapter.setData(null);
                loadConversation(status, null, null);
                loadActivity(status);

                final int position = mStatusAdapter.getFirstPositionOfItem(StatusAdapter.ITEM_IDX_STATUS);
                if (position != RecyclerView.NO_POSITION) {
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                }

                final TweetEvent event = TweetEvent.create(activity, status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.OPEN);
                mStatusEvent = event;
            } else if (readPosition != null) {
                restoreReadPosition(readPosition);
            }
            setState(STATE_LOADED);
        } else {
            mStatusAdapter.setLoadMoreSupportedPosition(IndicatorPosition.NONE);
            //TODO show errors
            setState(STATE_ERROR);
            final ImageView errorIconView = (ImageView) mErrorContainer.findViewById(R.id.error_icon);
            final TextView errorTextView = (TextView) mErrorContainer.findViewById(R.id.error_text);
            errorTextView.setText(Utils.getErrorMessage(getContext(), data.getException()));
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {
        final TweetEvent event = mStatusEvent;
        if (event == null) return;
        event.markEnd();
        final UserKey accountKey = new UserKey(event.getAccountId(), event.getAccountHost());
        HotMobiLogger.getInstance(getActivity()).log(accountKey, event);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_status, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuUtils.setMenuItemAvailability(menu, R.id.current_status, mStatusAdapter.getStatus() != null);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.current_status: {
                if (mStatusAdapter.getStatus() != null) {
                    final int position = mStatusAdapter.getFirstPositionOfItem(StatusAdapter.ITEM_IDX_STATUS);
                    mRecyclerView.smoothScrollToPosition(position);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setConversation(List<ParcelableStatus> data) {
        final ReadPosition readPosition = saveReadPosition();
        final boolean changed = mStatusAdapter.setData(data);
        mHasMoreConversation = data != null && changed;
        restoreReadPosition(readPosition);
    }

    public StatusAdapter getAdapter() {
        return mStatusAdapter;
    }

    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }

    @Override
    public void onLoadMoreContents(@IndicatorPosition int position) {
        if (!mHasMoreConversation) return;
        if ((position & IndicatorPosition.START) != 0) {
            final int start = mStatusAdapter.getIndexStart(StatusAdapter.ITEM_IDX_CONVERSATION);
            final ParcelableStatus status = mStatusAdapter.getStatus(start);
            if (status == null || status.in_reply_to_status_id == null) return;
            loadConversation(getStatus(), null, status.id);
        } else if ((position & IndicatorPosition.END) != 0) {
            final int start = mStatusAdapter.getIndexStart(StatusAdapter.ITEM_IDX_CONVERSATION);
            final ParcelableStatus status = mStatusAdapter.getStatus(start + mStatusAdapter.getStatusCount() - 1);
            if (status == null) return;
            loadConversation(getStatus(), status.id, null);
        }
        mStatusAdapter.setLoadMoreIndicatorPosition(position);
    }

    @Override
    public void setControlVisible(boolean visible) {
        // No-op
    }

    @Override
    public boolean isReachingEnd() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition() >= mStatusAdapter.getItemCount() - 1;
    }

    @Override
    public boolean isReachingStart() {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition() <= 1;
    }

    private ParcelableStatus getStatus() {
        return mStatusAdapter.getStatus();
    }

    private void loadConversation(ParcelableStatus status, String sinceId, String maxId) {
        if (status == null || getActivity() == null) return;
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, status.account_key);
        args.putString(EXTRA_STATUS_ID, status.is_retweet ? status.retweet_id : status.id);
        args.putString(EXTRA_SINCE_ID, sinceId);
        args.putString(EXTRA_MAX_ID, maxId);
        args.putParcelable(EXTRA_STATUS, status);
        if (mConversationLoaderInitialized) {
            getLoaderManager().restartLoader(LOADER_ID_STATUS_CONVERSATIONS, args, mConversationsLoaderCallback);
            return;
        }
        getLoaderManager().initLoader(LOADER_ID_STATUS_CONVERSATIONS, args, mConversationsLoaderCallback);
        mConversationLoaderInitialized = true;
    }


    private void loadActivity(ParcelableStatus status) {
        if (status == null || getHost() == null || isDetached()) return;
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ACCOUNT_KEY, status.account_key);
        args.putString(EXTRA_STATUS_ID, status.is_retweet ? status.retweet_id : status.id);
        if (mActivityLoaderInitialized) {
            getLoaderManager().restartLoader(LOADER_ID_STATUS_ACTIVITY, args, mStatusActivityLoaderCallback);
            return;
        }
        getLoaderManager().initLoader(LOADER_ID_STATUS_ACTIVITY, args, mStatusActivityLoaderCallback);
        mActivityLoaderInitialized = true;
    }

    private void loadTranslation(@Nullable ParcelableStatus status) {
        if (status == null) return;
        if (AsyncTaskUtils.isTaskRunning(mLoadTranslationTask)) {
            mLoadTranslationTask.cancel(true);
        }
        mLoadTranslationTask = new LoadTranslationTask(this);
        AsyncTaskUtils.executeTask(mLoadTranslationTask, status);
    }


    private void displayTranslation(TranslationResult translation) {
        mStatusAdapter.setTranslationResult(translation);
    }

    @Nullable
    private ReadPosition saveReadPosition() {
        final int position = mLayoutManager.findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) return null;
        final int itemType = mStatusAdapter.getItemType(position);
        long itemId = mStatusAdapter.getItemId(position);
        final View positionView;
        if (itemType == StatusAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE) {
            // Should be next item
            positionView = mLayoutManager.findViewByPosition(position + 1);
            itemId = mStatusAdapter.getItemId(position + 1);
        } else {
            positionView = mLayoutManager.findViewByPosition(position);
        }
        return new ReadPosition(itemId, positionView != null ? positionView.getTop() : 0);
    }

    private void restoreReadPosition(@Nullable ReadPosition position) {
        if (position == null) return;
        final int adapterPosition = mStatusAdapter.findPositionById(position.statusId);
        if (adapterPosition < 0) return;
        //TODO maintain read position
        mLayoutManager.scrollToPositionWithOffset(adapterPosition, position.offsetTop);
    }

    private void setState(int state) {
        mStatusContent.setVisibility(state == STATE_LOADED ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);
        mErrorContainer.setVisibility(state == STATE_ERROR ? View.VISIBLE : View.GONE);
    }

    private void showConversationError(Exception exception) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnTouchListener(mScrollListener.getOnTouchListener());
    }

    @Override
    public void onStop() {
        mRecyclerView.setOnTouchListener(null);
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!getUserVisibleHint() || menuInfo == null) return;
        final MenuInflater inflater = new MenuInflater(getContext());
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) menuInfo;
        final ParcelableStatus status = mStatusAdapter.getStatus(contextMenuInfo.getPosition());
        inflater.inflate(R.menu.action_status, menu);
        MenuUtils.setupForStatus(getContext(), mPreferences, menu, status,
                mTwitterWrapper);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) item.getMenuInfo();
        final ParcelableStatus status = mStatusAdapter.getStatus(contextMenuInfo.getPosition());
        if (status == null) return false;
        if (item.getItemId() == R.id.share) {
            final Intent shareIntent = Utils.createStatusShareIntent(getActivity(), status);
            final Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_status));
            Utils.addCopyLinkIntent(getContext(), chooser, LinkCreator.getStatusWebLink(status));
            startActivity(chooser);
            return true;
        }
        return MenuUtils.handleStatusClick(getActivity(), this, getFragmentManager(),
                mUserColorNameManager, mTwitterWrapper, status, item);
    }

    @Subscribe
    public void notifyStatusListChanged(StatusListChangedEvent event) {
        final StatusAdapter adapter = getAdapter();
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void notifyFavoriteTask(FavoriteTaskEvent event) {
        if (!event.isSucceeded()) return;
        final StatusAdapter adapter = getAdapter();
        final ParcelableStatus status = adapter.findStatusById(event.getAccountKey(), event.getStatusId());
        if (status != null) {
            switch (event.getAction()) {
                case FavoriteTaskEvent.Action.CREATE: {
                    status.is_favorite = true;
                    break;
                }
                case FavoriteTaskEvent.Action.DESTROY: {
                    status.is_favorite = false;
                    break;
                }
            }
        }
    }

    private void onUserClick(ParcelableUser user) {
        IntentUtils.openUserProfile(getContext(), user, null, true,
                UserFragment.Referral.TIMELINE_STATUS);
    }

    public static final class LoadSensitiveImageConfirmDialogFragment extends BaseDialogFragment implements
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
            final Context context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(R.string.sensitive_content_warning);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }
    }

    static class LoadTranslationTask extends AsyncTask<ParcelableStatus, Object,
            SingleResponse<TranslationResult>> {
        final Context context;
        final StatusFragment fragment;

        LoadTranslationTask(final StatusFragment fragment) {
            context = fragment.getActivity();
            this.fragment = fragment;
        }

        @Override
        protected SingleResponse<TranslationResult> doInBackground(ParcelableStatus... params) {
            final ParcelableStatus status = params[0];
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, status.account_key,
                    true);
            final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                    Context.MODE_PRIVATE);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final String prefDest = prefs.getString(KEY_TRANSLATION_DESTINATION, null);
                final String dest;
                if (TextUtils.isEmpty(prefDest)) {
                    dest = twitter.getAccountSettings().getLanguage();
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_TRANSLATION_DESTINATION, dest);
                    editor.apply();
                } else {
                    dest = prefDest;
                }
                final String statusId = status.is_retweet ? status.retweet_id : status.id;
                return SingleResponse.getInstance(twitter.showTranslation(statusId, dest));
            } catch (final MicroBlogException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(SingleResponse<TranslationResult> result) {
            if (result.hasData()) {
                fragment.displayTranslation(result.getData());
            } else if (result.hasException()) {
                Utils.showErrorMessage(context, R.string.translate, result.getException(), false);
            }
        }
    }

    private static class DetailStatusViewHolder extends ViewHolder implements OnClickListener,
            ActionMenuView.OnMenuItemClickListener {

        private final StatusAdapter adapter;

        private final ActionMenuView menuBar;
        private final NameView nameView, quotedNameView;
        private final TextView textView;
        private final TextView quotedTextView;
        private final ImageView profileImageView;
        private final ImageView profileTypeView;
        private final TextView timeSourceView;
        private final TextView retweetedByView;

        private final TextView quoteOriginalLink;
        private final ColorLabelRelativeLayout profileContainer;
        private final View mediaPreviewContainer;
        private final View mediaPreviewLoad;

        private final CardMediaContainer mediaPreview;
        private final TextView translateLabelView;

        private final View countsUsersHeightHolder;

        private final ForegroundColorView quoteIndicator;
        private final TextView locationView;
        private final TwitterCardContainer twitterCard;
        private final StatusLinkClickHandler linkClickHandler;
        private final TwidereLinkify linkify;
        private final View translateContainer;
        private final TextView translateResultView;
        private final RecyclerView countsUsersView;

        public DetailStatusViewHolder(final StatusAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            this.linkClickHandler = new DetailStatusLinkClickHandler(adapter.getContext(), null,
                    adapter, adapter.getPreferences());
            this.linkify = new TwidereLinkify(linkClickHandler);
            menuBar = (ActionMenuView) itemView.findViewById(R.id.menu_bar);
            nameView = (NameView) itemView.findViewById(R.id.name);
            textView = (TextView) itemView.findViewById(R.id.text);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
            profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
            timeSourceView = (TextView) itemView.findViewById(R.id.time_source);
            retweetedByView = (TextView) itemView.findViewById(R.id.retweeted_by);
            mediaPreviewContainer = itemView.findViewById(R.id.media_preview_container);
            mediaPreviewLoad = itemView.findViewById(R.id.media_preview_load);
            mediaPreview = (CardMediaContainer) itemView.findViewById(R.id.media_preview);
            locationView = (TextView) itemView.findViewById(R.id.location_view);
            quoteOriginalLink = (TextView) itemView.findViewById(R.id.quote_original_link);
            profileContainer = (ColorLabelRelativeLayout) itemView.findViewById(R.id.profile_container);
            twitterCard = (TwitterCardContainer) itemView.findViewById(R.id.twitter_card);

            quotedTextView = (TextView) itemView.findViewById(R.id.quoted_text);
            quotedNameView = (NameView) itemView.findViewById(R.id.quoted_name);
            quoteIndicator = (ForegroundColorView) itemView.findViewById(R.id.quote_indicator);
            translateLabelView = (TextView) itemView.findViewById(R.id.translate_label);
            translateContainer = itemView.findViewById(R.id.translate_container);
            translateResultView = (TextView) itemView.findViewById(R.id.translate_result);

            countsUsersView = (RecyclerView) itemView.findViewById(R.id.counts_users);

            countsUsersHeightHolder = itemView.findViewById(R.id.counts_users_height_holder);

            initViews();
        }

        @UiThread
        public void displayStatus(@Nullable final ParcelableCredentials account,
                                  @Nullable final ParcelableStatus status,
                                  @Nullable final StatusActivity statusActivity,
                                  @Nullable final TranslationResult translation) {
            if (account == null || status == null) return;
            final StatusFragment fragment = adapter.getFragment();
            final Context context = adapter.getContext();
            final MediaLoaderWrapper loader = adapter.getMediaLoader();
            final BidiFormatter formatter = adapter.getBidiFormatter();
            final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
            final boolean nameFirst = adapter.isNameFirst();

            linkClickHandler.setStatus(status);

            if (status.retweet_id != null) {
                final String retweetedBy = UserColorNameManager.decideDisplayName(status.retweet_user_nickname,
                        status.retweeted_by_user_name, status.retweeted_by_user_screen_name, nameFirst);
                retweetedByView.setText(context.getString(R.string.name_retweeted, retweetedBy));
                retweetedByView.setVisibility(View.VISIBLE);
            } else {
                retweetedByView.setText(null);
                retweetedByView.setVisibility(View.GONE);
            }

            profileContainer.drawEnd(status.account_color);

            final int layoutPosition = getLayoutPosition();
            final boolean skipLinksInText = status.extras != null && status.extras.support_entities;
            if (status.is_quote) {

                quoteIndicator.setVisibility(View.VISIBLE);

                boolean originalIdAvailable = !TextUtils.isEmpty(status.quoted_id);
                boolean quoteContentAvailable = status.quoted_text_plain != null
                        && status.quoted_text_unescaped != null;

                quoteOriginalLink.setVisibility(originalIdAvailable ? View.VISIBLE : View.GONE);

                if (quoteContentAvailable) {
                    quotedNameView.setVisibility(View.VISIBLE);
                    quotedTextView.setVisibility(View.VISIBLE);

                    quotedNameView.setName(UserColorNameManager.decideNickname(status.quoted_user_nickname,
                            status.quoted_user_name));
                    quotedNameView.setScreenName(String.format("@%s", status.quoted_user_screen_name));
                    quotedNameView.updateText(formatter);


                    int quotedDisplayEnd = -1;
                    if (status.extras.quoted_display_text_range != null) {
                        quotedDisplayEnd = status.extras.quoted_display_text_range[1];
                    }

                    final SpannableStringBuilder quotedText = SpannableStringBuilder.valueOf(status.quoted_text_unescaped);
                    ParcelableStatusUtils.applySpans(quotedText, status.quoted_spans);
                    linkify.applyAllLinks(quotedText, status.account_key, layoutPosition,
                            status.is_possibly_sensitive, skipLinksInText);
                    if (quotedDisplayEnd != -1 && quotedDisplayEnd <= quotedText.length()) {
                        quotedTextView.setText(quotedText.subSequence(0, quotedDisplayEnd));
                    } else {
                        quotedTextView.setText(quotedText);
                    }
                    if (quotedTextView.length() == 0) {
                        // No text
                        quotedTextView.setVisibility(View.GONE);
                    } else {
                        quotedTextView.setVisibility(View.VISIBLE);
                    }

                    quoteIndicator.setColor(status.quoted_user_color);
                } else {
                    quotedNameView.setVisibility(View.GONE);
                    quotedTextView.setVisibility(View.VISIBLE);

                    // Not available
                    final SpannableString string = SpannableString.valueOf(context.getString(R.string.status_not_available_text));
                    string.setSpan(new ForegroundColorSpan(ThemeUtils.getColorFromAttribute(context,
                            android.R.attr.textColorTertiary, textView.getCurrentTextColor())), 0,
                            string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    quotedTextView.setText(string);

                    quoteIndicator.setColor(0);
                }
            } else {
                quoteOriginalLink.setVisibility(View.GONE);
                quotedNameView.setVisibility(View.GONE);
                quotedTextView.setVisibility(View.GONE);
                quoteIndicator.setVisibility(View.GONE);
            }

            profileContainer.drawStart(status.user_color);

            final long timestamp;

            if (status.is_retweet) {
                timestamp = status.retweet_timestamp;
            } else {
                timestamp = status.timestamp;
            }

            nameView.setName(UserColorNameManager.decideNickname(status.user_nickname, status.user_name));
            nameView.setScreenName(String.format("@%s", status.user_screen_name));
            nameView.updateText(formatter);

            loader.displayProfileImage(profileImageView, status);

            final int typeIconRes = Utils.getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
            final int typeDescriptionRes = Utils.getUserTypeDescriptionRes(status.user_is_verified, status.user_is_protected);

            if (typeIconRes != 0 && typeDescriptionRes != 0) {
                profileTypeView.setImageResource(typeIconRes);
                profileTypeView.setContentDescription(context.getString(typeDescriptionRes));
                profileTypeView.setVisibility(View.VISIBLE);
            } else {
                profileTypeView.setImageDrawable(null);
                profileTypeView.setContentDescription(null);
                profileTypeView.setVisibility(View.GONE);
            }

            final String timeString = Utils.formatToLongTimeString(context, timestamp);
            if (!TextUtils.isEmpty(timeString) && !TextUtils.isEmpty(status.source)) {
                timeSourceView.setText(HtmlSpanBuilder.fromHtml(context.getString(R.string.time_source, timeString, status.source)));
            } else if (TextUtils.isEmpty(timeString) && !TextUtils.isEmpty(status.source)) {
                timeSourceView.setText(HtmlSpanBuilder.fromHtml(context.getString(R.string.source, status.source)));
            } else if (!TextUtils.isEmpty(timeString) && TextUtils.isEmpty(status.source)) {
                timeSourceView.setText(timeString);
            }
            timeSourceView.setMovementMethod(LinkMovementMethod.getInstance());

            int displayEnd = -1;
            if (status.extras.display_text_range != null) {
                displayEnd = status.extras.display_text_range[1];
            }

            final SpannableStringBuilder text = SpannableStringBuilder.valueOf(status.text_unescaped);
            ParcelableStatusUtils.applySpans(text, status.spans);
            linkify.applyAllLinks(text, status.account_key, layoutPosition,
                    status.is_possibly_sensitive, skipLinksInText);

            if (displayEnd != -1 && displayEnd <= text.length()) {
                textView.setText(text.subSequence(0, displayEnd));
            } else {
                textView.setText(text);
            }
            if (textView.length() == 0) {
                // No text
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
            }

            final ParcelableLocation location;
            final String placeFullName;
            if (status.is_quote) {
                location = status.quoted_location;
                placeFullName = status.quoted_place_full_name;
            } else {
                location = status.location;
                placeFullName = status.place_full_name;
            }

            if (!TextUtils.isEmpty(placeFullName)) {
                locationView.setVisibility(View.VISIBLE);
                locationView.setText(placeFullName);
                locationView.setClickable(ParcelableLocationUtils.isValidLocation(location));
            } else if (ParcelableLocationUtils.isValidLocation(location)) {
                locationView.setVisibility(View.VISIBLE);
                locationView.setText(R.string.view_map);
                locationView.setClickable(true);
            } else {
                locationView.setVisibility(View.GONE);
                locationView.setText(null);
            }

            final CountsUsersAdapter interactUsersAdapter = (CountsUsersAdapter) countsUsersView.getAdapter();
            if (statusActivity != null) {
                interactUsersAdapter.setUsers(statusActivity.getRetweeters());
                interactUsersAdapter.setCounts(statusActivity);
            } else {
                interactUsersAdapter.setUsers(null);
                interactUsersAdapter.setCounts(status);
            }

            if (interactUsersAdapter.getItemCount() > 0) {
                countsUsersView.setVisibility(View.VISIBLE);
                countsUsersHeightHolder.setVisibility(View.INVISIBLE);
            } else {
                countsUsersView.setVisibility(View.GONE);
                countsUsersHeightHolder.setVisibility(View.GONE);
            }

            final ParcelableMedia[] media = ParcelableMediaUtils.getPrimaryMedia(status);

            if (ArrayUtils.isEmpty(media)) {
                mediaPreviewContainer.setVisibility(View.GONE);
                mediaPreview.setVisibility(View.GONE);
                mediaPreviewLoad.setVisibility(View.GONE);
                mediaPreview.displayMedia();
            } else if (adapter.isDetailMediaExpanded()) {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreview.setVisibility(View.VISIBLE);
                mediaPreviewLoad.setVisibility(View.GONE);
                mediaPreview.displayMedia(media, loader, status.account_key, -1,
                        adapter.getFragment(), adapter.getMediaLoadingHandler());
            } else {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
                mediaPreview.setVisibility(View.GONE);
                mediaPreviewLoad.setVisibility(View.VISIBLE);
                mediaPreview.displayMedia();
            }

            if (TwitterCardUtils.isCardSupported(status)) {
                final Point size = TwitterCardUtils.getCardSize(status.card);
                twitterCard.setVisibility(View.VISIBLE);
                if (size != null) {
                    twitterCard.setCardSize(size.x, size.y);
                } else {
                    twitterCard.setCardSize(0, 0);
                }
                final Fragment cardFragment = TwitterCardUtils.createCardFragment(status);
                final FragmentManager fm = fragment.getChildFragmentManager();
                if (cardFragment != null && !FragmentManagerAccessor.isStateSaved(fm)) {
                    final FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.twitter_card, cardFragment);
                    ft.commit();
                } else {
                    twitterCard.setVisibility(View.GONE);
                }
            } else {
                twitterCard.setVisibility(View.GONE);
            }

            MenuUtils.setupForStatus(context, fragment.mPreferences, menuBar.getMenu(), status,
                    adapter.getStatusAccount(), twitter);


            final String lang = status.lang;
            if (!Utils.isOfficialCredentials(context, account) || !CheckUtils.isValidLocale(lang)) {
                translateLabelView.setText(R.string.unknown_language);
                translateContainer.setVisibility(View.GONE);
            } else {
                final Locale locale = new Locale(lang);
                translateContainer.setVisibility(View.VISIBLE);
                if (translation != null) {
                    translateLabelView.setText(context.getString(R.string.translation));
                    translateResultView.setVisibility(View.VISIBLE);
                    translateResultView.setText(translation.getText());
                } else {
                    translateLabelView.setText(context.getString(R.string.translate_from_language,
                            locale.getDisplayLanguage()));
                    translateResultView.setVisibility(View.GONE);
                }
            }

            textView.setTextIsSelectable(true);
            quotedTextView.setTextIsSelectable(true);
            translateResultView.setTextIsSelectable(true);

            textView.setMovementMethod(LinkMovementMethod.getInstance());
            quotedTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public void onClick(View v) {
            final ParcelableStatus status = adapter.getStatus(getLayoutPosition());
            final StatusFragment fragment = adapter.getFragment();
            if (status == null || fragment == null) return;
            final SharedPreferencesWrapper preferences = fragment.mPreferences;
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
                    IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                            status.user_screen_name, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            UserFragment.Referral.STATUS);
                    break;
                }
                case R.id.retweeted_by: {
                    if (status.retweet_id != null) {
                        IntentUtils.openUserProfile(adapter.getContext(), status.account_key,
                                status.retweeted_by_user_key, status.retweeted_by_user_screen_name,
                                null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                                UserFragment.Referral.STATUS);
                    }
                    break;
                }
                case R.id.location_view: {
                    final ParcelableLocation location = status.location;
                    if (!ParcelableLocationUtils.isValidLocation(location)) return;
                    IntentUtils.openMap(adapter.getContext(), location.latitude, location.longitude);
                    break;
                }
                case R.id.quoted_name: {
                    IntentUtils.openUserProfile(adapter.getContext(), status.account_key,
                            status.quoted_user_key, status.quoted_user_screen_name, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API), UserFragment.Referral.STATUS);
                    break;
                }
                case R.id.quote_original_link: {
                    IntentUtils.openStatus(adapter.getContext(), status.account_key, status.quoted_id);
                    break;
                }
                case R.id.translate_label: {
                    fragment.loadTranslation(adapter.getStatus());
                    break;
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final int layoutPosition = getLayoutPosition();
            if (layoutPosition < 0) return false;
            final StatusFragment fragment = adapter.getFragment();
            final ParcelableStatus status = adapter.getStatus(layoutPosition);
            if (status == null || fragment == null) return false;
            final AsyncTwitterWrapper twitter = fragment.mTwitterWrapper;
            final UserColorNameManager manager = fragment.mUserColorNameManager;
            final FragmentActivity activity = fragment.getActivity();
            final FragmentManager fm = fragment.getFragmentManager();
            if (item.getItemId() == R.id.retweet) {
                RetweetQuoteDialogFragment.show(fm, status);
                return true;
            }
            return MenuUtils.handleStatusClick(activity, fragment, fm, manager, twitter,
                    status, item);
        }

        private void initViews() {
//            menuBar.setOnMenuItemClickListener(this);
            menuBar.setOnMenuItemClickListener(this);
            final StatusFragment fragment = adapter.getFragment();
            final FragmentActivity activity = fragment.getActivity();
            final MenuInflater inflater = activity.getMenuInflater();
            final Menu menu = menuBar.getMenu();
            inflater.inflate(R.menu.menu_detail_status, menu);
            final MenuItem favoriteItem = menu.findItem(R.id.favorite);
            final ActionProvider provider = MenuItemCompat.getActionProvider(favoriteItem);
            if (provider instanceof FavoriteItemProvider) {
                final int defaultColor = ThemeUtils.getActionIconColor(activity);
                final FavoriteItemProvider itemProvider = (FavoriteItemProvider) provider;
                itemProvider.setDefaultColor(defaultColor);
                final int favoriteHighlight = ContextCompat.getColor(activity, R.color.highlight_favorite);
                final int likeHighlight = ContextCompat.getColor(activity, R.color.highlight_like);
                final boolean useStar = adapter.shouldUseStarsForLikes();
                itemProvider.setActivatedColor(useStar ? favoriteHighlight : likeHighlight);
                itemProvider.setIcon(useStar ? R.drawable.ic_action_star : R.drawable.ic_action_heart);
                itemProvider.setUseStar(useStar);
                itemProvider.init(menuBar, favoriteItem);
            }
            ThemeUtils.wrapMenuIcon(menuBar, MENU_GROUP_STATUS_SHARE);
            mediaPreviewLoad.setOnClickListener(this);
            profileContainer.setOnClickListener(this);
            quotedNameView.setOnClickListener(this);
            retweetedByView.setOnClickListener(this);
            locationView.setOnClickListener(this);
            quoteOriginalLink.setOnClickListener(this);
            translateLabelView.setOnClickListener(this);

            final float textSize = adapter.getTextSize();
            nameView.setPrimaryTextSize(textSize * 1.25f);
            nameView.setSecondaryTextSize(textSize * 0.85f);
            textView.setTextSize(textSize * 1.25f);

            quotedNameView.setPrimaryTextSize(textSize * 1.25f);
            quotedNameView.setSecondaryTextSize(textSize * 0.85f);
            quotedTextView.setTextSize(textSize * 1.25f);

            quoteOriginalLink.setTextSize(textSize * 0.85f);
            locationView.setTextSize(textSize * 0.85f);
            timeSourceView.setTextSize(textSize * 0.85f);
            translateLabelView.setTextSize(textSize * 0.85f);
            translateResultView.setTextSize(textSize * 1.05f);

            TextView countView = (TextView) countsUsersHeightHolder.findViewById(R.id.count);
            TextView labelView = (TextView) countsUsersHeightHolder.findViewById(R.id.label);

            countView.setTextSize(textSize * 1.25f);
            labelView.setTextSize(textSize * 0.85f);

            nameView.setNameFirst(adapter.isNameFirst());
            quotedNameView.setNameFirst(adapter.isNameFirst());

            mediaPreview.setStyle(adapter.getMediaPreviewStyle());

            quotedTextView.setCustomSelectionActionModeCallback(new StatusActionModeCallback(quotedTextView, activity));
            textView.setCustomSelectionActionModeCallback(new StatusActionModeCallback(textView, activity));

            final LinearLayoutManager layoutManager = new LinearLayoutManager(adapter.getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            countsUsersView.setLayoutManager(layoutManager);

            final CountsUsersAdapter countsUsersAdapter = new CountsUsersAdapter(fragment, adapter);
            countsUsersView.setAdapter(countsUsersAdapter);
            final Resources resources = activity.getResources();
            countsUsersView.addItemDecoration(new SpacingItemDecoration(resources.getDimensionPixelOffset(R.dimen.element_spacing_normal)));

        }


        private static class CountsUsersAdapter extends BaseRecyclerViewAdapter<ViewHolder> {
            private static final int ITEM_VIEW_TYPE_USER = 1;
            private static final int ITEM_VIEW_TYPE_COUNT = 2;

            private static final int KEY_REPLY_COUNT = 1;
            private static final int KEY_RETWEET_COUNT = 2;
            private static final int KEY_FAVORITE_COUNT = 3;

            private final LayoutInflater mInflater;
            private final StatusFragment mFragment;
            private final StatusAdapter mStatusAdapter;

            @Nullable
            private List<LabeledCount> mCounts;
            @Nullable
            private List<ParcelableUser> mUsers;

            public CountsUsersAdapter(StatusFragment fragment, StatusAdapter statusAdapter) {
                super(statusAdapter.getContext());
                mFragment = fragment;
                mInflater = LayoutInflater.from(statusAdapter.getContext());
                mStatusAdapter = statusAdapter;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                switch (getItemViewType(position)) {
                    case ITEM_VIEW_TYPE_USER: {
                        ((ProfileImageViewHolder) holder).displayUser(getUser(position));
                        break;
                    }
                    case ITEM_VIEW_TYPE_COUNT: {
                        ((CountViewHolder) holder).displayCount(getCount(position));
                        break;
                    }
                }
            }

            private LabeledCount getCount(int position) {
                if (mCounts == null) return null;
                if (position < getCountItemsCount()) {
                    return mCounts.get(position);
                }
                return null;
            }

            @Override
            public int getItemCount() {
                return getCountItemsCount() + getUsersCount();
            }


            @Override
            public int getItemViewType(int position) {
                final int countItemsCount = getCountItemsCount();
                if (position < countItemsCount) {
                    return ITEM_VIEW_TYPE_COUNT;
                }
                return ITEM_VIEW_TYPE_USER;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case ITEM_VIEW_TYPE_USER:
                        return new ProfileImageViewHolder(this, mInflater.inflate(R.layout.adapter_item_status_interact_user, parent, false));
                    case ITEM_VIEW_TYPE_COUNT:
                        return new CountViewHolder(this, mInflater.inflate(R.layout.adapter_item_status_count_label, parent, false));
                }
                throw new UnsupportedOperationException("Unsupported viewType " + viewType);
            }

            public void setUsers(@Nullable List<ParcelableUser> users) {
                mUsers = users;
                notifyDataSetChanged();
            }


            public void setCounts(@Nullable StatusActivity activity) {
                if (activity != null) {
                    ArrayList<LabeledCount> counts = new ArrayList<>();
                    final long replyCount = activity.getReplyCount();
                    if (replyCount > 0) {
                        counts.add(new LabeledCount(KEY_REPLY_COUNT, replyCount));
                    }
                    final long retweetCount = activity.getRetweetCount();
                    if (retweetCount > 0) {
                        counts.add(new LabeledCount(KEY_RETWEET_COUNT, retweetCount));
                    }
                    final long favoriteCount = activity.getFavoriteCount();
                    if (favoriteCount > 0) {
                        counts.add(new LabeledCount(KEY_FAVORITE_COUNT, favoriteCount));
                    }
                    mCounts = counts;
                } else {
                    mCounts = null;
                }
                notifyDataSetChanged();
            }

            public void setCounts(@Nullable ParcelableStatus status) {
                if (status != null) {
                    ArrayList<LabeledCount> counts = new ArrayList<>();
                    if (status.reply_count > 0) {
                        counts.add(new LabeledCount(KEY_REPLY_COUNT, status.reply_count));
                    }
                    if (status.retweet_count > 0) {
                        counts.add(new LabeledCount(KEY_RETWEET_COUNT, status.retweet_count));
                    }
                    if (status.favorite_count > 0) {
                        counts.add(new LabeledCount(KEY_FAVORITE_COUNT, status.favorite_count));
                    }
                    mCounts = counts;
                } else {
                    mCounts = null;
                }
                notifyDataSetChanged();
            }

            public boolean isProfileImageEnabled() {
                return mStatusAdapter.isProfileImageEnabled();
            }

            public float getTextSize() {
                return mStatusAdapter.getTextSize();
            }

            public int getCountItemsCount() {
                if (mCounts == null) return 0;
                return mCounts.size();
            }

            protected int getUsersCount() {
                if (mUsers == null) return 0;
                return mUsers.size();
            }

            private void notifyItemClick(int position) {
                switch (getItemViewType(position)) {
                    case ITEM_VIEW_TYPE_COUNT: {
                        final LabeledCount count = getCount(position);
                        final ParcelableStatus status = mStatusAdapter.getStatus();
                        if (count == null || status == null) return;
                        switch (count.type) {
                            case KEY_RETWEET_COUNT: {
                                if (status.is_retweet) {
                                    IntentUtils.openStatusRetweeters(getContext(), status.account_key,
                                            status.retweet_id);
                                } else {
                                    IntentUtils.openStatusRetweeters(getContext(), status.account_key,
                                            status.id);
                                }
                                break;
                            }
                            case KEY_FAVORITE_COUNT: {
                                final ParcelableCredentials account = mStatusAdapter.getStatusAccount();
                                if (!Utils.isOfficialCredentials(getContext(), account)) return;
                                if (status.is_retweet) {
                                    IntentUtils.openStatusFavoriters(getContext(), status.account_key,
                                            status.retweet_id);
                                } else {
                                    IntentUtils.openStatusFavoriters(getContext(), status.account_key,
                                            status.id);
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case ITEM_VIEW_TYPE_USER: {
                        mFragment.onUserClick(getUser(position));
                        break;
                    }
                }
            }

            private ParcelableUser getUser(int position) {
                final int countItemsCount = getCountItemsCount();
                if (mUsers == null || position < countItemsCount) return null;
                return mUsers.get(position - countItemsCount);
            }


            static class ProfileImageViewHolder extends ViewHolder implements OnClickListener {

                private final CountsUsersAdapter adapter;
                private final ImageView profileImageView;

                public ProfileImageViewHolder(CountsUsersAdapter adapter, View itemView) {
                    super(itemView);
                    itemView.setOnClickListener(this);
                    this.adapter = adapter;
                    profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
                }

                public void displayUser(ParcelableUser item) {
                    adapter.getMediaLoader().displayProfileImage(profileImageView, item);
                }

                @Override
                public void onClick(View v) {
                    adapter.notifyItemClick(getLayoutPosition());
                }
            }

            static class CountViewHolder extends ViewHolder implements OnClickListener {
                private final TextView countView;
                private final TextView labelView;
                private final CountsUsersAdapter adapter;

                public CountViewHolder(CountsUsersAdapter adapter, View itemView) {
                    super(itemView);
                    itemView.setOnClickListener(this);
                    this.adapter = adapter;
                    final float textSize = adapter.getTextSize();
                    countView = (TextView) itemView.findViewById(R.id.count);
                    labelView = (TextView) itemView.findViewById(R.id.label);
                    countView.setTextSize(textSize * 1.25f);
                    labelView.setTextSize(textSize * 0.85f);
                }

                @Override
                public void onClick(View v) {
                    adapter.notifyItemClick(getLayoutPosition());
                }

                public void displayCount(LabeledCount count) {
                    final String label;
                    switch (count.type) {
                        case KEY_REPLY_COUNT: {
                            label = adapter.getContext().getString(R.string.replies);
                            break;
                        }
                        case KEY_RETWEET_COUNT: {
                            label = adapter.getContext().getString(R.string.retweets);
                            break;
                        }
                        case KEY_FAVORITE_COUNT: {
                            label = adapter.getContext().getString(R.string.favorites);
                            break;
                        }
                        default: {
                            throw new UnsupportedOperationException("Unsupported type " + count.type);
                        }
                    }
                    countView.setText(Utils.getLocalizedNumber(Locale.getDefault(), count.count));
                    labelView.setText(label);
                }
            }

            static class LabeledCount {
                int type;
                long count;

                public LabeledCount(int type, long count) {
                    this.type = type;
                    this.count = count;
                }
            }
        }

        private static class DetailStatusLinkClickHandler extends StatusLinkClickHandler {
            private final StatusAdapter adapter;

            public DetailStatusLinkClickHandler(Context context, MultiSelectManager manager,
                                                StatusAdapter adapter,
                                                SharedPreferencesWrapper preferences) {
                super(context, manager, preferences);
                this.adapter = adapter;
            }

            @Override
            public boolean onLinkClick(final String link, final String orig, final UserKey accountKey,
                                       long extraId, int type, boolean sensitive, int start, int end) {
                ParcelableMedia current = getCurrentMedia(link, (int) extraId);
                if (current != null && !current.open_browser) {
                    expandOrOpenMedia(current);
                    return true;
                }
                return super.onLinkClick(link, orig, accountKey, extraId, type, sensitive, start, end);
            }

            private void expandOrOpenMedia(ParcelableMedia current) {
                if (adapter.isDetailMediaExpanded()) {
                    IntentUtils.openMedia(adapter.getContext(), adapter.getStatus(), current, null,
                            preferences.getBoolean(KEY_NEW_DOCUMENT_API));
                    return;
                }
                adapter.setDetailMediaExpanded(true);
            }

            @Override
            protected boolean isMedia(String link, long extraId) {
                final ParcelableMedia current = getCurrentMedia(link, (int) extraId);
                return current != null && !current.open_browser;
            }

            private ParcelableMedia getCurrentMedia(String link, int extraId) {
                final ParcelableStatus status = adapter.getStatus(extraId);
                final ParcelableMedia[] media = ParcelableMediaUtils.getAllMedia(status);
                return StatusLinkClickHandler.findByLink(media, link);
            }
        }

        private static class SpacingItemDecoration extends RecyclerView.ItemDecoration {
            private final int spacing;

            public SpacingItemDecoration(int spacing) {
                this.spacing = spacing;
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (ViewCompat.getLayoutDirection(parent) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    outRect.set(spacing, 0, 0, 0);
                } else {
                    outRect.set(0, 0, spacing, 0);
                }
            }
        }
    }

    private static class SpaceViewHolder extends ViewHolder {

        public SpaceViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class StatusAdapter extends LoadMoreSupportAdapter<ViewHolder>
            implements IStatusesAdapter<List<ParcelableStatus>> {

        private static final int VIEW_TYPE_LIST_STATUS = 0;
        private static final int VIEW_TYPE_DETAIL_STATUS = 1;
        private static final int VIEW_TYPE_CONVERSATION_LOAD_INDICATOR = 2;
        private static final int VIEW_TYPE_REPLIES_LOAD_INDICATOR = 3;
        private static final int VIEW_TYPE_REPLY_ERROR = 4;
        private static final int VIEW_TYPE_CONVERSATION_ERROR = 5;
        private static final int VIEW_TYPE_SPACE = 6;

        private static final int ITEM_IDX_CONVERSATION_LOAD_MORE = 0;
        private static final int ITEM_IDX_CONVERSATION_ERROR = 1;
        private static final int ITEM_IDX_CONVERSATION = 2;
        private static final int ITEM_IDX_STATUS = 3;
        private static final int ITEM_IDX_REPLY = 4;
        private static final int ITEM_IDX_REPLY_ERROR = 5;
        private static final int ITEM_IDX_REPLY_LOAD_MORE = 6;
        private static final int ITEM_IDX_SPACE = 7;
        private static final int ITEM_TYPES_SUM = 8;


        private final StatusFragment mFragment;
        private final LayoutInflater mInflater;
        private final MediaLoadingHandler mMediaLoadingHandler;
        private final TwidereLinkify mTwidereLinkify;

        private StatusClickListener mStatusClickListener;
        private RecyclerView mRecyclerView;
        private DetailStatusViewHolder mStatusViewHolder;

        private final int[] mItemCounts;

        private final boolean mNameFirst;
        private final int mTextSize;
        private final int mCardBackgroundColor;
        private final int mProfileImageStyle;
        private final int mMediaPreviewStyle;
        private final int mLinkHighlightingStyle;
        private final boolean mDisplayMediaPreview;
        private final boolean mDisplayProfileImage;
        private final boolean mSensitiveContentEnabled;
        private final boolean mShowCardActions;
        private final boolean mUseStarsForLikes;
        private final boolean mShowAbsoluteTime;
        private boolean mDetailMediaExpanded;

        private ParcelableStatus mStatus;
        private TranslationResult mTranslationResult;
        private StatusActivity mStatusActivity;
        private ParcelableCredentials mStatusAccount;

        private List<ParcelableStatus> mData;
        private CharSequence mReplyError, mConversationError;
        private int mReplyStart;
        private int mShowingActionCardPosition;

        public StatusAdapter(StatusFragment fragment) {
            super(fragment.getContext());
            setHasStableIds(true);
            final Context context = fragment.getActivity();
            final Resources res = context.getResources();
            mItemCounts = new int[ITEM_TYPES_SUM];
            // There's always a space at the end of the list
            mItemCounts[ITEM_IDX_SPACE] = 1;
            mItemCounts[ITEM_IDX_STATUS] = 1;
            mItemCounts[ITEM_IDX_CONVERSATION_LOAD_MORE] = 1;
            mItemCounts[ITEM_IDX_REPLY_LOAD_MORE] = 1;
            mFragment = fragment;
            mInflater = LayoutInflater.from(context);
            mMediaLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
            mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                    ThemeUtils.getThemeBackgroundOption(context),
                    ThemeUtils.getUserThemeBackgroundAlpha(context));
            mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
            mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, res.getInteger(R.integer.default_text_size));
            mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
            mLinkHighlightingStyle = Utils.getLinkHighlightingStyleInt(mPreferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
            mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
            mDisplayMediaPreview = Utils.isMediaPreviewEnabled(context, mPreferences);
            mSensitiveContentEnabled = mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
            mShowCardActions = !mPreferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
            mUseStarsForLikes = mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK);
            mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME);
            final StatusAdapterLinkClickHandler<List<ParcelableStatus>> listener = new StatusAdapterLinkClickHandler<>(context,
                    mPreferences);
            listener.setAdapter(this);
            mTwidereLinkify = new TwidereLinkify(listener);
        }

        public int findPositionById(long itemId) {
            for (int i = 0, j = getItemCount(); i < j; i++) {
                if (getItemId(i) == itemId) return i;
            }
            return RecyclerView.NO_POSITION;
        }

        @Override
        public int getProfileImageStyle() {
            return mProfileImageStyle;
        }

        @Override
        public float getTextSize() {
            return mTextSize;
        }

        @Override
        public boolean isProfileImageEnabled() {
            return mDisplayProfileImage;
        }

        public StatusFragment getFragment() {
            return mFragment;
        }

        @Override
        public int getLinkHighlightingStyle() {
            return mLinkHighlightingStyle;
        }

        @Override
        public int getMediaPreviewStyle() {
            return mMediaPreviewStyle;
        }

        @Override
        public ParcelableStatus getStatus(int position) {
            final int itemType = getItemType(position);
            switch (itemType) {
                case ITEM_IDX_CONVERSATION: {
                    if (mData == null) return null;
                    return mData.get(position - getIndexStart(ITEM_IDX_CONVERSATION));
                }
                case ITEM_IDX_REPLY: {
                    if (mData == null || mReplyStart < 0) return null;
                    return mData.get(position - getIndexStart(ITEM_IDX_CONVERSATION)
                            - getTypeCount(ITEM_IDX_CONVERSATION) - getTypeCount(ITEM_IDX_STATUS)
                            + mReplyStart);
                }
                case ITEM_IDX_STATUS: {
                    return mStatus;
                }
            }
            return null;
        }

        public int getIndexStart(int index) {
            if (index == 0) return 0;
            return TwidereMathUtils.sum(mItemCounts, 0, index - 1);
        }

        @Nullable
        @Override
        public String getStatusId(int position) {
            final ParcelableStatus status = getStatus(position);
            return status != null ? status.id : null;
        }

        @Override
        public long getStatusTimestamp(int position) {
            final ParcelableStatus status = getStatus(position);
            return status != null ? status.timestamp : -1;
        }

        @Override
        public long getStatusPositionKey(int position) {
            final ParcelableStatus status = getStatus(position);
            if (status == null) return -1;
            return status.position_key > 0 ? status.timestamp : getStatusTimestamp(position);
        }

        @Override
        public UserKey getAccountKey(int position) {
            final ParcelableStatus status = getStatus(position);
            return status != null ? status.account_key : null;
        }

        @Override
        public ParcelableStatus findStatusById(UserKey accountId, String statusId) {
            if (mStatus != null && accountId.equals(mStatus.account_key) && TextUtils.equals(statusId, mStatus.id)) {
                return mStatus;
            }
            for (ParcelableStatus status : Nullables.list(mData)) {
                if (accountId.equals(status.account_key) && TextUtils.equals(status.id, statusId))
                    return status;
            }
            return null;
        }

        @Override
        public int getStatusCount() {
            return getRawStatusCount();
        }

        @Override
        public int getRawStatusCount() {
            return getTypeCount(ITEM_IDX_CONVERSATION) + getTypeCount(ITEM_IDX_STATUS)
                    + getTypeCount(ITEM_IDX_REPLY);
        }

        @Override
        public TwidereLinkify getTwidereLinkify() {
            return mTwidereLinkify;
        }

        @Override
        public boolean isCardActionsShown(int position) {
            if (position == RecyclerView.NO_POSITION) return mShowCardActions;
            return mShowCardActions || mShowingActionCardPosition == position;
        }

        @Override
        public void showCardActions(int position) {
            if (mShowingActionCardPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(mShowingActionCardPosition);
            }
            mShowingActionCardPosition = position;
            if (position != RecyclerView.NO_POSITION) {
                notifyItemChanged(position);
            }
        }

        @Override
        public boolean isMediaPreviewEnabled() {
            return mDisplayMediaPreview;
        }

        @Override
        public boolean isNameFirst() {
            return mNameFirst;
        }

        @Override
        public boolean isSensitiveContentEnabled() {
            return mSensitiveContentEnabled;
        }

        @Override
        public boolean isShowAbsoluteTime() {
            return mShowAbsoluteTime;
        }

        @Override
        public boolean setData(List<ParcelableStatus> data) {
            final ParcelableStatus status = mStatus;
            if (status == null) return false;
            boolean changed = !CompareUtils.objectEquals(mData, data);
            mData = data;
            if (data == null || data.isEmpty()) {
                setTypeCount(ITEM_IDX_CONVERSATION, 0);
                setTypeCount(ITEM_IDX_REPLY, 0);
                mReplyStart = -1;
            } else {
                long sortId = status.sort_id;
                if (status.is_retweet) {
                    for (ParcelableStatus item : data) {
                        if (TextUtils.equals(status.retweet_id, item.id)) {
                            sortId = item.sort_id;
                            break;
                        }
                    }
                }
                int conversationCount = 0, replyCount = 0;
                int replyStart = -1;
                for (int i = 0, j = data.size(); i < j; i++) {
                    ParcelableStatus item = data.get(i);
                    if (item.sort_id < sortId) {
                        conversationCount++;
                    } else if (item.sort_id > sortId) {
                        if (replyStart < 0) {
                            replyStart = i;
                        }
                        replyCount++;
                    }
                }
                setTypeCount(ITEM_IDX_CONVERSATION, conversationCount);
                setTypeCount(ITEM_IDX_REPLY, replyCount);
                mReplyStart = replyStart;
            }
            notifyDataSetChanged();
            updateItemDecoration();
            return changed;
        }

        @Override
        public boolean shouldShowAccountsColor() {
            return false;
        }

        @Override
        public boolean shouldUseStarsForLikes() {
            return mUseStarsForLikes;
        }

        @Override
        public MediaLoadingHandler getMediaLoadingHandler() {
            return mMediaLoadingHandler;
        }

        @Nullable
        @Override
        public StatusClickListener getStatusClickListener() {
            return mStatusClickListener;
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

        @Nullable
        @Override
        public GapClickListener getGapClickListener() {
            return mStatusClickListener;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_DETAIL_STATUS: {
                    if (mStatusViewHolder != null) {
                        return mStatusViewHolder;
                    }
                    final View view = mInflater.inflate(R.layout.header_status_compact, parent, false);
                    final View cardView = view.findViewById(R.id.compact_card);
                    cardView.setBackgroundColor(mCardBackgroundColor);
                    return new DetailStatusViewHolder(this, view);
                }
                case VIEW_TYPE_LIST_STATUS: {
                    final View view = mInflater.inflate(R.layout.card_item_status_compact, parent, false);
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
                    return new SpaceViewHolder(new Space(getContext()));
                }
                case VIEW_TYPE_REPLY_ERROR: {
                    final View view = mInflater.inflate(R.layout.adapter_item_status_error, parent,
                            false);
                    return new StatusErrorItemViewHolder(view);
                }
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final int itemType = getItemType(position);
            final int itemViewType = getItemViewTypeByItemType(itemType);
            switch (itemViewType) {
                case VIEW_TYPE_DETAIL_STATUS: {
                    final ParcelableStatus status = getStatus(position);
                    final DetailStatusViewHolder detailHolder = (DetailStatusViewHolder) holder;
                    detailHolder.displayStatus(getStatusAccount(), status, mStatusActivity,
                            getTranslationResult());
                    break;
                }
                case VIEW_TYPE_LIST_STATUS: {
                    final ParcelableStatus status = getStatus(position);
                    final IStatusViewHolder statusHolder = (IStatusViewHolder) holder;
                    // Display 'in reply to' for first item
                    // useful to indicate whether first tweet has reply or not
                    // We only display that indicator for first conversation item
                    statusHolder.displayStatus(status, itemType == ITEM_IDX_CONVERSATION
                            && (position - getItemTypeStart(position)) == 0);
                    break;
                }
                case VIEW_TYPE_REPLY_ERROR: {
                    final StatusErrorItemViewHolder errorHolder = (StatusErrorItemViewHolder) holder;
                    errorHolder.showError(mReplyError);
                    break;
                }
                case VIEW_TYPE_CONVERSATION_ERROR: {
                    final StatusErrorItemViewHolder errorHolder = (StatusErrorItemViewHolder) holder;
                    errorHolder.showError(mConversationError);
                    break;
                }
                case VIEW_TYPE_CONVERSATION_LOAD_INDICATOR: {
                    LoadIndicatorViewHolder indicatorHolder = ((LoadIndicatorViewHolder) holder);
                    indicatorHolder.setLoadProgressVisible(isConversationsLoading());
                    break;
                }
                case VIEW_TYPE_REPLIES_LOAD_INDICATOR: {
                    LoadIndicatorViewHolder indicatorHolder = ((LoadIndicatorViewHolder) holder);
                    indicatorHolder.setLoadProgressVisible(isRepliesLoading());
                    break;
                }
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (holder instanceof DetailStatusViewHolder) {
                mStatusViewHolder = (DetailStatusViewHolder) holder;
            }
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            if (holder == mStatusViewHolder) {
                mStatusViewHolder = null;
            }
            super.onViewAttachedToWindow(holder);
        }


        private TranslationResult getTranslationResult() {
            return mTranslationResult;
        }

        public void setTranslationResult(@Nullable TranslationResult translation) {
            if (mStatus == null || translation == null || !TextUtils.equals(InternalTwitterContentUtils
                    .getOriginalId(mStatus), translation.getId())) {
                mTranslationResult = null;
            } else {
                mTranslationResult = translation;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeByItemType(getItemType(position));
        }

        private int getItemViewTypeByItemType(int type) {
            switch (type) {
                case ITEM_IDX_CONVERSATION:
                case ITEM_IDX_REPLY:
                    return VIEW_TYPE_LIST_STATUS;
                case ITEM_IDX_CONVERSATION_LOAD_MORE:
                    return VIEW_TYPE_CONVERSATION_LOAD_INDICATOR;
                case ITEM_IDX_REPLY_LOAD_MORE:
                    return VIEW_TYPE_REPLIES_LOAD_INDICATOR;
                case ITEM_IDX_STATUS:
                    return VIEW_TYPE_DETAIL_STATUS;
                case ITEM_IDX_SPACE:
                    return VIEW_TYPE_SPACE;
                case ITEM_IDX_REPLY_ERROR:
                    return VIEW_TYPE_REPLY_ERROR;
                case ITEM_IDX_CONVERSATION_ERROR:
                    return VIEW_TYPE_CONVERSATION_ERROR;
            }
            throw new IllegalStateException();
        }

        private int getItemType(int position) {
            int typeStart = 0;
            for (int type = 0; type < ITEM_TYPES_SUM; type++) {
                int typeCount = getTypeCount(type);
                final int typeEnd = typeStart + typeCount;
                if (position >= typeStart && position < typeEnd) return type;
                typeStart = typeEnd;
            }
            throw new IllegalStateException("Unknown position " + position);
        }

        private int getItemTypeStart(int position) {
            int typeStart = 0;
            for (int type = 0; type < ITEM_TYPES_SUM; type++) {
                int typeCount = getTypeCount(type);
                final int typeEnd = typeStart + typeCount;
                if (position >= typeStart && position < typeEnd) return typeStart;
                typeStart = typeEnd;
            }
            throw new IllegalStateException();
        }

        @Override
        public long getItemId(int position) {
            final ParcelableStatus status = getStatus(position);
            if (status != null) return status.hashCode();
            return getItemType(position);
        }

        @Override
        public int getItemCount() {
            if (mStatus == null) return 0;
            return TwidereMathUtils.sum(mItemCounts);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            mRecyclerView = recyclerView;
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            mRecyclerView = null;
        }

        private void setTypeCount(int idx, int size) {
            mItemCounts[idx] = size;
            notifyDataSetChanged();
        }

        public int getTypeCount(int idx) {
            return mItemCounts[idx];
        }

        public void setStatusClickListener(StatusClickListener listener) {
            mStatusClickListener = listener;
        }

        public void setReplyError(CharSequence error) {
            mReplyError = error;
            setTypeCount(ITEM_IDX_REPLY_ERROR, error != null ? 1 : 0);
            updateItemDecoration();
        }

        public void setConversationError(CharSequence error) {
            mConversationError = error;
            setTypeCount(ITEM_IDX_CONVERSATION_ERROR, error != null ? 1 : 0);
            updateItemDecoration();
        }

        public boolean setStatus(final ParcelableStatus status, final ParcelableCredentials credentials) {
            final ParcelableStatus old = mStatus;
            mStatus = status;
            mStatusAccount = credentials;
            notifyDataSetChanged();
            updateItemDecoration();
            return !CompareUtils.objectEquals(old, status);
        }

        private void updateItemDecoration() {
            if (mRecyclerView == null) return;
        }

        public void setRepliesLoading(boolean loading) {
            if (loading) {
                setLoadMoreIndicatorPosition(getLoadMoreIndicatorPosition() | IndicatorPosition.END);
            } else {
                setLoadMoreIndicatorPosition(getLoadMoreIndicatorPosition() & ~IndicatorPosition.END);
            }
            updateItemDecoration();
        }

        public void setConversationsLoading(boolean loading) {
            if (loading) {
                setLoadMoreIndicatorPosition(getLoadMoreIndicatorPosition() | IndicatorPosition.START);
            } else {
                setLoadMoreIndicatorPosition(getLoadMoreIndicatorPosition() & ~IndicatorPosition.START);
            }
            updateItemDecoration();
        }

        public int getFirstPositionOfItem(int itemIdx) {
            int position = 0;
            for (int i = 0; i < ITEM_TYPES_SUM; i++) {
                if (itemIdx == i) return position;
                position += getTypeCount(i);
            }
            return RecyclerView.NO_POSITION;
        }

        public void setStatusActivity(StatusActivity activity) {
            final ParcelableStatus status = getStatus();
            if (status == null) return;
            if (activity != null && activity.isStatus(status)) {
                return;
            }
            mStatusActivity = activity;
            notifyDataSetChanged();
        }

        public List<ParcelableStatus> getData() {
            return mData;
        }

        public boolean isConversationsLoading() {
            return IndicatorPositionUtils.has(getLoadMoreIndicatorPosition(), IndicatorPosition.START);
        }

        public boolean isRepliesLoading() {
            return IndicatorPositionUtils.has(getLoadMoreIndicatorPosition(), IndicatorPosition.END);
        }

        public static class StatusErrorItemViewHolder extends ViewHolder {
            private final TextView textView;

            public StatusErrorItemViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(android.R.id.text1);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setLinksClickable(true);
            }

            public void showError(CharSequence text) {
                textView.setText(text);
            }
        }
    }

    private static class StatusListLinearLayoutManager extends FixedLinearLayoutManager {

        private final RecyclerView recyclerView;
        private int mSpaceHeight;

        public StatusListLinearLayoutManager(Context context, RecyclerView recyclerView) {
            super(context);
            setOrientation(LinearLayoutManager.VERTICAL);
            this.recyclerView = recyclerView;
        }

        @Override
        public int getDecoratedMeasuredHeight(View child) {
            int heightBeforeSpace = 0;
            if (getItemViewType(child) == StatusAdapter.VIEW_TYPE_SPACE) {
                for (int i = 0, j = getChildCount(); i < j; i++) {
                    final View childToMeasure = getChildAt(i);
                    final LayoutParams paramsToMeasure = (LayoutParams) childToMeasure.getLayoutParams();
                    final int typeToMeasure = getItemViewType(childToMeasure);
                    if (typeToMeasure == StatusAdapter.VIEW_TYPE_SPACE) {
                        break;
                    }
                    if (typeToMeasure == StatusAdapter.VIEW_TYPE_DETAIL_STATUS || heightBeforeSpace != 0) {
                        heightBeforeSpace += super.getDecoratedMeasuredHeight(childToMeasure)
                                + paramsToMeasure.topMargin + paramsToMeasure.bottomMargin;
                    }
                }
                if (heightBeforeSpace != 0) {
                    final int spaceHeight = recyclerView.getMeasuredHeight() - heightBeforeSpace;
                    return mSpaceHeight = Math.max(0, spaceHeight);
                }
            }
            return super.getDecoratedMeasuredHeight(child);
        }

        @Override
        public void setOrientation(int orientation) {
            if (orientation != VERTICAL)
                throw new IllegalArgumentException("Only VERTICAL orientation supported");
            super.setOrientation(orientation);
        }


        @Override
        public int computeVerticalScrollExtent(RecyclerView.State state) {
            final int firstPosition = findFirstVisibleItemPosition();
            final int lastPosition = Math.min(getValidScrollItemCount() - 1, findLastVisibleItemPosition());
            if (firstPosition < 0 || lastPosition < 0) return 0;
            int childCount = lastPosition - firstPosition + 1;
            if (childCount > 0) {
                if (isSmoothScrollbarEnabled()) {
                    int extent = childCount * 100;
                    View view = findViewByPosition(firstPosition);
                    final int top = view.getTop();
                    int height = view.getHeight();
                    if (height > 0) {
                        extent += (top * 100) / height;
                    }

                    view = findViewByPosition(lastPosition);
                    final int bottom = view.getBottom();
                    height = view.getHeight();
                    if (height > 0) {
                        extent -= ((bottom - getHeight()) * 100) / height;
                    }
                    return extent;
                } else {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int computeVerticalScrollOffset(RecyclerView.State state) {
            final int firstPosition = findFirstVisibleItemPosition();
            final int lastPosition = Math.min(getValidScrollItemCount() - 1, findLastVisibleItemPosition());
            if (firstPosition < 0 || lastPosition < 0) return 0;
            int childCount = lastPosition - firstPosition + 1;
            final int skippedCount = getSkippedScrollItemCount();
            if (firstPosition >= skippedCount && childCount > 0) {
                if (isSmoothScrollbarEnabled()) {
                    final View view = findViewByPosition(firstPosition);
                    final int top = view.getTop();
                    int height = view.getHeight();
                    if (height > 0) {
                        return Math.max((firstPosition - skippedCount) * 100 - (top * 100) / height, 0);
                    }
                } else {
                    int index;
                    final int count = getValidScrollItemCount();
                    if (firstPosition == 0) {
                        index = 0;
                    } else if (firstPosition + childCount == count) {
                        index = count;
                    } else {
                        index = firstPosition + childCount / 2;
                    }
                    return (int) (firstPosition + childCount * (index / (float) count));
                }
            }
            return 0;
        }

        @Override
        public int computeVerticalScrollRange(RecyclerView.State state) {
            int result;
            if (isSmoothScrollbarEnabled()) {
                result = Math.max(getValidScrollItemCount() * 100, 0);
            } else {
                result = getValidScrollItemCount();
            }
            return result;
        }

        private int getSkippedScrollItemCount() {
            final StatusAdapter adapter = (StatusAdapter) recyclerView.getAdapter();
            int skipped = 0;
            if (!adapter.isConversationsLoading()) {
                skipped += adapter.getTypeCount(StatusAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE);
            }
            return skipped;
        }

        private int getValidScrollItemCount() {
            final StatusAdapter adapter = (StatusAdapter) recyclerView.getAdapter();
            int count = 0;
            if (adapter.isConversationsLoading()) {
                count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE);
            }
            count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_CONVERSATION_ERROR);
            count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_CONVERSATION);
            count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_STATUS);
            count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_REPLY);
            count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_REPLY_ERROR);
            final int spaceHeight = getSpaceHeight();
            if (adapter.isRepliesLoading()) {
                count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_REPLY_LOAD_MORE);
            }
            if (spaceHeight > 0) {
                count += adapter.getTypeCount(StatusAdapter.ITEM_IDX_SPACE);
            }
            return count;
        }

        private int getSpaceHeight() {
            final View space = findViewByPosition(getItemCount() - 1);
            if (space == null) return mSpaceHeight;
            return getDecoratedMeasuredHeight(space);
        }

    }

    public static class StatusActivitySummaryLoader extends AsyncTaskLoader<StatusActivity> {
        private final UserKey mAccountKey;
        private final String mStatusId;

        public StatusActivitySummaryLoader(Context context, UserKey accountKey, String statusId) {
            super(context);
            mAccountKey = accountKey;
            mStatusId = statusId;
        }

        @Override
        public StatusActivity loadInBackground() {
            final Context context = getContext();
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                    mAccountKey);
            if (credentials == null || !ParcelableAccount.Type.TWITTER.equals(ParcelableAccountUtils.getAccountType(credentials))) {
                return null;
            }
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(context, mAccountKey, false);
            if (twitter == null) return null;
            final Paging paging = new Paging();
            paging.setCount(10);
            final StatusActivity activitySummary = new StatusActivity(mStatusId);
            final List<ParcelableUser> retweeters = new ArrayList<>();
            try {
                for (Status status : twitter.getRetweets(mStatusId, paging)) {
                    final ParcelableUser user = ParcelableUserUtils.fromUser(status.getUser(), mAccountKey);
                    if (!DataStoreUtils.isFilteringUser(context, user.key.toString())) {
                        retweeters.add(user);
                    }
                }
                activitySummary.setRetweeters(retweeters);
                final ContentValues countValues = new ContentValues();
                final Status status = twitter.showStatus(mStatusId);
                activitySummary.setFavoriteCount(status.getFavoriteCount());
                activitySummary.setRetweetCount(status.getRetweetCount());
                activitySummary.setReplyCount(status.getReplyCount());

                countValues.put(Statuses.REPLY_COUNT, activitySummary.replyCount);
                countValues.put(Statuses.FAVORITE_COUNT, activitySummary.favoriteCount);
                countValues.put(Statuses.RETWEET_COUNT, activitySummary.retweetCount);

                final ContentResolver cr = context.getContentResolver();
                final Expression statusWhere = Expression.and(
                        Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                        Expression.or(
                                Expression.equalsArgs(Statuses.STATUS_ID),
                                Expression.equalsArgs(Statuses.RETWEET_ID)
                        ));
                final String[] statusWhereArgs = {mAccountKey.toString(), mStatusId, mStatusId};
                cr.update(Statuses.CONTENT_URI, countValues, statusWhere.getSQL(), statusWhereArgs);
                final Expression activityWhere = Expression.and(
                        Expression.equalsArgs(Activities.ACCOUNT_KEY),
                        Expression.or(
                                Expression.equalsArgs(Activities.STATUS_ID),
                                Expression.equalsArgs(Activities.STATUS_RETWEET_ID)
                        ));

                final ParcelableStatus pStatus = ParcelableStatusUtils.fromStatus(status,
                        mAccountKey, false);
                cr.insert(CachedStatuses.CONTENT_URI, ParcelableStatusValuesCreator.create(pStatus));

                final Cursor activityCursor = cr.query(Activities.AboutMe.CONTENT_URI,
                        Activities.COLUMNS, activityWhere.getSQL(), statusWhereArgs, null);
                assert activityCursor != null;
                try {
                    activityCursor.moveToFirst();
                    ParcelableActivityCursorIndices ci = new ParcelableActivityCursorIndices(activityCursor);
                    while (!activityCursor.isAfterLast()) {
                        final ParcelableActivity activity = ci.newObject(activityCursor);
                        ParcelableStatus activityStatus = ParcelableActivityUtils.getActivityStatus(activity);
                        if (activityStatus != null) {
                            activityStatus.favorite_count = activitySummary.favoriteCount;
                            activityStatus.reply_count = activitySummary.replyCount;
                            activityStatus.retweet_count = activitySummary.retweetCount;
                        }
                        cr.update(Activities.AboutMe.CONTENT_URI, ParcelableActivityValuesCreator.create(activity),
                                Expression.equals(Activities._ID, activity._id).getSQL(), null);
                        activityCursor.moveToNext();
                    }
                } finally {
                    activityCursor.close();
                }
                return activitySummary;
            } catch (MicroBlogException e) {
                return null;
            }
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    static class StatusActivity {

        List<ParcelableUser> retweeters;

        String statusId;

        long favoriteCount;
        long replyCount = -1;
        long retweetCount;

        public StatusActivity(String statusId) {
            this.statusId = statusId;
        }

        public List<ParcelableUser> getRetweeters() {
            return retweeters;
        }

        public void setRetweeters(List<ParcelableUser> retweeters) {
            this.retweeters = retweeters;
        }

        public long getFavoriteCount() {
            return favoriteCount;
        }

        public void setFavoriteCount(long favoriteCount) {
            this.favoriteCount = favoriteCount;
        }

        public long getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(long repliersCount) {
            this.replyCount = repliersCount;
        }

        public long getRetweetCount() {
            return retweetCount;
        }

        public void setRetweetCount(long retweetCount) {
            this.retweetCount = retweetCount;
        }

        @Override
        public String toString() {
            return "StatusActivity{" +
                    "retweeters=" + retweeters +
                    ", statusId=" + statusId +
                    ", favoriteCount=" + favoriteCount +
                    ", replyCount=" + replyCount +
                    ", retweetCount=" + retweetCount +
                    '}';
        }

        public boolean isStatus(ParcelableStatus status) {
            return TextUtils.equals(statusId, status.is_retweet ? status.retweet_id : status.id);
        }
    }

    static class ReadPosition {
        long statusId;
        int offsetTop;

        public ReadPosition(long statusId, int offsetTop) {
            this.statusId = statusId;
            this.offsetTop = offsetTop;
        }

        @Override
        public String toString() {
            return "ReadPosition{" +
                    "statusId=" + statusId +
                    ", offsetTop=" + offsetTop +
                    '}';
        }
    }

    private static class StatusDividerItemDecoration extends DividerItemDecoration {
        private final StatusAdapter statusAdapter;

        public StatusDividerItemDecoration(Context context, StatusAdapter statusAdapter, int orientation) {
            super(context, orientation);
            this.statusAdapter = statusAdapter;
        }

        @Override
        protected boolean isDividerEnabled(int childPos) {
            if (childPos >= statusAdapter.getItemCount() || childPos < 0) return false;
            final int itemType = statusAdapter.getItemType(childPos);
            switch (itemType) {
                case StatusAdapter.ITEM_IDX_REPLY_LOAD_MORE:
                case StatusAdapter.ITEM_IDX_REPLY_ERROR:
                case StatusAdapter.ITEM_IDX_SPACE:
                    return false;
            }
            return true;
        }

    }
}