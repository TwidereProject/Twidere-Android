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
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableUserListsAdapter;
import org.mariotaku.twidere.loader.UserListsLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.UserListDestroyedEvent;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

public class UserListsFragment extends ParcelableUserListsFragment {

    @Override
    public Loader<List<ParcelableUserList>> onCreateUserListsLoader(final Context context,
                                                                    final Bundle args, final boolean fromUser) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        return new UserListsLoader(getActivity(), accountKey, userKey, screenName, true, getData());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_lists_owned, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_user_list: {
                final DialogFragment f = new CreateUserListDialogFragment();
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_ACCOUNT_KEY, getAccountKey());
                f.setArguments(args);
                f.show(getFragmentManager(), null);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final MenuItem item = menu.findItem(R.id.new_user_list);
        final UserKey accountId = getAccountKey();
        if (accountId == null || item == null) return;
        if (accountId.equals(getUserId())) {
            MenuUtils.setMenuItemAvailability(menu, R.id.new_user_list, true);
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.new_user_list, Utils.isMyAccount(getActivity(), getScreenName()));
        }
    }

    private String getScreenName() {
        return getArguments().getString(EXTRA_SCREEN_NAME);
    }

    private UserKey getUserId() {
        return getArguments().getParcelable(EXTRA_USER_KEY);
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

    @Subscribe
    public void onUserListDestroyed(UserListDestroyedEvent event) {
        removeUserList(event.userList.id);
    }

    private void removeUserList(final String id) {
        final ParcelableUserListsAdapter adapter = getAdapter();
//        final int listsIdx = adapter.findItemPosition(id);
//        if (listsIdx >= 0) {
//            adapter.removeAt(listsIdx);
//        }
    }

}
