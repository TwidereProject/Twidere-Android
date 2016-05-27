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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import org.mariotaku.twidere.adapter.ParcelableUserListsAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter.UserListClickListener;
import org.mariotaku.twidere.loader.iface.ICursorSupportLoader;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.view.holder.UserListViewHolder;

import java.util.List;

public abstract class ParcelableUserListsFragment extends AbsContentListRecyclerViewFragment<ParcelableUserListsAdapter>
        implements LoaderCallbacks<List<ParcelableUserList>>, UserListClickListener, KeyboardShortcutCallback {

    private RecyclerViewNavigationHelper mNavigationHelper;
    private long mNextCursor;
    private long mPrevCursor;

    @Override
    public boolean isRefreshing() {
        if (getContext() == null || isDetached()) return false;
        final LoaderManager lm = getLoaderManager();
        return lm.hasRunningLoaders();
    }

    @NonNull
    @Override
    protected final ParcelableUserListsAdapter onCreateAdapter(Context context) {
        return new ParcelableUserListsAdapter(context);
    }

    @Override
    protected void setupRecyclerView(Context context) {
        super.setupRecyclerView(context);
    }

    @Nullable
    protected UserKey getAccountKey() {
        final Bundle args = getArguments();
        return args.getParcelable(EXTRA_ACCOUNT_KEY);
    }

    protected boolean hasMoreData(List<ParcelableUserList> data) {
        return data == null || !data.isEmpty();
    }

    public void onLoadFinished(Loader<List<ParcelableUserList>> loader, List<ParcelableUserList> data) {
        final ParcelableUserListsAdapter adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupportedPosition(hasMoreData(data) ? IndicatorPosition.END : IndicatorPosition.NONE);
            setRefreshEnabled(true);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        if (loader instanceof ICursorSupportLoader) {
            mNextCursor = ((ICursorSupportLoader) loader).getNextCursor();
            mPrevCursor = ((ICursorSupportLoader) loader).getNextCursor();
        }
        showContent();
        setRefreshEnabled(true);
        setRefreshing(false);
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    @Override
    public void onLoadMoreContents(@IndicatorPosition int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        loaderArgs.putLong(EXTRA_NEXT_CURSOR, getNextCursor());
        getLoaderManager().restartLoader(0, loaderArgs, this);
    }

    protected void removeUsers(long... ids) {
        //TODO remove from adapter
    }

    public final List<ParcelableUserList> getData() {
        return getAdapter().getData();
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ParcelableUserListsAdapter adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        adapter.setUserListClickListener(this);

        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
    }

    @Override
    public final Loader<List<ParcelableUserList>> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateUserListsLoader(getActivity(), args, fromUser);
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableUserList>> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onUserListClick(UserListViewHolder holder, int position) {
        final ParcelableUserList userList = getAdapter().getUserList(position);
        if (userList == null) return;
        IntentUtils.openUserListDetails(getActivity(), userList);
    }

    public long getPrevCursor() {
        return mPrevCursor;
    }

    public long getNextCursor() {
        return mNextCursor;
    }

    @Override
    public boolean onUserListLongClick(UserListViewHolder holder, int position) {
        return true;
    }

    protected abstract Loader<List<ParcelableUserList>> onCreateUserListsLoader(Context context, Bundle args, boolean fromUser);
}
