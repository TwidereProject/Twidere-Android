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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.adapter.iface.IStatusesListAdapter;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.Account.AccountWithCredentials;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.PositionManager;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.collection.NoDuplicatesCopyOnWriteArrayList;
import org.mariotaku.twidere.view.holder.StatusListViewHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mariotaku.twidere.util.Utils.cancelRetweet;
import static org.mariotaku.twidere.util.Utils.clearListViewChoices;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.showOkMessage;
import static org.mariotaku.twidere.util.Utils.startStatusShareChooser;

abstract class BaseStatusesListFragment<Data> extends BasePullToRefreshListFragment implements LoaderCallbacks<Data>,
        OnItemLongClickListener, OnMenuItemClickListener, Panes.Left, MultiSelectManager.Callback,
        MenuButtonClickListener {

    private AsyncTaskManager mAsyncTaskManager;
    private SharedPreferences mPreferences;

    private ListView mListView;
    private IStatusesListAdapter<Data> mAdapter;

    private Data mData;
    private ParcelableStatus mSelectedStatus;

    private boolean mLoadMoreAutomatically;
    private int mListScrollOffset;

    private MultiSelectManager mMultiSelectManager;
    private PositionManager mPositionManager;

    private int mFirstVisibleItem;
    private int mSelectedPosition;

    private final Map<Long, Set<Long>> mUnreadCountsToRemove = Collections
            .synchronizedMap(new HashMap<Long, Set<Long>>());
    private final List<Integer> mReadPositions = new NoDuplicatesCopyOnWriteArrayList<Integer>();

    private RemoveUnreadCountsTask<Data> mRemoveUnreadCountsTask;

    public AsyncTaskManager getAsyncTaskManager() {
        return mAsyncTaskManager;
    }

    public final Data getData() {
        return mData;
    }

    @Override
    public IStatusesListAdapter<Data> getListAdapter() {
        return mAdapter;
    }

    public ParcelableStatus getSelectedStatus() {
        return mSelectedStatus;
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }

    public abstract int getStatuses(long[] account_ids, long[] max_ids, long[] since_ids);

    public final Map<Long, Set<Long>> getUnreadCountsToRemove() {
        return mUnreadCountsToRemove;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getActivity();
        mAsyncTaskManager = getAsyncTaskManager();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPositionManager = new PositionManager(context);
        mMultiSelectManager = getMultiSelectManager();
        mListView = getListView();
        final boolean plainListStyle = mPreferences.getBoolean(KEY_PLAIN_LIST_STYLE, false);
        final boolean compactCards = mPreferences.getBoolean(KEY_COMPACT_CARDS, false);
        mAdapter = newAdapterInstance(compactCards, plainListStyle);
        mAdapter.setMenuButtonClickListener(this);
        setListAdapter(null);
        setListHeaderFooters(mListView);
        setListAdapter(mAdapter);
        if (!plainListStyle) {
            mListView.setDivider(null);
        }
        mListView.setSelector(android.R.color.transparent);
        mListView.setOnItemLongClickListener(this);
        setListShown(false);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public abstract Loader<Data> onCreateLoader(int id, Bundle args);

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Object tag = view.getTag();
        if (tag instanceof StatusListViewHolder) {
            final StatusListViewHolder holder = (StatusListViewHolder) tag;
            final ParcelableStatus status = mAdapter.getStatus(position - mListView.getHeaderViewsCount());
            final AsyncTwitterWrapper twitter = getTwitterWrapper();
            if (twitter != null) {
                TwitterWrapper.removeUnreadCounts(getActivity(), getTabPosition(), status.account_id, status.id);
            }
            if (holder.show_as_gap) return false;
            if (mPreferences.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false)) {
                openMenu(holder.content.getFakeOverflowButton(), status, position);
            } else {
                setItemSelected(status, position, !mMultiSelectManager.isSelected(status));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onItemsCleared() {
        clearListViewChoices(mListView);
    }

    @Override
    public void onItemSelected(final Object item) {
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void onItemUnselected(final Object item) {
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Object tag = v.getTag();
        if (tag instanceof StatusListViewHolder) {
            final int pos = position - l.getHeaderViewsCount();
            final ParcelableStatus status = mAdapter.getStatus(pos);
            if (status == null) return;
            final AsyncTwitterWrapper twitter = getTwitterWrapper();
            if (twitter != null) {
                TwitterWrapper.removeUnreadCounts(getActivity(), getTabPosition(), status.account_id, status.id);
            }
            if (((StatusListViewHolder) tag).show_as_gap) {
                final long since_id = position + 1 < mAdapter.getStatusCount() ? mAdapter.getStatus(pos + 1).id : -1;
                getStatuses(new long[]{status.account_id}, new long[]{status.id}, new long[]{since_id});
                mListView.setItemChecked(position, false);
            } else {
                if (mMultiSelectManager.isActive()) {
                    setItemSelected(status, position, !mMultiSelectManager.isSelected(status));
                    return;
                }
                openStatus(getActivity(), status.account_id, status.id);
            }
        }
    }

    @Override
    public final void onLoaderReset(final Loader<Data> loader) {
        mAdapter.setData(mData = null);
    }

    @Override
    public void onLoadFinished(final Loader<Data> loader, final Data data) {
        if (getActivity() == null || getView() == null) return;
        setListShown(true);
        setRefreshComplete();
        setProgressBarIndeterminateVisibility(false);
        setData(data);
        mFirstVisibleItem = -1;
        mReadPositions.clear();
        final int firstVisiblePosition = mListView.getFirstVisiblePosition();
        final int lastVisiblePosition = mListView.getLastVisiblePosition();
        final int listVisiblePosition, savedChildIndex;
        final boolean rememberPosition = mPreferences.getBoolean(KEY_REMEMBER_POSITION, true);
        final boolean loadMoreFromTop = mPreferences.getBoolean(KEY_LOAD_MORE_FROM_TOP, false);
        final int childCount = mListView.getChildCount();
        if (firstVisiblePosition != 0 && lastVisiblePosition != mListView.getCount() - 1 && loadMoreFromTop) {
            listVisiblePosition = lastVisiblePosition;
            savedChildIndex = childCount - 1;
            if (childCount > 0) {
                final View lastChild = mListView.getChildAt(savedChildIndex);
                mListScrollOffset = lastChild != null ? lastChild.getTop() - mListView.getListPaddingTop() : 0;
            }
        } else {
            listVisiblePosition = firstVisiblePosition;
            savedChildIndex = 0;
            if (childCount > 0) {
                final View firstChild = mListView.getChildAt(savedChildIndex);
                mListScrollOffset = firstChild != null ? firstChild.getTop() - mListView.getListPaddingTop() : 0;
            }
        }
        final long lastViewedId = mAdapter.getStatusId(listVisiblePosition);
        mAdapter.setData(data);
        mAdapter.setShowAccountColor(shouldShowAccountColor());
        final int currFirstVisiblePosition = mListView.getFirstVisiblePosition();
        final long currViewedId = mAdapter.getStatusId(currFirstVisiblePosition);
        final long statusId;
        if (lastViewedId <= 0) {
            if (!rememberPosition) return;
            statusId = mPositionManager.getPosition(getPositionKey());
        } else if ((listVisiblePosition > 0 || rememberPosition) && currViewedId > 0 && lastViewedId != currViewedId) {
            statusId = lastViewedId;
        } else {
            if (listVisiblePosition == 0 && mAdapter.getStatusId(0) != lastViewedId) {
                mAdapter.setMaxAnimationPosition(mListView.getLastVisiblePosition());
            }
            return;
        }
        final int position = mAdapter.findPositionByStatusId(statusId);
        if (position > -1 && position < mListView.getCount()) {
            mAdapter.setMaxAnimationPosition(mListView.getLastVisiblePosition());
            mListView.setSelectionFromTop(position, mListScrollOffset);
            mListScrollOffset = 0;
        }
    }

    @Override
    public void onMenuButtonClick(final View button, final int position, final long id) {
        if (mMultiSelectManager.isActive()) return;
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        openMenu(button, status, position);
    }

    @Override
    public final boolean onMenuItemClick(final MenuItem item) {
        final ParcelableStatus status = mSelectedStatus;
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (status == null || twitter == null) return false;
        switch (item.getItemId()) {
            case MENU_VIEW: {
                openStatus(getActivity(), status);
                break;
            }
            case MENU_SHARE: {
                startStatusShareChooser(getActivity(), status);
                break;
            }
            case MENU_COPY: {
                if (ClipboardUtils.setText(getActivity(), status.text_plain)) {
                    showOkMessage(getActivity(), R.string.text_copied, false);
                }
                break;
            }
            case MENU_RETWEET: {
                if (isMyRetweet(status)) {
                    cancelRetweet(twitter, status);
                } else {
                    final long id_to_retweet = status.retweet_id > 0 ? status.retweet_id : status.id;
                    twitter.retweetStatus(status.account_id, id_to_retweet);
                }
                break;
            }
            case MENU_QUOTE: {
                final Intent intent = new Intent(INTENT_ACTION_QUOTE);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_STATUS, status);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case MENU_REPLY: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_STATUS, status);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case MENU_FAVORITE: {
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
            case MENU_DELETE: {
                DestroyStatusDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case MENU_ADD_TO_FILTER: {
                AddStatusFilterDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case MENU_TRANSLATE: {
                final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(),
                        status.account_id);
                if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
                    StatusTranslateDialogFragment.show(getFragmentManager(), status);
                } else {

                }
                break;
            }
            case MENU_MULTI_SELECT: {
                final boolean isSelected = !mMultiSelectManager.isSelected(status);
                setItemSelected(status, mSelectedPosition, isSelected);
                break;
            }
            default: {
                if (item.getIntent() != null) {
                    try {
                        startActivity(item.getIntent());
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

    @Override
    public void onRefreshFromEnd() {
        if (mLoadMoreAutomatically) return;
        loadMoreStatuses();
    }

    @Override
    public void onResume() {
        super.onResume();
        mListView.setFastScrollEnabled(mPreferences.getBoolean(KEY_FAST_SCROLL_THUMB, false));
        configBaseCardAdapter(getActivity(), mAdapter);
        final boolean displayImagePreview = mPreferences.getBoolean(KEY_DISPLAY_IMAGE_PREVIEW, false);
        final boolean displaySensitiveContents = mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
        final boolean indicateMyStatus = mPreferences.getBoolean(KEY_INDICATE_MY_STATUS, true);
        final String cardHighlightOption = mPreferences.getString(KEY_CARD_HIGHLIGHT_OPTION,
                DEFAULT_CARD_HIGHLIGHT_OPTION);
        final String previewScaleType = Utils.getNonEmptyString(mPreferences, KEY_IMAGE_PREVIEW_SCALE_TYPE,
                ScaleType.CENTER_CROP.name());
        mAdapter.setDisplayImagePreview(displayImagePreview);
        mAdapter.setImagePreviewScaleType(previewScaleType);
        mAdapter.setDisplaySensitiveContents(displaySensitiveContents);
        mAdapter.setIndicateMyStatusDisabled(isMyTimeline() || !indicateMyStatus);
        mAdapter.setCardHighlightOption(cardHighlightOption);
        mAdapter.notifyDataSetChanged();
        mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                         final int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        addReadPosition(firstVisibleItem);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                for (int i = mListView.getFirstVisiblePosition(), j = mListView.getLastVisiblePosition(); i < j; i++) {
                    mReadPositions.add(i);
                }
                removeUnreadCounts();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mMultiSelectManager.registerCallback(this);
        final int choiceMode = mListView.getChoiceMode();
        if (mMultiSelectManager.isActive()) {
            if (choiceMode != ListView.CHOICE_MODE_MULTIPLE) {
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        } else {
            if (choiceMode != ListView.CHOICE_MODE_NONE) {
                Utils.clearListViewChoices(mListView);
            }
        }
        updateRefreshState();
    }

    @Override
    public void onStop() {
        savePosition();
        mMultiSelectManager.unregisterCallback(this);
        super.onStop();
    }

    @Override
    public boolean scrollToStart() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final int tab_position = getTabPosition();
        if (twitter != null && tab_position >= 0) {
            twitter.clearUnreadCountAsync(tab_position);
        }
        return super.scrollToStart();
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        updateRefreshState();
    }

    protected final int getListScrollOffset() {
        return mListScrollOffset;
    }

    protected abstract long[] getNewestStatusIds();

    protected abstract long[] getOldestStatusIds();

    protected abstract String getPositionKey();

    protected boolean isMyTimeline() {
        return false;
    }

    protected abstract void loadMoreStatuses();

    protected abstract IStatusesListAdapter<Data> newAdapterInstance(boolean compact, boolean plain);

    @Override
    protected void onReachedBottom() {
        if (!mLoadMoreAutomatically) return;
        loadMoreStatuses();
    }

    protected void savePosition() {
        final int first_visible_position = mListView.getFirstVisiblePosition();
        if (mListView.getChildCount() > 0) {
            final View first_child = mListView.getChildAt(0);
            mListScrollOffset = first_child != null ? first_child.getTop() : 0;
        }
        final long status_id = mAdapter.getStatusId(first_visible_position);
        mPositionManager.setPosition(getPositionKey(), status_id);
    }

    protected final void setData(final Data data) {
        mData = data;
    }

    protected void setItemSelected(final ParcelableStatus status, final int position, final boolean selected) {
        if (selected) {
            mMultiSelectManager.selectItem(status);
        } else {
            mMultiSelectManager.unselectItem(status);
        }
        if (position >= 0) {
            mListView.setItemChecked(position, selected);
        }
    }

    protected void setListHeaderFooters(final ListView list) {

    }

    protected boolean shouldEnablePullToRefresh() {
        return true;
    }

    protected abstract boolean shouldShowAccountColor();

    protected abstract void updateRefreshState();

    private void addReadPosition(final int firstVisibleItem) {
        if (mFirstVisibleItem != firstVisibleItem) {
            mReadPositions.add(firstVisibleItem);
        }
        mFirstVisibleItem = firstVisibleItem;
    }

    private void addUnreadCountsToRemove(final long account_id, final long id) {
        if (mUnreadCountsToRemove.containsKey(account_id)) {
            final Set<Long> counts = mUnreadCountsToRemove.get(account_id);
            counts.add(id);
        } else {
            final Set<Long> counts = new HashSet<Long>();
            counts.add(id);
            mUnreadCountsToRemove.put(account_id, counts);
        }
    }

    private void openMenu(final View view, final ParcelableStatus status, final int position) {
        mSelectedStatus = status;
        mSelectedPosition = position;
        final FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || view == null || status == null) return;
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter != null) {
            TwitterWrapper.removeUnreadCounts(getActivity(), getTabPosition(), status.account_id, status.id);
        }
        final StatusMenuDialogFragment df = new StatusMenuDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        df.setArguments(args);
        df.show(getChildFragmentManager(), "status_menu");
    }

    private void removeUnreadCounts() {
        if (mRemoveUnreadCountsTask != null && mRemoveUnreadCountsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        mRemoveUnreadCountsTask = new RemoveUnreadCountsTask<Data>(mReadPositions, this);
        mRemoveUnreadCountsTask.execute();
    }

    static class RemoveUnreadCountsTask<T> extends AsyncTask<Void, Void, Void> {
        private final List<Integer> read_positions;
        private final IStatusesListAdapter<T> adapter;
        private final BaseStatusesListFragment<T> fragment;

        RemoveUnreadCountsTask(final List<Integer> read_positions, final BaseStatusesListFragment<T> fragment) {
            this.read_positions = read_positions;
            this.fragment = fragment;
            this.adapter = fragment.getListAdapter();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            for (final int pos : read_positions) {
                final long id = adapter.getStatusId(pos), account_id = adapter.getAccountId(pos);
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
