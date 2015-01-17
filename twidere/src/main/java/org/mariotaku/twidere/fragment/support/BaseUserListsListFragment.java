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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.mariotaku.twidere.adapter.ParcelableUserListsListAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.loader.support.BaseUserListsLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;

import java.util.ArrayList;
import java.util.List;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;

abstract class BaseUserListsListFragment extends BasePullToRefreshListFragment implements
        LoaderCallbacks<List<ParcelableUserList>>, OnMenuItemClickListener, MenuButtonClickListener {

    private ParcelableUserListsListAdapter mAdapter;

    private SharedPreferences mPreferences;
    private ListView mListView;

    private long mAccountId, mUserId;
    private String mScreenName;
    private final ArrayList<ParcelableUserList> mData = new ArrayList<>();
    private ParcelableUserList mSelectedUserList;
    private long mCursor = -1;
    private boolean mLoadMoreAutomatically;

    private AsyncTwitterWrapper mTwitterWrapper;
    private MultiSelectManager mMultiSelectManager;

    public long getAccountId() {
        return mAccountId;
    }

    public long getCursor() {
        return mCursor;
    }

    public final ArrayList<ParcelableUserList> getData() {
        return mData;
    }

    @Override
    public ParcelableUserListsListAdapter getListAdapter() {
        return mAdapter;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public long getUserId() {
        return mUserId;
    }

    public void loadMoreUserLists() {
        final int count = mAdapter.getCount();
        if (count - 1 > 0) {
            final Bundle args = getArguments();
            if (args != null) {
                args.putLong(EXTRA_MAX_ID, mAdapter.getItem(count - 1).user_id);
            }
            if (!getLoaderManager().hasRunningLoaders()) {
                getLoaderManager().restartLoader(0, args, this);
            }
        }
    }

    public abstract Loader<List<ParcelableUserList>> newLoaderInstance(long accountId, long userId, String screen_name);

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTwitterWrapper = getTwitterWrapper();
        mMultiSelectManager = getMultiSelectManager();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Bundle args = getArguments();
        if (args != null) {
            mAccountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            mUserId = args.getLong(EXTRA_USER_ID, -1);
            mScreenName = args.getString(EXTRA_SCREEN_NAME);
        }
        mAdapter = new ParcelableUserListsListAdapter(getActivity());
        mListView = getListView();
        mListView.setFastScrollEnabled(mPreferences.getBoolean(KEY_FAST_SCROLL_THUMB, false));
        // final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
        // if (mAccountId != account_id) {
        // mAdapter.clear();
        // mData.clear();
        // }
        // mAccountId = account_id;
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, getArguments(), this);
        setListShown(false);
    }

    @Override
    public Loader<List<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
        setProgressBarIndeterminateVisibility(true);
        return newLoaderInstance(mAccountId, mUserId, mScreenName);
    }

    @Override
    public final void onListItemClick(final ListView view, final View child, final int position, final long id) {
        if (mMultiSelectManager.isActive()) return;
        final int userListPosition = mAdapter.findItemPosition(id);
        if (userListPosition < 0) return;
        openUserListDetails(getActivity(), mAdapter.getItem(userListPosition));
    }

    @Override
    public void onLoaderReset(final Loader<List<ParcelableUserList>> loader) {
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoadFinished(final Loader<List<ParcelableUserList>> loader, final List<ParcelableUserList> data) {
        setProgressBarIndeterminateVisibility(false);
        mAdapter.appendData(data);
        if (loader instanceof BaseUserListsLoader) {
            final long cursor = ((BaseUserListsLoader) loader).getNextCursor();
            if (cursor != -2) {
                mCursor = cursor;
            }
        }
        setRefreshing(false);
        setListShown(true);
    }

    @Override
    public void onMenuButtonClick(final View button, final int position, final long id) {
        final ParcelableUserList userList = mAdapter.getItem(position - mListView.getHeaderViewsCount());
        if (userList == null) return;
        showMenu(button, userList);
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        if (mSelectedUserList == null) return false;
        switch (item.getItemId()) {
            case MENU_ADD: {
                AddUserListMemberDialogFragment.show(getFragmentManager(), mSelectedUserList.account_id,
                        mSelectedUserList.id);
                break;
            }
            case MENU_DELETE: {
                mTwitterWrapper.destroyUserListAsync(mSelectedUserList.account_id, mSelectedUserList.id);
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
    public void onRefresh() {
        if (isRefreshing()) return;
        getLoaderManager().restartLoader(0, getArguments(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
        configBaseCardAdapter(getActivity(), mAdapter);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    }

    @Override
    protected void onReachedBottom() {
        if (!mLoadMoreAutomatically) return;
        loadMoreUserLists();
    }

    private void showMenu(final View view, final ParcelableUserList userList) {
        mSelectedUserList = userList;
        //TODO show menu
    }

}
