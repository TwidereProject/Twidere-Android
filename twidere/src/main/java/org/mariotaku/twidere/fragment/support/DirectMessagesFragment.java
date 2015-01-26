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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.DirectMessageEntry;
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.MessageEntriesAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.task.TwidereAsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.openMessageConversation;

public class DirectMessagesFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>, RefreshScrollTopInterface, OnRefreshListener, MessageEntriesAdapterListener {

    private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
            this, 0, this);
    private final LongSparseArray<Set<Long>> mUnreadCountsToRemove = new LongSparseArray<>();
    private final Set<Integer> mReadPositions = Collections.synchronizedSet(new HashSet<Integer>());
    private MultiSelectManager mMultiSelectManager;
    private SharedPreferences mPreferences;
    private RecyclerView mRecyclerView;
    private MessageEntriesAdapter mAdapter;
    private int mFirstVisibleItem;
    private RemoveUnreadCountsTask mRemoveUnreadCountsTask;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mContentView;
    private View mProgressContainer;
    private LinearLayoutManager mLayoutManager;

    public final LongSparseArray<Set<Long>> getUnreadCountsToRemove() {
        return mUnreadCountsToRemove;
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
//        updateRefreshState();
    }

    @Override
    public void onEntryClick(int position, DirectMessageEntry entry) {
        Utils.openMessageConversation(getActivity(), entry.account_id, entry.conversation_id);
    }

    @Override
    public void onRefresh() {

    }

    private void setListShown(boolean shown) {
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mContentView = view.findViewById(R.id.fragment_content);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = DirectMessages.ConversationEntries.CONTENT_URI;
        final long account_id = getAccountId();
        final long[] account_ids = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(getActivity());
        final boolean no_account_selected = account_ids.length == 0;
//        setEmptyText(no_account_selected ? getString(R.string.no_account_selected) : null);
//        if (!no_account_selected) {
//            getListView().setEmptyView(null);
//        }
        final Expression account_where = Expression.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(account_ids));
        return new CursorLoader(getActivity(), uri, null, account_where.getSQL(), null, null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (getActivity() == null) return;
        mFirstVisibleItem = -1;
        mAdapter.setCursor(cursor);
//        mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

//    @Override
//    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
//        if (mMultiSelectManager.isActive()) return;
//        final int pos = position - l.getHeaderViewsCount();
//        final long conversationId = mAdapter.getConversationId(pos);
//        final long accountId = mAdapter.getAccountId(pos);
//        mReadPositions.add(pos);
//        removeUnreadCounts();
//        if (conversationId > 0 && accountId > 0) {
//            openMessageConversation(getActivity(), accountId, conversationId);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        if (view == null) throw new AssertionError();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Context viewContext = view.getContext();
        mMultiSelectManager = getMultiSelectManager();
        mAdapter = new MessageEntriesAdapter(viewContext);
        mAdapter.setListener(this);
        mLayoutManager = new LinearLayoutManager(viewContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ThemeUtils.getUserAccentColor(viewContext));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(viewContext, mLayoutManager.getOrientation());
        final Resources res = viewContext.getResources();
        final int decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 3
                + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image);
        itemDecoration.setPadding(decorPaddingLeft, 0, 0, 0);
        mRecyclerView.addItemDecoration(itemDecoration);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver);
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
    }

//    @Override
//    public void onRefresh() {
//        if (isRefreshing()) return;
//        new TwidereAsyncTask<Void, Void, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Void... params) {
//                final long[][] result = new long[2][];
//                result[0] = getActivatedAccountIds(getActivity());
//                result[1] = getNewestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(final long[][] result) {
//                final AsyncTwitterWrapper twitter = getTwitterWrapper();
//                if (twitter == null) return;
//                twitter.getReceivedDirectMessagesAsync(result[0], null, result[1]);
//                twitter.getSentDirectMessagesAsync(result[0], null, null);
//            }
//
//        }.executeTask();
//    }


