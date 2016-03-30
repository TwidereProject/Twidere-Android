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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.UserListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.UserListMembersChangedEvent;
import org.mariotaku.twidere.model.util.ParcelableUserListUtils;
import org.mariotaku.twidere.view.ExtendedRecyclerView;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.Arrays;
import java.util.List;

public class UserListMembersFragment extends CursorSupportUsersListFragment {

    private ParcelableUserList mUserList;

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context,
                                                        @NonNull final Bundle args, boolean fromUser) {
        final UserKey accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String listId = args.getString(EXTRA_LIST_ID);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final UserListMembersLoader loader = new UserListMembersLoader(context, accountId, listId,
                userKey, screenName, listName, getData(), fromUser);
        loader.setCursor(getNextCursor());
        loader.setPage(getNextPage());
        return loader;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getRecyclerView());
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER_LIST, getUserList());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onUserLongClick(UserViewHolder holder, int position) {
        return getRecyclerView().showContextMenuForChild(holder.itemView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!getUserVisibleHint() || menuInfo == null) return;
        final ParcelableUserList userList = getUserList();
        if (userList == null) return;
        final Bundle args = getArguments();
        final UserKey accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        if (accountId == null || !accountId.equals(userKey)) return;
        final ParcelableUsersAdapter adapter = getAdapter();
        final MenuInflater inflater = new MenuInflater(getContext());
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) menuInfo;
        inflater.inflate(R.menu.action_user_list_member, menu);
        final ParcelableUser user = adapter.getUser(contextMenuInfo.getPosition());
        menu.setHeaderTitle(mUserColorNameManager.getDisplayName(user, mPreferences.getBoolean(KEY_NAME_FIRST)));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        final ParcelableUserList userList = getUserList();
        if (userList == null) return false;
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) item.getMenuInfo();
        final ParcelableUser user = getAdapter().getUser(contextMenuInfo.getPosition());
        if (user == null) return false;
        switch (item.getItemId()) {
            case R.id.delete_from_list: {
                DeleteUserListMembersDialogFragment.show(getFragmentManager(), getUserList(), user);
                return true;
            }
        }
        return false;
    }

    public ParcelableUserList getUserList() {
        Fragment parent = getParentFragment();
        if (parent instanceof UserListFragment) {
            return ((UserListFragment) parent).getUserList();
        }
        return null;
    }

    @Subscribe
    public void onUserListMembersChanged(UserListMembersChangedEvent event) {
        ParcelableUserList userList = event.getUserList();
        final Bundle args = getArguments();
        final UserKey accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String listId = args.getString(EXTRA_LIST_ID);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        if (!ParcelableUserListUtils.check(userList, accountId, listId, userKey, screenName, listName)) {
            return;
        }
        switch (event.getAction()) {
            case UserListMembersChangedEvent.Action.ADDED: {
                final ParcelableUsersAdapter adapter = getAdapter();
                final List<ParcelableUser> newUsers = Arrays.asList(event.getUsers());
                final List<ParcelableUser> users = adapter.getData();
                users.removeAll(newUsers);
                users.addAll(0, newUsers);
                for (int i = 0, j = users.size(); i < j; i++) {
                    users.get(i).position = i;
                }
                adapter.notifyDataSetChanged();
                break;
            }
            case UserListMembersChangedEvent.Action.REMOVED: {
                final ParcelableUsersAdapter adapter = getAdapter();
                final List<ParcelableUser> removedUsers = Arrays.asList(event.getUsers());
                final List<ParcelableUser> users = adapter.getData();
                users.removeAll(removedUsers);
                for (int i = 0, j = users.size(); i < j; i++) {
                    users.get(i).position = i;
                }
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}
