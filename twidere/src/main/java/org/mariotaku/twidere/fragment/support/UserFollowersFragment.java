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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.loader.support.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.support.UserFollowersLoader;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.message.UsersBlockedEvent;

import static org.mariotaku.twidere.util.DataStoreUtils.getAccountScreenName;

public class UserFollowersFragment extends CursorSupportUsersListFragment {

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context,
                                                        @NonNull final Bundle args, boolean fromUser) {
        final AccountKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final UserFollowersLoader loader = new UserFollowersLoader(context, accountKey, userId,
                screenName, getData(), fromUser);
        loader.setCursor(getNextCursor());
        return loader;
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
    public void onUsersBlocked(UsersBlockedEvent event) {
        final AccountKey accountKey = event.getAccountKey();
        final String screen_name = getAccountScreenName(getActivity(), accountKey);
        final Bundle args = getArguments();
        if (args == null) return;
        if (accountKey != null && accountKey.getId() == args.getLong(EXTRA_USER_ID, -1) || screen_name != null
                && screen_name.equalsIgnoreCase(args.getString(EXTRA_SCREEN_NAME))) {
            removeUsers(event.getUserIds());
        }
    }

}
