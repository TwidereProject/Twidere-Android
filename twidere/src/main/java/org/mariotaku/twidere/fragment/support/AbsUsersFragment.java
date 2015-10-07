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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import org.mariotaku.twidere.adapter.AbsUsersAdapter;
import org.mariotaku.twidere.adapter.AbsUsersAdapter.UserAdapterListener;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.UserViewHolder;

abstract class AbsUsersFragment<Data> extends AbsContentRecyclerViewFragment<AbsUsersAdapter<Data>>
        implements LoaderCallbacks<Data>, UserAdapterListener, KeyboardShortcutCallback {

    private RecyclerViewNavigationHelper mNavigationHelper;

    public final Data getData() {
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

        final FragmentActivity activity = getActivity();
        final AbsUsersAdapter<Data> adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        adapter.setListener(this);

        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
    }

    @Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateUsersLoader(getActivity(), args, fromUser);
    }

    @Override
    public void onLoadFinished(Loader<Data> loader, Data data) {
        final AbsUsersAdapter<Data> adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupported(hasMoreData(data));
            setRefreshEnabled(true);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        showContent();
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onUserClick(UserViewHolder holder, int position) {
        final ParcelableUser user = getAdapter().getUser(position);
        final FragmentActivity activity = getActivity();
        final View profileImageView = holder.getProfileImageView();
        final View profileTypeView = holder.getProfileTypeView();
        final Bundle options = Utils.makeSceneTransitionOption(activity,
                new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                new Pair<>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
        Utils.openUserProfile(activity, user.account_id, user.id, user.screen_name, options);
    }


    @Override
    public boolean onUserLongClick(UserViewHolder holder, int position) {

        return true;
    }

    protected ParcelableUser getSelectedUser() {
        //TODO return selected
        return null;
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract Loader<Data> onCreateUsersLoader(Context context, Bundle args, boolean fromUser);

}