//    @Override
//    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
//                         final int totalItemCount) {
//        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
//        addReadPosition(firstVisibleItem);
//    }
//
//    @Override
//    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
//        switch (scrollState) {
//            case SCROLL_STATE_FLING:
//            case SCROLL_STATE_TOUCH_SCROLL: {
//                break;
//            }
//            case SCROLL_STATE_IDLE: {
//                for (int i = mRecyclerView.getFirstVisiblePosition(), j = mRecyclerView.getLastVisiblePosition(); i < j; i++) {
//                    mReadPositions.add(i);
//                }
//                removeUnreadCounts();
//                break;
//            }
//        }
//    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mReloadContentObserver);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_COMPOSE: {
                openMessageConversation(getActivity(), -1, -1);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean scrollToStart() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final int tabPosition = getTabPosition();
        if (twitter != null && tabPosition >= 0) {
            twitter.clearUnreadCountAsync(tabPosition);
        }
        mRecyclerView.smoothScrollToPosition(0);
        return true;
    }

    @Override
    public boolean triggerRefresh() {
        return true;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
//            updateRefreshState();
        }
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mContentView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

//    @Override
//    protected void onListTouched() {
//        final AsyncTwitterWrapper twitter = getTwitterWrapper();
//        if (twitter != null) {
//            twitter.clearNotificationAsync(NOTIFICATION_ID_DIRECT_MESSAGES, getAccountId());
//        }
//    }

    protected long getAccountId() {
        final Bundle args = getArguments();
        return args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
    }

//    @Override
//    protected void onReachedBottom() {
//        loadMoreMessages();
//    }

//    protected void updateRefreshState() {
//        final AsyncTwitterWrapper twitter = getTwitterWrapper();
//        if (twitter == null || !getUserVisibleHint()) return;
//        setRefreshing(twitter.isReceivedDirectMessagesRefreshing() || twitter.isSentDirectMessagesRefreshing());
//    }

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
//
//    private void loadMoreMessages() {
//        if (isRefreshing()) return;
//        new TwidereAsyncTask<Void, Void, long[][]>() {
//
//            @Override
//            protected long[][] doInBackground(final Void... params) {
//                final long[][] result = new long[3][];
//                result[0] = getActivatedAccountIds(getActivity());
//                result[1] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
//                result[2] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(final long[][] result) {
//                final AsyncTwitterWrapper twitter = getTwitterWrapper();
//                if (twitter == null) return;
//                twitter.getReceivedDirectMessagesAsync(result[0], result[1], null);
//                twitter.getSentDirectMessagesAsync(result[0], result[2], null);
//            }
//
//        }.executeTask();
//    }

    private MessageEntriesAdapter getAdapter() {
        return mAdapter;
    }

    private void removeUnreadCounts() {
        if (mRemoveUnreadCountsTask != null && mRemoveUnreadCountsTask.getStatus() == TwidereAsyncTask.Status.RUNNING)
            return;
        mRemoveUnreadCountsTask = new RemoveUnreadCountsTask(mReadPositions, this);
        mRemoveUnreadCountsTask.executeTask();
    }

    static class RemoveUnreadCountsTask extends TwidereAsyncTask<Void, Void, Void> {
        private final Set<Integer> read_positions;
        private final MessageEntriesAdapter adapter;
        private final DirectMessagesFragment fragment;

        RemoveUnreadCountsTask(final Set<Integer> read_positions, final DirectMessagesFragment fragment) {
            this.read_positions = Collections.synchronizedSet(new HashSet<>(read_positions));
            this.fragment = fragment;
            adapter = fragment.getAdapter();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            for (final int pos : read_positions) {
                final DirectMessageEntry entry = adapter.getEntry(pos);
                final long id = entry.conversation_id, account_id = entry.account_id;
                fragment.addUnreadCountsToRemove(account_id, id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            final AsyncTwitterWrapper twitter = fragment.getTwitterWrapper();
            if (twitter != null) {
                twitter.removeUnreadCountsAsync(fragment.getTabPosition(), fragment.getUnreadCountsToRemove());
            }
        }

    }

}
