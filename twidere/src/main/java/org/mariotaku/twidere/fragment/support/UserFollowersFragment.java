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
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.loader.support.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.support.UserFollowersLoader;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.UsersBlockedEvent;

import static org.mariotaku.twidere.util.DataStoreUtils.getAccountScreenName;

public class UserFollowersFragment extends CursorSupportUsersListFragment {

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context,
                                                        @NonNull final Bundle args,
                                                        final boolean fromUser) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String userId = args.getString(EXTRA_USER_ID);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final UserFollowersLoader loader = new UserFollowersLoader(context, accountKey, userId,
                screenName, getData(), fromUser);
        loader.setCursor(getNextCursor());
        loader.setPage(getNextPage());
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
        final UserKey accountKey = event.getAccountKey();
        final String screenName = getAccountScreenName(getActivity(), accountKey);
        final Bundle args = getArguments();
        if (args == null) return;
        if (accountKey != null && TextUtils.equals(accountKey.getId(), args.getString(EXTRA_USER_ID))
                || screenName != null && screenName.equalsIgnoreCase(args.getString(EXTRA_SCREEN_NAME))) {
            removeUsers(event.getUserIds());
        }
    }

}
