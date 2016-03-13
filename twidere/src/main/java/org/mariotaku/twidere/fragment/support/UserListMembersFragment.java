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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.UserListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableUserListUtils;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;

public class UserListMembersFragment extends CursorSupportUsersListFragment {

    private ParcelableUserList mUserList;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            if (BROADCAST_USER_LIST_MEMBERS_DELETED.equals(action)) {
                final ParcelableUserList list = intent.getParcelableExtra(EXTRA_USER_LIST);
                if (mUserList != null && list != null && list.id == mUserList.id) {
                    removeUsers(intent.getStringExtra(EXTRA_USER_IDS));
                }
            }
        }
    };

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context,
                                                        @NonNull final Bundle args, boolean fromUser) {
        final long listId = args.getLong(EXTRA_LIST_ID, -1);
        final UserKey accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String userId = args.getString(EXTRA_USER_ID);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final UserListMembersLoader loader = new UserListMembersLoader(context, accountId, listId,
                userId, screenName, listName, getData(), fromUser);
        loader.setCursor(getNextCursor());
        loader.setPage(getNextPage());
        return loader;
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
            final UserKey accountId = args.getParcelable(EXTRA_ACCOUNT_KEY);
            final String userId = args.getString(EXTRA_USER_ID);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            final String listName = args.getString(EXTRA_LIST_NAME);
            AsyncTaskUtils.executeTask(new GetUserListTask(accountId, listId, listName, userId,
                    screenName));
        }
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

    private class GetUserListTask extends AsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        @Nullable
        private final UserKey mAccountKey;
        private final String mUserId;
        private final long mListId;
        private final String mScreenName, mListName;

        private GetUserListTask(@Nullable final UserKey accountKey, final long listId,
                                final String listName, final String userId, final String screenName) {
            this.mAccountKey = accountKey;
            this.mUserId = userId;
            this.mListId = listId;
            this.mScreenName = screenName;
            this.mListName = listName;
        }

        @Override
        @NonNull
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            if (mAccountKey == null)
                return SingleResponse.getInstance(new TwitterException("No Account"));
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getActivity(), mAccountKey, true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList list;
                if (mListId > 0) {
                    list = twitter.showUserList(mListId);
                } else if (mUserId != null) {
                    list = twitter.showUserList(mListName, mUserId);
                } else if (mScreenName != null) {
                    list = twitter.showUserList(mListName, mScreenName);
                } else
                    throw new TwitterException("list_id or list_name and user_id (or screen_name) required");
                return SingleResponse.getInstance(ParcelableUserListUtils.from(list, mAccountKey));
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            if (mUserList != null) return;
            mUserList = result.getData();
        }
    }
}
