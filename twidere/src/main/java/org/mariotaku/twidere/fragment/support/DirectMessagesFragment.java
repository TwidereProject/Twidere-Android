/*
 * 				Twidere - Twitter client for Android
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.DirectMessageEntry;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.MessageEntriesAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.BaseRefreshTaskParam;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.message.GetMessagesTaskEvent;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DirectMessagesFragment extends AbsContentListRecyclerViewFragment<MessageEntriesAdapter>
        implements LoaderCallbacks<Cursor>, MessageEntriesAdapterListener, KeyboardShortcutCallback {

    // Listeners
    private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
            this, 0, this);

    private RemoveUnreadCountsTask mRemoveUnreadCountsTask;
    private RecyclerViewNavigationHelper mNavigationHelper;

    // Data fields
    private final LongSparseArray<Set<Long>> mUnreadCountsToRemove = new LongSparseArray<>();
    private final Set<Integer> mReadPositions = Collections.synchronizedSet(new HashSet<Integer>());
    private int mFirstVisibleItem;

    @NonNull
    @Override
    protected MessageEntriesAdapter onCreateAdapter(Context context, boolean compact) {
        return new MessageEntriesAdapter(context);
    }

    @Override
    public void onLoadMoreContents(@IndicatorPosition int position) {
        // Only supports load from end, so remove START flag
        position &= ~IndicatorPosition.START;
        if (position == 0) return;
        loadMoreMessages();
    }

    @Override
    public void setControlVisible(boolean visible) {
        final FragmentActivity activity = getActivity();
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).setControlBarVisibleAnimate(visible);
        }
    }

    @Override
    public boolean isRefreshing() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        return twitter != null && (twitter.isReceivedDirectMessagesRefreshing() || twitter.isSentDirectMessagesRefreshing());
    }

    public final LongSparseArray<Set<Long>> getUnreadCountsToRemove() {
        return mUnreadCountsToRemove;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, @NonNull final KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_REFRESH.equals(action)) {
            triggerRefresh();
            return true;
        }
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_REFRESH.equals(action) || mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = DirectMessages.ConversationEntries.CONTENT_URI;
        final AccountKey[] accountIds = getAccountKeys();
        final Expression account_where = Expression.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(accountIds));
        return new CursorLoader(getActivity(), uri, null, account_where.getSQL(), null, null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (getActivity() == null) return;
        final boolean isEmpty = cursor != null && cursor.getCount() == 0;
        mFirstVisibleItem = -1;
        final MessageEntriesAdapter adapter = getAdapter();
        adapter.setCursor(cursor);
        adapter.setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
        adapter.setLoadMoreSupportedPosition(hasMoreData(cursor) ? IndicatorPosition.END : IndicatorPosition.NONE);
        final AccountKey[] accountIds = getAccountKeys();
        adapter.setShowAccountsColor(accountIds.length > 1);
        setRefreshEnabled(true);

        if (accountIds.length > 0) {
            final ErrorInfoStore.DisplayErrorInfo errorInfo = ErrorInfoStore.getErrorInfo(getContext(),
                    mErrorInfoStore.get(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountIds[0]));
            if (isEmpty && errorInfo != null) {
                showEmpty(errorInfo.getIcon(), errorInfo.getMessage());
            } else {
                showContent();
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.no_account_selected));
        }
    }

    protected boolean hasMoreData(final Cursor cursor) {
        return cursor != null && cursor.getCount() != 0;
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        final MessageEntriesAdapter adapter = getAdapter();
        adapter.setCursor(null);
    }

    @Override
    public void onEntryClick(int position, DirectMessageEntry entry) {
        Utils.openMessageConversation(getActivity(), new AccountKey(entry.account_id,
                entry.account_host), entry.conversation_id);
    }

    @Override
    public void onUserClick(int position, DirectMessageEntry entry) {
        IntentUtils.openUserProfile(getActivity(), entry.account_id, entry.conversation_id,
                entry.screen_name, null, true, null);
    }

    @Subscribe
    public void onGetMessagesTaskChanged(GetMessagesTaskEvent event) {
        if (event.uri.equals(Inbox.CONTENT_URI) && !event.running) {
            setRefreshing(false);
            setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
            setRefreshEnabled(true);
        }
    }

    @Override
    public void onRefresh() {
        triggerRefresh();
    }

    @Override
    public boolean scrollToStart() {
        final boolean result = super.scrollToStart();
        if (result) {
            final AsyncTwitterWrapper twitter = mTwitterWrapper;
            final int tabPosition = getTabPosition();
            if (twitter != null && tabPosition >= 0) {
                twitter.clearUnreadCountAsync(tabPosition);
            }
        }
        return result;
    }

    @Override
    public boolean triggerRefresh() {
        AsyncTaskUtils.executeTask(new AsyncTask<Object, Object, RefreshTaskParam>() {

            @Override
            protected RefreshTaskParam doInBackground(final Object... params) {
                final Context context = getContext();
                if (context == null) return null;
                AccountKey[] accountIds = getAccountKeys();
                long[] ids = DataStoreUtils.getNewestMessageIds(context,
                        DirectMessages.Inbox.CONTENT_URI, accountIds);
                return new BaseRefreshTaskParam(accountIds, ids, null);
            }

            @Override
            protected void onPostExecute(final RefreshTaskParam result) {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (twitter == null || result == null) return;
                twitter.getReceivedDirectMessagesAsync(result);
                twitter.getSentDirectMessagesAsync(new BaseRefreshTaskParam(result.getAccountKeys(), null, null));
            }

        });
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_direct_messages, menu);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(getActivity() instanceof LinkHandlerActivity);
        final View view = getView();
        assert view != null;
        final Context viewContext = view.getContext();
        final MessageEntriesAdapter adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this);

        adapter.setListener(this);

        final DividerItemDecoration itemDecoration = new DividerItemDecoration(viewContext, layoutManager.getOrientation());
        final Resources res = viewContext.getResources();
        final int decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 3
                + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image);
        itemDecoration.setPadding(decorPaddingLeft, 0, 0, 0);
        itemDecoration.setDecorationEndOffset(1);
        recyclerView.addItemDecoration(itemDecoration);
        getLoaderManager().initLoader(0, null, this);
        showProgress();
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver);
        mBus.register(this);
        final MessageEntriesAdapter adapter = getAdapter();
        adapter.updateReadState();
        updateRefreshState();
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mReloadContentObserver);
        super.onStop();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compose: {
                openNewMessageConversation();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void openNewMessageConversation() {
        final AccountKey[] accountIds = getAccountKeys();
        if (accountIds.length == 1) {
            Utils.openMessageConversation(getActivity(), accountIds[0], -1);
        } else {
            Utils.openMessageConversation(getActivity(), null, -1);
        }
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        final FragmentActivity activity = getActivity();
        if (isVisibleToUser && activity != null) {
            for (AccountKey accountKey : getAccountKeys()) {
                final String tag = "messages_" + accountKey;
                mNotificationManager.cancel(tag, NOTIFICATION_ID_DIRECT_MESSAGES);
            }
        }
    }

    @NonNull
    protected AccountKey[] getAccountKeys() {
        final Bundle args = getArguments();
        AccountKey[] accountKeys = Utils.getAccountKeys(args);
        if (accountKeys != null) {
            return accountKeys;
        }
        final FragmentActivity activity = getActivity();
        if (activity instanceof HomeActivity) {
            return ((HomeActivity) activity).getActivatedAccountKeys();
        }
        return DataStoreUtils.getActivatedAccountKeys(getActivity());
    }

    protected void updateRefreshState() {
        setRefreshing(mTwitterWrapper.isReceivedDirectMessagesRefreshing() || mTwitterWrapper.isSentDirectMessagesRefreshing());
    }

    private void addReadPosition(final int firstVisibleItem) {
        if (mFirstVisibleItem != firstVisibleItem) {
            mReadPositions.add(firstVisibleItem);
        }
        mFirstVisibleItem = firstVisibleItem;
    }

    private void addUnreadCountsToRemove(final long accountId, final long id) {
        if (mUnreadCountsToRemove.indexOfKey(accountId) < 0) {
            final Set<Long> counts = new HashSet<>();
            counts.add(id);
            mUnreadCountsToRemove.put(accountId, counts);
        } else {
            final Set<Long> counts = mUnreadCountsToRemove.get(accountId);
            counts.add(id);
        }
    }

    private void loadMoreMessages() {
        if (isRefreshing()) return;
        setLoadMoreIndicatorPosition(IndicatorPosition.END);
        setRefreshEnabled(false);
        AsyncTaskUtils.executeTask(new AsyncTask<Object, Object, RefreshTaskParam[]>() {

            @Override
            protected RefreshTaskParam[] doInBackground(final Object... params) {
                final Context context = getContext();
                if (context == null) return null;
                RefreshTaskParam[] result = new RefreshTaskParam[2];
                AccountKey[] accountKeys = getAccountKeys();
                result[0] = new BaseRefreshTaskParam(accountKeys, DataStoreUtils.getOldestMessageIds(context,
                        DirectMessages.Inbox.CONTENT_URI, accountKeys), null);
                result[1] = new BaseRefreshTaskParam(accountKeys, DataStoreUtils.getOldestMessageIds(context,
                        DirectMessages.Outbox.CONTENT_URI, accountKeys), null);
                return result;
            }

            @Override
            protected void onPostExecute(final RefreshTaskParam[] result) {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (twitter == null || result == null) return;
                twitter.getReceivedDirectMessagesAsync(result[0]);
                twitter.getSentDirectMessagesAsync(result[1]);
            }

        });
    }

    private void removeUnreadCounts() {
        if (mRemoveUnreadCountsTask != null && mRemoveUnreadCountsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        mRemoveUnreadCountsTask = new RemoveUnreadCountsTask(mReadPositions, this);
        AsyncTaskUtils.executeTask(mRemoveUnreadCountsTask);
    }

    static class RemoveUnreadCountsTask extends AsyncTask<Object, Object, Object> {
        private final Set<Integer> read_positions;
        private final MessageEntriesAdapter adapter;
        private final DirectMessagesFragment fragment;

        RemoveUnreadCountsTask(final Set<Integer> read_positions, final DirectMessagesFragment fragment) {
            this.read_positions = Collections.synchronizedSet(new HashSet<>(read_positions));
            this.fragment = fragment;
            adapter = fragment.getAdapter();
        }

        @Override
        protected Object doInBackground(final Object... params) {
            for (final int pos : read_positions) {
                final DirectMessageEntry entry = adapter.getEntry(pos);
                final long id = entry.conversation_id, account_id = entry.account_id;
                fragment.addUnreadCountsToRemove(account_id, id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Object result) {
            final AsyncTwitterWrapper twitter = fragment.mTwitterWrapper;
            if (twitter != null) {
                twitter.removeUnreadCountsAsync(fragment.getTabPosition(), fragment.getUnreadCountsToRemove());
            }
        }

    }

}
