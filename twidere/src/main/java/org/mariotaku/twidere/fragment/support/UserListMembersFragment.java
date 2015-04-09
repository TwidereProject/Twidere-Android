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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.support.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.support.UserListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.AsyncTaskUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

public class UserListMembersFragment extends CursorSupportUsersListFragment implements OnMenuItemClickListener {

    private ParcelableUserList mUserList;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            if (BROADCAST_USER_LIST_MEMBERS_DELETED.equals(action)) {
                final ParcelableUserList list = intent.getParcelableExtra(EXTRA_USER_LIST);
                if (mUserList != null && list != null && list.id == mUserList.id) {
                    removeUsers(intent.getLongArrayExtra(EXTRA_USER_IDS));
                }
            }
        }
    };

    @Override
    public CursorSupportUsersLoader newLoaderInstance(final Context context, final Bundle args) {
        if (args == null) return null;
        final long listId = args.getLong(EXTRA_LIST_ID, -1);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        return new UserListMembersLoader(context, accountId, listId, userId, screenName, listName,
                getNextCursor(), getData());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        final Bundle args = getArguments();
        if (savedInstanceState != null) {
            mUserList = savedInstanceState.getParcelable(EXTRA_USER_LIST);
        } else if (args != null) {
            mUserList = args.getParcelable(EXTRA_USER_LIST);
        }
        super.onActivityCreated(savedInstanceState);
        if (mUserList == null && args != null) {
            final long listId = args.getLong(EXTRA_LIST_ID, -1);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            final String listName = args.getString(EXTRA_LIST_NAME);
            AsyncTaskUtils.executeTask(new GetUserListTask(accountId, listId, listName, userId, screenName));
        }
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        final ParcelableUser user = getSelectedUser();
        if (user == null || mUserList == null) return false;
        switch (item.getItemId()) {
            case MENU_DELETE_FROM_LIST: {
                DeleteUserListMembersDialogFragment.show(getFragmentManager(), mUserList, user);
                break;
            }
            default: {
                return super.onMenuItemClick(item);
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(EXTRA_USER_LIST, mUserList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_MEMBERS_DELETED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    @Override
    protected int getUserMenuResource() {
        return R.menu.action_user_list_member;
    }

    private class GetUserListTask extends AsyncTask<Object, Object, ParcelableUserList> {

        private final long accountId, userId;
        private final long listId;
        private final String screenName, listName;

        private GetUserListTask(final long accountId, final long listId, final String listName, final long userId,
                                final String screenName) {
            this.accountId = accountId;
            this.userId = userId;
            this.listId = listId;
            this.screenName = screenName;
            this.listName = listName;
        }

        @Override
        protected ParcelableUserList doInBackground(final Object... params) {
            final Twitter twitter = getTwitterInstance(getActivity(), accountId, true);
            if (twitter == null) return null;
            try {
                final UserList list;
                if (listId > 0) {
                    list = twitter.showUserList(listId);
                } else if (userId > 0) {
                    list = twitter.showUserList(listName, userId);
                } else if (screenName != null) {
                    list = twitter.showUserList(listName, screenName);
                } else
                    return null;
                return new ParcelableUserList(list, accountId);
            } catch (final TwitterException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ParcelableUserList result) {
            if (mUserList != null) return;
            mUserList = result;
        }
    }
}
